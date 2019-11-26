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
package io.syndesis.server.api.generator.openapi.v2;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apicurio.datamodels.openapi.v2.models.Oas20Document;
import io.apicurio.datamodels.openapi.v2.models.Oas20Operation;
import io.apicurio.datamodels.openapi.v2.models.Oas20Parameter;
import io.apicurio.datamodels.openapi.v2.models.Oas20ParameterDefinitions;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.server.api.generator.openapi.util.OasModelHelper;

import static org.apache.commons.lang3.StringUtils.trimToNull;

/**
 * @author Christoph Deppisch
 */
public final class Oas20ConnectorGeneratorSupport {

    private static final Oas20DataShapeGenerator DATA_SHAPE_GENERATOR = new UnifiedDataShapeGenerator();

    private Oas20ConnectorGeneratorSupport() {
        // utility class.
    }

    public static ConnectorDescriptor.Builder createDescriptor(final ObjectNode json,
                                                               final Oas20Document openApiDoc, final Oas20Operation operation) {
        final ConnectorDescriptor.Builder actionDescriptor = new ConnectorDescriptor.Builder();

        final DataShape inputDataShape = DATA_SHAPE_GENERATOR.createShapeFromRequest(json, openApiDoc, operation);
        actionDescriptor.inputDataShape(inputDataShape);

        final DataShape outputDataShape = DATA_SHAPE_GENERATOR.createShapeFromResponse(json, openApiDoc, operation);
        actionDescriptor.outputDataShape(outputDataShape);

        actionDescriptor.putConfiguredProperty("operationId", operation.operationId);

        return actionDescriptor;
    }

    public static void addGlobalParameters(Connector.Builder builder, Oas20Document openApiDoc) {
        final Oas20ParameterDefinitions globalParameters = openApiDoc.parameters;
        if (globalParameters == null) {
            return;
        }

        globalParameters.getItems().forEach(parameter -> {
            createPropertyFromParameter(parameter).ifPresent(property -> {
                builder.putProperty(parameter.getName(), property);
            });
        });
    }

    static Optional<ConfigurationProperty> createPropertyFromParameter(final Oas20Parameter parameter) {
        if (OasModelHelper.isReferenceType(parameter) || OasModelHelper.isBody(parameter)) {
            // Reference parameters are not supported, body parameters are
            // handled in createShape* methods

            return Optional.empty();
        }

        if (!OasModelHelper.isSerializable(parameter)) {
            throw new IllegalArgumentException("Unexpected parameter type received, neither ref, body nor serializable: " + parameter);
        }

        final String name = trimToNull(parameter.name);
        final String description = trimToNull(parameter.description);
        final boolean required = parameter.required;

        final ConfigurationProperty.Builder propertyBuilder = new ConfigurationProperty.Builder()
            .kind("property")
            .displayName(name)
            .description(description)
            .group("producer")
            .required(required)
            .componentProperty(false)
            .deprecated(false)
            .secret(false);

        final Object defaultValue = parameter.default_;
        if (defaultValue != null) {
            propertyBuilder.defaultValue(String.valueOf(defaultValue));
        }

        final String type = parameter.type;
        propertyBuilder.type(type).javaType(Oas20ModelHelper.javaTypeFor(parameter));

        final List<String> enums = parameter.enum_;
        if (enums != null) {
            propertyBuilder.addAllEnum(createEnums(enums));
        }

        return Optional.of(propertyBuilder.build());
    }

    private static List<ConfigurationProperty.PropertyValue> createEnums(final List<String> enums) {
        return enums.stream().map(Oas20ConnectorGeneratorSupport::createPropertyValue).collect(Collectors.toList());
    }

    private static ConfigurationProperty.PropertyValue createPropertyValue(final String value) {
        return new ConfigurationProperty.PropertyValue.Builder().label(value).value(value).build();
    }
}
