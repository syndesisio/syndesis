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
package io.syndesis.connector.sql;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import com.fasterxml.jackson.databind.ObjectWriter;
import io.syndesis.connector.sql.common.DatabaseProduct;
import io.syndesis.connector.sql.stored.SqlStoredConnectorMetaDataExtension;
import io.syndesis.common.util.Json;
import io.syndesis.connector.support.verifier.api.SyndesisMetadata;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.MetaDataExtension.MetaData;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;

import static org.assertj.core.api.Assertions.fail;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

public class SqlMetadataAdapterTest {
    private static final String DERBY_DEMO_ADD2_SQL =
            "CREATE PROCEDURE DEMO_ADD2( IN A INTEGER, IN B INTEGER, OUT C INTEGER ) " +
            "PARAMETER STYLE JAVA " +
            "LANGUAGE JAVA " +
            "EXTERNAL NAME 'io.syndesis.connector.SampleStoredProcedures.demo_add'";
    public static final String DERBY_DEMO_ADD_SQL =
            "CREATE PROCEDURE DEMO_ADD( IN A INTEGER, IN B INTEGER, OUT C INTEGER ) " +
            "PARAMETER STYLE JAVA " +
            "LANGUAGE JAVA " +
            "EXTERNAL NAME 'io.syndesis.connector.SampleStoredProcedures.demo_add'";
    public static final String DERBY_DEMO_OUT_SQL =
            "CREATE PROCEDURE DEMO_OUT( OUT C INTEGER ) " +
            "PARAMETER STYLE JAVA " +
            "LANGUAGE JAVA " +
            "EXTERNAL NAME 'io.syndesis.connector.SampleStoredProcedures.demo_add'";

    private static Connection connection;
    private static Properties properties = new Properties();

