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
import java.util.HashMap;
import java.util.Map;

import io.syndesis.connector.sql.SqlSupport;
import io.syndesis.connector.sql.common.DatabaseContainers.Database;
import io.syndesis.connector.sql.common.stored.StoredProcedureColumn;
import io.syndesis.connector.sql.common.stored.StoredProcedureMetadata;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.JdbcDatabaseContainer;

import static org.assertj.core.api.Assertions.assertThat;

@Database({"mariadb:10.5.8"})
public class MariaDBMetadataITCase {

    @BeforeEach
    public void createTestObjects(final JdbcDatabaseContainer<?> mariadb) throws SQLException {
        try (Connection connection = mariadb.createConnection("")) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(
                    "CREATE PROCEDURE create_lead (OUT first_name VARCHAR(50), OUT last_name VARCHAR(50), OUT company VARCHAR(50), OUT lead_source VARCHAR(50), IN create_date DATE) BEGIN SELECT first_name, last_name, company, lead_source FROM contact; END;");
                stmt.execute("CREATE FUNCTION add_two_numbers (a INTEGER, b INTEGER) RETURNS INTEGER DETERMINISTIC RETURN a + b;");
            }
        }
    }

    @AfterEach
    public void dropTestObjects(final JdbcDatabaseContainer<?> mariadb) throws SQLException {
        try (Connection connection = mariadb.createConnection("")) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DROP PROCEDURE create_lead");
                stmt.execute("DROP FUNCTION add_two_numbers");
            }
        }
    }

    @ExtendWith(DatabaseContainers.class)
    @TestTemplate
    public void shouldFetchFunctionMetadata(final JdbcDatabaseContainer<?> mariadb) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("url", mariadb.getJdbcUrl());
        parameters.put("user", mariadb.getUsername());
        parameters.put("password", mariadb.getPassword());
        parameters.put("procedure-pattern", "add_two_numbers");

        final Map<String, StoredProcedureMetadata> metadata = SqlSupport.getProceduresAndFunctions(parameters);
        assertThat(metadata).containsKey("add_two_numbers");
        final StoredProcedureMetadata procedureMetadata = metadata.get("add_two_numbers");
        assertThat(procedureMetadata.getColumnList())
            .extracting(StoredProcedureColumn::toProcedureParameterString)
            .containsOnly("INTEGER ${body[a]}", "INTEGER ${body[b]}");
    }

    @ExtendWith(DatabaseContainers.class)
    @TestTemplate
    public void shouldFetchListOfStoredProceduresAndFunctions(final JdbcDatabaseContainer<?> mariadb) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("url", mariadb.getJdbcUrl());
        parameters.put("user", mariadb.getUsername());
        parameters.put("password", mariadb.getPassword());

        final Map<String, StoredProcedureMetadata> metadata = SqlSupport.getProceduresAndFunctions(parameters);
        assertThat(metadata).containsOnlyKeys("create_lead", "add_two_numbers");
    }

    @ExtendWith(DatabaseContainers.class)
    @TestTemplate
    public void shouldFetchStoredProcedureMetadata(final JdbcDatabaseContainer<?> mariadb) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("url", mariadb.getJdbcUrl());
        parameters.put("user", mariadb.getUsername());
        parameters.put("password", mariadb.getPassword());
        parameters.put("procedure-pattern", "create_lead");

        final Map<String, StoredProcedureMetadata> metadata = SqlSupport.getProceduresAndFunctions(parameters);
        assertThat(metadata).containsKey("create_lead");
        final StoredProcedureMetadata procedureMetadata = metadata.get("create_lead");
        assertThat(procedureMetadata.getColumnList())
            .extracting(StoredProcedureColumn::toProcedureParameterString)
            .containsOnly("OUT VARCHAR first_name", "OUT VARCHAR last_name", "OUT VARCHAR company", "OUT VARCHAR lead_source", "DATE ${body[create_date]}");
    }
}
