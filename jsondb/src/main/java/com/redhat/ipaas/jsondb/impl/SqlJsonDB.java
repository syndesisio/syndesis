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
package com.redhat.ipaas.jsondb.impl;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.redhat.ipaas.core.EventBus;
import com.redhat.ipaas.core.KeyGenerator;
import com.redhat.ipaas.jsondb.GetOptions;
import com.redhat.ipaas.jsondb.JsonDB;
import com.redhat.ipaas.jsondb.JsonDBException;
import org.skife.jdbi.v2.*;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import org.skife.jdbi.v2.util.IntegerColumnMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Implements the JsonDB via DBI/JDBC
 *
 * Each value in the JSON tree is stored a simple record with the primary key being the
 * path to the value.
 */
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
            } catch (Exception e) {
                throw new IllegalStateException("Could not determine the database type", e);
            }
        });

    }

    public void createTables() {
        withTransaction(dbi -> {
            switch (databaseKind) {
                case PostgreSQL:
                    dbi.update("CREATE TABLE jsondb (path VARCHAR COLLATE \"C\" PRIMARY KEY, value VARCHAR, kind INT)");
                    break;
                default:
                    dbi.update("CREATE TABLE jsondb (path VARCHAR PRIMARY KEY, value VARCHAR, kind INT)");
                    break;
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
        if( options!=null ) {
            o = options;
        } else {
            o = new GetOptions();
        }

        // Lets normalize the path a bit
        String baseDBPath = JsonRecordSupport.convertToDBPath(path);
        String like = baseDBPath+"%";

        Consumer<OutputStream> result = null;
        final Handle h = dbi.open();
        try {
            String sql = "select path,value,kind from jsondb where path LIKE :like order by path";
            ResultIterator<JsonRecord> iterator = h.createQuery(sql)
                .bind("like", like)
                .map(JsonRecordMapper.INSTANCE)
                .iterator();
            if( iterator.hasNext() ) {
                result = output -> {
                    try {
                        try {
                            Consumer<JsonRecord> toJson = JsonRecordSupport.recordsToJsonStream(baseDBPath, output, o);
                            iterator.forEachRemaining(toJson);
                            toJson.accept(null);
                        } catch (IOException e) {
                            throw new JsonDBException(e);
                        }
                    } finally {
                        h.close();
                    }
                };
            }

        } finally {
            if( result==null ) {
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
    public String push(String path, InputStream body) {
        String key = createKey();
        set(Strings.suffix(Strings.prefix(path, "/"), "/") + key + "/", body);
        return key;
    }

    private class BatchManager {

        private Handle dbi;
        private long batchSize;
        private PreparedBatch insertBatch;

        public BatchManager(Handle dbi) {
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

                JsonParser jp = new JsonFactory().createParser(is);

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

        LinkedList<String> params = getAllParentPaths(baseDBPath);

        String sql = "DELETE from jsondb where path LIKE ?";
        if( !params.isEmpty() ) {
            sql += " OR path in ( ";
            sql += params.stream().map(x->"?").collect(Collectors.joining(", "));
            sql += " )";
        }

        params.addFirst(like);
        return dbi.update(sql, params.toArray());
    }

    static private LinkedList<String> getAllParentPaths(String baseDBPath) {
        LinkedList<String> params = new LinkedList<String>();
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
        public JsonRecord map(int index, ResultSet r, StatementContext ctx) throws SQLException {
            return JsonRecord.of(r.getString("path"), r.getString("value"), r.getInt("kind"));
        }
    }

    private void withTransaction(Consumer<Handle> cb) {
        final Handle h = dbi.open();
        boolean committed = false;
        try {
            h.begin();
            cb.accept(h);
            h.commit();
            committed  = true;
        } finally {
            if( !committed ) {
                h.rollback();
            }
            h.close();
        }
    }
}
