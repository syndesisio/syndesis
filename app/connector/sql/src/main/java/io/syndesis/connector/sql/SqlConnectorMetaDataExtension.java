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
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

import io.syndesis.common.util.SyndesisServerException;
import io.syndesis.connector.sql.common.DbMetaDataHelper;
import io.syndesis.connector.sql.common.SqlStatementMetaData;
import io.syndesis.connector.sql.common.SqlStatementParser;
import io.syndesis.connector.support.util.ConnectorOptions;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.metadata.AbstractMetaDataExtension;
import org.apache.camel.component.extension.metadata.DefaultMetaData;

public class SqlConnectorMetaDataExtension extends AbstractMetaDataExtension {
    private static final MetaData EMPTY_METADATA = new DefaultMetaData(null, null, null);

    public SqlConnectorMetaDataExtension(CamelContext camelContext) {
        super(camelContext);
    }

    @Override
    public Optional<MetaData> meta(final Map<String, Object> properties) {
        final String sqlStatement = ConnectorOptions.extractOption(properties, "query");
        final boolean batch = ConnectorOptions.extractOptionAndMap(
             properties, "batch", Boolean::valueOf, false);

        MetaData metaData = EMPTY_METADATA;

        if (sqlStatement != null) {
            try (Connection connection = SqlSupport.createConnection(properties)) {
                DbMetaDataHelper dbHelper = new DbMetaDataHelper(connection);
                final String defaultSchema = dbHelper.getDefaultSchema(ConnectorOptions.extractOption(properties, "user", ""));
                final String schemaPattern = ConnectorOptions.extractOption(properties, "schema", defaultSchema);
                final SqlStatementParser parser = new SqlStatementParser(connection, schemaPattern, sqlStatement);
                final SqlStatementMetaData sqlStatementMetaData = parseStatement(parser);

                sqlStatementMetaData.setBatch(batch);

                metaData = new DefaultMetaData(null, null, sqlStatementMetaData);
            } catch (final SQLException e) {
                throw new SyndesisServerException(e.getMessage(),e);
            }
        }

        return Optional.of(metaData);
    }

    // *********************************
    // Helpers
    // *********************************

    private SqlStatementMetaData parseStatement(SqlStatementParser parser) throws SQLException {
        return parser.parse();
    }
}
