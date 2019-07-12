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
import io.syndesis.common.model.DataShapeMetaData;
import io.syndesis.common.model.connection.DynamicActionMetadata;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.util.Json;
import io.syndesis.common.util.json.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

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
    public DynamicActionMetadata createMetadata(Step step, List<Step> previousSteps, List<Step> subsequentSteps) {
        DataShape inputShape = StepMetadataHelper.getFirstWithInputShape(subsequentSteps)
                                            .flatMap(Step::inputDataShape)
                                            .orElse(StepMetadataHelper.NO_SHAPE);

        DataShape outputShape = StepMetadataHelper.getLastWithOutputShape(previousSteps)
                                            .flatMap(Step::outputDataShape)
                                            .orElse(StepMetadataHelper.NO_SHAPE);

        return new DynamicActionMetadata.Builder()
                        .inputShape(inputShape)
                        .outputShape(outputShape)
                        .build();
    }

    @Override
    public DynamicActionMetadata handle(DynamicActionMetadata metadata) {
        DataShape outputShape = metadata.outputShape();
        DataShape inputShape = metadata.inputShape();

        try {
            if (metadata.outputShape() != null) {
                outputShape = adaptOutputShape(new DataShape.Builder()
                        .createFrom(metadata.outputShape())
                        .decompress()
                        .build());
            }
        } catch (IOException e) {
            LOG.warn("Unable to read input data shape on dynamic meta data inspection", e);
            LOG.warn("Using original input shape as fallback");
        }

        try {
            if (metadata.inputShape() != null) {
                inputShape = adaptInputShape(new DataShape.Builder()
                                                        .createFrom(metadata.inputShape())
                                                        .decompress()
                                                        .build());
            }
        } catch (IOException e) {
            LOG.warn("Unable to read output data shape on dynamic meta data inspection", e);
            LOG.warn("Using original output shape as fallback");
        }

        return new DynamicActionMetadata.Builder()
                .createFrom(metadata)
                .inputShape(inputShape)
                .outputShape(outputShape)
                .build();
    }

    DataShape adaptInputShape(DataShape dataShape) throws IOException {
        Optional<DataShape> singleElementShape = dataShape.findVariantByMeta(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_ELEMENT);

        if (singleElementShape.isPresent()) {
            if (dataShape.equals(singleElementShape.get())) {
                return singleElementShape.get();
            } else {
                return new DataShape.Builder()
                        .createFrom(singleElementShape.get())
                        .addAllVariants(StepMetadataHelper.extractVariants(dataShape, singleElementShape.get(), DataShapeMetaData.VARIANT_ELEMENT))
                        .build();
            }
        }

        if (StepMetadataHelper.isUnifiedJsonSchemaShape(dataShape)) {
            return new DataShape.Builder()
                            .createFrom(dataShape)
                            .specification(adaptUnifiedJsonBodySpecToSingleElement(dataShape.getSpecification()))
                            .putMetadata(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_ELEMENT)
                            .build();
        }

        DataShape collectionShape = dataShape.findVariantByMeta(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_COLLECTION).orElse(dataShape);
        String specification = collectionShape.getSpecification();
        if (StringUtils.hasText(specification)) {
            if (collectionShape.getKind() == DataShapeKinds.JSON_SCHEMA) {
                JsonSchema schema = Json.reader().forType(JsonSchema.class).readValue(specification);

                if (schema.isArraySchema()) {
                    ArraySchema.Items items = schema.asArraySchema().getItems();
                    if (items.isSingleItems()) {
                        JsonSchema itemSchema = items.asSingleItems().getSchema();
                        itemSchema.set$schema(schema.get$schema());
                        return new DataShape.Builder().createFrom(collectionShape)
                                .putMetadata(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_ELEMENT)
                                .specification(Json.writer().writeValueAsString(itemSchema))
                                .addAllVariants(StepMetadataHelper.extractVariants(dataShape, collectionShape, DataShapeMetaData.VARIANT_COLLECTION))
                                .build();
                    }
                } else {
                    return new DataShape.Builder().createFrom(collectionShape)
                            .putMetadata(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_ELEMENT)
                            .build();
                }
            } else if (collectionShape.getKind() == DataShapeKinds.JSON_INSTANCE) {
                if (JsonUtils.isJsonArray(specification)) {
                    List<Object> items = Json.reader().forType(List.class).readValue(specification);
                    if (!items.isEmpty()) {
                        return new DataShape.Builder().createFrom(collectionShape)
                                .putMetadata(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_ELEMENT)
                                .specification(Json.writer().writeValueAsString(items.get(0)))
                                .addAllVariants(StepMetadataHelper.extractVariants(dataShape, collectionShape, DataShapeMetaData.VARIANT_COLLECTION))
                                .build();
                    }
                } else {
                    return new DataShape.Builder().createFrom(collectionShape)
                            .putMetadata(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_ELEMENT)
                            .build();
                }
            }
        }

        return dataShape;
    }

    DataShape adaptOutputShape(DataShape dataShape) throws IOException {
        Optional<DataShape> collectionShape = dataShape.findVariantByMeta(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_COLLECTION);

        if (collectionShape.isPresent()) {
            if (dataShape.equals(collectionShape.get())) {
                return collectionShape.get();
            } else {
                return new DataShape.Builder().createFrom(collectionShape.get())
                        .addAllVariants(StepMetadataHelper.extractVariants(dataShape, collectionShape.get(), DataShapeMetaData.VARIANT_COLLECTION))
                        .build();
            }
        }

        if (StepMetadataHelper.isUnifiedJsonSchemaShape(dataShape)) {
            return new DataShape.Builder()
                    .createFrom(dataShape)
                    .specification(adaptUnifiedJsonBodySpecToCollection(dataShape.getSpecification()))
                    .putMetadata(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_COLLECTION)
                    .build();
        }

        DataShape singleElementShape = dataShape.findVariantByMeta(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_ELEMENT).orElse(dataShape);
        String specification = singleElementShape.getSpecification();
        if (StringUtils.hasText(specification)) {
            if (singleElementShape.getKind() == DataShapeKinds.JSON_SCHEMA) {
                JsonSchema schema = Json.reader().forType(JsonSchema.class).readValue(specification);

                if (schema.isArraySchema()) {
                    return new DataShape.Builder().createFrom(singleElementShape)
                            .putMetadata(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_COLLECTION)
                            .build();
                }

                ArraySchema collectionSchema = new ArraySchema();
                collectionSchema.set$schema(Optional.ofNullable(schema.get$schema()).orElse(JSON_SCHEMA_ORG_SCHEMA));
                collectionSchema.setItemsSchema(schema);
                schema.set$schema(null);

                return new DataShape.Builder().createFrom(singleElementShape)
                        .putMetadata(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_COLLECTION)
                        .specification(Json.writer().writeValueAsString(collectionSchema))
                        .addAllVariants(StepMetadataHelper.extractVariants(dataShape, singleElementShape, DataShapeMetaData.VARIANT_ELEMENT))
                        .build();
            } else if (singleElementShape.getKind() == DataShapeKinds.JSON_INSTANCE) {
                if (JsonUtils.isJsonArray(specification)) {
                    return new DataShape.Builder().createFrom(singleElementShape)
                            .putMetadata(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_COLLECTION)
                            .build();
                } else {
                    return new DataShape.Builder().createFrom(singleElementShape)
                            .putMetadata(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_COLLECTION)
                            .specification("[" + specification + "]")
                            .addAllVariants(StepMetadataHelper.extractVariants(dataShape, singleElementShape, DataShapeMetaData.VARIANT_ELEMENT))
                            .build();
                }
            }
        }

        return dataShape;
    }

    /**
     * Converts unified Json body specification. Unified Json schema specifications hold
     * the actual body specification in a property. This method converts this body specification
     * from array to single element if necessary.
     *
     * @param specification
     * @return
     * @throws IOException
     */
    private String adaptUnifiedJsonBodySpecToSingleElement(String specification) throws IOException {
        JsonSchema schema = Json.reader().forType(JsonSchema.class).readValue(specification);
        if (schema.isObjectSchema()) {
            JsonSchema bodySchema = schema.asObjectSchema().getProperties().get("body");
            if (bodySchema != null && bodySchema.isArraySchema()) {
                ArraySchema.Items items = bodySchema.asArraySchema().getItems();
                if (items.isSingleItems()) {
                    schema.asObjectSchema().getProperties().put("body", items.asSingleItems().getSchema());
                    return Json.writer().writeValueAsString(schema);
                }
            }
        }

        return specification;
    }

    /**
     * Converts unified Json body specification. Unified Json schema specifications hold
     * the actual body specification in a property. This method converts this body specification
     * from single element to collection if necessary.
     *
     * @param specification
     * @return
     * @throws IOException
     */
    private String adaptUnifiedJsonBodySpecToCollection(String specification) throws IOException {
        JsonSchema schema = Json.reader().forType(JsonSchema.class).readValue(specification);
        if (schema.isObjectSchema()) {
            JsonSchema bodySchema = schema.asObjectSchema().getProperties().get("body");
            if (bodySchema != null && bodySchema.isObjectSchema()) {
                ArraySchema arraySchema = new ArraySchema();
                arraySchema.set$schema(JSON_SCHEMA_ORG_SCHEMA);
                arraySchema.setItemsSchema(bodySchema);
                schema.asObjectSchema().getProperties().put("body", arraySchema);
                return Json.writer().writeValueAsString(schema);
            }
        }

        return specification;
    }
}
