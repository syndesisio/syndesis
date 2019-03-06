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

package io.syndesis.server.endpoint.v1.handler.meta;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ArraySchema;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.connection.DynamicActionMetadata;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.util.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 */
class AggregateMetadataHandler implements StepMetadataHandler {

    private static final String JSON_SCHEMA_ORG_SCHEMA = "http://json-schema.org/schema#";

    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(AggregateMetadataHandler.class);

    @Override
    public boolean canHandle(StepKind kind) {
        return StepKind.aggregate.equals(kind);
    }

    @Override
    public DynamicActionMetadata handle(DynamicActionMetadata metadata) {
        try {
            DataShape outputShape = metadata.outputShape();
            DataShape inputShape = metadata.inputShape();

            if (metadata.outputShape() != null) {
                outputShape = adaptOutputShape(new DataShape.Builder()
                                                        .createFrom(metadata.outputShape())
                                                        .decompress()
                                                        .build());
            }

            if (metadata.inputShape() != null) {
                inputShape = adaptInputShape(new DataShape.Builder()
                                                        .createFrom(metadata.inputShape())
                                                        .decompress()
                                                        .build());
            }

            return new DynamicActionMetadata.Builder()
                    .createFrom(metadata)
                    .inputShape(inputShape)
                    .outputShape(outputShape)
                    .build();
        } catch (IOException e) {
            LOG.warn("Unable to read output data shape on dynamic meta data inspection", e);
        }

        return metadata;
    }

    DataShape adaptInputShape(DataShape dataShape) throws IOException {
        Optional<DataShape> singleElementShape = dataShape.findVariantByMeta(VARIANT_METADATA_KEY, VARIANT_ELEMENT);

        if (singleElementShape.isPresent()) {
            return singleElementShape.get();
        }

        DataShape collectionShape = dataShape.findVariantByMeta(VARIANT_METADATA_KEY, VARIANT_COLLECTION).orElse(dataShape);

        if (collectionShape.getKind().equals(DataShapeKinds.JSON_SCHEMA)) {
            String specification = dataShape.getSpecification();
            JsonSchema schema = Json.reader().forType(JsonSchema.class).readValue(specification);

            if (schema.isArraySchema()) {
                ArraySchema.Items items = ((ArraySchema) schema).getItems();
                JsonSchema itemSchema = items.asSingleItems().getSchema();
                itemSchema.set$schema(schema.get$schema());
                if (items.isSingleItems()) {
                    return new DataShape.Builder().createFrom(collectionShape)
                                                        .putMetadata(VARIANT_METADATA_KEY, VARIANT_ELEMENT)
                                                        .specification(Json.writer().writeValueAsString(itemSchema))
                                                        .build();
                }
            }
        } else if (collectionShape.getKind().equals(DataShapeKinds.JSON_INSTANCE)) {
            String specification = dataShape.getSpecification();
            List<Object> items = Json.reader().forType(List.class).readValue(specification);
            if (!items.isEmpty()) {
                return new DataShape.Builder().createFrom(collectionShape)
                                                        .putMetadata(VARIANT_METADATA_KEY, VARIANT_ELEMENT)
                                                        .specification(Json.writer().writeValueAsString(items.get(0)))
                                                        .build();
            }
        }

        return dataShape;
    }

    DataShape adaptOutputShape(DataShape dataShape) throws IOException {
        Optional<DataShape> collectionShape = dataShape.findVariantByMeta(VARIANT_METADATA_KEY, VARIANT_COLLECTION);

        if (collectionShape.isPresent()) {
            return collectionShape.get();
        }

        DataShape singleElementShape = dataShape.findVariantByMeta(VARIANT_METADATA_KEY, VARIANT_ELEMENT).orElse(dataShape);

        if (singleElementShape.getKind().equals(DataShapeKinds.JSON_SCHEMA)) {
            String specification = dataShape.getSpecification();
            JsonSchema schema = Json.reader().forType(JsonSchema.class).readValue(specification);

            ArraySchema collectionSchema = new ArraySchema();
            collectionSchema.set$schema(Optional.ofNullable(schema.get$schema()).orElse(JSON_SCHEMA_ORG_SCHEMA));
            collectionSchema.setItemsSchema(schema);
            schema.set$schema(null);

            return new DataShape.Builder().createFrom(singleElementShape)
                                            .putMetadata(VARIANT_METADATA_KEY, VARIANT_COLLECTION)
                                            .specification(Json.writer().writeValueAsString(collectionSchema))
                                            .build();
        } else if (singleElementShape.getKind().equals(DataShapeKinds.JSON_INSTANCE)) {
            String specification = dataShape.getSpecification();
            return new DataShape.Builder().createFrom(singleElementShape)
                                            .putMetadata(VARIANT_METADATA_KEY, VARIANT_COLLECTION)
                                            .specification("[" + specification + "]")
                                            .build();
        }

        return dataShape;
    }
}
