/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.jsondb.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import io.syndesis.core.EventBus;
import io.syndesis.core.KeyGenerator;
import io.syndesis.jsondb.GetOptions;
import io.syndesis.jsondb.JsonDB;
import io.syndesis.jsondb.JsonDBException;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.PreparedBatch;
import org.skife.jdbi.v2.ResultIterator;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import org.skife.jdbi.v2.util.IntegerColumnMapper;
import org.skife.jdbi.v2.util.StringColumnMapper;

/**
 * Implements the JsonDB via DBI/JDBC
 *
 * Each value in the JSON tree is stored a simple record with the primary key being the
 * path to the value.
 */
@SuppressWarnings({"PMD.GodClass", "PMD.CyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity"})
public class SqlJsonDB implements JsonDB {

    enum DatabaseKind {
        PostgreSQL, SQLite, H2, CockroachDB
    }

    private final DBI dbi;
    private final EventBus bus;

    // These values are used to compute a seq key
    private DatabaseKind databaseKind = DatabaseKind.PostgreSQL;

    public SqlJsonDB(DBI dbi, EventBus bus) {
        this.dbi = dbi;
        this.bus = bus;


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
                dbi.update("CREATE TABLE jsondb (path VARCHAR COLLATE \"C\" PRIMARY KEY, value VARCHAR, kind INT)");
            } else {
                dbi.update("CREATE TABLE jsondb (path VARCHAR PRIMARY KEY, value VARCHAR, kind INT)");
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

    @Override
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

        Consumer<OutputStream> result = null;
        final Handle h = dbi.open();
        try {
            // Creating the iterator could fail with a runtime exception,
            String sql = "select path,value,kind from jsondb where path LIKE :like order by path";
            ResultIterator<JsonRecord> iterator = h.createQuery(sql)
                .bind("like", like)
                .map(JsonRecordMapper.INSTANCE)
                .iterator();
            try {
                // At this point we know if we can produce results..
                if (iterator.hasNext()) {
                    result = output -> {
                        try {
                            Consumer<JsonRecord> toJson = JsonRecordSupport.recordsToJsonStream(baseDBPath, output, o);
                            iterator.forEachRemaining(toJson);
                            toJson.accept(null);
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
            bus.broadcast("jsondb-deleted", Strings.prefix(Strings.trimSuffix(path, "/"), "/"));
        }
        return rc[0];
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

            final List<String> paths = dbi.createQuery(query).bind(0, pathRegex).bind(1, value)
                .map(StringColumnMapper.INSTANCE).list();

            ret.set(new HashSet<>(paths));
        });

        return ret.get();
    }

    @Override
    public String push(String path, InputStream body) {
        String key = createKey();
        set(Strings.suffix(Strings.prefix(path, "/"), "/") + key + "/", body);
        return key;
    }

    /* default */ class BatchManager {

        private final Handle dbi;
        private long batchSize;
        private PreparedBatch insertBatch;

        /* default */ BatchManager(Handle dbi) {
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
                    .bind("kind", r.getKind())
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
                insertBatch = dbi.prepareBatch("INSERT into jsondb (path, value, kind) values (:path, :value, :kind)");
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
                JsonRecordSupport.jsonStreamToRecords(baseDBPath, body, mb.createSetConsumer());
            } catch (IOException e) {
                throw new JsonDBException(e);
            }
            mb.flush();
        });
        if( bus!=null ) {
            bus.broadcast("jsondb-updated", Strings.prefix(Strings.trimSuffix(path, "/"), "/"));
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

                        String key = Strings.suffix(path, "/")+jp.getCurrentName();
                        updatePaths.add(key);
                        String baseDBPath = JsonRecordSupport.convertToDBPath(key);
                        mb.deleteRecordsForSet(baseDBPath);

                        try {
                            JsonRecordSupport.jsonStreamToRecords(jp, baseDBPath, mb.createSetConsumer());
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
                bus.broadcast("jsondb-updated", Strings.prefix(Strings.trimSuffix(updatePath, "/"), "/"));
            }
        }
    }


    private int deleteJsonRecords(Handle dbi, String baseDBPath, String like) {

        Deque<String> params = getAllParentPaths(baseDBPath);

        StringBuilder sql = new StringBuilder("DELETE from jsondb where path LIKE ?");
        if( !params.isEmpty() ) {
            sql.append(" OR path in ( ")
               .append(String.join(", ", Collections.nCopies(params.size(), "?")))
               .append(" )");
        }

        params.addFirst(like);
        return dbi.update(sql.toString(), params.toArray());
    }

    private static Deque<String> getAllParentPaths(String baseDBPath) {
        Deque<String> params = new ArrayDeque<String>();
        Pattern compile = Pattern.compile("/[^/]*$");
        String current = Strings.trimSuffix(baseDBPath, "/");
        while( true ) {
            current = compile.matcher(current).replaceFirst("");
            if( current.isEmpty() ) {
                break;
            }
            params.add(current+"/");
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
            return JsonRecord.of(r.getString("path"), r.getString("value"), r.getInt("kind"));
        }
    }

    private void withTransaction(Consumer<Handle> cb) {
        try (final Handle h = dbi.open()) {
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

}
