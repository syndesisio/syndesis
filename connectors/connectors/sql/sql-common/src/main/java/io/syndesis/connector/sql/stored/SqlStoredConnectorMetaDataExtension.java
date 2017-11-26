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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.camel.component.extension.metadata.AbstractMetaDataExtension;
import org.apache.camel.component.extension.metadata.DefaultMetaData;

import io.syndesis.connector.sql.DatabaseProduct;

public class SqlStoredConnectorMetaDataExtension extends AbstractMetaDataExtension {

    @Override
    public Optional<MetaData> meta(final Map<String, Object> properties) {

        MetaData metaData = null;

        final Map<String, StoredProcedureMetadata> list = getStoredProcedures(properties);
        metaData = new DefaultMetaData(null, null, list);
        return Optional.of(metaData);
    }

    protected String getDefaultSchema(final String databaseProductName, final Map<String, Object> parameters) {

        String defaultSchema = null;
        // Oracle uses the username as schema
        if (databaseProductName.equalsIgnoreCase(DatabaseProduct.ORACLE.name())) {
            defaultSchema = parameters.get("user").toString();
        } else if (databaseProductName.equalsIgnoreCase(DatabaseProduct.POSTGRESQL.name())) {
            defaultSchema = "public";
        } else if (databaseProductName.equalsIgnoreCase(DatabaseProduct.APACHE_DERBY.nameWithSpaces())) {
            if (parameters.get("user") != null) {
                defaultSchema = parameters.get("user").toString().toUpperCase();
            } else {
                defaultSchema = "NULL";
            }
        }
        return defaultSchema;
    }

    protected StoredProcedureMetadata getStoredProcedureMetadata(final Connection connection, final String catalog,
        final String schema, final String procedureName) {

        final StoredProcedureMetadata storedProcedureMetadata = new StoredProcedureMetadata();
        storedProcedureMetadata.setName(procedureName);
        try {
            final DatabaseMetaData meta = connection.getMetaData();
            try (ResultSet columnSet = fetchProcedureColumns(meta, catalog, schema, procedureName)) {
                final List<StoredProcedureColumn> columnList = new ArrayList<>();
                while (columnSet.next()) {
                    final ColumnMode mode = ColumnMode.valueOf(columnSet.getInt("COLUMN_TYPE"));
                    if (ColumnMode.IN == mode || ColumnMode.OUT == mode) {
                        final StoredProcedureColumn column = new StoredProcedureColumn();
                        column.setName(columnSet.getString("COLUMN_NAME"));
                        column.setMode(mode);
                        column.setJdbcType(JDBCType.valueOf(columnSet.getInt("DATA_TYPE")));
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

    protected Map<String, StoredProcedureMetadata> getStoredProcedures(final Map<String, Object> parameters) {

        final Map<String, StoredProcedureMetadata> storedProcedures = new HashMap<>();

        try (Connection connection = DriverManager.getConnection(String.valueOf(parameters.get("url")),
            String.valueOf(parameters.get("user")), String.valueOf(parameters.get("password")));) {

            final DatabaseMetaData meta = connection.getMetaData();
            final String catalog = (String) parameters.getOrDefault("catalog", null);
            final String defaultSchema = getDefaultSchema(meta.getDatabaseProductName(), parameters);
            final String schemaPattern = (String) parameters.getOrDefault("schema-pattern", defaultSchema);
            final String procedurePattern = (String) parameters.getOrDefault("procedure-pattern", null);

            try (ResultSet procedureSet = fetchProcedures(meta, catalog, schemaPattern, procedurePattern)) {
                while (procedureSet.next()) {
                    final String name = procedureSet.getString("PROCEDURE_NAME");
                    final StoredProcedureMetadata storedProcedureMetadata = getStoredProcedureMetadata(connection,
                        catalog, schemaPattern, name);
                    storedProcedureMetadata.setName(procedureSet.getString("PROCEDURE_NAME"));
                    storedProcedureMetadata.setType(procedureSet.getString("PROCEDURE_TYPE"));
                    storedProcedureMetadata.setRemark(procedureSet.getString("REMARKS"));
                    storedProcedures.put(storedProcedureMetadata.getName(), storedProcedureMetadata);
                }
            }
            return storedProcedures;

        } catch (final SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    /* default */ static ResultSet fetchProcedureColumns(final DatabaseMetaData meta, final String catalog,
        final String schema, final String procedureName) throws SQLException {
        if (meta.getDatabaseProductName().equalsIgnoreCase(DatabaseProduct.POSTGRESQL.name())) {
            return meta.getFunctionColumns(catalog, schema, procedureName, null);
        }

        return meta.getProcedureColumns(catalog, schema, procedureName, null);
    }

    /* default */ static ResultSet fetchProcedures(final DatabaseMetaData meta, final String catalog,
        final String schemaPattern, final String procedurePattern) throws SQLException {
        if (meta.getDatabaseProductName().equalsIgnoreCase(DatabaseProduct.POSTGRESQL.name())) {
            return meta.getFunctions(catalog, schemaPattern, procedurePattern);
        }

        return meta.getProcedures(catalog, schemaPattern, procedurePattern);
    }

}
