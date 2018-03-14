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
package io.syndesis.connector.sql.stored;

import java.sql.CallableStatement;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import io.syndesis.connector.sql.SqlSupport;
import io.syndesis.connector.sql.common.SqlConnectionRule;
import io.syndesis.connector.sql.common.stored.StoredProcedureMetadata;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.syndesis.connector.sql.stored.SqlStoredCommon.setupStoredProcedure;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class SqlStoredProcedureTest  {
    private static final Logger LOGGER = LoggerFactory.getLogger(SqlStoredProcedureTest.class);

    @ClassRule
    public static SqlConnectionRule db = new SqlConnectionRule();

    @Before
    public void setUp() throws Exception {
        setupStoredProcedure(db.connection, db.properties);
    }

    @Test
    public void listAllStoredProcedures() {
        Map<String, Object> parameters = new HashMap<>();
        for (final String name : db.properties.stringPropertyNames()) {
            parameters.put(name.substring(name.indexOf('.') + 1), db.properties.getProperty(name));
        }
        Map<String, StoredProcedureMetadata> storedProcedures = SqlSupport.getStoredProcedures(parameters);
        assertThat(storedProcedures.isEmpty()).isFalse();
        // Find 'demo_add'
        assertThat(storedProcedures.keySet().contains("DEMO_ADD")).isTrue();

        for (String storedProcedureName : storedProcedures.keySet()) {
            StoredProcedureMetadata md = storedProcedures.get(storedProcedureName);
            LOGGER.info("{}:{}", storedProcedureName, md.getTemplate());
        }

        // Inspect demo_add
        StoredProcedureMetadata metaData = storedProcedures.get("DEMO_ADD");
        assertThat(metaData.getTemplate()).isEqualTo("DEMO_ADD(INTEGER ${body[A]}, INTEGER ${body[B]}, OUT INTEGER C)");
    }

    @Test
    public void listSchemasTest() throws SQLException {

        DatabaseMetaData meta = db.connection.getMetaData();
        String catalog = null;
        String schemaPattern = null;

        LOGGER.info("Querying for all Schemas...");

        ResultSet schema = meta.getSchemas(catalog, schemaPattern);
        int schemaCount = 0;
        while (schema.next()) {
            String catalogName = schema.getString("TABLE_CATALOG");
            String schemaName = schema.getString("TABLE_SCHEM");
            LOGGER.info("{}:{}", catalogName, schemaName);

            schemaCount++;
        }
        assertThat(schemaCount).isGreaterThan(0);
    }

    @Test
    public void callStoredProcedureTest() throws SQLException {
        db.connection.setAutoCommit(true);
        try (CallableStatement cStmt = db.connection.prepareCall("{call DEMO_ADD(?, ?, ?)}")) {
            cStmt.setInt(1, 1);
            cStmt.setInt(2, 2);
            cStmt.registerOutParameter(3, Types.NUMERIC);
            cStmt.execute();

            String c = cStmt.getBigDecimal(3).toPlainString();
            LOGGER.info("OUTPUT {}", c);
            assertThat(c).isEqualTo("3");
        } catch (SQLException e) {
            LOGGER.warn("", e);
            fail("", e);
        }
    }
}
