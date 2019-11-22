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

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apicurio.datamodels.openapi.v2.models.Oas20Document;
import io.apicurio.datamodels.openapi.v2.models.Oas20Operation;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.server.api.generator.openapi.DataShapeGenerator;

public final class OpenApiUnifiedShapeConnectorGenerator extends BaseOpenApiConnectorGenerator {

    private final DataShapeGenerator dataShapeGenerator = new UnifiedDataShapeGenerator();

    public OpenApiUnifiedShapeConnectorGenerator(final Connector restSwaggerConnector) {
        super(restSwaggerConnector);
    }

    OpenApiUnifiedShapeConnectorGenerator(final Connector restSwaggerConnector, final Supplier<String> operationIdGenerator) {
        super(restSwaggerConnector, operationIdGenerator);
    }

    @Override
    ConnectorDescriptor.Builder createDescriptor(final ObjectNode json, final Oas20Document openApiDoc, final Oas20Operation operation) {
        final ConnectorDescriptor.Builder actionDescriptor = new ConnectorDescriptor.Builder();

        final DataShape inputDataShape = dataShapeGenerator.createShapeFromRequest(json, openApiDoc, operation);
        actionDescriptor.inputDataShape(inputDataShape);

        final DataShape outputDataShape = dataShapeGenerator.createShapeFromResponse(json, openApiDoc, operation);
        actionDescriptor.outputDataShape(outputDataShape);

        actionDescriptor.putConfiguredProperty("operationId", operation.operationId);

        return actionDescriptor;
    }

}
