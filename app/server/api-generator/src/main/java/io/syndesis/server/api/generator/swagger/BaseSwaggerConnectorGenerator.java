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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import io.swagger.models.HttpMethod;
import io.swagger.models.Info;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.AbstractSerializableParameter;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.RefParameter;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.action.ActionsSummary;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.api.APISummary;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.ConfigurationProperty.PropertyValue;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.connection.ConnectorSettings;
import io.syndesis.common.model.connection.ConnectorTemplate;
import io.syndesis.server.api.generator.APIValidationContext;
import io.syndesis.server.api.generator.ConnectorGenerator;
import io.syndesis.server.api.generator.swagger.util.JsonSchemaHelper;
import io.syndesis.server.api.generator.swagger.util.OperationDescription;
import io.syndesis.server.api.generator.swagger.util.SwaggerHelper;
import io.syndesis.server.api.generator.util.ActionComparator;

import com.fasterxml.jackson.databind.node.ObjectNode;

import static java.util.Optional.ofNullable;

import static org.apache.commons.lang3.StringUtils.trimToNull;

abstract class BaseSwaggerConnectorGenerator extends ConnectorGenerator {

    static final DataShape DATA_SHAPE_NONE = new DataShape.Builder().kind(DataShapeKinds.NONE).build();

    static final ConfigurationProperty OPERATION_ID_PROPERTY = new ConfigurationProperty.Builder()
        .kind("property")
        .displayName("Operation ID")
        .group("producer")
        .label("producer")
        .required(true)
        .type("hidden")
        .javaType("java.lang.String")
        .deprecated(false)
        .secret(false)
        .componentProperty(false)
        .description("ID of operation to invoke")
        .build();

    static final String URL_EXTENSION = "x-syndesis-swagger-url";

    final Supplier<String> operationIdGenerator;

    public BaseSwaggerConnectorGenerator(final Connector baseConnector, final Supplier<String> operationIdGenerator) {
        super(baseConnector);

        this.operationIdGenerator = operationIdGenerator;
    }

    BaseSwaggerConnectorGenerator(final Connector baseConnector) {
        this(baseConnector, BaseSwaggerConnectorGenerator::randomUUID);
    }

    @Override
    public final Connector generate(final ConnectorTemplate connectorTemplate, final ConnectorSettings connectorSettings) {
        final Connector connector = basicConnector(connectorTemplate, connectorSettings);

        return configureConnector(connectorTemplate, connector, connectorSettings);
    }

    @Override
    public final APISummary info(final ConnectorTemplate connectorTemplate, final ConnectorSettings connectorSettings) {
        final SwaggerModelInfo swaggerInfo = parseSpecification(connectorSettings, APIValidationContext.CONSUMED_API);

        final Swagger model = swaggerInfo.getModel();
        if (model == null) {
            final APISummary.Builder summaryBuilder = new APISummary.Builder()
                .errors(swaggerInfo.getErrors())
                .warnings(swaggerInfo.getWarnings());

            if (swaggerInfo.getResolvedSpecification() != null) {
                summaryBuilder.putConfiguredProperty("specification", swaggerInfo.getResolvedSpecification());
            }

            return summaryBuilder.build();
        }

        // No matter if the validation fails, try to process the swagger
        final Connector connector = basicConnector(connectorTemplate, connectorSettings);

        final Map<String, Path> paths = model.getPaths();

        final AtomicInteger total = new AtomicInteger(0);

        final Map<String, Integer> tagCounts;
        if (paths == null) {
            tagCounts = Collections.emptyMap();
        } else {
            tagCounts = paths.entrySet().stream()
                .flatMap(p -> p.getValue().getOperations().stream())
                .peek(o -> total.incrementAndGet())
                .flatMap(o -> SwaggerHelper.sanitizeTags(o.getTags()).distinct())
                .collect(
                    Collectors.groupingBy(
                        Function.identity(),
                        Collectors.reducing(0, (e) -> 1, Integer::sum)));
        }

        final ActionsSummary actionsSummary = new ActionsSummary.Builder()
            .totalActions(total.intValue())
            .actionCountByTags(tagCounts)
            .build();

        return new APISummary.Builder()
            .createFrom(connector)
            .actionsSummary(actionsSummary)
            .errors(swaggerInfo.getErrors())
            .warnings(swaggerInfo.getWarnings())
            .putAllConfiguredProperties(connectorSettings.getConfiguredProperties())
            .putConfiguredProperty("specification", swaggerInfo.getResolvedSpecification())
            .build();
    }

    abstract ConnectorDescriptor.Builder createDescriptor(ObjectNode json, Swagger swagger, Operation operation);

