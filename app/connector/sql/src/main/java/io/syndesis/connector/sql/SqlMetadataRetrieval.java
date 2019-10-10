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

import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.MetaDataExtension;
import org.apache.camel.component.extension.MetaDataExtension.MetaData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.JsonSchemaFactory;
import com.fasterxml.jackson.module.jsonSchema.types.ArraySchema;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import com.fasterxml.jackson.module.jsonSchema.types.StringSchema;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.DataShapeMetaData;
import io.syndesis.common.util.Json;
import io.syndesis.connector.sql.common.SqlParam;
import io.syndesis.connector.sql.common.SqlStatementMetaData;
import io.syndesis.connector.sql.common.stored.ColumnMode;
import io.syndesis.connector.sql.common.stored.StoredProcedureColumn;
import io.syndesis.connector.sql.common.stored.StoredProcedureMetadata;
import io.syndesis.connector.sql.stored.SqlStoredConnectorMetaDataExtension;
import io.syndesis.connector.support.util.ConnectorOptions;
import io.syndesis.connector.support.verifier.api.ComponentMetadataRetrieval;
import io.syndesis.connector.support.verifier.api.PropertyPair;
import io.syndesis.connector.support.verifier.api.SyndesisMetadata;

@SuppressWarnings("PMD.GodClass")
public final class SqlMetadataRetrieval extends ComponentMetadataRetrieval {

    private static final String JSON_SCHEMA_ORG_SCHEMA = "http://json-schema.org/schema#";

    static final String PROCEDURE_NAME = "procedureName";
    static final String PROCEDURE_TEMPLATE = "template";
    static final String PATTERN = "Pattern";
    static final String FROM_PATTERN = "From";
    static final String QUERY = "query";

    @Override
    protected SyndesisMetadata adapt(CamelContext context, String componentId, String actionId, Map<String, Object> properties, MetaData metadata) {
        if (actionId.startsWith("sql-stored")) {
            return adaptForStoredSql(actionId, properties, metadata);
        } else {
            return adaptForSql(actionId, properties, metadata);
        }
    }

