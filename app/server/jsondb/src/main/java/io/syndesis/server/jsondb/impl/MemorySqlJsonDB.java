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

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

import org.h2.jdbcx.JdbcDataSource;
import org.skife.jdbi.v2.DBI;

import io.syndesis.common.util.KeyGenerator;
import io.syndesis.server.jsondb.CloseableJsonDB;
import io.syndesis.server.jsondb.JsonDBException;
import io.syndesis.server.jsondb.NativeJsonDB;

/**
 * Used to create in memory version impl of JsonDB
 */
public class MemorySqlJsonDB {

    private static class ClosableSqlJsonDB extends SqlJsonDB implements NativeJsonDB {

        private final Connection keepsDBOpen;
        private final AtomicBoolean closed = new AtomicBoolean();

        public ClosableSqlJsonDB(Connection keepsDBOpen, DBI dbi, Collection<Index> indexes) {
            super(dbi, null, indexes);
            this.keepsDBOpen = keepsDBOpen;
        }

        @Override
        public void close() throws IOException {
            if( closed.compareAndSet(false, true) ) {
                try {
                    keepsDBOpen.close();
                } catch (SQLException e) {
                    throw new IOException(e);
                }
            }
        }

        @Override
        public DBI database() {
            return dbi;
        }
    }

    public static CloseableJsonDB create(Collection<Index> indexes) {
        JdbcDataSource ds = new JdbcDataSource();
        DBI dbi = new DBI(ds);
        ds.setURL("jdbc:h2:mem:"+ KeyGenerator.createKey()+";MODE=PostgreSQL");
        try {
            Connection keepsDBOpen = ds.getConnection();
            ClosableSqlJsonDB result = new ClosableSqlJsonDB(keepsDBOpen, dbi, indexes);
            result.createTables();
            return result;
        } catch (SQLException e) {
            throw new JsonDBException(e);
        }
    }
}
