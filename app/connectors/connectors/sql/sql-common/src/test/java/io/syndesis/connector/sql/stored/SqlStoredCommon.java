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

import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import io.syndesis.connector.sql.DatabaseProduct;

public class SqlStoredCommon {

    public static void setupStoredProcedure(Connection connection, Properties properties) throws Exception {

        try {
            String databaseProductName = connection.getMetaData().getDatabaseProductName();
            SqlStoredConnectorMetaDataExtension ext = new SqlStoredConnectorMetaDataExtension();
            Map<String,Object> parameters = new HashMap<String,Object>();
            for (final String name: properties.stringPropertyNames()) {
                parameters.put(name.substring(name.indexOf(".")+1), properties.getProperty(name));
            }
            Map<String,StoredProcedureMetadata> storedProcedures = ext.getStoredProcedures(parameters);

            if (!storedProcedures.keySet().contains("DEMO_ADD") 
                    && databaseProductName.equalsIgnoreCase(DatabaseProduct.APACHE_DERBY.nameWithSpaces())) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute(SampleStoredProcedures.DERBY_DEMO_ADD_SQL);
                    System.out.println("Created procedure " + SampleStoredProcedures.DERBY_DEMO_ADD_SQL);
                } catch (Exception e) {
                    e.printStackTrace();
                    fail("Exception during Stored Procedure Creation.");
                }
            }
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
            ex.printStackTrace();
            fail("Exception during database startup.");
        }
    }

}
