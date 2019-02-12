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
            if (metadata.outputShape() != null) {
                DataShape dataShape = new DataShape.Builder()
                                                .createFrom(metadata.outputShape())
                                                .decompress()
                                                .build();

                Optional<DataShape> collectionShape = dataShape.findVariantByMeta("variant", "collection");

                if (collectionShape.isPresent()) {
                    return new DynamicActionMetadata.Builder()
                            .createFrom(metadata)
                            .outputShape(collectionShape.get())
                            .build();
                }

                DataShape singleElementShape = dataShape.findVariantByMeta("variant", "element").orElse(dataShape);

                if (singleElementShape.getKind().equals(DataShapeKinds.JSON_SCHEMA)) {
                    String specification = dataShape.getSpecification();
                    JsonSchema schema = Json.reader().forType(JsonSchema.class).readValue(specification);

                    ArraySchema collectionSchema = new ArraySchema();
                    collectionSchema.set$schema(Optional.ofNullable(schema.get$schema()).orElse(JSON_SCHEMA_ORG_SCHEMA));
                    collectionSchema.setItemsSchema(schema);
                    schema.set$schema(null);

                    return new DynamicActionMetadata.Builder()
                                .createFrom(metadata)
                                .outputShape(new DataShape.Builder().createFrom(singleElementShape)
                                                                    .specification(Json.writer().writeValueAsString(collectionSchema))
                                                                    .build())
                                .build();
                } else if (singleElementShape.getKind().equals(DataShapeKinds.JSON_INSTANCE)) {
                    String specification = dataShape.getSpecification();
                    return new DynamicActionMetadata.Builder()
                            .createFrom(metadata)
                            .outputShape(new DataShape.Builder().createFrom(singleElementShape)
                                                                .specification("[" + specification + "]")
                                                                .build())
                            .build();
                }
            }
        } catch (IOException e) {
            LOG.warn("Unable to read output data shape on dynamic meta data inspection", e);
        }

        return metadata;
    }
}
