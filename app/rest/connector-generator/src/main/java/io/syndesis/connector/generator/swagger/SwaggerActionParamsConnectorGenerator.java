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
package io.syndesis.connector.generator.swagger;

import java.util.List;
import java.util.Optional;

import io.swagger.models.Operation;
import io.swagger.models.Response;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.syndesis.model.DataShape;
import io.syndesis.model.action.ActionDescriptor;
import io.syndesis.model.action.ActionDescriptor.ActionDescriptorStep;
import io.syndesis.model.action.ConnectorDescriptor;
import io.syndesis.model.connection.ConfigurationProperty;

import static io.syndesis.connector.generator.swagger.DataShapeHelper.createShapeFromModel;
import static io.syndesis.connector.generator.swagger.DataShapeHelper.createShapeFromResponse;

public final class SwaggerActionParamsConnectorGenerator extends BaseSwaggerConnectorGenerator {

    @Override
    ConnectorDescriptor.Builder createDescriptor(final String specification, final Operation operation) {
        final ConnectorDescriptor.Builder actionDescriptor = new ConnectorDescriptor.Builder();

        final List<Parameter> parameters = operation.getParameters();
        final Optional<BodyParameter> maybeRequestBody = parameters.stream()
            .filter(p -> p instanceof BodyParameter && ((BodyParameter) p).getSchema() != null).map(BodyParameter.class::cast).findFirst();
        final DataShape inputDataShape = maybeRequestBody.map(requestBody -> createShapeFromModel(specification, requestBody.getSchema()))
            .orElse(DATA_SHAPE_NONE);
        actionDescriptor.inputDataShape(inputDataShape);

        final ActionDescriptor.ActionDescriptorStep.Builder stepBuilder = new ActionDescriptor.ActionDescriptorStep.Builder()
            .name("Query parameters").description("Specify query parameters");

        for (final Parameter parameter : parameters) {
            final Optional<ConfigurationProperty> property = createPropertyFromParameter(parameter);

            if (property.isPresent()) {
                stepBuilder.putProperty(parameter.getName(), property.get());
            }
        }

        final Optional<Response> maybeResponse = operation.getResponses().values().stream().filter(r -> r.getSchema() != null).findFirst();
        final DataShape outputDataShape = maybeResponse.map(response -> createShapeFromResponse(specification, response))
            .orElse(DATA_SHAPE_NONE);
        actionDescriptor.outputDataShape(outputDataShape);

        actionDescriptor.putConfiguredProperty("operationId", operation.getOperationId());

        final ActionDescriptorStep step = stepBuilder.build();
        if (!step.getProperties().isEmpty()) {
            actionDescriptor.addPropertyDefinitionStep(step);
        }

        return actionDescriptor;
    }

}
