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
import java.sql.DriverManager;
import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.syndesis.connector.sql.common.DbMetaDataHelper;
import io.syndesis.connector.sql.common.JDBCTypeHelper;
import io.syndesis.connector.sql.common.stored.ColumnMode;
import io.syndesis.connector.sql.common.stored.StoredProcedureColumn;
import io.syndesis.connector.sql.common.stored.StoredProcedureMetadata;
import io.syndesis.connector.support.util.ConnectorOptions;

public final class SqlSupport {

    private SqlSupport() {
    }

    public static Connection createConnection(final Map<String, Object> properties) throws SQLException {
        return DriverManager.getConnection(
            ConnectorOptions.extractOption(properties, "url"),
            ConnectorOptions.extractOption(properties, "user"),
            ConnectorOptions.extractOption(properties, "password")
        );
    }

    public static StoredProcedureMetadata getProcedureMetadata(final Connection connection, final String catalog,
                                                                         final String schema, final String procedureName) {
        // We fetch procedure columns as this is what the connector
        // supports, functions might work as a side effect, we have very
        // little support for them. We (currently) test PostgreSQL and
        // MariaDB functions and stored procedures, and fetching
        // meta data for both via stored procedures meta data seems to
        // yield best results
        final StoredProcedureMetadata storedProcedureMetadata = new StoredProcedureMetadata();
        storedProcedureMetadata.setName(procedureName);
        try {
            final DbMetaDataHelper dbHelper = new DbMetaDataHelper(connection);
            try (ResultSet columnSet = dbHelper.fetchProcedureColumns(catalog, schema, procedureName)) {
                final List<StoredProcedureColumn> columnList = new ArrayList<>();
                while (columnSet.next()) {
                    final ColumnMode mode = ColumnMode.valueOf(columnSet.getInt("COLUMN_TYPE"));
                    if (ColumnMode.IN == mode || ColumnMode.OUT == mode || ColumnMode.INOUT == mode) {
                        final StoredProcedureColumn column = new StoredProcedureColumn();
                        column.setName(columnSet.getString("COLUMN_NAME"));
                        column.setMode(mode);

                        final JDBCType jdbcType = JDBCTypeHelper.determineJDBCType(columnSet);
                        column.setJdbcType(jdbcType);
                        columnList.add(column);
                    }
                }

                final String template = columnList.stream().map(StoredProcedureColumn::toProcedureParameterString)
                    .collect(Collectors.joining(", ", procedureName + "(", ")"));
                storedProcedureMetadata.setTemplate(template);
                storedProcedureMetadata.setColumnList(columnList);
                return storedProcedureMetadata;
            }
        } catch (final SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Map<String, StoredProcedureMetadata> getProceduresAndFunctions(final Map<String, Object> parameters) {

        final Map<String, StoredProcedureMetadata> storedProcedures = new HashMap<>();

        try (Connection connection = DriverManager.getConnection(
                    ConnectorOptions.extractOption(parameters, "url"),
                    ConnectorOptions.extractOption(parameters, "user"),
                    ConnectorOptions.extractOption(parameters, "password"))) {

            final DbMetaDataHelper dbHelper = new DbMetaDataHelper(connection);
            final String catalog = ConnectorOptions.extractOption(parameters, "catalog");
            final String defaultSchema = dbHelper.getDefaultSchema(ConnectorOptions.extractOption(parameters, "user", ""));
            final String schemaPattern = ConnectorOptions.extractOption(parameters, "schema", defaultSchema);
            final String procedurePattern = ConnectorOptions.extractOption(parameters, "procedure-pattern");

            // Procedures were added to postgresql 11, but before that the jdbc getProcedures method returned the list
            // of database functions, then, to maintain compatibility with previous behavior, syndesis will return the
            // list of procedures and functions.

            try (ResultSet procedureSet = dbHelper.fetchProcedures(catalog, schemaPattern, procedurePattern)) {
                while (procedureSet.next()) {
                    final String name = procedureSet.getString("PROCEDURE_NAME");
                    final StoredProcedureMetadata storedProcedureMetadata = getProcedureMetadata(connection,
                        catalog, schemaPattern, name);
                    storedProcedureMetadata.setName(name);
                    storedProcedureMetadata.setType(procedureSet.getString("PROCEDURE_TYPE"));
                    storedProcedureMetadata.setRemark(procedureSet.getString("REMARKS"));
                    storedProcedures.put(storedProcedureMetadata.getName(), storedProcedureMetadata);
                }
            }

            try (ResultSet functionsSet = dbHelper.fetchFunctions(catalog, schemaPattern, procedurePattern)) {
                while (functionsSet.next()) {
                    final String name = functionsSet.getString("FUNCTION_NAME");
                    final StoredProcedureMetadata storedProcedureMetadata = getProcedureMetadata(connection,
                        catalog, schemaPattern, name);
                    storedProcedureMetadata.setName(name);
                    storedProcedureMetadata.setType(functionsSet.getString("FUNCTION_TYPE"));
                    storedProcedureMetadata.setRemark(functionsSet.getString("REMARKS"));
                    storedProcedures.put(storedProcedureMetadata.getName(), storedProcedureMetadata);
                }
            }

            return storedProcedures;

        } catch (final SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
