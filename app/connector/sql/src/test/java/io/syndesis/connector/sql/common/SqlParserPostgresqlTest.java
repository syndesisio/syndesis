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
import java.sql.SQLException;
import java.sql.Statement;

import io.syndesis.connector.sql.common.DatabaseContainers.Database;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.JdbcDatabaseContainer;

import static org.assertj.core.api.Assertions.assertThat;

@Database("postgres:11.11")
public class SqlParserPostgresqlTest {

    @BeforeEach
    public void createTodoTable(final JdbcDatabaseContainer<?> postgresql) throws SQLException {
        try (Connection connection = postgresql.createConnection("")) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(
                    "CREATE TABLE todo (id SERIAL PRIMARY KEY, task VARCHAR, completed INTEGER)");
            }
        }
    }

    @AfterEach
    public void dropTestObjects(final JdbcDatabaseContainer<?> postgresql) throws SQLException {
        try (Connection connection = postgresql.createConnection("")) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DROP TABLE todo");
            }
        }
    }

    @ExtendWith(DatabaseContainers.class)
    @TestTemplate
    public void parseOnConflict(final JdbcDatabaseContainer<?> postgresql) throws SQLException {
        try (Connection connection = postgresql.createConnection("")) {
            final SqlStatementParser parser = new SqlStatementParser(connection,
                "INSERT INTO todo(id, completed, task) VALUES (:#id, :#completed, :#task) ON CONFLICT (id) DO UPDATE SET completed=:#completed, task=:#task");
            final SqlStatementMetaData metaData = parser.parse();

            assertThat(metaData.getInParams()).extracting(SqlParam::getName).containsOnly("id", "completed", "task");
            assertThat(metaData.getOutParams()).extracting(SqlParam::getName).containsOnly("id");
            assertThat(metaData.getTableNames()).containsOnly("TODO");
        }
    }

    @ExtendWith(DatabaseContainers.class)
    @TestTemplate
    public void parseSelectWithLimit(final JdbcDatabaseContainer<?> postgresql) throws SQLException {
        try (Connection connection = postgresql.createConnection("")) {
            // can't mix parameterized tests and test template tests
            {
                final SqlStatementParser parser = new SqlStatementParser(connection, "SELECT * FROM todo limit(1)");
                final SqlStatementMetaData metaData = parser.parse();

                assertThat(metaData.getInParams()).isEmpty();
                assertThat(metaData.getOutParams()).extracting(SqlParam::getName).containsOnly("id", "task", "completed");
                // falls back to the legacy parser which doesn't fetch the table
                // name
                assertThat(metaData.getTableNames()).isEmpty();
            }

            {
                final SqlStatementParser parser = new SqlStatementParser(connection, "SELECT * FROM todo LIMIT 1");
                final SqlStatementMetaData metaData = parser.parse();

                assertThat(metaData.getInParams()).isEmpty();
                assertThat(metaData.getOutParams()).extracting(SqlParam::getName).containsOnly("id", "task", "completed");
                assertThat(metaData.getTableNames()).containsOnly("todo");
            }
        }
    }

    @ExtendWith(DatabaseContainers.class)
    @TestTemplate
    public void parseSelectWithoutFrom(final JdbcDatabaseContainer<?> postgresql) throws SQLException {
        try (Connection connection = postgresql.createConnection("")) {
            final SqlStatementParser parser = new SqlStatementParser(connection, "SELECT 3 AS id");
            final SqlStatementMetaData metaData = parser.parse();

            assertThat(metaData.getInParams()).isEmpty();
            assertThat(metaData.getOutParams()).extracting(SqlParam::getName).containsOnly("id");
            assertThat(metaData.getTableNames()).isEmpty();
        }
    }
}
