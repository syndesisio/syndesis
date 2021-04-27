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

import static java.util.Optional.ofNullable;

import io.apicurio.datamodels.core.models.Extension;
import io.apicurio.datamodels.core.models.common.Info;
import io.apicurio.datamodels.openapi.models.OasDocument;
import io.apicurio.datamodels.openapi.models.OasOperation;
import io.apicurio.datamodels.openapi.models.OasPathItem;
import io.apicurio.datamodels.openapi.models.OasPaths;
import io.apicurio.datamodels.openapi.v2.models.Oas20Operation;
import io.apicurio.datamodels.openapi.v3.models.Oas30Operation;
import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.action.ActionsSummary;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.api.APISummary;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.connection.ConnectorSettings;
import io.syndesis.common.model.connection.ConnectorTemplate;
import io.syndesis.server.api.generator.APIValidationContext;
import io.syndesis.server.api.generator.ConnectorGenerator;
import io.syndesis.server.api.generator.openapi.util.OasModelHelper;
import io.syndesis.server.api.generator.openapi.util.OpenApiModelParser;
import io.syndesis.server.api.generator.openapi.util.OperationDescription;
import io.syndesis.server.api.generator.openapi.util.SpecificationOptimizer;
import io.syndesis.server.api.generator.openapi.v2.Oas20DescriptorGenerator;
import io.syndesis.server.api.generator.openapi.v2.Oas20ParameterGenerator;
import io.syndesis.server.api.generator.openapi.v2.Oas20PropertyGenerators;
import io.syndesis.server.api.generator.openapi.v3.Oas30DescriptorGenerator;
import io.syndesis.server.api.generator.openapi.v3.Oas30ParameterGenerator;
import io.syndesis.server.api.generator.openapi.v3.Oas30PropertyGenerators;
import io.syndesis.server.api.generator.util.ActionComparator;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class OpenApiConnectorGenerator extends ConnectorGenerator {

    private final Supplier<String> operationIdGenerator;

    private static final Oas20ParameterGenerator OAS20_PARAMETER_GENERATOR = new Oas20ParameterGenerator();
    private static final Oas30ParameterGenerator OAS30_PARAMETER_GENERATOR = new Oas30ParameterGenerator();

    private final Oas20PropertyGenerators oas20PropertyGenerators = new Oas20PropertyGenerators();
    private final Oas30PropertyGenerators oas30PropertyGenerators = new Oas30PropertyGenerators();

    private final Oas20DescriptorGenerator oas20DescriptorGenerator = new Oas20DescriptorGenerator();
    private final Oas30DescriptorGenerator oas30DescriptorGenerator = new Oas30DescriptorGenerator();

    public OpenApiConnectorGenerator(final Connector baseConnector, final Supplier<String> operationIdGenerator) {
        super(baseConnector);

        this.operationIdGenerator = operationIdGenerator;
    }

    public OpenApiConnectorGenerator(final Connector baseConnector) {
        this(baseConnector, OpenApiConnectorGenerator::randomUUID);
    }

    @Override
    public final Connector generate(final ConnectorTemplate connectorTemplate, final ConnectorSettings connectorSettings) {
        final Connector connector = basicConnector(connectorTemplate, connectorSettings);

        return configureConnector(connectorTemplate, connector, connectorSettings);
    }

    @Override
    public final APISummary info(final ConnectorTemplate connectorTemplate, final ConnectorSettings connectorSettings) {
        final OpenApiModelInfo modelInfo = parseSpecification(connectorSettings, APIValidationContext.CONSUMED_API);

        final OasDocument model = modelInfo.getModel();
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
            tagCounts = OasModelHelper.getPathItems(paths)
                .stream()
                .flatMap(p -> OasModelHelper.getOperationMap(p).values().stream())
                .peek(o -> total.incrementAndGet())
                .flatMap(o -> OasModelHelper.sanitizeTags(o.tags).distinct())
                .collect(
                    Collectors.groupingBy(
                        Function.identity(),
                        Collectors.reducing(0, (e) -> 1, Integer::sum)));
        }

        final ActionsSummary actionsSummary = new ActionsSummary.Builder()
            .totalActions(total.intValue())
            .actionCountByTags(tagCounts)
            .build();

        return APISummary.Builder.createFrom(connector)
            .actionsSummary(actionsSummary)
            .errors(modelInfo.getErrors())
            .warnings(modelInfo.getWarnings())
            .putAllConfiguredProperties(connectorSettings.getConfiguredProperties())
            .putConfiguredProperty("specification", modelInfo.getResolvedSpecification())
            .build();
    }

    protected ConnectorDescriptor createDescriptor(String connectorId, OpenApiModelInfo info, OasOperation operation) {
        switch (info.getApiVersion()) {
            case V2:
                return oas20DescriptorGenerator.createDescriptor(info.getResolvedJsonGraph(), info.getV2Model(), (Oas20Operation) operation)
                    .connectorId(connectorId)
                    .build();
            case V3:
                return oas30DescriptorGenerator.createDescriptor(info.getResolvedJsonGraph(), info.getV3Model(), (Oas30Operation) operation)
                    .connectorId(connectorId)
                    .build();
            default:
                throw new IllegalStateException(String.format("Unable to build connector descriptor for OpenAPI document type '%s'", info.getModel().getClass()));
        }
    }

    private Connector basicConnector(final ConnectorTemplate connectorTemplate, final ConnectorSettings connectorSettings) {
        final OpenApiModelInfo info = parseSpecification(connectorSettings, APIValidationContext.NONE);

        // could be either JSON of the Swagger specification or a URL to one
        final String specification = requiredSpecification(connectorSettings);

        if (specification.startsWith("http")) {
            Extension urlExtension = new Extension();
            urlExtension.name = OasModelHelper.URL_EXTENSION;
            urlExtension.value = URI.create(specification);
            info.getModel().addExtension(OasModelHelper.URL_EXTENSION, urlExtension);
        }

        final Connector baseConnector = baseConnectorFrom(connectorTemplate, connectorSettings);

        final Connector.Builder builder = new Connector.Builder().createFrom(baseConnector);

        final Map<String, String> alreadyConfiguredProperties = builder.build().getConfiguredProperties();

        connectorTemplate.getConnectorProperties().forEach((propertyName, template) -> {
            Optional<ConfigurationProperty> maybeProperty;
            switch (info.getApiVersion()) {
                case V2:
                    maybeProperty = oas20PropertyGenerators.createProperty(propertyName, info, template, connectorSettings);
                    break;
                case V3:
                    maybeProperty = oas30PropertyGenerators.createProperty(propertyName, info, template, connectorSettings);
                    break;
                default:
                    maybeProperty = Optional.empty();
                    break;
            }

            maybeProperty.ifPresent(property -> {
                builder.putProperty(propertyName, property);

                if (!alreadyConfiguredProperties.containsKey(propertyName)) {
                    final String defaultValue = Objects.toString(property.getDefaultValue(), null);
                    if (defaultValue != null) {
                        builder.putConfiguredProperty(propertyName, defaultValue);
                    }
                }
            });
        });

        return builder.build();
    }

    final Connector configureConnector(final ConnectorTemplate connectorTemplate, final Connector connector,
                                       final ConnectorSettings connectorSettings) {

        final Connector.Builder builder = new Connector.Builder().createFrom(connector);

        final OpenApiModelInfo info = parseSpecification(connectorSettings, APIValidationContext.NONE);
        final OasDocument openApiDoc = info.getModel();
        addGlobalParameters(builder, info);

        final OasPaths paths = ofNullable(openApiDoc.paths)
                                   .orElse(openApiDoc.createPaths());
        final String connectorId = connector.getId().orElseThrow(() -> new IllegalArgumentException("Missing connector identifier"));
        final List<ConnectorAction> actions = new ArrayList<>();
        final Map<String, Integer> operationIdCounts = new HashMap<>();
        for (final OasPathItem path : OasModelHelper.getPathItems(paths)) {
            final Map<String, OasOperation> operationMap = OasModelHelper.getOperationMap(path);

            for (final Map.Entry<String, OasOperation> entry : operationMap.entrySet()) {
                final OasOperation operation = entry.getValue();
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

                final ConnectorDescriptor descriptor = createDescriptor(connectorId, info, operation);

                final OperationDescription description = OasModelHelper.operationDescriptionOf(openApiDoc, operation, (m, p) -> "Send " + m + " request to " + p);

                final ConnectorAction action = new ConnectorAction.Builder()
                    .id(createActionId(connectorId, operation))
                    .name(description.name)
                    .description(description.description)
                    .pattern(Action.Pattern.To)
                    .descriptor(descriptor).tags(OasModelHelper.sanitizeTags(operation.tags).distinct().collect(Collectors.toList()))
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
        final OasDocument openApiDoc = parseSpecification(connectorSettings, APIValidationContext.NONE).getModel();

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

        final OasDocument openApiDoc = modelInfo.getModel();

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

    private static void addGlobalParameters(final Connector.Builder builder, final OpenApiModelInfo info) {
        switch (info.getApiVersion()) {
            case V2:
                OAS20_PARAMETER_GENERATOR.addGlobalParameters(builder, info.getV2Model());
                break;
            case V3:
                OAS30_PARAMETER_GENERATOR.addGlobalParameters(builder, info.getV3Model());
                break;
            default:
                throw new IllegalStateException(String.format("Unable to build connector for OpenAPI document type '%s'", info.getModel().getClass()));
        }
    }

    private static String createActionId(final String connectorId, final OasOperation operation) {
        return connectorId + ":" + operation.operationId;
    }

    static OpenApiModelInfo parseSpecification(final ConnectorSettings connectorSettings, final APIValidationContext validationContext) {
        final String specification = requiredSpecification(connectorSettings);
        return OpenApiModelParser.parse(specification, validationContext);
    }

    private static String requiredSpecification(final ConnectorSettings connectorSettings) {
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
