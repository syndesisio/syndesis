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

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.junit.rules.ExternalResource;

public final class SqlConnectionRule extends ExternalResource {

    public final Properties properties = new Properties();
    public Connection connection;
    public String schema;

    @Override
    protected void after() {
        if (connection != null) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DROP table NAME0");
                stmt.execute("DROP table ADDRESS0");
                connection.close();
            } catch (final SQLException e) {
                throw new AssertionError("Exception during database cleanup.", e);
            }
        }
    }

    @Override
    protected void before() throws Throwable {
        try (InputStream is = SqlConnectionRule.class.getClassLoader().getResourceAsStream("test-options.properties")) {
            properties.load(is);
        } catch (final IOException e) {
            throw new AssertionError("Unable to read application.properties", e);
        }

        final String user = properties.getProperty("sql-connector.user");
        final String password = properties.getProperty("sql-connector.password");
        final String url = properties.getProperty("sql-connector.url");

        try {
            connection = DriverManager.getConnection(url, user, password);
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DROP table NAME0");
                stmt.execute("DROP table ADDRESS0");
            } catch (final SQLException e) { // NOPMD
                //ignore
            }
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate("CREATE TABLE NAME0 (id INTEGER PRIMARY KEY, firstName VARCHAR(255), " + "lastName VARCHAR(255))");
                stmt.executeUpdate("CREATE TABLE ADDRESS0 (id INTEGER PRIMARY KEY, Address VARCHAR(255), " + "lastName VARCHAR(255))");
            }
        } catch (final SQLException e) {
            throw new AssertionError("Exception during database startup.", e);
        }

        schema = new DbMetaDataHelper(connection).getDefaultSchema(user);
    }

}