    @Override
    protected MetaDataExtension resolveMetaDataExtension(CamelContext context, Class<? extends MetaDataExtension> metaDataExtensionClass, String componentId, String actionId) {
        if (actionId.startsWith("sql-stored")) {
            return new SqlStoredConnectorMetaDataExtension(context);
        } else {
            return new SqlConnectorMetaDataExtension(context);
        }
    }

    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.StdCyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity"})
    public SyndesisMetadata adaptForSql(final String actionId, final Map<String, Object> properties, final MetaData metadata) {

        final Map<String, List<PropertyPair>> enrichedProperties = new HashMap<>();
        @SuppressWarnings("unchecked")
        final SqlStatementMetaData sqlStatementMetaData = (SqlStatementMetaData) metadata.getPayload();

        if (sqlStatementMetaData != null) {
            enrichedProperties.put(QUERY, Collections.singletonList(new PropertyPair(sqlStatementMetaData.getSqlStatement())));

            // build the input and output schemas
            final JsonSchema specIn;
            final ObjectSchema builderIn = new ObjectSchema();
            builderIn.setTitle("SQL_PARAM_IN");

            if (sqlStatementMetaData.isVerifiedBatchUpdateMode()) {
                ArraySchema arraySpec = new ArraySchema();
                arraySpec.set$schema(JSON_SCHEMA_ORG_SCHEMA);
                arraySpec.setItemsSchema(builderIn);
                specIn = arraySpec;
            } else {
                builderIn.set$schema(JSON_SCHEMA_ORG_SCHEMA);
                specIn = builderIn;
            }

            for (SqlParam inParam: sqlStatementMetaData.getInParams()) {
                builderIn.putProperty(inParam.getName(), schemaFor(inParam.getJdbcType()));
            }

            final ObjectSchema builderOut = new ObjectSchema();
            builderOut.setTitle("SQL_PARAM_OUT");
            for (SqlParam outParam: sqlStatementMetaData.getOutParams()) {
                builderOut.putProperty(outParam.getName(), schemaFor(outParam.getJdbcType()));
            }
            final ArraySchema outputSpec = new ArraySchema();
            outputSpec.set$schema(JSON_SCHEMA_ORG_SCHEMA);
            outputSpec.setItemsSchema(builderOut);

            try {
                DataShape.Builder inDataShapeBuilder = new DataShape.Builder().type(builderIn.getTitle());
                if (builderIn.getProperties().isEmpty()) {
                    inDataShapeBuilder.kind(DataShapeKinds.NONE);
                } else {
                    inDataShapeBuilder.kind(DataShapeKinds.JSON_SCHEMA)
                        .name("SQL Parameter")
                        .description(String.format("Parameters of SQL [%s]", sqlStatementMetaData.getSqlStatement()))
                        .specification(Json.writer().writeValueAsString(specIn));

                    if (specIn.isObjectSchema()) {
                        inDataShapeBuilder.putMetadata(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_ELEMENT);
                    }

                    if (specIn.isArraySchema()) {
                        inDataShapeBuilder.putMetadata(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_COLLECTION);
                    }
                }
                DataShape.Builder outDataShapeBuilder = new DataShape.Builder().type(builderOut.getTitle());
                if (builderOut.getProperties().isEmpty()) {
                    outDataShapeBuilder.kind(DataShapeKinds.NONE);
                } else {
                    outDataShapeBuilder.kind(DataShapeKinds.JSON_SCHEMA)
                        .name("SQL Result")
                        .description(String.format("Result of SQL [%s]", sqlStatementMetaData.getSqlStatement()))
                        .putMetadata(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_COLLECTION)
                        .specification(Json.writer().writeValueAsString(outputSpec));
                }

                return new SyndesisMetadata(enrichedProperties,
                        inDataShapeBuilder.build(), outDataShapeBuilder.build());
            } catch (JsonProcessingException e) {
                throw new IllegalStateException(e);
            }
        } else {
            return new SyndesisMetadata(enrichedProperties, null, null);
        }
    }

    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.StdCyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity"})
    public SyndesisMetadata adaptForStoredSql(final String actionId, final Map<String, Object> properties, final MetaData metadata) {

        final Map<String, List<PropertyPair>> enrichedProperties = new HashMap<>();

        // list of stored procedures in the database
        @SuppressWarnings("unchecked")
        final Map<String, StoredProcedureMetadata> procedureMap = (Map<String, StoredProcedureMetadata>) metadata.getPayload();
        if (isPresentAndNonNull(properties, PATTERN) && FROM_PATTERN.equalsIgnoreCase(ConnectorOptions.extractOption(properties, PATTERN))) {
            enrichedProperties.put(PROCEDURE_NAME, obtainFromProcedureList(procedureMap));
        } else {
            enrichedProperties.put(PROCEDURE_NAME, obtainToProcedureList(procedureMap));
        }
        // metadata for the named procedure
        if (isPresentAndNonNull(properties, PROCEDURE_NAME)) {
            final List<PropertyPair> ppList = new ArrayList<>();
            final String procedureName = ConnectorOptions.extractOption(properties, PROCEDURE_NAME);
            final StoredProcedureMetadata storedProcedure = procedureMap.get(procedureName);
            ppList.add(new PropertyPair(storedProcedure.getTemplate(), PROCEDURE_TEMPLATE));
            enrichedProperties.put(PROCEDURE_TEMPLATE, ppList);

            // build the input and output schemas
            final ObjectSchema builderIn = new ObjectSchema();
            builderIn.set$schema(JSON_SCHEMA_ORG_SCHEMA);
            builderIn.setTitle(procedureName + "_IN");

            final ObjectSchema builderOut = new ObjectSchema();
            builderOut.setTitle(procedureName + "_OUT");
            builderOut.set$schema(JSON_SCHEMA_ORG_SCHEMA);

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

            try {
                DataShape.Builder inDataShapeBuilder = new DataShape.Builder().type(builderIn.getTitle());
                if (builderIn.getProperties().isEmpty()) {
                    inDataShapeBuilder.kind(DataShapeKinds.NONE);
                } else {
                    inDataShapeBuilder.kind(DataShapeKinds.JSON_SCHEMA)
                        .name(procedureName + " Parameter")
                        .description(String.format("Parameters of Stored Procedure '%s'", procedureName))
                        .specification(Json.writer().writeValueAsString(builderIn));
                }
                DataShape.Builder outDataShapeBuilder = new DataShape.Builder().type(builderOut.getTitle());
                if (builderOut.getProperties().isEmpty()) {
                    outDataShapeBuilder.kind(DataShapeKinds.NONE);
                } else {
                    outDataShapeBuilder.kind(DataShapeKinds.JSON_SCHEMA)
                        .name(procedureName + " Return")
                        .description(String.format("Return value of Stored Procedure '%s'", procedureName))
                        .putMetadata(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_ELEMENT)
                        .specification(Json.writer().writeValueAsString(builderOut));
                }

                return new SyndesisMetadata(enrichedProperties,
                        inDataShapeBuilder.build(), outDataShapeBuilder.build());
            } catch (JsonProcessingException e) {
                throw new IllegalStateException(e);
            }
        }

        return new SyndesisMetadata(enrichedProperties, null, null);
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
        return ConnectorOptions.extractOption(properties, property) != null;
    }

    @SuppressWarnings("PMD.CyclomaticComplexity")
    static JsonSchema schemaFor(final JDBCType jdbcType) {
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
