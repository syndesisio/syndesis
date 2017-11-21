/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.connector.generator.swagger;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.auth.OAuth2Definition;
import io.swagger.models.auth.SecuritySchemeDefinition;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.RefParameter;
import io.swagger.models.parameters.SerializableParameter;
import io.swagger.parser.SwaggerParser;
import io.syndesis.connector.generator.ConnectorGenerator;
import io.syndesis.core.Json;
import io.syndesis.credential.Credentials;
import io.syndesis.model.DataShape;
import io.syndesis.model.action.Action;
import io.syndesis.model.action.ActionDescriptor;
import io.syndesis.model.action.ConnectorAction;
import io.syndesis.model.action.ConnectorDescriptor;
import io.syndesis.model.connection.ConfigurationProperty;
import io.syndesis.model.connection.ConfigurationProperty.PropertyValue;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.connection.ConnectorTemplate;

import static io.syndesis.connector.generator.swagger.DataShapeHelper.createShapeFromModel;
import static io.syndesis.connector.generator.swagger.DataShapeHelper.createShapeFromResponse;

@SuppressWarnings("PMD.ExcessiveImports")
public class SwaggerConnectorGenerator implements ConnectorGenerator {

    private static final DataShape DATA_SHAPE_NONE = new DataShape.Builder().kind("none").build();

    /* default */ static class PropertyData {
        private final String defaultValue;

        private final String description;

        private final String displayName;

        private final String[] tags;

        /* default */ PropertyData(final String displayName, final String description, final String defaultValue,
            final String... tags) {
            this.displayName = displayName;
            this.description = description;
            this.defaultValue = defaultValue;
            this.tags = tags;
        }
    }

    @Override
    public Connector generate(final ConnectorTemplate connectorTemplate, final Connector template) {
        final Map<String, String> configuredProperties = template.getConfiguredProperties();

        final String specification = configuredProperties.get("specification");

        if (specification == null) {
            throw new IllegalStateException(
                "Configured properties of the given Connector template does not include `specification` property");
        }

        final Connector baseConnector = baseConnectorFrom(connectorTemplate, template);

        final Connector connector = new Connector.Builder()//
            .createFrom(baseConnector)//
            .putConfiguredProperty("specification", specification)//
            .build();

        return configureConnector(connectorTemplate, connector, specification);
    }

    /* default */ static void addGlobalParameters(final Connector.Builder builder, final Swagger swagger) {
        final Map<String, Parameter> globalParameters = swagger.getParameters();
        if (globalParameters != null) {
            globalParameters.forEach((name, parameter) -> {
                createPropertyFromParameter(parameter).ifPresent(property -> {
                    builder.putProperty(name, property);
                });
            });
        }
    }

    /* default */ static Connector configureConnector(final ConnectorTemplate connectorTemplate,
        final Connector connector, final String specification) {

        final Connector.Builder builder = new Connector.Builder().createFrom(connector);

        final Swagger swagger = new SwaggerParser().parse(specification);
        addGlobalParameters(builder, swagger);

        final Map<String, Path> paths = swagger.getPaths();

        final String connectorId = connector.getId().get();
        final String connectorGav = connectorTemplate.getCamelConnectorGAV();
        final String connectorScheme = connectorTemplate.getCamelConnectorPrefix();

        int idx = 0;
        for (final Entry<String, Path> pathEntry : paths.entrySet()) {
            final Path path = pathEntry.getValue();

            final Map<HttpMethod, Operation> operationMap = path.getOperationMap();

            for (final Entry<HttpMethod, Operation> entry : operationMap.entrySet()) {
                final Operation operation = entry.getValue();
                if (operation.getOperationId() == null) {
                    operation.operationId("operation-" + idx++);
                }

                final ConnectorDescriptor descriptor = createDescriptor(specification, operation)
                    .camelConnectorGAV(connectorGav)//
                    .camelConnectorPrefix(connectorScheme)//
                    .connectorId(connectorId)//
                    .build();

                final ConnectorAction action = new ConnectorAction.Builder()//
                    .id(createActionId(connectorId, connectorGav, operation))//
                    .name(Optional.ofNullable(operation.getSummary())
                        .orElseGet(() -> entry.getKey() + " " + pathEntry.getKey()))//
                    .description(Optional.ofNullable(operation.getDescription()).orElse(""))//
                    .pattern(Action.Pattern.To)//
                    .descriptor(descriptor)
                    .tags(Optional.ofNullable(operation.getTags()).orElse(Collections.emptyList()))//
                    .build();

                builder.addAction(action);
            }
        }

        if (idx != 0) {
            // we changed the Swagger specification by adding missing
            // operationIds
            builder.putConfiguredProperty("specification", serialize(swagger));
        }

        final Map<String, SecuritySchemeDefinition> securityDefinitions = swagger.getSecurityDefinitions();
        if (securityDefinitions == null) {
            return builder.build();
        }

        return withSecurityConfiguration(securityDefinitions, builder);
    }

