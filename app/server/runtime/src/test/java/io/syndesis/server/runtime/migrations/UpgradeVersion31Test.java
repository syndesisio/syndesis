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

package io.syndesis.server.runtime.migrations;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import io.syndesis.server.jsondb.NativeJsonDB;
import io.syndesis.server.jsondb.dao.Migrator;
import io.syndesis.server.jsondb.impl.MemorySqlJsonDB;
import io.syndesis.server.runtime.DefaultMigrator;

import org.junit.Test;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.springframework.core.io.DefaultResourceLoader;

import static org.assertj.core.api.Assertions.assertThat;

public class UpgradeVersion31Test {

    private static class FetchIndex implements HandleCallback<Set<String>> {

        private static final FetchIndex INSTANCE = new FetchIndex();

        @Override
        public Set<String> withHandle(final Handle handle) throws Exception {
            try (Connection con = handle.getConnection()) {
                final DatabaseMetaData metaData = con.getMetaData();
                try (ResultSet rs = metaData.getIndexInfo(con.getCatalog(), con.getSchema(), "JSONDB", false, false)) {
                    final Set<String> indexes = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
                    while (rs.next()) {
                        final String indexName = rs.getString("INDEX_NAME");
                        indexes.add(indexName);
                    }

                    return indexes;
                }
            }
        }

    }

    @Test
    public void shouldPerformSchemaUpgrade() throws IOException {
        try (final NativeJsonDB jsondb = (NativeJsonDB) MemorySqlJsonDB.create(Collections.emptyList())) {
            assertThat(jsondb.database().withHandle(FetchIndex.INSTANCE)).doesNotContain("jsondb_activity_idx");

            final Migrator migrator = new DefaultMigrator(new DefaultResourceLoader());
            migrator.migrate(jsondb, 31);

            assertThat(jsondb.database().withHandle(FetchIndex.INSTANCE)).contains("jsondb_activity_idx");
        }
    }

}
