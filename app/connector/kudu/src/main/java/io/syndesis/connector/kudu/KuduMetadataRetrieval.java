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

package io.syndesis.connector.kudu;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.module.jsonSchema.factories.JsonSchemaFactory;
import com.fasterxml.jackson.module.jsonSchema.types.ArraySchema;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.util.Json;
import io.syndesis.common.util.SyndesisServerException;
import io.syndesis.connector.kudu.common.KuduSupport;
import io.syndesis.connector.kudu.meta.KuduMetaData;
import io.syndesis.connector.support.verifier.api.ComponentMetadataRetrieval;
import io.syndesis.connector.support.verifier.api.SyndesisMetadata;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.MetaDataExtension;
import org.apache.camel.util.ObjectHelper;
import org.apache.kudu.ColumnSchema;
import org.apache.kudu.client.KuduClient;
import org.apache.kudu.client.KuduException;
import org.apache.kudu.client.KuduTable;

public class KuduMetadataRetrieval extends ComponentMetadataRetrieval {
    private static final String JSON_SCHEMA_ORG_SCHEMA = "http://json-schema.org/schema#";
    private static final String KUDU_INSERT_ACTION = "kudu-insert-connector";
    private static final String KUDU_SCAN_ACTION = "kudu-scan-connector";

    @Override
    protected SyndesisMetadata adapt(CamelContext context, String componentId, String actionId, Map<String, Object> properties, MetaDataExtension.MetaData metadata) {
        final KuduMetaData kuduMetaData = (KuduMetaData) metadata.getPayload();

        if (kuduMetaData != null) {
            // build the input and output schemas
            final ObjectSchema spec = createSpec(kuduMetaData);

            try {
                DataShape.Builder inDataShapeBuilder = new DataShape.Builder().type("KUDU_TABLE_IN");
                if (ObjectHelper.equal(actionId, KUDU_INSERT_ACTION)) {
                    inDataShapeBuilder.kind(DataShapeKinds.JSON_SCHEMA)
                            .name("Kudu table")
                            .description(String.format("Columns for table [%s]", kuduMetaData.getTableName()))
                            .specification(Json.writer().writeValueAsString(spec));
                } else {
                    inDataShapeBuilder.kind(DataShapeKinds.NONE);
                }

                DataShape.Builder outDataShapeBuilder = new DataShape.Builder().type("KUDU_TABLE_OUT");
                if (ObjectHelper.equal(actionId, KUDU_SCAN_ACTION)) {
                    outDataShapeBuilder.kind(DataShapeKinds.JSON_SCHEMA)
                            .name("Kudu table")
                            .description(String.format("Columns for table [%s]", kuduMetaData.getTableName()));

                    ArraySchema collectionSpec = new ArraySchema();
                    collectionSpec.set$schema(JSON_SCHEMA_ORG_SCHEMA);
                    collectionSpec.setItemsSchema(spec);
                    outDataShapeBuilder.specification(Json.writer().writeValueAsString(collectionSpec));
                } else {
                    outDataShapeBuilder.kind(DataShapeKinds.NONE);
                }

                return SyndesisMetadata.of(inDataShapeBuilder.build(), outDataShapeBuilder.build());
            } catch (JsonProcessingException e) {
                throw new IllegalStateException(e);
            }
        } else {
            return SyndesisMetadata.EMPTY;
        }
    }

    @Override
    protected MetaDataExtension resolveMetaDataExtension(CamelContext context, Class<? extends MetaDataExtension> metaDataExtensionClass, String componentId, String actionId) {
        return new KuduMetaDataExtension(context);
    }

    private ObjectSchema createSpec(KuduMetaData kuduMetaData) {
        // build the input and output schemas
        ObjectSchema spec = new ObjectSchema();

        spec.set$schema(JSON_SCHEMA_ORG_SCHEMA);
        spec.setTitle("KUDU_INSERT");

        Map<String, Object> options = new HashMap<>();
        options.put("host", kuduMetaData.getHost());
        options.put("port", kuduMetaData.getPort());
        KuduClient client = KuduSupport.createConnection(options);

        try {
            KuduTable table = client.openTable(kuduMetaData.getTableName());
            Iterator<ColumnSchema> columns = table.getSchema().getColumns().iterator();

            while (columns.hasNext()) {
                ColumnSchema column = columns.next();

                switch (column.getType().getName()) {
                    case "string":
                        spec.putProperty(column.getName(), new JsonSchemaFactory().stringSchema());
                        break;
                    case "bool":
                        spec.putProperty(column.getName(), new JsonSchemaFactory().booleanSchema());
                        break;
                    case "float":
                    case "double":
                    case "int8":
                    case "int16":
                    case "int32":
                    case "int64":
                        spec.putProperty(column.getName(), new JsonSchemaFactory().integerSchema());
                        break;
                    default:
                        throw new SyndesisServerException("The column schema type " + column.getType().getName()
                                + " for column " + column.getName()
                                + " is not supported at the moment");
                }
            }
        } catch (KuduException e) {
            throw new SyndesisServerException("Unable to connect to kudu schema " + kuduMetaData.getTableName(), e);
        }

        return spec;
    }
}
