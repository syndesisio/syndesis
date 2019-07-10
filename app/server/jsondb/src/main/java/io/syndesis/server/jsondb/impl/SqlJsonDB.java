/*
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.server.jsondb.impl;

import static io.syndesis.server.jsondb.impl.JsonRecordSupport.STRING_VALUE_PREFIX;
import static io.syndesis.server.jsondb.impl.JsonRecordSupport.validateKey;
import static io.syndesis.server.jsondb.impl.Strings.prefix;
import static io.syndesis.server.jsondb.impl.Strings.suffix;
import static io.syndesis.server.jsondb.impl.Strings.trimSuffix;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.PreparedBatch;
import org.skife.jdbi.v2.Query;
import org.skife.jdbi.v2.ResultIterator;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import org.skife.jdbi.v2.util.IntegerColumnMapper;
import org.skife.jdbi.v2.util.StringColumnMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import io.syndesis.common.util.EventBus;
import io.syndesis.common.util.KeyGenerator;
import io.syndesis.common.util.SyndesisServerException;
import io.syndesis.common.util.TransactedEventBus;
import io.syndesis.server.jsondb.GetOptions;
import io.syndesis.server.jsondb.JsonDB;
import io.syndesis.server.jsondb.JsonDBException;
import io.syndesis.server.jsondb.WithGlobalTransaction;
import io.syndesis.server.jsondb.impl.expr.SqlExpressionBuilder;

/**
 * Implements the JsonDB via DBI/JDBC
 *
 * Each value in the JSON tree is stored a simple record with the primary key being the
 * path to the value.
 */
@SuppressWarnings({"PMD.GodClass", "PMD.CyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity"})
public class SqlJsonDB implements JsonDB, WithGlobalTransaction {

    private static final Logger LOG = LoggerFactory.getLogger(SqlJsonDB.class);

    public enum DatabaseKind {
        PostgreSQL, SQLite, H2, CockroachDB
    }

    protected final DBI dbi;
    private final EventBus bus;
    private final Collection<Index> indexes;
    private final Set<String> indexPaths = new HashSet<>();

    // These values are used to compute a seq key
    private DatabaseKind databaseKind = DatabaseKind.PostgreSQL;

    public SqlJsonDB(DBI dbi, EventBus bus) {
        this(dbi, bus, Collections.emptyList());
    }

