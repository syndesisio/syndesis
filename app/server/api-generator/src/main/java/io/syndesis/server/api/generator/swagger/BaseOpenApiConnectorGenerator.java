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

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apicurio.datamodels.core.models.Extension;
import io.apicurio.datamodels.core.models.common.Info;
import io.apicurio.datamodels.openapi.models.OasOperation;
import io.apicurio.datamodels.openapi.models.OasPaths;
import io.apicurio.datamodels.openapi.v2.models.Oas20Document;
import io.apicurio.datamodels.openapi.v2.models.Oas20Operation;
import io.apicurio.datamodels.openapi.v2.models.Oas20Parameter;
import io.apicurio.datamodels.openapi.v2.models.Oas20ParameterDefinitions;
import io.apicurio.datamodels.openapi.v2.models.Oas20PathItem;
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
import io.syndesis.server.api.generator.swagger.util.Oas20ModelHelper;
import io.syndesis.server.api.generator.swagger.util.Oas20ModelParser;
import io.syndesis.server.api.generator.swagger.util.SpecificationOptimizer;
import io.syndesis.server.api.generator.swagger.util.OperationDescription;
import io.syndesis.server.api.generator.util.ActionComparator;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.trimToNull;

abstract class BaseOpenApiConnectorGenerator extends ConnectorGenerator {

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

    public BaseOpenApiConnectorGenerator(final Connector baseConnector, final Supplier<String> operationIdGenerator) {
        super(baseConnector);

        this.operationIdGenerator = operationIdGenerator;
    }

    BaseOpenApiConnectorGenerator(final Connector baseConnector) {
        this(baseConnector, BaseOpenApiConnectorGenerator::randomUUID);
    }

    @Override
    public final Connector generate(final ConnectorTemplate connectorTemplate, final ConnectorSettings connectorSettings) {
        final Connector connector = basicConnector(connectorTemplate, connectorSettings);

        return configureConnector(connectorTemplate, connector, connectorSettings);
    }

