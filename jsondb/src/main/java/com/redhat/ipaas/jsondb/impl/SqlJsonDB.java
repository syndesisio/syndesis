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

import com.redhat.ipaas.jsondb.GetOptions;
import com.redhat.ipaas.jsondb.JsonDB;
import com.redhat.ipaas.jsondb.JsonDBException;
import org.keycloak.common.util.Base64;
import org.skife.jdbi.v2.*;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import org.skife.jdbi.v2.util.IntegerColumnMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Random;
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

    private final DBI dbi;
    private final EventBus bus;

    // These values are used to compute a seq key
    private long lastTimestamp = System.currentTimeMillis();
    private final byte randomnessByte;
    private long randomnessLong;

    public SqlJsonDB(DBI dbi, EventBus bus) {
        this.dbi = dbi;
        this.bus = bus;

        Random random = new Random();
        randomnessByte = (byte) random.nextInt();
        randomnessLong = random.nextLong();
    }

    public void createTables() {
        withTransaction(dbi -> {
            dbi.update("CREATE TABLE rtdb (path VARCHAR PRIMARY KEY, value VARCHAR, kind INT)");
        });
    }

    public void dropTables() {
        withTransaction(dbi -> {
            dbi.update("DROP TABLE rtdb");
        });
    }


    @Override
    public String createKey() {
        long timeStamp = System.currentTimeMillis();
        long randomnessLong = 0;
        // lets make sure we don't create dup keys
        synchronized (this) {
            if( timeStamp == lastTimestamp ) {
                // increment the randomness.
                this.randomnessLong ++;
            } else {
                lastTimestamp = timeStamp;
            }
            randomnessLong = this.randomnessLong;
        }
        ByteBuffer buffer = ByteBuffer.wrap(new byte[8 + 1 + 8]);
        buffer.putLong(timeStamp);
        buffer.put(randomnessByte);
        buffer.putLong(randomnessLong);

        try {
            return Base64.encodeBytes(buffer.array(), 2, 15, Base64.ORDERED);
        } catch (IOException e) {
            throw new JsonDBException(e);
        }
    }
    @Override
    public Consumer<OutputStream> getAsStreamingOutput(String path, GetOptions options) {

        GetOptions o;
        if( options!=null ) {
            o = options;
        } else {
            o = GetOptions.builder().build();
        }

        // Lets normalize the path a bit
        String baseDBPath = JsonRecordSupport.convertToDBPath(path);
        String like = baseDBPath+"%";

        Consumer<OutputStream> result = null;
        final Handle h = dbi.open();
        try {
            ResultIterator<JsonRecord> iterator = h.createQuery("select path,value,kind from rtdb where path LIKE :like")
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
            bus.broadcast("rtdb-deleted", Strings.prefix(Strings.trimSuffix(path, "/"), "/"));
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

    @Override
    public void set(String path, InputStream body) {

        String baseDBPath = JsonRecordSupport.convertToDBPath(path);
        String like = baseDBPath+"%";

        withTransaction(dbi -> {
            deleteJsonRecords(dbi, baseDBPath, like);

            long byteSize[] = {0};
            PreparedBatch pb = dbi.prepareBatch("INSERT into rtdb (path, value, kind) values (:path, :value, :kind)");
            try {
                JsonRecordSupport.jsonStreamToRecords(baseDBPath, body, r -> {
                    pb.bind("path", r.getPath())
                        .bind("value", r.getValue())
                        .bind("kind", r.getKind())
                        .add();

                    byteSize[0] += r.getPath().length() + r.getValue().length();
                    if (byteSize[0] > 512 * 1024) { // Write the batch once we have enough data.
                        pb.execute();
                        byteSize[0] = 0;
                    }
                });
            } catch (IOException e) {
                throw new JsonDBException(e);
            }

            // Flush the batch...
            if (byteSize[0] > 0) {
                pb.execute();
            }
        });
        if( bus!=null ) {
            bus.broadcast("rtdb-updated", Strings.prefix(Strings.trimSuffix(path, "/"), "/"));
        }
    }

    private int deleteJsonRecords(Handle dbi, String baseDBPath, String like) {

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

        String sql = "DELETE from rtdb where path LIKE ?";
        if( !params.isEmpty() ) {
            sql += " OR path in ( ";
            sql += params.stream().map(x->"?").collect(Collectors.joining(", "));
            sql += " )";
        }

        params.addFirst(like);
        return dbi.update(sql, params.toArray());
    }

    private int countJsonRecords(Handle dbi, String like) {
        Integer result = dbi.createQuery("SELECT COUNT(*) from rtdb where path LIKE ?")
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
