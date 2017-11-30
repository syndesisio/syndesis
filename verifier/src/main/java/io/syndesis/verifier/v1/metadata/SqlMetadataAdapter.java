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
package io.syndesis.verifier.v1.metadata;

import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.JsonSchemaFactory;
import com.fasterxml.jackson.module.jsonSchema.types.ArraySchema;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import com.fasterxml.jackson.module.jsonSchema.types.StringSchema;

import io.syndesis.connector.sql.SqlParam;
import io.syndesis.connector.sql.SqlStatementMetaData;
import io.syndesis.connector.sql.stored.ColumnMode;
import io.syndesis.connector.sql.stored.StoredProcedureColumn;
import io.syndesis.connector.sql.stored.StoredProcedureMetadata;

import org.apache.camel.component.extension.MetaDataExtension.MetaData;
import org.springframework.stereotype.Component;

@Component("sql-adapter")
public final class SqlMetadataAdapter implements MetadataAdapter<JsonSchema> {

    static final String PROCEDURE_NAME = "procedureName";
    static final String PROCEDURE_TEMPLATE = "template";
    static final String PATTERN = "Pattern";
    static final String FROM_PATTERN = "From";

    static final String QUERY = "query";

    @Override
    public SyndesisMetadata<JsonSchema> adapt(final String actionId, final Map<String, Object> properties, final MetaData metadata) {

        if (actionId.startsWith("sql-stored")) {
            return adaptForStoredSql(actionId, properties, metadata);
        } else {
            return adaptForSql(actionId, properties, metadata);
        }
    }

    public SyndesisMetadata<JsonSchema> adaptForSql(final String actionId, final Map<String, Object> properties, final MetaData metadata) {

        final Map<String, List<PropertyPair>> enrichedProperties = new HashMap<>();

        final List<PropertyPair> ppList = new ArrayList<>();
        @SuppressWarnings("unchecked")
        final SqlStatementMetaData sqlStatementMetaData = (SqlStatementMetaData) metadata.getPayload();
        if (sqlStatementMetaData!=null) {
            ppList.add(new PropertyPair(sqlStatementMetaData.getSqlStatement(), QUERY));
            enrichedProperties.put(QUERY, ppList);

            // build the input and output schemas
            final ObjectSchema builderIn = new ObjectSchema();
            builderIn.set$schema("http://json-schema.org/schema#");
            builderIn.setTitle("SQL_PARAM_IN");
            for (SqlParam inParam: sqlStatementMetaData.getInParams()) {
                builderIn.putProperty(inParam.getName(), schemaFor(inParam.getJdbcType()));
            }

            final ObjectSchema builderOut = new ObjectSchema();
            builderOut.setTitle("SQL_PARAM_OUT");
            builderOut.set$schema("http://json-schema.org/schema#");
            for (SqlParam outParam: sqlStatementMetaData.getOutParams()) {
                builderOut.putProperty(outParam.getName(), schemaFor(outParam.getJdbcType()));
            }

            return new SyndesisMetadata<>(enrichedProperties, builderIn, builderOut);
        } else {
            return new SyndesisMetadata<>(enrichedProperties, null, null);
        }
    }
    public SyndesisMetadata<JsonSchema> adaptForStoredSql(final String actionId, final Map<String, Object> properties, final MetaData metadata) {

        final Map<String, List<PropertyPair>> enrichedProperties = new HashMap<>();

        if (isPresentAndNonNull(properties, PROCEDURE_NAME)) {
            // fetch metadata for the named procedure
            final List<PropertyPair> ppList = new ArrayList<>();
            @SuppressWarnings("unchecked")
            final Map<String, StoredProcedureMetadata> procedureMap = (Map<String, StoredProcedureMetadata>) metadata
                .getPayload();
            final String procedureName = (String) properties.get(PROCEDURE_NAME);
            final StoredProcedureMetadata storedProcedure = procedureMap.get(procedureName);
            ppList.add(new PropertyPair(storedProcedure.getTemplate(), PROCEDURE_TEMPLATE));
            enrichedProperties.put(PROCEDURE_TEMPLATE, ppList);

            // build the input and output schemas
            final ObjectSchema builderIn = new ObjectSchema();
            builderIn.set$schema("http://json-schema.org/schema#");
            builderIn.setTitle(procedureName + "_IN");

            final ObjectSchema builderOut = new ObjectSchema();
            builderOut.setTitle(procedureName + "_OUT");
            builderOut.set$schema("http://json-schema.org/schema#");

            if (storedProcedure.getColumnList() != null && !storedProcedure.getColumnList().isEmpty()) {
                for (final StoredProcedureColumn column : storedProcedure.getColumnList()) {
                    if (column.getMode().equals(ColumnMode.IN) || column.getMode().equals(ColumnMode.INOUT)) {
                        builderIn.putProperty(column.getName(), schemaFor(column.getJdbcType()));
                    }
                    if (column.getMode().equals(ColumnMode.OUT) || column.getMode().equals(ColumnMode.INOUT)) {
                        builderOut.putProperty(column.getName(), schemaFor(column.getJdbcType()));
                    }
                }
            }
            return new SyndesisMetadata<>(enrichedProperties, builderIn, builderOut);
        }

        // return list of stored procedures in the database
        @SuppressWarnings("unchecked")
        final Map<String, StoredProcedureMetadata> procedureMap = (Map<String, StoredProcedureMetadata>) metadata
            .getPayload();
        if (isPresentAndNonNull(properties, PATTERN) && FROM_PATTERN.equalsIgnoreCase(String.valueOf(properties.get(PATTERN)))) {
            enrichedProperties.put(PROCEDURE_NAME, obtainFromProcedureList(procedureMap));
        } else {
            enrichedProperties.put(PROCEDURE_NAME, obtainToProcedureList(procedureMap));
        }
        return new SyndesisMetadata<>(enrichedProperties, null, null);
    }
    /**
     * Puts all stored procedures in the list, as all queries adhere to the `To` pattern.
     *
     * @param procedureMap
     * @return list of property pairs containing the stored procedure names
     */
    private List<PropertyPair> obtainToProcedureList (Map<String, StoredProcedureMetadata> procedureMap) {
        final List<PropertyPair> ppList = new ArrayList<>();
        for (final String storedProcedureName : procedureMap.keySet()) {
            final PropertyPair pp = new PropertyPair(storedProcedureName, storedProcedureName);
            ppList.add(pp);
        }
        return ppList;
    }
    /**
     * Puts stored procedures in the list that have NO input parameters, which adheres to the `From`
     * pattern.
     *
     * @param procedureMap
     * @return list of property pairs containing the stored procedure names
     */
    private List<PropertyPair> obtainFromProcedureList (Map<String, StoredProcedureMetadata> procedureMap) {
        final List<PropertyPair> ppList = new ArrayList<>();
        for (final StoredProcedureMetadata storedProcedure : procedureMap.values() ) {
            if (! containsInputParams(storedProcedure)) {
                final PropertyPair pp = new PropertyPair(storedProcedure.getName(), storedProcedure.getName());
                ppList.add(pp);
            }
        }
        return ppList;
    }
    /**
     * Checks if the given stored procedure contains input parameters.
     *
     * @param storedProcedure
     * @return boolean - true if input params present, false if no input params.
     */
    private boolean containsInputParams(StoredProcedureMetadata storedProcedure) {
        if (storedProcedure.getColumnList() != null && !storedProcedure.getColumnList().isEmpty()) {
            for (final StoredProcedureColumn column : storedProcedure.getColumnList()) {
                if (column.getMode().equals(ColumnMode.IN) || column.getMode().equals(ColumnMode.INOUT)) {
                    return true;
                }
            }
        }
        return false;
    }

