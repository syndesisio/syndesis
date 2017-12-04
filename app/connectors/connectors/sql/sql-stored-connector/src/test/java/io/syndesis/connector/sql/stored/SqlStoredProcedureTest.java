/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.connector.sql.stored;

import static org.junit.Assert.assertTrue;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import io.syndesis.connector.sql.SqlCommon;
import io.syndesis.connector.sql.stored.StoredProcedureMetadata;

public class SqlStoredProcedureTest {

    private static Connection connection;
    private static Properties properties = new Properties();
    private static SqlCommon sqlCommon;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        sqlCommon = new SqlCommon();
        connection = sqlCommon.setupConnection(connection, properties);
        SqlStoredCommon.setupStoredProcedure(connection, properties);
    }

    @AfterClass
    public static void afterClass() throws SQLException {
        sqlCommon.closeConnection(connection);
    }

    @Test
    public void listAllStoredProcedures() {

        SqlStoredConnectorMetaDataExtension ext = new SqlStoredConnectorMetaDataExtension();
        Map<String, Object> parameters = new HashMap<String, Object>();
        for (final String name : properties.stringPropertyNames()) {
            parameters.put(name.substring(name.indexOf(".") + 1), properties.getProperty(name));
        }
        Map<String, StoredProcedureMetadata> storedProcedures = ext.getStoredProcedures(parameters);
        assertTrue(storedProcedures.size() > 0);
        // Find 'demo_add'
        assertTrue(storedProcedures.keySet().contains("DEMO_ADD"));

        for (String storedProcedureName : storedProcedures.keySet()) {
            StoredProcedureMetadata md = storedProcedures.get(storedProcedureName);
            System.out.println(storedProcedureName + " : " + md.getTemplate());
        }

        // Inspect demo_add
        StoredProcedureMetadata metaData = storedProcedures.get("DEMO_ADD");
        Assert.assertEquals("DEMO_ADD(INTEGER ${body[A]}, INTEGER ${body[B]}, OUT INTEGER ${body[C]})",
            metaData.getTemplate());
    }

    @Test
    public void listSchemasTest() throws SQLException {

        DatabaseMetaData meta = connection.getMetaData();
        String catalog = null;
        String schemaPattern = null;

        System.out.println("Querying for all Schemas...");
        ResultSet schema = meta.getSchemas(catalog, schemaPattern);
        int schemaCount = 0;
        while (schema.next()) {
            String catalogName = schema.getString("TABLE_CATALOG");
            String schemaName = schema.getString("TABLE_SCHEM");
            System.out.println(catalogName + ":" + schemaName);
            schemaCount++;
        }
        assertTrue(schemaCount > 0);
    }

    @Test
    public void callStoredProcedureTest() throws SQLException {

        String c = "";
        connection.setAutoCommit(true);
        try (CallableStatement cStmt = connection.prepareCall("{call DEMO_ADD(?, ?, ?)}")) {
            cStmt.setInt(1, 1);
            cStmt.setInt(2, 2);
            cStmt.registerOutParameter(3, Types.NUMERIC);
            cStmt.execute();

            c = cStmt.getBigDecimal(3).toPlainString();
            System.out.println("OUTPUT " + c);
            Assert.assertEquals("3", c);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

}
