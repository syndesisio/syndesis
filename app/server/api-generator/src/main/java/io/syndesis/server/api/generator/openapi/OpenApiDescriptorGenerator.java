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

package io.syndesis.server.api.generator.openapi;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apicurio.datamodels.openapi.models.OasDocument;
import io.apicurio.datamodels.openapi.models.OasOperation;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.action.ConnectorDescriptor;

/**
 * Generator creates a connector descriptor and adds properties and data shapes generated from the
 * OpenAPI operation specification. This is the abstract base class for specific specific 2.x and 3.x OpenAPI implementations.
 */
public class OpenApiDescriptorGenerator<T extends OasDocument, O extends OasOperation> {

    private final DataShapeGenerator<T, O> dataShapeGenerator;

    protected OpenApiDescriptorGenerator(DataShapeGenerator<T, O> dataShapeGenerator) {
        this.dataShapeGenerator = dataShapeGenerator;
    }

    public final ConnectorDescriptor.Builder createDescriptor(final ObjectNode json, final T openApiDoc, final O operation) {
        final ConnectorDescriptor.Builder actionDescriptor = new ConnectorDescriptor.Builder();

        final DataShape inputDataShape = dataShapeGenerator.createShapeFromRequest(json, openApiDoc, operation);
        actionDescriptor.inputDataShape(inputDataShape);

        final DataShape outputDataShape = dataShapeGenerator.createShapeFromResponse(json, openApiDoc, operation);
        actionDescriptor.outputDataShape(outputDataShape);

        actionDescriptor.putConfiguredProperty("operationId", operation.operationId);

        return actionDescriptor;
    }
}
