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
package io.syndesis.connector.sql.common;

import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import io.syndesis.connector.sql.common.DatabaseContainers.Database;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.JdbcDatabaseContainer;

import static org.assertj.core.api.Assertions.assertThat;

@Database("postgres:11.11")
public class PostgreSqlMetaDataITCase {

    @BeforeEach
    public void createTestTable(final JdbcDatabaseContainer<?> postgresql) throws SQLException {
        try (Connection connection = postgresql.createConnection("")) {
            try (Statement stmt = connection.createStatement()) {
                final String createTable = "CREATE TABLE UUID_TABLE (uuid UUID)";
                stmt.executeUpdate(createTable);
            }
        }
    }

    @AfterEach
    public void dropTestTable(final JdbcDatabaseContainer<?> postgresql) throws SQLException {
        try (Connection connection = postgresql.createConnection("")) {
            try (Statement stmt = connection.createStatement()) {
                final String createTable = "DROP TABLE UUID_TABLE";
                stmt.executeUpdate(createTable);
            }
        }
    }

    @ExtendWith(DatabaseContainers.class)
    @TestTemplate
    public void shouldCreateMetadataForPostgreSqlSpecificDataTypes(final JdbcDatabaseContainer<?> postgresql) throws SQLException {
        try (Connection connection = postgresql.createConnection("")) {
            final SqlStatementParser parser = new SqlStatementParser(connection, "INSERT INTO UUID_TABLE VALUES (:#uuid)");
            final SqlStatementMetaData info = parser.parse();
            final List<SqlParam> inParams = info.getInParams();

            final DbMetaDataHelper helper = new DbMetaDataHelper(connection);
            final List<SqlParam> paramList = helper.getJDBCInfoByColumnOrder(null, null, "UUID_TABLE", inParams);

            assertThat(paramList).hasSize(1);

            final SqlParam sqlParam = paramList.get(0);
            assertThat(sqlParam.getJdbcType()).isEqualTo(JDBCType.VARCHAR);
        }
    }

    @ExtendWith(DatabaseContainers.class)
    @TestTemplate
    public void shouldCreateMetadataForSelectPostgreSqlSpecificDataTypes(final JdbcDatabaseContainer<?> postgresql) throws SQLException {
        try (Connection connection = postgresql.createConnection("")) {
            final SqlStatementParser parser = new SqlStatementParser(connection, "SELECT * FROM UUID_TABLE");
            final SqlStatementMetaData info = parser.parse();
            final List<SqlParam> outParams = info.getOutParams();

            final DbMetaDataHelper helper = new DbMetaDataHelper(connection);
            final List<SqlParam> paramList = helper.getJDBCInfoByColumnOrder(null, null, "UUID_TABLE", outParams);

            assertThat(paramList).hasSize(1);

            final SqlParam sqlParam = paramList.get(0);
            assertThat(sqlParam.getJdbcType()).isEqualTo(JDBCType.VARCHAR);
        }
    }

}
