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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

import io.swagger.models.HttpMethod;
import io.swagger.models.Info;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.AbstractSerializableParameter;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.RefParameter;
import io.syndesis.connector.generator.ConnectorGenerator;
import io.syndesis.connector.generator.util.ActionComparator;
import io.syndesis.core.SyndesisServerException;
import io.syndesis.model.DataShape;
import io.syndesis.model.action.Action;
import io.syndesis.model.action.ActionsSummary;
import io.syndesis.model.action.ConnectorAction;
import io.syndesis.model.action.ConnectorDescriptor;
import io.syndesis.model.connection.ConfigurationProperty;
import io.syndesis.model.connection.ConfigurationProperty.PropertyValue;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.connection.ConnectorSettings;
import io.syndesis.model.connection.ConnectorSummary;
import io.syndesis.model.connection.ConnectorTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.lang3.StringUtils.trimToNull;

abstract class BaseSwaggerConnectorGenerator extends ConnectorGenerator {

    /* default */ static final DataShape DATA_SHAPE_NONE = new DataShape.Builder().kind("none").build();

    /* default */ static final ConfigurationProperty OPERATION_ID_PROPERTY = new ConfigurationProperty.Builder()//
        .kind("property")//
        .displayName("Operation ID")//
        .group("producer")//
        .label("producer")//
        .required(true)//
        .type("hidden")//
        .javaType("java.lang.String")//
        .deprecated(false)//
        .secret(false)//
        .componentProperty(false)//
        .description("ID of operation to invoke")//
        .build();

    /* default */ static final String URL_EXTENSION = "x-syndesis-swagger-url";

    private static final Logger LOG = LoggerFactory.getLogger(BaseSwaggerConnectorGenerator.class);

    @Override
    public final Connector generate(final ConnectorTemplate connectorTemplate, final ConnectorSettings connectorSettings) {
        final Connector connector = basicConnector(connectorTemplate, connectorSettings);

        return configureConnector(connectorTemplate, connector, connectorSettings);
    }

    @Override
    public final ConnectorSummary info(final ConnectorTemplate connectorTemplate, final ConnectorSettings connectorSettings) {
        final SwaggerModelInfo swaggerInfo = parseSpecification(connectorSettings, true);
        try {
            // No matter if the validation fails, try to process the swagger
            final Connector connector = basicConnector(connectorTemplate, connectorSettings);
            final Map<String, Path> paths = swaggerInfo.getModel().getPaths();

            AtomicInteger total = new AtomicInteger(0);

            final Map<String, Integer> tagCounts = paths.entrySet().stream()
                .flatMap(p -> p.getValue().getOperations().stream())
                .peek(o -> total.incrementAndGet())
                .flatMap(o -> o.getTags().stream().distinct())
                .collect(
                    Collectors.groupingBy(
                        Function.identity(),
                        Collectors.reducing(0, (e) -> 1, Integer::sum)
                    )
                );

            final ActionsSummary actionsSummary = new ActionsSummary.Builder()//
                .totalActions(total.intValue())//
                .actionCountByTags(tagCounts)
                .build();
            return new ConnectorSummary.Builder().createFrom(connector).actionsSummary(actionsSummary).errors(swaggerInfo.getErrors())
                .warnings(swaggerInfo.getWarnings()).build();
        } catch (final Exception ex) {
            if (!swaggerInfo.getErrors().isEmpty()) {
                // Just log and return the validation errors if any
                LOG.error("An error occurred while trying to create a swagger connector", ex);
                return new ConnectorSummary.Builder().errors(swaggerInfo.getErrors()).warnings(swaggerInfo.getWarnings()).build();
            }

            throw SyndesisServerException.launderThrowable("An error occurred while trying to create a swagger connector", ex);
        }
    }

    abstract ConnectorDescriptor.Builder createDescriptor(String specification, Operation operation);

    protected final Connector basicConnector(final ConnectorTemplate connectorTemplate, final ConnectorSettings connectorSettings) {
        final Swagger swagger = parseSpecification(connectorSettings, false).getModel();

        // could be either JSON of the Swagger specification or a URL to one
        final String specification = requiredSpecification(connectorSettings);

        if (specification.startsWith("http")) {
            swagger.vendorExtension(URL_EXTENSION, URI.create(specification));
        }

        final Connector baseConnector = baseConnectorFrom(connectorTemplate, connectorSettings);

        final Connector.Builder builder = new Connector.Builder().createFrom(baseConnector);

        final Map<String, String> alreadyConfiguredProperties = builder.build().getConfiguredProperties();

        connectorTemplate.getConnectorProperties().forEach((propertyName, template) -> {
            final Optional<ConfigurationProperty> maybeProperty = PropertyGenerators.createProperty(propertyName, swagger, template);

            maybeProperty.ifPresent(property -> {
                builder.putProperty(propertyName, property);

                if (!alreadyConfiguredProperties.containsKey(propertyName)) {
                    final String defaultValue = property.getDefaultValue();
                    if (defaultValue != null) {
                        builder.putConfiguredProperty(propertyName, defaultValue);
                    }
                }
            });
        });

        return builder.build();
    }

