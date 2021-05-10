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
import java.util.HashMap;
import java.util.List;
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

@Database({"postgres:11.11", "postgres:12.6"})
public class PostgreSqlMetaDataITCase {

    @BeforeEach
    public void createTestTable(final JdbcDatabaseContainer<?> postgresql) throws SQLException {
        try (Connection connection = postgresql.createConnection("")) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("CREATE TABLE UUID_TABLE (uuid UUID)");
                stmt.execute(
                    "CREATE TABLE IF NOT EXISTS contact (first_name VARCHAR, last_name VARCHAR, company VARCHAR, lead_source VARCHAR, create_date DATE)");
                stmt.execute(
                    "CREATE OR REPLACE FUNCTION create_lead(OUT first_name text, OUT last_name text, OUT company text, OUT lead_source text) RETURNS SETOF record AS $$ SELECT first_name, last_name, company, lead_source FROM contact; $$ LANGUAGE 'sql' VOLATILE");
                stmt.execute(
                    "CREATE OR REPLACE PROCEDURE create_lead_proc(INOUT first_name text, INOUT last_name text, INOUT company text, INOUT lead_source text) LANGUAGE SQL AS $$ SELECT first_name, last_name, company, lead_source FROM contact; $$");
            }
        }
    }

    @AfterEach
    public void dropTestTable(final JdbcDatabaseContainer<?> postgresql) throws SQLException {
        try (Connection connection = postgresql.createConnection("")) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DROP TABLE UUID_TABLE");
                stmt.execute("DROP FUNCTION create_lead");
                stmt.execute("DROP PROCEDURE create_lead_proc");
                stmt.execute("DROP TABLE contact");
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

    @ExtendWith(DatabaseContainers.class)
    @TestTemplate
    public void shouldFetchFunctionMetadata(final JdbcDatabaseContainer<?> postgresql) throws SQLException {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("url", postgresql.getJdbcUrl());
        parameters.put("user", postgresql.getUsername());
        parameters.put("password", postgresql.getPassword());
        parameters.put("procedure-pattern", "create_lead");

        final Map<String, StoredProcedureMetadata> metadata = SqlSupport.getProceduresAndFunctions(parameters);
        assertThat(metadata).containsKey("create_lead");
        final StoredProcedureMetadata procedureMetadata = metadata.get("create_lead");
        assertThat(procedureMetadata.getColumnList())
            .extracting(StoredProcedureColumn::toProcedureParameterString)
            .containsOnly("OUT VARCHAR first_name", "OUT VARCHAR last_name", "OUT VARCHAR company", "OUT VARCHAR lead_source");
    }

    @ExtendWith(DatabaseContainers.class)
    @TestTemplate
    public void shouldFetchProcedureMetadata(final JdbcDatabaseContainer<?> postgresql) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("url", postgresql.getJdbcUrl());
        parameters.put("user", postgresql.getUsername());
        parameters.put("password", postgresql.getPassword());
        parameters.put("procedure-pattern", "create_lead_proc");

        final Map<String, StoredProcedureMetadata> metadata = SqlSupport.getProceduresAndFunctions(parameters);
        assertThat(metadata).containsKey("create_lead_proc");
        final StoredProcedureMetadata procedureMetadata = metadata.get("create_lead_proc");
        assertThat(procedureMetadata.getColumnList())
            .extracting(StoredProcedureColumn::toProcedureParameterString)
            .containsOnly("VARCHAR ${body[first_name]}", "VARCHAR ${body[last_name]}", "VARCHAR ${body[company]}", "VARCHAR ${body[lead_source]}");
    }
}
