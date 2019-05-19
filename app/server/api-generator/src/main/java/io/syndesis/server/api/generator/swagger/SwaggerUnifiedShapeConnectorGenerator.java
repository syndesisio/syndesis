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
package io.syndesis.server.api.generator.swagger;

import java.util.function.Supplier;

import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.Connector;

import com.fasterxml.jackson.databind.node.ObjectNode;

public final class SwaggerUnifiedShapeConnectorGenerator extends BaseSwaggerConnectorGenerator {

    private final DataShapeGenerator dataShapeGenerator = new UnifiedDataShapeGenerator();

    public SwaggerUnifiedShapeConnectorGenerator(final Connector restSwaggerConnector) {
        super(restSwaggerConnector);
    }

    SwaggerUnifiedShapeConnectorGenerator(final Connector restSwaggerConnector, final Supplier<String> operationIdGenerator) {
        super(restSwaggerConnector, operationIdGenerator);
    }

    @Override
    ConnectorDescriptor.Builder createDescriptor(final ObjectNode json, final Swagger swagger, final Operation operation) {
        final ConnectorDescriptor.Builder actionDescriptor = new ConnectorDescriptor.Builder();

        final DataShape inputDataShape = dataShapeGenerator.createShapeFromRequest(json, swagger, operation);
        actionDescriptor.inputDataShape(inputDataShape);

        final DataShape outputDataShape = dataShapeGenerator.createShapeFromResponse(json, swagger, operation);
        actionDescriptor.outputDataShape(outputDataShape);

        actionDescriptor.putConfiguredProperty("operationId", operation.getOperationId());

        return actionDescriptor;
    }

}