    protected final Connector configureConnector(final ConnectorTemplate connectorTemplate, final Connector connector,
        final ConnectorSettings connectorSettings) {

        final Connector.Builder builder = new Connector.Builder().createFrom(connector);

        final SwaggerModelInfo info = parseSpecification(connectorSettings, false);
        final Swagger swagger = info.getModel();
        addGlobalParameters(builder, swagger);

        final Map<String, Path> paths = swagger.getPaths();

        final String connectorId = connector.getId().get();
        final String connectorGav = connectorTemplate.getCamelConnectorGAV();
        final String connectorScheme = connectorTemplate.getCamelConnectorPrefix();

        final List<ConnectorAction> actions = new ArrayList<>();
        int idx = 0;
        for (final Entry<String, Path> pathEntry : paths.entrySet()) {
            final Path path = pathEntry.getValue();

            final Map<HttpMethod, Operation> operationMap = path.getOperationMap();

            for (final Entry<HttpMethod, Operation> entry : operationMap.entrySet()) {
                final Operation operation = entry.getValue();
                if (operation.getOperationId() == null) {
                    operation.operationId("operation-" + idx++);
                }

                final ConnectorDescriptor descriptor = createDescriptor(info.getResolvedSpecification(), operation)//
                    .camelConnectorGAV(connectorGav)//
                    .camelConnectorPrefix(connectorScheme)//
                    .connectorId(connectorId)//
                    .build();

                final String summary = trimToNull(operation.getSummary());
                final String specifiedDescription = trimToNull(operation.getDescription());

                final String name;
                final String description;
                if (summary == null && specifiedDescription == null) {
                    name = entry.getKey() + " " + pathEntry.getKey();
                    description = null;
                } else if (specifiedDescription == null) {
                    name = entry.getKey() + " " + pathEntry.getKey();
                    description = summary;
                } else {
                    name = summary;
                    description = specifiedDescription;
                }

                final ConnectorAction action = new ConnectorAction.Builder()//
                    .id(createActionId(connectorId, connectorGav, operation))//
                    .name(name)//
                    .description(description)//
                    .pattern(Action.Pattern.To)//
                    .descriptor(descriptor).tags(ofNullable(operation.getTags()).orElse(Collections.emptyList()))//
                    .build();

                actions.add(action);
            }
        }

        actions.sort(ActionComparator.INSTANCE);
        builder.addAllActions(actions);

        builder.putConfiguredProperty("specification", SwaggerHelper.serialize(swagger));

        return builder.build();
    }

    @Override
    protected final String determineConnectorDescription(final ConnectorTemplate connectorTemplate,
        final ConnectorSettings connectorSettings) {
        final Swagger swagger = parseSpecification(connectorSettings, false).getModel();

        final Info info = swagger.getInfo();
        if (info == null) {
            return super.determineConnectorDescription(connectorTemplate, connectorSettings);
        }

        final String description = info.getDescription();
        if (description == null) {
            return super.determineConnectorDescription(connectorTemplate, connectorSettings);
        }

        return description;
    }

    @Override
    protected final String determineConnectorName(final ConnectorTemplate connectorTemplate, final ConnectorSettings connectorSettings) {
        final Swagger swagger = parseSpecification(connectorSettings, false).getModel();

        final Info info = swagger.getInfo();
        if (info == null) {
            return super.determineConnectorName(connectorTemplate, connectorSettings);
        }

        final String title = info.getTitle();
        if (title == null) {
            return super.determineConnectorName(connectorTemplate, connectorSettings);
        }

        return title;
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

    /* default */ static String createActionId(final String connectorId, final String connectorGav, final Operation operation) {
        return connectorGav + ":" + connectorId + ":" + operation.getOperationId();
    }

    /* default */ static List<PropertyValue> createEnums(final List<String> enums) {
        return enums.stream().map(BaseSwaggerConnectorGenerator::createPropertyValue).collect(Collectors.toList());
    }

    /* default */ static Optional<ConfigurationProperty> createPropertyFromParameter(final Parameter parameter) {
        if (parameter instanceof RefParameter || parameter instanceof BodyParameter) {
            // Reference parameters are not supported, body parameters are
            // handled in createShape* methods

            return Optional.empty();
        }

        if (!(parameter instanceof AbstractSerializableParameter<?>)) {
            throw new IllegalArgumentException("Unexpected parameter type received, neither ref, body nor serializable: " + parameter);
        }

        final String name = trimToNull(parameter.getName());
        final String description = trimToNull(parameter.getDescription());
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

        final AbstractSerializableParameter<?> serializableParameter = (AbstractSerializableParameter<?>) parameter;

        final Object defaultValue = serializableParameter.getDefaultValue();
        if (defaultValue != null) {
            propertyBuilder.defaultValue(String.valueOf(defaultValue));
        }

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

    /* default */ static SwaggerModelInfo parseSpecification(final ConnectorSettings connectorSettings, final boolean validate) {
        final String specification = requiredSpecification(connectorSettings);
        return SwaggerHelper.parse(specification, validate);
    }

    /* default */ static String requiredSpecification(final ConnectorSettings connectorSettings) {
        final Map<String, String> configuredProperties = connectorSettings.getConfiguredProperties();

        final String specification = configuredProperties.get("specification");

        if (specification == null) {
            throw new IllegalArgumentException(
                "Configured properties of the given Connector template does not include `specification` property");
        }
        return specification;
    }
}
