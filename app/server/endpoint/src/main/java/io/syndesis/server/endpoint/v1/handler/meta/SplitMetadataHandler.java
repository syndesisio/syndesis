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
class SplitMetadataHandler implements StepMetadataHandler {

    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(SplitMetadataHandler.class);

    @Override
    public boolean canHandle(StepKind kind) {
        return StepKind.split.equals(kind);
    }

    @Override
    public DynamicActionMetadata createMetadata(Step step, List<Step> previousSteps, List<Step> subsequentSteps) {
        Optional<Step> previousStepWithDataShape = StepMetadataHelper.getLastWithOutputShape(previousSteps);

        DataShape inputShape = previousStepWithDataShape.flatMap(Step::inputDataShape)
                                                        .orElse(StepMetadataHelper.NO_SHAPE);

        DataShape outputShape = previousStepWithDataShape.flatMap(Step::outputDataShape)
                                                        .orElse(StepMetadataHelper.NO_SHAPE);

        return new DynamicActionMetadata.Builder()
                .inputShape(inputShape)
                .outputShape(outputShape)
                .build();
    }

    @Override
    public DynamicActionMetadata handle(DynamicActionMetadata metadata) {
        try {
            if (metadata.outputShape() != null) {
                DataShape dataShape = new DataShape.Builder()
                                            .createFrom(metadata.outputShape())
                                            .decompress()
                                            .build();

                if (StepMetadataHelper.isUnifiedJsonSchemaShape(dataShape)) {
                    dataShape = new DataShape.Builder()
                                        .createFrom(dataShape)
                                        .specification(extractUnifiedJsonBodySpec(dataShape.getSpecification()))
                                        .build();
                }

                Optional<DataShape> singleElementShape = dataShape.findVariantByMeta(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_ELEMENT);

                if (singleElementShape.isPresent()) {
                    if (dataShape.equals(singleElementShape.get())) {
                        return new DynamicActionMetadata.Builder()
                                        .createFrom(metadata)
                                        .outputShape(singleElementShape.get())
                                .build();
                    } else {
                        return new DynamicActionMetadata.Builder()
                                .createFrom(metadata)
                                .outputShape(new DataShape.Builder()
                                        .createFrom(singleElementShape.get())
                                        .addAllVariants(StepMetadataHelper.extractVariants(dataShape, singleElementShape.get(), DataShapeMetaData.VARIANT_ELEMENT))
                                        .build())
                                .build();
                    }
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
                                return new DynamicActionMetadata.Builder()
                                        .createFrom(metadata)
                                        .outputShape(new DataShape.Builder().createFrom(collectionShape)
                                                .putMetadata(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_ELEMENT)
                                                .specification(Json.writer().writeValueAsString(itemSchema))
                                                .addAllVariants(StepMetadataHelper.extractVariants(dataShape, collectionShape, DataShapeMetaData.VARIANT_COLLECTION))
                                                .build())
                                        .build();
                            }
                        } else {
                            return new DynamicActionMetadata.Builder()
                                    .createFrom(metadata)
                                    .outputShape(new DataShape.Builder().createFrom(collectionShape)
                                            .putMetadata(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_ELEMENT)
                                            .build())
                                    .build();
                        }
                    } else if (collectionShape.getKind() == DataShapeKinds.JSON_INSTANCE) {
                        if (JsonUtils.isJsonArray(specification)) {
                            List<Object> items = Json.reader().forType(List.class).readValue(specification);
                            if (!items.isEmpty()) {
                                return new DynamicActionMetadata.Builder()
                                        .createFrom(metadata)
                                        .outputShape(new DataShape.Builder().createFrom(collectionShape)
                                                .putMetadata(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_ELEMENT)
                                                .specification(Json.writer().writeValueAsString(items.get(0)))
                                                .addAllVariants(StepMetadataHelper.extractVariants(dataShape, collectionShape, DataShapeMetaData.VARIANT_COLLECTION))
                                                .build())
                                        .build();
                            }
                        } else {
                            return new DynamicActionMetadata.Builder()
                                    .createFrom(metadata)
                                    .outputShape(new DataShape.Builder().createFrom(collectionShape)
                                            .putMetadata(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_ELEMENT)
                                            .build())
                                    .build();
                        }
                    }
                }
            }
        } catch (IOException e) {
            LOG.warn("Unable to read output data shape on dynamic meta data inspection", e);
        }

        return DynamicActionMetadata.NOTHING;
    }

    /**
     * Extract unified Json body specification from data shape specification. Unified Json schema specifications hold
     * the actual body specification in a property. This method extracts this property as new body specification.
     *
     * @param specification
     * @return
     * @throws IOException
     */
    private String extractUnifiedJsonBodySpec(String specification) throws IOException {
        JsonSchema schema = Json.reader().forType(JsonSchema.class).readValue(specification);
        if (schema.isObjectSchema()) {
            JsonSchema bodySchema = schema.asObjectSchema().getProperties().get("body");
            if (bodySchema != null) {
                if (bodySchema.isArraySchema()) {
                    ArraySchema.Items items = bodySchema.asArraySchema().getItems();
                    if (items.isSingleItems()) {
                        JsonSchema itemSchema = items.asSingleItems().getSchema();
                        itemSchema.set$schema(schema.get$schema());
                        return Json.writer().writeValueAsString(itemSchema);
                    }
                } else {
                    return Json.writer().writeValueAsString(bodySchema);
                }
            }
        }

        return specification;
    }
}