    protected final Connector basicConnector(final ConnectorTemplate connectorTemplate, final ConnectorSettings connectorSettings) {
        final Swagger swagger = parseSpecification(connectorSettings, APIValidationContext.NONE).getModel();

        // could be either JSON of the Swagger specification or a URL to one
        final String specification = requiredSpecification(connectorSettings);

        if (specification.startsWith("http")) {
            swagger.vendorExtension(URL_EXTENSION, URI.create(specification));
        }

        final Connector baseConnector = baseConnectorFrom(connectorTemplate, connectorSettings);

        final Connector.Builder builder = new Connector.Builder().createFrom(baseConnector);

        final Map<String, String> alreadyConfiguredProperties = builder.build().getConfiguredProperties();

        connectorTemplate.getConnectorProperties().forEach((propertyName, template) -> {
            final Optional<ConfigurationProperty> maybeProperty = PropertyGenerators.createProperty(propertyName, swagger, template, connectorSettings);

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

        final SwaggerModelInfo info = parseSpecification(connectorSettings, APIValidationContext.NONE);
        final Swagger swagger = info.getModel();
        addGlobalParameters(builder, swagger);

        final Map<String, Path> paths = swagger.getPaths();

        final String connectorId = connector.getId().get();

        final List<ConnectorAction> actions = new ArrayList<>();
        final Map<String, Integer> operationIdCounts = new HashMap<>();
        for (final Entry<String, Path> pathEntry : paths.entrySet()) {
            final Path path = pathEntry.getValue();

            final Map<HttpMethod, Operation> operationMap = path.getOperationMap();

            for (final Entry<HttpMethod, Operation> entry : operationMap.entrySet()) {
                final Operation operation = entry.getValue();
                final String operationId = operation.getOperationId();
                if (operationId == null) {
                    operation.operationId(operationIdGenerator.get());
                } else {
                    // we tolerate that some operations might have the same
                    // operationId, if that's the case we generate a unique
                    // operationId by appending the count of the duplicates,
                    // e.g. operation ids for non unique operation id "foo" will
                    // be "foo", "foo1", "foo2", ... this will be reflected in
                    // the Swagger specification stored in `specification`
                    // property
                    final Integer count = operationIdCounts.compute(operationId,
                        (id, currentCount) -> ofNullable(currentCount).map(c -> ++c).orElse(0));

                    if (count > 0) {
                        operation.operationId(operationId + count);
                    }
                }

                final ConnectorDescriptor descriptor = createDescriptor(info.getResolvedJsonGraph(), swagger, operation)
                    .connectorId(connectorId)
                    .build();

                final OperationDescription description = SwaggerHelper.operationDescriptionOf(swagger, operation, (m, p) -> "Send " + m + " request to " + p);

                final ConnectorAction action = new ConnectorAction.Builder()
                    .id(createActionId(connectorId, operation))
                    .name(description.name)
                    .description(description.description)
                    .pattern(Action.Pattern.To)
                    .descriptor(descriptor).tags(SwaggerHelper.sanitizeTags(operation.getTags()).distinct()::iterator)
                    .build();

                actions.add(action);
            }
        }

        actions.sort(ActionComparator.INSTANCE);
        builder.addAllActions(actions);

        builder.putConfiguredProperty("specification", SwaggerHelper.minimalSwaggerUsedByComponent(swagger));

        return builder.build();
    }

    @Override
    protected final String determineConnectorDescription(final ConnectorTemplate connectorTemplate,
        final ConnectorSettings connectorSettings) {
        final Swagger swagger = parseSpecification(connectorSettings, APIValidationContext.NONE).getModel();

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
        final SwaggerModelInfo modelInfo = parseSpecification(connectorSettings, APIValidationContext.NONE);
        if (!modelInfo.getErrors().isEmpty()) {
            throw new IllegalArgumentException("Given OpenAPI specification contains errors: " + modelInfo);
        }

        final Swagger swagger = modelInfo.getModel();

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

    static void addGlobalParameters(final Connector.Builder builder, final Swagger swagger) {
        final Map<String, Parameter> globalParameters = swagger.getParameters();
        if (globalParameters != null) {
            globalParameters.forEach((name, parameter) -> {
                createPropertyFromParameter(parameter).ifPresent(property -> {
                    builder.putProperty(name, property);
                });
            });
        }
    }

    static String createActionId(final String connectorId, final Operation operation) {
        return connectorId + ":" + operation.getOperationId();
    }

    static List<PropertyValue> createEnums(final List<String> enums) {
        return enums.stream().map(BaseSwaggerConnectorGenerator::createPropertyValue).collect(Collectors.toList());
    }

    static Optional<ConfigurationProperty> createPropertyFromParameter(final Parameter parameter) {
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

        final ConfigurationProperty.Builder propertyBuilder = new ConfigurationProperty.Builder()
            .kind("property")
            .displayName(name)
            .description(description)
            .group("producer")
            .required(required)
            .componentProperty(false)
            .deprecated(false)
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

    static PropertyValue createPropertyValue(final String value) {
        return new PropertyValue.Builder().label(value).value(value).build();
    }

    static SwaggerModelInfo parseSpecification(final ConnectorSettings connectorSettings, final APIValidationContext validationContext) {
        final String specification = requiredSpecification(connectorSettings);
        return SwaggerHelper.parse(specification, validationContext);
    }

    static String requiredSpecification(final ConnectorSettings connectorSettings) {
        final Map<String, String> configuredProperties = connectorSettings.getConfiguredProperties();

        final String specification = configuredProperties.get("specification");

        if (specification == null) {
            throw new IllegalArgumentException(
                "Configured properties of the given connector template does not include `specification` property");
        }
        return specification;
    }

    private static String randomUUID() {
        return UUID.randomUUID().toString();
    }

}
