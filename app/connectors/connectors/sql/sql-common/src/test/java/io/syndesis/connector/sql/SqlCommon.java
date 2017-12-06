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
package io.syndesis.connector.sql;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import static org.junit.Assert.fail;

public class SqlCommon {
    public Connection setupConnection(Connection connection, Properties properties) throws Exception {

        InputStream is = SqlCommon.class.getClassLoader().getResourceAsStream("application.properties");
        properties.load(is);
        String user     = String.valueOf(properties.get("sql-connector.user"));
        String password = String.valueOf(properties.get("sql-connector.password"));
        String url      = String.valueOf(properties.get("sql-connector.url"));

        System.out.println("Connecting to the database for unit tests");
        try {
            connection = DriverManager.getConnection(url,user,password);
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
