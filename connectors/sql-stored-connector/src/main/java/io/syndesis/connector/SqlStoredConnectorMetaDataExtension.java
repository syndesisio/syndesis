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
package io.syndesis.connector;

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

import org.apache.camel.component.extension.metadata.AbstractMetaDataExtension;
import org.apache.camel.component.extension.metadata.DefaultMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlStoredConnectorMetaDataExtension extends AbstractMetaDataExtension {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqlStoredConnectorMetaDataExtension.class);

    @Override
    public Optional<MetaData> meta(Map<String, Object> properties) {

        MetaData metaData = null;
 
        Map<String, StoredProcedureMetadata> list = getStoredProcedures(properties);
        metaData = new DefaultMetaData(null, null, list);
        return Optional.of(metaData);
    }

    protected Map<String, StoredProcedureMetadata> getStoredProcedures(Map<String, Object> parameters) {

        Map<String, StoredProcedureMetadata> storedProcedures = new HashMap<>();
        ResultSet procedureSet = null;

        try (Connection connection = DriverManager.getConnection(
                String.valueOf(parameters.get("url")),
                String.valueOf(parameters.get("user")), 
                String.valueOf(parameters.get("password")));) {

            DatabaseMetaData meta = connection.getMetaData();
            String catalog = (String) parameters.getOrDefault("catalog", null);
            String defaultSchema = getDefaultSchema(meta.getDatabaseProductName(), parameters);
            String schemaPattern = (String) parameters.getOrDefault("schema-pattern", defaultSchema);
            String procedurePattern = (String) parameters.getOrDefault("procedure-pattern", null);

            if (meta.getDatabaseProductName().equalsIgnoreCase(DatabaseProduct.POSTGRESQL.name())) {
                procedureSet = meta.getFunctions(catalog, schemaPattern, procedurePattern);
            } else {
                procedureSet = meta.getProcedures(catalog, schemaPattern, procedurePattern);
            }
            while (procedureSet.next()) {
                String name = procedureSet.getString("PROCEDURE_NAME");
                StoredProcedureMetadata storedProcedureMetadata = 
                        getStoredProcedureMetadata(connection, catalog, schemaPattern, name);
                storedProcedureMetadata.setName(procedureSet.getString("PROCEDURE_NAME"));
                storedProcedureMetadata.setType(procedureSet.getString("PROCEDURE_TYPE"));
                storedProcedureMetadata.setRemark(procedureSet.getString("REMARKS"));
                storedProcedures.put(storedProcedureMetadata.getName(), storedProcedureMetadata);
            }
            return storedProcedures;

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (procedureSet != null) {
                try {
                    if (!procedureSet.isClosed()) {
                        procedureSet.close();
                    }
                } catch (SQLException e) {
                    LOGGER.warn(e.getMessage());
                }
            }
        }
    }

    protected String getDefaultSchema(String databaseProductName, Map<String, Object> parameters) {

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

    protected StoredProcedureMetadata getStoredProcedureMetadata(Connection connection, String catalog, String schema,
            String procedureName) {

        ResultSet columnSet = null;
        StoredProcedureMetadata storedProcedureMetadata = new StoredProcedureMetadata();
        storedProcedureMetadata.setName(procedureName);
        try {
            DatabaseMetaData meta = connection.getMetaData();
            if (meta.getDatabaseProductName().equalsIgnoreCase(DatabaseProduct.POSTGRESQL.name())) {
                columnSet = meta.getFunctionColumns(catalog, schema, procedureName, null);
            } else {
                columnSet = meta.getProcedureColumns(catalog, schema, procedureName, null);
            }

            List<StoredProcedureColumn> columnList = new ArrayList<>();
            String template = procedureName + "(";
            while (columnSet.next()) {
                StoredProcedureColumn column = new StoredProcedureColumn();
                column.setName(columnSet.getString("COLUMN_NAME"));
                column.setMode(ColumnMode.valueOf(columnSet.getInt("COLUMN_TYPE")));
                column.setJdbcType(JDBCType.valueOf(columnSet.getInt("DATA_TYPE")));
                if (ColumnMode.IN.equals(column.getMode())){
                    template += " " + column.getJdbcType() + " ${body[" + column.getName() + "], ";
                    columnList.add(column);
                }
                if (ColumnMode.OUT.equals(column.getMode())){
                    template += " " + column.getMode().name() + " " + column.getJdbcType() + " ${body[" + column.getName() + "], ";
                    columnList.add(column);
                }
            }
            template = template.substring(0, template.length() - 2) + ")";
            storedProcedureMetadata.setTemplate(template);
            storedProcedureMetadata.setColumnList(columnList);
            return storedProcedureMetadata;

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (columnSet != null) {
                try {
                    if (!columnSet.isClosed()) {
                        columnSet.close();
                    }
                } catch (SQLException e) {
                    LOGGER.warn(e.getMessage());
                }
            }
        }
    }

}
