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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.junit.rules.ExternalResource;

public final class SqlConnectionRule extends ExternalResource {

    Connection connection;

    final Properties properties = new Properties();

    String schema;

    public Connection getConnection() {
        return connection;
    }

    @Override
    protected void after() {
        if (connection != null) {
            try {
                connection.close();
            } catch (final SQLException e) {
                throw new AssertionError("Exception during database cleanup.", e);
            }
        }
    }

    @Override
    protected void before() throws Throwable {
        try (InputStream is = SqlConnectionRule.class.getClassLoader().getResourceAsStream("application.properties")) {
            properties.load(is);
        } catch (final IOException e) {
            throw new AssertionError("Unable to read application.properties", e);
        }

        final String user = String.valueOf(properties.get("sql-connector.user"));
        final String password = String.valueOf(properties.get("sql-connector.password"));
        final String url = String.valueOf(properties.get("sql-connector.url"));

        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (final SQLException e) {
            throw new AssertionError("Exception during database startup.", e);
        }

        schema = DatabaseMetaDataHelper.getDefaultSchema(connection.getMetaData().getDatabaseProductName(), user);
    }

}
