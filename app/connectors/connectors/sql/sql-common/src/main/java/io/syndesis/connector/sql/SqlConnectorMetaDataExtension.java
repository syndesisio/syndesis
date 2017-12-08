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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

import org.apache.camel.component.extension.metadata.AbstractMetaDataExtension;
import org.apache.camel.component.extension.metadata.DefaultMetaData;

public class SqlConnectorMetaDataExtension extends AbstractMetaDataExtension {

    @Override
    public Optional<MetaData> meta(final Map<String, Object> properties) {

        final String sqlStatement = (String) properties.get("query");

        if (sqlStatement!=null) {
            try (Connection connection = DriverManager.getConnection(String.valueOf(properties.get("url")),
                    String.valueOf(properties.get("user")), String.valueOf(properties.get("password")));) {
    
                final DatabaseMetaData meta = connection.getMetaData();
                final String defaultSchema = DatabaseMetaDataHelper.getDefaultSchema(
                        meta.getDatabaseProductName(), String.valueOf(properties.get("user")));
                final String schemaPattern = (String) properties.getOrDefault("schema-pattern", defaultSchema);
                final SqlStatementParser parser = new SqlStatementParser(connection, schemaPattern, sqlStatement);
                final SqlStatementMetaData sqlStatementMetaData = parser.parse();
                final MetaData metaData = new DefaultMetaData(null, null, sqlStatementMetaData);
                return Optional.of(metaData);
    
            } catch (final SQLException e) {
                throw new IllegalStateException(e);
            }
        } else {
            final MetaData metaData = new DefaultMetaData(null, null, null);
            return Optional.of(metaData);
        }
    }
}