    /* default */ static String createActionId(final String connectorId, final String connectorGav,
        final Operation operation) {
        return connectorGav + ":" + connectorId + ":" + operation.getOperationId();
    }

    /* default */ static ConnectorDescriptor.Builder createDescriptor(final String specification,
        final Operation operation) {
        final ConnectorDescriptor.Builder actionDescriptor = new ConnectorDescriptor.Builder();

        final Optional<BodyParameter> maybeRequestBody = operation.getParameters().stream()
            .filter(p -> p instanceof BodyParameter && ((BodyParameter) p).getSchema() != null)
            .map(BodyParameter.class::cast).findFirst();
        final DataShape inputDataShape = maybeRequestBody
            .map(requestBody -> createShapeFromModel(specification, requestBody.getSchema())).orElse(DATA_SHAPE_NONE);
        actionDescriptor.inputDataShape(inputDataShape);

        final Optional<Response> maybeResponse = operation.getResponses().values().stream()
            .filter(r -> r.getSchema() != null).findFirst();
        final DataShape outputDataShape = maybeResponse
            .map(response -> createShapeFromResponse(specification, response)).orElse(DATA_SHAPE_NONE);
        actionDescriptor.outputDataShape(outputDataShape);

        final ActionDescriptor.ActionDescriptorStep.Builder step = new ActionDescriptor.ActionDescriptorStep.Builder()
            .name("Query parameters").description("Specify query parameters");

        for (final Parameter parameter : operation.getParameters()) {
            final Optional<ConfigurationProperty> property = createPropertyFromParameter(parameter);

            if (property.isPresent()) {
                step.putProperty(parameter.getName(), property.get());
            }
        }

        step.putProperty("operationId",
            new ConfigurationProperty.Builder()//
                .kind("property")//
                .displayName("operationId")//
                .group("producer")//
                .required(true)//
                .type("hidden")//
                .javaType("java.lang.String")//
                .deprecated(false)//
                .secret(false)//
                .componentProperty(false)//
                .defaultValue(operation.getOperationId())//
                .build());

        actionDescriptor.addPropertyDefinitionStep(step.build());

        return actionDescriptor;
    }

    /* default */ static List<PropertyValue> createEnums(final List<String> enums) {
        return enums.stream().map(SwaggerConnectorGenerator::createPropertyValue).collect(Collectors.toList());
    }