    @BeforeClass
    public static void setUpBeforeClass() throws IOException {
        try (InputStream is = SqlMetadataAdapterTest.class.getClassLoader().getResourceAsStream("test-options.properties")) {
            properties.load(is);
            String user     = String.valueOf(properties.get("sql-connector.user"));
            String password = String.valueOf(properties.get("sql-connector.password"));
            String url      = String.valueOf(properties.get("sql-connector.url"));

            connection = DriverManager.getConnection(url,user,password);
            String databaseProductName = connection.getMetaData().getDatabaseProductName();
            Map<String,Object> parameters = new HashMap<>();
            for (final String name: properties.stringPropertyNames()) {
                parameters.put(name.substring(name.indexOf('.') + 1), properties.getProperty(name));
            }
            if (databaseProductName.equalsIgnoreCase(DatabaseProduct.APACHE_DERBY.nameWithSpaces())) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute(DERBY_DEMO_OUT_SQL);
                    stmt.execute(DERBY_DEMO_ADD_SQL);
                    stmt.execute(DERBY_DEMO_ADD2_SQL);
                } catch (SQLException e) {
                    fail("Exception during Stored Procedure Creation.", e);
                }
            }
            Statement stmt = connection.createStatement();
            String createTable = "CREATE TABLE NAME (ID INTEGER PRIMARY KEY, FIRSTNAME VARCHAR(255), " +
                    "LASTNAME VARCHAR(255))";
            stmt.executeUpdate(createTable);
        } catch (SQLException ex) {
            fail("Exception", ex);
        }
    }

    @AfterClass
    public static void afterClass() throws SQLException {
        if (connection!=null && !connection.isClosed()) {
            Statement stmt = connection.createStatement();
            stmt.execute("DROP TABLE NAME");
            connection.close();
        }
    }

    @Test
    public void adaptForSqlTest() throws IOException, JSONException {
        CamelContext camelContext = new DefaultCamelContext();
        SqlConnectorMetaDataExtension ext = new SqlConnectorMetaDataExtension(camelContext);
        Map<String,Object> parameters = new HashMap<>();
        for (final String name: properties.stringPropertyNames()) {
            parameters.put(name.substring(name.indexOf('.') + 1), properties.getProperty(name));
        }
        parameters.put("query", "SELECT * FROM NAME WHERE ID=:#id");
        Optional<MetaData> metadata = ext.meta(parameters);
        SqlMetadataRetrieval adapter = new SqlMetadataRetrieval();

        SyndesisMetadata syndesisMetaData2 = adapter.adapt(camelContext, "sql", "sql-connector", parameters, metadata.get());
        String expectedMetadata = IOUtils.toString(this.getClass().getResource("/sql/name_sql_metadata.json"), StandardCharsets.UTF_8).trim();
        ObjectWriter writer = Json.writer();
        String actualMetadata = writer.with(writer.getConfig().getDefaultPrettyPrinter()).writeValueAsString(syndesisMetaData2);
        assertEquals(expectedMetadata, actualMetadata, JSONCompareMode.STRICT);

    }

    @Test
    public void adaptForSqlNoParamTest() throws IOException, JSONException {
        CamelContext camelContext = new DefaultCamelContext();
        SqlConnectorMetaDataExtension ext = new SqlConnectorMetaDataExtension(camelContext);
        Map<String,Object> parameters = new HashMap<>();
        for (final String name: properties.stringPropertyNames()) {
            parameters.put(name.substring(name.indexOf('.') + 1), properties.getProperty(name));
        }
        parameters.put("query", "SELECT * FROM NAME");
        Optional<MetaData> metadata = ext.meta(parameters);
        SqlMetadataRetrieval adapter = new SqlMetadataRetrieval();

        SyndesisMetadata syndesisMetaData2 = adapter.adapt(camelContext, "sql", "sql-connector", parameters, metadata.get());
        String expectedMetadata = IOUtils.toString(this.getClass().getResource("/sql/name_sql_no_param_metadata.json"), StandardCharsets.UTF_8).trim();
        ObjectWriter writer = Json.writer();
        String actualMetadata = writer.with(writer.getConfig().getDefaultPrettyPrinter()).writeValueAsString(syndesisMetaData2);
        assertEquals(expectedMetadata, actualMetadata, JSONCompareMode.STRICT);

    }

    @Test
    public void adaptForSqlStoredTest() throws IOException, JSONException {
        CamelContext camelContext = new DefaultCamelContext();
        SqlStoredConnectorMetaDataExtension ext = new SqlStoredConnectorMetaDataExtension(camelContext);
        Map<String,Object> parameters = new HashMap<>();
        for (final String name: properties.stringPropertyNames()) {
            parameters.put(name.substring(name.indexOf(".")+1), properties.getProperty(name));
        }
        Optional<MetaData> metadata = ext.meta(parameters);

        SqlMetadataRetrieval adapter = new SqlMetadataRetrieval();
        SyndesisMetadata syndesisMetaData = adapter.adapt(camelContext, "sql", "sql-stored-connector", parameters, metadata.get());

        ObjectWriter writer = Json.writer();

        String expectedListOfProcedures = IOUtils.toString(this.getClass().getResource("/sql/stored_procedure_list.json"), StandardCharsets.UTF_8).trim();
        String actualListOfProcedures = writer.with(writer.getConfig().getDefaultPrettyPrinter()).writeValueAsString(syndesisMetaData);
        assertEquals(expectedListOfProcedures, actualListOfProcedures, JSONCompareMode.STRICT);

        parameters.put(SqlMetadataRetrieval.PATTERN, SqlMetadataRetrieval.FROM_PATTERN);
        String expectedListOfStartProcedures = IOUtils.toString(this.getClass().getResource("/sql/stored_procedure_list.json"), StandardCharsets.UTF_8).trim();
        String actualListOfStartProcedures = writer.with(writer.getConfig().getDefaultPrettyPrinter()).writeValueAsString(syndesisMetaData);
        assertEquals(expectedListOfStartProcedures, actualListOfStartProcedures, JSONCompareMode.STRICT);

        parameters.put("procedureName", "DEMO_ADD");
        SyndesisMetadata syndesisMetaData2 = adapter.adapt(camelContext, "sql", "sql-stored-connector", parameters, metadata.get());
        String expectedMetadata = IOUtils.toString(this.getClass().getResource("/sql/demo_add_metadata.json"), StandardCharsets.UTF_8).trim();
        String actualMetadata = writer.with(writer.getConfig().getDefaultPrettyPrinter()).writeValueAsString(syndesisMetaData2);
        assertEquals(expectedMetadata, actualMetadata, JSONCompareMode.STRICT);

    }
}