    @Override
    public final APISummary info(final ConnectorTemplate connectorTemplate, final ConnectorSettings connectorSettings) {
        final OpenApiModelInfo modelInfo = parseSpecification(connectorSettings, APIValidationContext.CONSUMED_API);

        final Oas20Document model = modelInfo.getModel();
        if (model == null) {
            final APISummary.Builder summaryBuilder = new APISummary.Builder()
                .errors(modelInfo.getErrors())
                .warnings(modelInfo.getWarnings());

            if (modelInfo.getResolvedSpecification() != null) {
                summaryBuilder.putConfiguredProperty("specification", modelInfo.getResolvedSpecification());
            }

            return summaryBuilder.build();
        }

        // No matter if the validation fails, try to process the swagger
        final Connector connector = basicConnector(connectorTemplate, connectorSettings);

        final OasPaths paths = model.paths;
        final AtomicInteger total = new AtomicInteger(0);
        final Map<String, Integer> tagCounts;
        if (paths == null) {
            tagCounts = Collections.emptyMap();
        } else {
            tagCounts = Oas20ModelHelper.getPathItems(paths, Oas20PathItem.class)
                .stream()
                .flatMap(p -> Oas20ModelHelper.getOperationMap(p).values().stream())
                .peek(o -> total.incrementAndGet())
                .flatMap(o -> Oas20ModelHelper.sanitizeTags(o.tags).distinct())
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
            .errors(modelInfo.getErrors())
            .warnings(modelInfo.getWarnings())
            .putAllConfiguredProperties(connectorSettings.getConfiguredProperties())
            .putConfiguredProperty("specification", modelInfo.getResolvedSpecification())
            .build();
    }

    abstract ConnectorDescriptor.Builder createDescriptor(ObjectNode json, Oas20Document openApiDoc, Oas20Operation operation);

    protected final Connector basicConnector(final ConnectorTemplate connectorTemplate, final ConnectorSettings connectorSettings) {
        final Oas20Document openApiDoc = parseSpecification(connectorSettings, APIValidationContext.NONE).getModel();

        // could be either JSON of the Swagger specification or a URL to one
        final String specification = requiredSpecification(connectorSettings);

        if (specification.startsWith("http")) {
            Extension urlExtension = new Extension();
            urlExtension.name = URL_EXTENSION;
            urlExtension.value = URI.create(specification);
            openApiDoc.addExtension(URL_EXTENSION, urlExtension);
        }

        final Connector baseConnector = baseConnectorFrom(connectorTemplate, connectorSettings);

        final Connector.Builder builder = new Connector.Builder().createFrom(baseConnector);

        final Map<String, String> alreadyConfiguredProperties = builder.build().getConfiguredProperties();

        connectorTemplate.getConnectorProperties().forEach((propertyName, template) -> {
            final Optional<ConfigurationProperty> maybeProperty = PropertyGenerators.createProperty(propertyName, openApiDoc, template, connectorSettings);

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

        final OpenApiModelInfo info = parseSpecification(connectorSettings, APIValidationContext.NONE);
        final Oas20Document openApiDoc = info.getModel();
        addGlobalParameters(builder, openApiDoc);

        final OasPaths paths = ofNullable(openApiDoc.paths)
                                   .orElse(openApiDoc.createPaths());
        final String connectorId = connector.getId().orElseThrow(() -> new IllegalArgumentException("Missing connector identifier"));
        final List<ConnectorAction> actions = new ArrayList<>();
        final Map<String, Integer> operationIdCounts = new HashMap<>();
        for (final Oas20PathItem path : Oas20ModelHelper.getPathItems(paths, Oas20PathItem.class)) {
            final Map<String, Oas20Operation> operationMap = Oas20ModelHelper.getOperationMap(path);

            for (final Entry<String, Oas20Operation> entry : operationMap.entrySet()) {
                final Oas20Operation operation = entry.getValue();
                final String operationId = operation.operationId;
                if (operationId == null) {
                    operation.operationId = operationIdGenerator.get();
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
                        operation.operationId = operationId + count;
                    }
                }

                final ConnectorDescriptor descriptor = createDescriptor(info.getResolvedJsonGraph(), openApiDoc, operation)
                    .connectorId(connectorId)
                    .build();

                final OperationDescription description = Oas20ModelHelper.operationDescriptionOf(openApiDoc, operation, (m, p) -> "Send " + m + " request to " + p);

                final ConnectorAction action = new ConnectorAction.Builder()
                    .id(createActionId(connectorId, operation))
                    .name(description.name)
                    .description(description.description)
                    .pattern(Action.Pattern.To)
                    .descriptor(descriptor).tags(Oas20ModelHelper.sanitizeTags(operation.tags).distinct()::iterator)
                    .build();

                actions.add(action);
            }
        }

        actions.sort(ActionComparator.INSTANCE);
        builder.addAllActions(actions);

        builder.putConfiguredProperty("specification", SpecificationOptimizer.minimizeForComponent(openApiDoc));

        return builder.build();
    }

    @Override
    protected final String determineConnectorDescription(final ConnectorTemplate connectorTemplate,
        final ConnectorSettings connectorSettings) {
        final Oas20Document openApiDoc = parseSpecification(connectorSettings, APIValidationContext.NONE).getModel();

        final Info info = openApiDoc.info;
        if (info == null) {
            return super.determineConnectorDescription(connectorTemplate, connectorSettings);
        }

        final String description = info.description;
        if (description == null) {
            return super.determineConnectorDescription(connectorTemplate, connectorSettings);
        }

        return description;
    }

    @Override
    protected final String determineConnectorName(final ConnectorTemplate connectorTemplate, final ConnectorSettings connectorSettings) {
        final OpenApiModelInfo modelInfo = parseSpecification(connectorSettings, APIValidationContext.NONE);
        if (!modelInfo.getErrors().isEmpty()) {
            throw new IllegalArgumentException("Given OpenAPI specification contains errors: " + modelInfo);
        }

        final Oas20Document openApiDoc = modelInfo.getModel();

        final Info info = openApiDoc.info;
        if (info == null) {
            return super.determineConnectorName(connectorTemplate, connectorSettings);
        }

        final String title = info.title;
        if (title == null) {
            return super.determineConnectorName(connectorTemplate, connectorSettings);
        }

        return title;
    }

    static void addGlobalParameters(final Connector.Builder builder, final Oas20Document openApiDoc) {
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

    static String createActionId(final String connectorId, final OasOperation operation) {
        return connectorId + ":" + operation.operationId;
    }

    static List<PropertyValue> createEnums(final List<String> enums) {
        return enums.stream().map(BaseOpenApiConnectorGenerator::createPropertyValue).collect(Collectors.toList());
    }

    static Optional<ConfigurationProperty> createPropertyFromParameter(final Oas20Parameter parameter) {
        if (Oas20ModelHelper.isReferenceType(parameter) || Oas20ModelHelper.isBody(parameter)) {
            // Reference parameters are not supported, body parameters are
            // handled in createShape* methods

            return Optional.empty();
        }

        if (!Oas20ModelHelper.isSerializable(parameter)) {
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
        propertyBuilder.type(type).javaType(JsonSchemaHelper.javaTypeFor(parameter));

        final List<String> enums = parameter.enum_;
        if (enums != null) {
            propertyBuilder.addAllEnum(createEnums(enums));
        }

        return Optional.of(propertyBuilder.build());
    }

    static PropertyValue createPropertyValue(final String value) {
        return new PropertyValue.Builder().label(value).value(value).build();
    }

    static OpenApiModelInfo parseSpecification(final ConnectorSettings connectorSettings, final APIValidationContext validationContext) {
        final String specification = requiredSpecification(connectorSettings);
        return Oas20ModelParser.parse(specification, validationContext);
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