    static boolean isPresent(final Map<String, Object> properties, final String property) {
        return properties != null && properties.containsKey(property);
    }

    static boolean isPresentAndNonNull(final Map<String, Object> properties, final String property) {
        return isPresent(properties, property) && properties.get(property) != null;
    }

    /* default */ static JsonSchema schemaFor(final JDBCType jdbcType) {
        final JsonSchemaFactory factory = new JsonSchemaFactory();
        switch (jdbcType) {
        case ARRAY:
            return factory.arraySchema();
        case BINARY:
        case BLOB:
        case LONGVARBINARY:
        case VARBINARY:
            final ArraySchema binary = factory.arraySchema();
            binary.setItemsSchema(factory.integerSchema());
            return binary;
        case BIT:
        case BOOLEAN:
            return factory.booleanSchema();
        case CHAR:
        case CLOB:
        case DATALINK:
        case LONGNVARCHAR:
        case LONGVARCHAR:
        case NCHAR:
        case NCLOB:
        case NVARCHAR:
        case ROWID:
        case SQLXML:
        case VARCHAR:
            return factory.stringSchema();
        case DATE:
        case TIME:
        case TIMESTAMP:
        case TIMESTAMP_WITH_TIMEZONE:
        case TIME_WITH_TIMEZONE:
            final StringSchema date = factory.stringSchema();
            date.setFormat(JsonValueFormat.DATE_TIME);
            return date;
        case DECIMAL:
        case DOUBLE:
        case FLOAT:
        case NUMERIC:
        case REAL:
            return factory.numberSchema();
        case INTEGER:
        case BIGINT:
        case SMALLINT:
        case TINYINT:
            return factory.integerSchema();
        case NULL:
            return factory.nullSchema();
        case DISTINCT:
        case JAVA_OBJECT:
        case OTHER:
        case REF:
        case REF_CURSOR:
        case STRUCT:
        default:
            return factory.anySchema();
        }
    }

}