    /* default */ static Optional<ConfigurationProperty> createPropertyFromParameter(final Parameter parameter) {
        if (parameter instanceof RefParameter || parameter instanceof BodyParameter) {
            // Reference parameters are not supported, body parameters are
            // handled in createShape* methods

            return Optional.empty();
        }

        if (!(parameter instanceof SerializableParameter)) {
            throw new IllegalStateException(
                "Unexpected parameter type received, neither ref, body nor serializable: " + parameter);
        }

        final String name = parameter.getName();
        final String description = parameter.getDescription();
        final boolean required = parameter.getRequired();

        final ConfigurationProperty.Builder propertyBuilder = new ConfigurationProperty.Builder()//
            .kind("property")//
            .displayName(name)//
            .description(description)//
            .group("producer")//
            .required(required)//
            .componentProperty(false)//
            .deprecated(false)//
            .secret(false);

        final SerializableParameter serializableParameter = (SerializableParameter) parameter;

        final String type = serializableParameter.getType();
        propertyBuilder.type(type).javaType(JsonSchemaHelper.javaTypeFor(serializableParameter));

        final List<String> enums = serializableParameter.getEnum();
        if (enums != null) {
            propertyBuilder.addAllEnum(createEnums(enums));
        }

        return Optional.of(propertyBuilder.build());
    }

    /* default */ static PropertyValue createPropertyValue(final String value) {
        return new PropertyValue.Builder().label(value).value(value).build();
    }

    /* default */ static ConfigurationProperty property(final PropertyData propertyData) {
        final ConfigurationProperty.Builder property = new ConfigurationProperty.Builder()//
            .kind("property")//
            .displayName(propertyData.displayName)//
            .group("security")//
            .label("common,security")//
            .required(true)//
            .type("string")//
            .javaType("java.lang.String")//
            .componentProperty(true)//
            .description(propertyData.description);

        if (propertyData.tags != null && propertyData.tags.length > 0) {
            property.addTag(propertyData.tags);
        }

        if (propertyData.defaultValue != null) {
            property.defaultValue(propertyData.defaultValue);
        }

        return property.build();
    }

    /* default */ static ConfigurationProperty securityProperty(final PropertyData propertyData) {
        final ConfigurationProperty property = property(propertyData);

        return new ConfigurationProperty.Builder().createFrom(property).secret(true).build();
    }

    /* default */ static String serialize(final Swagger swagger) {
        try {
            return Json.mapper().writeValueAsString(swagger);
        } catch (final JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize Swagger specification", e);
        }
    }

    /* default */ static Connector withSecurityConfiguration(
        final Map<String, SecuritySchemeDefinition> securityDefinitions, final Connector.Builder builder) {
        final Optional<OAuth2Definition> maybeOauth2Definition = securityDefinitions.values().stream()
            .filter(d -> "oauth2".equals(d.getType())).map(OAuth2Definition.class::cast).findFirst();

        if (maybeOauth2Definition.isPresent()) {
            final OAuth2Definition oauth2Definition = maybeOauth2Definition.get();

            final String tokenUrl = oauth2Definition.getTokenUrl();
            final String authorizationUrl = oauth2Definition.getAuthorizationUrl();

            builder
                .putProperty("authenticationType",
                    property(new PropertyData("Authentication type", "Authentication type", "oauth2",
                        Credentials.AUTHENTICATION_TYPE_TAG)))
                .putConfiguredProperty("authenticationType", "oauth2")
                .putProperty("clientId",
                    property(
                        new PropertyData("Client ID", "OAuth Application Client ID", null, Credentials.CLIENT_ID_TAG)))
                .putProperty("clientSecret",
                    securityProperty(new PropertyData("Client Secret", "OAuth Application Client Secret", null,
                        Credentials.CLIENT_SECRET_TAG)))
                .putProperty("accessTokenUrl",
                    property(new PropertyData("Access token URL", "URL for the OAuth access token retrieval", tokenUrl,
                        Credentials.ACCESS_TOKEN_URL_TAG)))
                .putConfiguredProperty("accessTokenUrl", tokenUrl)
                .putProperty("authorizationUrl",
                    property(new PropertyData("Authorization URL", "URL for the OAuth authorization", authorizationUrl,
                        Credentials.AUTHORIZATION_URL_TAG)))
                .putProperty("accessToken",
                    securityProperty(new PropertyData("Access token", "OAuth Access Token", null)));

            if (authorizationUrl != null) {
                builder.putConfiguredProperty("authorizationUrl", authorizationUrl);
            }
        }

        return builder.build();
    }

}