    public SqlJsonDB(DBI dbi, EventBus bus, Collection<Index> indexes) {
        this.dbi = dbi;
        this.bus = bus;
        this.indexes = indexes;

        for (Index index : indexes) {
            this.indexPaths.add(index.getPath()+"/#"+index.getField());
        }

        // Lets find out the type of DB we are working with.
        withTransaction(x -> {
            try {
                String dbName = x.getConnection().getMetaData().getDatabaseProductName();
                databaseKind = DatabaseKind.valueOf(dbName);

                // CockroachDB uses the PostgreSQL driver.. so need to look a little closer.
                if( databaseKind == DatabaseKind.PostgreSQL ) {
                    String version = x.createQuery("SELECT VERSION()").mapTo(String.class).first();
                    if( version.startsWith("CockroachDB") ) {
                        databaseKind = DatabaseKind.CockroachDB;
                    }
                }
            } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception e) {
                throw new IllegalStateException("Could not determine the database type", e);
            }
        });
    }


    public void createTables() {
        withTransaction(dbi -> {
            if(databaseKind == DatabaseKind.PostgreSQL) {
                dbi.update("CREATE TABLE IF NOT EXISTS jsondb (path VARCHAR COLLATE \"C\" PRIMARY KEY, value VARCHAR, ovalue VARCHAR, idx VARCHAR COLLATE \"C\")");
                dbi.update("CREATE INDEX IF NOT EXISTS jsondb_idx ON jsondb (idx, value) WHERE idx IS NOT NULL");
                dbi.update("CREATE INDEX IF NOT EXISTS jsondb_activity_idx ON jsondb (path DESC)");
            } else {
                dbi.update("CREATE TABLE jsondb (path VARCHAR PRIMARY KEY, value VARCHAR, ovalue VARCHAR, idx VARCHAR)");
            }
            if( databaseKind == DatabaseKind.H2 ) {
                dbi.update("CREATE ALIAS IF NOT EXISTS split_part FOR \""+Strings.class.getName()+".splitPart\"");
                dbi.update("CREATE ALIAS IF NOT EXISTS trim_suffix FOR \""+Strings.class.getName()+".trimSuffix\"");
            }
        });
    }

    public void dropTables() {
        withTransaction(dbi -> {
            dbi.update("DROP TABLE jsondb");
        });
    }

    @Override
    public String createKey() {
        return KeyGenerator.createKey();
    }

    private static String incrementKey(String value) {
        if( value == null || value.isEmpty()) {
            return value;
        }
        char[] chars = value.toCharArray();
        chars[chars.length-1]++;
        return new String(chars);
    }

    @Override
    @SuppressWarnings({"PMD.ExcessiveMethodLength", "PMD.NPathComplexity"})
    public Consumer<OutputStream> getAsStreamingOutput(String path, GetOptions options) {

        GetOptions o;
        if (options != null) {
            o = options;
        } else {
            o = new GetOptions();
        }

        // Lets normalize the path a bit
        String baseDBPath = JsonRecordSupport.convertToDBPath(path);
        String like = baseDBPath + "%";
        GetOptions.Order order = o.order();
        if( order == null ) {
            order = GetOptions.Order.ASC;
        }

        Consumer<OutputStream> result = null;
        final Handle h = dbi.open();
        try {

            StringBuilder sql = new StringBuilder(250);
            // Creating the iterator could fail with a runtime exception,
            ArrayList<Consumer<Query<Map<String, Object>>>> binds = new ArrayList<>();

            if( o.filter() == null ) {
                sql.append("select path,value,ovalue from jsondb where path LIKE :like");
            } else {
                sql.append("SELECT path,value,ovalue FROM jsondb A INNER JOIN (");
                SqlExpressionBuilder.create(this, o.filter(), baseDBPath).build(sql, binds);
                sql.append(") B ON A.path LIKE B.match_path||'%'");
            }

            if (o.startAfter() != null) {
                String startAfter = validateKey(o.startAfter());
                if (o.order() == GetOptions.Order.DESC) {
                    sql.append(" and path <= :startAfter");
                    binds.add(query -> {
                        String bindPath = baseDBPath + startAfter;
                        query.bind("startAfter", bindPath);
                    });
                } else {
                    sql.append(" and path >= :startAfter");
                    binds.add(query -> {
                        String bindPath = baseDBPath + incrementKey(startAfter);
                        query.bind("startAfter", bindPath);
                    });
                }
            }
            if (o.startAt() != null) {
                String startAt = validateKey(o.startAt());
                if (o.order() == GetOptions.Order.DESC) {
                    sql.append(" and path < :startAt");
                    binds.add(query -> {
                        String bindPath = baseDBPath + incrementKey(startAt);
                        query.bind("startAt", bindPath);
                    });
                } else {
                    sql.append(" and path >= :startAt");
                    binds.add(query -> {
                        String bindPath = baseDBPath + startAt;
                        query.bind("startAt", bindPath);
                    });
                }
            }
            if (o.endAt() != null) {
                String endAt = validateKey(o.endAt());
                if (o.order() == GetOptions.Order.DESC) {
                    sql.append(" and path > :endAt");
                    binds.add(query -> {
                        String value = baseDBPath + endAt;
                        query.bind("endAt", value);
                    });
                } else {
                    sql.append(" and path < :endAt");
                    binds.add(query -> {
                        String bindPath = baseDBPath + incrementKey(endAt);
                        query.bind("endAt", bindPath);
                    });
                }
            }
            if (o.endBefore() != null) {
                String endBefore = validateKey(o.endBefore());
                if (o.order() == GetOptions.Order.DESC) {
                    sql.append(" and path >= :endBefore");
                    binds.add(query -> {
                        String value = baseDBPath + incrementKey(endBefore);
                        query.bind("endBefore", value);
                    });
                } else {
                    sql.append(" and path < :endBefore");
                    binds.add(query -> {
                        String value = baseDBPath + endBefore;
                        query.bind("endBefore", value);
                    });
                }
            }

            sql.append(" order by path ").append(order);
            Query<Map<String, Object>> query = h.createQuery(sql.toString()).bind("like", like);
            for (Consumer<Query<Map<String, Object>>> bind : binds) {
                bind.accept(query);
            }
            ResultIterator<JsonRecord> iterator = query.map(JsonRecordMapper.INSTANCE).iterator();

            try {
                // At this point we know if we can produce results..
                if (iterator.hasNext()) {
                    result = output -> {
                        try (JsonRecordConsumer toJson = new JsonRecordConsumer(baseDBPath, output, o)) {
                            while ( !toJson.isClosed() && iterator.hasNext() ) {
                                toJson.accept(iterator.next());
                            }
                        } catch (IOException e) {
                            throw new JsonDBException(e);
                        } finally {
                            iterator.close();
                            h.close();
                        }
                    };
                }
            } finally {
                // if we are producing results, then defer closing the iterator
                if (result == null) {
                    iterator.close();
                }
            }
        } finally {
            // if we are producing results, then defer closing the handle
            if (result == null) {
                h.close();
            }
        }
        return result;
    }


    @Override
    public boolean delete(String path) {
        String baseDBPath = JsonRecordSupport.convertToDBPath(path);
        String like = baseDBPath+"%";
        boolean rc[] = new boolean[]{false};
        withTransaction(dbi -> {
            rc[0] = deleteJsonRecords(dbi, baseDBPath, like) > 0;
        });
        if( bus!=null && rc[0] ) {
            bus.broadcast("jsondb-deleted", prefix(trimSuffix(path, "/"), "/"));
        }
        return rc[0];
    }

    public void executeNative(final String sql, final Object... parameters) {
        withTransaction(handle -> handle.execute(sql, parameters));
    }

    @Override
    public boolean exists(String path) {
        String baseDBPath = JsonRecordSupport.convertToDBPath(path);
        String like = baseDBPath+"%";
        boolean rc[] = new boolean[]{false};
        withTransaction(dbi -> {
            rc[0] = countJsonRecords(dbi, like) > 0;
        });
        return rc[0];
    }

    @Override
    public Set<String> fetchIdsByPropertyValue(final String collectionPath, final String property, final String value) {

        String path = prefix(trimSuffix(collectionPath, "/"), "/");

        String idx = path+"/#"+property;
        if( !indexPaths.contains(idx) ) {
            String message = "Index not defined for:  collectionPath: " + path + ", property: " + property;
            LOG.warn("fetchIdsByPropertyValue not optimzed !!!: {}", message);
            return fetchIdsByPropertyValueFullTableScan(collectionPath, property, value);
        } else {
            final AtomicReference<Set<String>> ret = new AtomicReference<>();
            withTransaction(dbi -> {
                final String query = "SELECT path FROM jsondb WHERE idx = ? AND value = ?";
                final List<String> paths = dbi.createQuery(query)
                    .bind(0, idx)
                    .bind(1, STRING_VALUE_PREFIX+value)
                    .map(StringColumnMapper.INSTANCE).list();

                String suffix = "/" + property + "/";
                ret.set(paths.stream()
                    .map(x -> trimSuffix(x, suffix))
                    .collect(Collectors.toCollection(HashSet::new)));
            });
            return ret.get();
        }
    }

    protected Set<String> fetchIdsByPropertyValueFullTableScan(final String collectionPath, final String property, final String value) {
        final String pathRegex = collectionPath + "/:[^/]+/" + property;

        final AtomicReference<Set<String>> ret = new AtomicReference<>();
        withTransaction(dbi -> {
            final String query;
            if (databaseKind == DatabaseKind.PostgreSQL) {
                query = "SELECT regexp_replace(path, '(/.+/:[^/]+).*', '\\1') from jsondb where path ~ ? and value = ?";
            } else if (databaseKind == DatabaseKind.H2) {
                query = "SELECT regexp_replace(path, '(/.+/:[^/]+).*', '$1') from jsondb where path regexp ? and value = ?";
            } else {
                throw new UnsupportedOperationException(
                    "Don't know how to use regex in a query with database: " + databaseKind);
            }

            final List<String> paths = dbi.createQuery(query)
                .bind(0, pathRegex)
                .bind(1, STRING_VALUE_PREFIX+value)
                .map(StringColumnMapper.INSTANCE).list();

            ret.set(new HashSet<>(paths));
        });

        return ret.get();
    }

    @Override
    public String push(String path, InputStream body) {
        String key = createKey();
        set(suffix(prefix(path, "/"), "/") + key + "/", body);
        return key;
    }

    class BatchManager {

        private final Handle dbi;
        private long batchSize;
        private PreparedBatch insertBatch;

        BatchManager(Handle dbi) {
            this.dbi = dbi;
        }

        public void deleteRecordsForSet(String baseDBPath) {
            String like = baseDBPath + "%";
            deleteJsonRecords(dbi, baseDBPath, like);
        }

        public Consumer<JsonRecord> createSetConsumer() {
            return r -> {
                PreparedBatch insert = getInsertBatch();
                insert.bind("path", r.getPath())
                    .bind("value", r.getValue())
                    .bind("ovalue", r.getOValue())
                    .bind("idx", r.getIndex())
                    .add();

                batchSize += r.getPath().length() + r.getValue().length();
                if (batchSize > 512 * 1024) { // Write the batch once we have enough data.
                    insert.execute();
                    batchSize = 0;
                }
            };
        }

        public PreparedBatch getInsertBatch() {
            if (insertBatch == null) {
                insertBatch = dbi.prepareBatch("INSERT into jsondb (path, value, ovalue, idx) values (:path, :value, :ovalue, :idx)");
            }
            return insertBatch;
        }

        public void flush() {
            if (batchSize > 0 && insertBatch != null) {
                insertBatch.execute();

            }
        }
    }

    @Override
    public void set(String path, InputStream body) {
        withTransaction(dbi -> {
            BatchManager mb = new BatchManager(dbi);
            String baseDBPath = JsonRecordSupport.convertToDBPath(path);
            mb.deleteRecordsForSet(baseDBPath);
            try {
                JsonRecordSupport.jsonStreamToRecords(indexPaths, baseDBPath, body, mb.createSetConsumer());
            } catch (IOException e) {
                throw new JsonDBException(e);
            }
            mb.flush();
        });
        if( bus!=null ) {
            bus.broadcast("jsondb-updated", prefix(trimSuffix(path, "/"), "/"));
        }
    }

    @Override
    public void update(String path, InputStream is) {
        ArrayList<String> updatePaths = new ArrayList<>();
        withTransaction(dbi -> {
            try {
                BatchManager mb = new BatchManager(dbi);

                try (JsonParser jp = new JsonFactory().createParser(is)) {
                    JsonToken nextToken = jp.nextToken();
                    if (nextToken != JsonToken.START_OBJECT ) {
                        throw new JsonParseException(jp, "Update did not contain a json object");
                    }

                    while(true) {

                        nextToken = jp.nextToken();
                        if (nextToken == JsonToken.END_OBJECT ) {
                            break;
                        }
                        if (nextToken != JsonToken.FIELD_NAME ) {
                            throw new JsonParseException(jp, "Expected a field name");
                        }

                        String key = suffix(path, "/")+jp.getCurrentName();
                        updatePaths.add(key);
                        String baseDBPath = JsonRecordSupport.convertToDBPath(key);
                        mb.deleteRecordsForSet(baseDBPath);

                        try {
                            JsonRecordSupport.jsonStreamToRecords(indexPaths, jp, baseDBPath, mb.createSetConsumer());
                        } catch (IOException e) {
                            throw new JsonDBException(e);
                        }
                    }

                    nextToken = jp.nextToken();
                    if (nextToken != null) {
                        throw new JsonParseException(jp, "Document did not terminate as expected.");
                    }
                    mb.flush();
                }
            } catch (IOException e) {
                throw new JsonDBException(e);
            }

        });
        if( bus!=null ) {
            for (String updatePath : updatePaths) {
                bus.broadcast("jsondb-updated", prefix(trimSuffix(updatePath, "/"), "/"));
            }
        }
    }


    private int deleteJsonRecords(Handle dbi, String baseDBPath, String like) {

        ArrayList<String> expressions = new ArrayList<>();
        ArrayList<String> queryParams = new ArrayList<>();
        for (String p : getAllParentPaths(baseDBPath)) {
            expressions.add("path = ?");
            queryParams.add(p);
        }
        expressions.add("path LIKE ?");
        queryParams.add(like);

        StringBuilder sql = new StringBuilder("DELETE FROM jsondb WHERE ");
        sql.append(String.join(" OR ", expressions));

        return dbi.update(sql.toString(), queryParams.toArray());
    }

    private static Deque<String> getAllParentPaths(String baseDBPath) {
        Deque<String> params = new ArrayDeque<>();
        Pattern compile = Pattern.compile("/[^/]*$");
        String current = trimSuffix(baseDBPath, "/");
        while( true ) {
            current = compile.matcher(current).replaceFirst("");
            if( current.isEmpty() ) {
                break;
            }
            params.addFirst(current+"/");
        }
        return  params;
    }

    private int countJsonRecords(Handle dbi, String like) {
        Integer result = dbi.createQuery("SELECT COUNT(*) from jsondb where path LIKE ?")
            .bind(0, like)
            .map(IntegerColumnMapper.PRIMITIVE).first();
        return result.intValue();
    }

    private static class JsonRecordMapper implements ResultSetMapper<JsonRecord> {
        private static final JsonRecordMapper INSTANCE = new JsonRecordMapper();
        @Override
        public JsonRecord map(int index, ResultSet r, StatementContext ctx) throws SQLException {
            return JsonRecord.of(r.getString("path"), r.getString("value"), r.getString("ovalue"), null);
        }
    }

    private void withTransaction(Consumer<Handle> cb) {
        try (Handle h = dbi.open()) {
            try {
                h.begin();
                cb.accept(h);
                h.commit();
            } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException")RuntimeException e) {
                h.rollback();
                throw e;
            }
        }
    }

    public Collection<Index> getIndexes() {
        return indexes;
    }

    public Set<String> getIndexPaths() {
        return indexPaths;
    }

    public DatabaseKind getDatabaseKind() {
        return databaseKind;
    }

    @Override
    public void withGlobalTransaction(final Consumer<JsonDB> handler) {
        final String checkpoint = UUID.randomUUID().toString();

        try (Handle handle = dbi.open()) {
            handle.begin();
            handle.checkpoint(checkpoint);

            try (Connection connection = handle.getConnection(); Connection transacted = withoutTransactionControl(connection)) {
                final TransactedEventBus transactedBus = new TransactedEventBus(bus);
                final SqlJsonDB checkpointed = new SqlJsonDB(new DBI(() -> transacted), transactedBus);

                boolean committed = false;
                try {
                    handler.accept(checkpointed);
                    handle.commit();
                    committed = true;
                    transactedBus.commit();
                } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") final RuntimeException e) {
                    if (!committed) {
                        // if event bus blows up we can't rollback as the transaction has already been committed
                        handle.rollback(checkpoint);
                    }

                    throw SyndesisServerException.launderThrowable(e);
                }
            } catch (SQLException e) {
                throw SyndesisServerException.launderThrowable(e);
            }
        }
    }

    private static Connection withoutTransactionControl(Connection connection) {
        return (Connection) Proxy.newProxyInstance(SqlJsonDB.class.getClassLoader(), new Class<?>[] { Connection.class }, (proxy, method, args) -> {
            // we control the transaction not the DBI or the consumer
            if (!"close".equals(method.getName()) && !"commit".equals(method.getName()) && !"rollback".equals(method.getName())) {
                return method.invoke(connection, args);
            }

            return null;
        });
    }

}
