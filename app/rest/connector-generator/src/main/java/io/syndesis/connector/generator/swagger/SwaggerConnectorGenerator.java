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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

import io.swagger.models.HttpMethod;
import io.swagger.models.Info;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.RefParameter;
import io.swagger.models.parameters.SerializableParameter;
import io.swagger.parser.SwaggerParser;
import io.syndesis.connector.generator.ActionsSummary;
import io.syndesis.connector.generator.ConnectorGenerator;
import io.syndesis.connector.generator.ConnectorSummary;
import io.syndesis.connector.generator.util.ActionComparator;
import io.syndesis.model.DataShape;
import io.syndesis.model.action.Action;
import io.syndesis.model.action.ActionDescriptor;
import io.syndesis.model.action.ConnectorAction;
import io.syndesis.model.action.ConnectorDescriptor;
import io.syndesis.model.connection.ConfigurationProperty;
import io.syndesis.model.connection.ConfigurationProperty.PropertyValue;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.connection.ConnectorSettings;
import io.syndesis.model.connection.ConnectorTemplate;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.syndesis.connector.generator.swagger.DataShapeHelper.createShapeFromModel;
import static io.syndesis.connector.generator.swagger.DataShapeHelper.createShapeFromResponse;

import static org.apache.commons.lang3.StringUtils.trimToNull;

@SuppressWarnings("PMD.ExcessiveImports")
public class SwaggerConnectorGenerator extends ConnectorGenerator {

    private static final DataShape DATA_SHAPE_NONE = new DataShape.Builder().kind("none").build();

    private static final Logger LOG = LoggerFactory.getLogger(SwaggerConnectorGenerator.class);

    @Override
    public Connector generate(final ConnectorTemplate connectorTemplate, final ConnectorSettings connectorSettings) {
        final Connector connector = basicConnector(connectorTemplate, connectorSettings);

        return configureConnector(connectorTemplate, connector, connectorSettings);
    }

    @Override
    public ConnectorSummary info(final ConnectorTemplate connectorTemplate, final ConnectorSettings connectorSettings) {
        final Connector connector = basicConnector(connectorTemplate, connectorSettings);

        final Swagger swagger = parseSpecification(connectorSettings);
        final Map<String, Path> paths = swagger.getPaths();

        int total = 0;
        final Map<String, AtomicInteger> tagCounts = new HashMap<>();
        for (final Entry<String, Path> pathEntry : paths.entrySet()) {
            final Path path = pathEntry.getValue();

            final Map<HttpMethod, Operation> operationMap = path.getOperationMap();

            for (final Entry<HttpMethod, Operation> entry : operationMap.entrySet()) {
                final Operation operation = entry.getValue();
                total++;
                operation.getTags().forEach(tag -> tagCounts.computeIfAbsent(tag, x -> new AtomicInteger(0)).incrementAndGet());
            }
        }

        final ActionsSummary actionsSummary = new ActionsSummary.Builder()//
            .totalActions(total)//
            .actionCountByTags(tagCounts.entrySet().stream().collect(Collectors.toMap(Entry::getKey, e -> e.getValue().intValue())))
            .build();
        return new ConnectorSummary.Builder().createFrom(connector).actionsSummary(actionsSummary).build();
    }

    /* default */ Connector basicConnector(final ConnectorTemplate connectorTemplate, final ConnectorSettings connectorSettings) {
        final Swagger swagger = parseSpecification(connectorSettings);

        requiredSpecification(connectorSettings);

        final Connector baseConnector = baseConnectorFrom(connectorTemplate, connectorSettings);

        final Connector.Builder builder = new Connector.Builder().createFrom(baseConnector);

        final Map<String, String> alreadyConfiguredProperties = ((Connector) builder.build()).getConfiguredProperties();

        connectorTemplate.getConnectorProperties().forEach((propertyName, template) -> {
            if (alreadyConfiguredProperties.containsKey(propertyName)) {
                return;
            }

            final Optional<ConfigurationProperty> maybeProperty = PropertyGenerators.createProperty(propertyName, swagger, template);

            maybeProperty.ifPresent(property -> {
                builder.putProperty(propertyName, property);

                final String defaultValue = property.getDefaultValue();
                if (defaultValue != null) {
                    builder.putConfiguredProperty(propertyName, defaultValue);
                }
            });
        });

        return builder.build();
    }

    @Override
    protected String determineConnectorDescription(final ConnectorTemplate connectorTemplate, final ConnectorSettings connectorSettings) {
        final Swagger swagger = parseSpecification(connectorSettings);

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
    protected String determineConnectorName(final ConnectorTemplate connectorTemplate, final ConnectorSettings connectorSettings) {
        final Swagger swagger = parseSpecification(connectorSettings);

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

    /* default */ static Connector configureConnector(final ConnectorTemplate connectorTemplate, final Connector connector,
        final ConnectorSettings connectorSettings) {

        final Connector.Builder builder = new Connector.Builder().createFrom(connector);

        final Swagger swagger = parseSpecification(connectorSettings);
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

                final String specification = requiredSpecification(connectorSettings);
                final ConnectorDescriptor descriptor = createDescriptor(specification, operation).camelConnectorGAV(connectorGav)//
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

        if (idx != 0) {
            // we changed the Swagger specification by adding missing
            // operationIds
            builder.putConfiguredProperty("specification", SwaggerHelper.serialize(swagger));
        }

        return builder.build();
    }

    /* default */ static String createActionId(final String connectorId, final String connectorGav, final Operation operation) {
        return connectorGav + ":" + connectorId + ":" + operation.getOperationId();
    }

    /* default */ static ConnectorDescriptor.Builder createDescriptor(final String specification, final Operation operation) {
        final ConnectorDescriptor.Builder actionDescriptor = new ConnectorDescriptor.Builder();

        final Optional<BodyParameter> maybeRequestBody = operation.getParameters().stream()
            .filter(p -> p instanceof BodyParameter && ((BodyParameter) p).getSchema() != null).map(BodyParameter.class::cast).findFirst();
        final DataShape inputDataShape = maybeRequestBody.map(requestBody -> createShapeFromModel(specification, requestBody.getSchema()))
            .orElse(DATA_SHAPE_NONE);
        actionDescriptor.inputDataShape(inputDataShape);

        final Optional<Response> maybeResponse = operation.getResponses().values().stream().filter(r -> r.getSchema() != null).findFirst();
        final DataShape outputDataShape = maybeResponse.map(response -> createShapeFromResponse(specification, response))
            .orElse(DATA_SHAPE_NONE);
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

    /* default */ static Swagger parseSpecification(final ConnectorSettings connectorSettings) {
        final String specification = requiredSpecification(connectorSettings);

        final SwaggerParser parser = new SwaggerParser();
        final Swagger swagger = ofNullable(parser.read(specification)).orElseGet(() -> parser.parse(specification));
        if (swagger == null) {
            LOG.debug("Unable to read Swagger specification\n{}\n", specification);
            throw new IllegalArgumentException(
                "Unable to read Swagger specification from: " + ofNullable(specification).map(s -> StringUtils.abbreviate(s, 100)));
        }

        return swagger;
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
