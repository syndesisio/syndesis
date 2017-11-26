/*
 * Copyright (C) 2017 Red Hat, Inc.
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
package io.syndesis.connector.sql.stored;

import static org.junit.Assert.fail;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import io.syndesis.connector.sql.DatabaseProduct;
import io.syndesis.connector.sql.stored.SqlStoredConnectorMetaDataExtension;

public class SqlStoredCommon {

    public Connection setupConnectionAndStoredProcedure(Connection connection, Properties properties) throws Exception {

        InputStream is = SqlStoredCommon.class.getClassLoader().getResourceAsStream("application.properties");
        properties.load(is);
        String user     = String.valueOf(properties.get("sql-stored-start-connector.user"));
        String password = String.valueOf(properties.get("sql-stored-start-connector.password"));
        String url      = String.valueOf(properties.get("sql-stored-start-connector.url"));

        System.out.println("Connecting to the database for unit tests");
        try {
            connection = DriverManager.getConnection(url,user,password);
            String databaseProductName = connection.getMetaData().getDatabaseProductName();
            SqlStoredConnectorMetaDataExtension ext = new SqlStoredConnectorMetaDataExtension();
            Map<String,Object> parameters = new HashMap<String,Object>();
            for (final String name: properties.stringPropertyNames()) {
                parameters.put(name.substring(name.indexOf(".")+1), properties.getProperty(name));
            }
            Map<String,StoredProcedureMetadata> storedProcedures = ext.getStoredProcedures(parameters);

            if (!storedProcedures.keySet().contains("DEMO_OUT")
                    && databaseProductName.equalsIgnoreCase(DatabaseProduct.APACHE_DERBY.nameWithSpaces())) {
                try (Statement stmt = connection.createStatement()) {
                    //Create procedure
                    stmt.execute(SampleStoredProcedures.DERBY_DEMO_OUT_SQL);
                } catch (Exception e) {
                    fail("Exception during Stored Procedure Creation.");
                }
            }
        } catch (Exception ex) {
            fail("Exception during database startup.");
        }
        return connection;
    }

    public void closeConnection(Connection connection) throws SQLException {
        if (connection!=null && !connection.isClosed()) {
            connection.close();
        }
    }

}
