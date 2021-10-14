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

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.core.models.common.Info;
import io.apicurio.datamodels.openapi.models.OasDocument;
import io.apicurio.datamodels.openapi.models.OasPathItem;
import io.apicurio.datamodels.openapi.models.OasPaths;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.ResourceIdentifier;
import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.action.ActionsSummary;
import io.syndesis.common.model.api.APISummary;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.FlowMetadata;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.openapi.OpenApi;
import io.syndesis.common.util.KeyGenerator;
import io.syndesis.server.api.generator.APIGenerator;
import io.syndesis.server.api.generator.APIIntegration;
import io.syndesis.server.api.generator.APIValidationContext;
import io.syndesis.server.api.generator.ProvidedApiTemplate;
import io.syndesis.server.api.generator.openapi.util.OasModelHelper;
import io.syndesis.server.api.generator.openapi.util.OpenApiModelParser;
import io.syndesis.server.api.generator.openapi.v2.Oas20FlowGenerator;
import io.syndesis.server.api.generator.openapi.v3.Oas30FlowGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic API generator generates integrations from Open API specifications. Supports both Open API v2 and V3 by delegating
 * to specific integration generator implementations.
 */
public class OpenApiGenerator implements APIGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(OpenApiGenerator.class);

    private static final String HTTP_RESPONSE_CODE_PROPERTY = "httpResponseCode";

    private final Oas20FlowGenerator oas20FlowGenerator = new Oas20FlowGenerator();
    private final Oas30FlowGenerator oas30FlowGenerator = new Oas30FlowGenerator();

    @Override
    @SuppressWarnings({"PMD.ExcessiveMethodLength"})
    public APIIntegration generateIntegration(final String specification, final ProvidedApiTemplate template) {

        final OpenApiModelInfo info = OpenApiModelParser.parse(specification, APIValidationContext.NONE);
        final OasDocument openApiDoc = info.getModel();

        final String name = Optional.ofNullable(openApiDoc.info)
            .flatMap(i -> Optional.ofNullable(i.title))
            .orElse("Untitled");

        final Integration.Builder integration = new Integration.Builder()
            .addTag("api-provider")
            .createdAt(System.currentTimeMillis())
            .name(name);

        switch (info.getApiVersion()) {
            case V2:
                oas20FlowGenerator.generateFlows(info.getV2Model(), integration, info, template);
                break;
            case V3:
                oas30FlowGenerator.generateFlows(info.getV3Model(), integration, info, template);
                break;
            default:
                throw new IllegalStateException(String.format("Unable to retrieve integration flow generator for OpenAPI document type '%s'", openApiDoc.getClass()));
        }

        // TODO: evaluate what can be shrunk (e.g. SpecificationOptimizer#minimizeForComponent)
        final byte[] updatedSpecification = Library.writeDocumentToJSONString(openApiDoc).getBytes(StandardCharsets.UTF_8);

        final String apiId = KeyGenerator.createKey();
        final OpenApi api = new OpenApi.Builder()
            .id(apiId)
            .name(name)
            .document(updatedSpecification)
            .putMetadata("Content-Type", getContentType(specification))
            .build();

        integration.addResource(new ResourceIdentifier.Builder()
            .id(apiId)
            .kind(Kind.OpenApi)
            .build());

        return new APIIntegration(integration.build(), api);
    }

    @Override
    public APISummary info(final String specification, final APIValidationContext validation) {
        final OpenApiModelInfo modelInfo = OpenApiModelParser.parse(specification, validation);

        final OasDocument model = modelInfo.getModel();
        if (model == null) {
            return new APISummary.Builder().errors(modelInfo.getErrors()).warnings(modelInfo.getWarnings()).build();
        }

        final OasPaths paths = model.paths;
        final ActionsSummary actionsSummary = determineSummaryFrom(OasModelHelper.getPathItems(paths));

        final Info info = model.info;
        final String title = Optional.ofNullable(info).map(i -> i.title).orElse("unspecified");
        final String description = Optional.ofNullable(info).map(i -> i.description).orElse("unspecified");

        return new APISummary.Builder()//
            .name(title)//
            .description(description)//
            .addActionsSummary(actionsSummary)//
            .errors(modelInfo.getErrors())//
            .warnings(modelInfo.getWarnings())//
            // needed if the user wants to change it
            .putConfiguredProperty("specification", modelInfo.getResolvedSpecification())
            .build();
    }

    @Override
    public Integration updateFlowExcerpts(final Integration integration) {
        // Update excerpt for api provider endpoints only
        if (integration == null || !integration.getTags().contains("api-provider")) {
            return integration;
        }

        return new Integration.Builder()
            .createFrom(integration)
            .flows(integration.getFlows().stream()
                .map(OpenApiGenerator::flowWithExcerpts)
                .collect(Collectors.toList()))
            .build();
    }

    private static String decodeHttpReturnCode(final List<Step> steps, final String code) {
        if (code == null || steps.isEmpty()) {
            return code;
        }
        final Step lastStep = steps.get(steps.size() - 1);
        final Optional<Action> lastAction = lastStep.getAction();
        if (!lastAction.isPresent()) {
            return code;
        }
        final Optional<String> httpCodeDescription = lastAction
            .flatMap(a -> Optional.ofNullable(a.getProperties().get(HTTP_RESPONSE_CODE_PROPERTY)))
            .flatMap(prop -> prop.getEnum().stream()
                .filter(e -> code.equals(e.getValue()))
                .map(ConfigurationProperty.PropertyValue::getLabel)
                .findFirst());
        return httpCodeDescription.orElse(code);
    }

    /**
     * Determine content type for given specification (Json or YAML). When specification is a Http URL
     * this method requests the specification content from that endpoint.
     * @param specification the specification or Http URL pointing to it.
     * @return content type for given specification. Usually Json or YAML content type.
     */
    private static String getContentType(String specification) {
        String resolvedSpecification = specification;
        try {
            if (specification.toLowerCase(Locale.US).startsWith("http")) {
                resolvedSpecification = OpenApiModelParser.resolve(new URL(specification));
            }
        } catch (MalformedURLException e) {
            LOG.warn("Unable to resolve OpenAPI document\n{}\n", specification, e);
        }

        if (OpenApiModelParser.isJsonSpec(resolvedSpecification)) {
            // means it's JSON (kinda)
            return "application/vnd.oai.openapi+json";
        } else {
            // YAML
            return "application/vnd.oai.openapi";
        }
    }

    private static Flow flowWithExcerpts(final Flow flow) {
        final List<Step> steps = flow.getSteps();
        if (steps == null || steps.isEmpty()) {
            return flow;
        }

        final Step last = steps.get(steps.size() - 1);
        if (last.getConfiguredProperties().containsKey(HTTP_RESPONSE_CODE_PROPERTY)) {
            final String responseCode = last.getConfiguredProperties().get(HTTP_RESPONSE_CODE_PROPERTY);
            final String responseDesc = decodeHttpReturnCode(steps, responseCode);
            return new Flow.Builder()
                .createFrom(flow)
                .putMetadata(FlowMetadata.EXCERPT, responseDesc)
                .build();
        } else if (flow.getMetadata(FlowMetadata.EXCERPT).isPresent()) {
            final Map<String, String> newMetadata = new HashMap<>(flow.getMetadata());
            newMetadata.remove(FlowMetadata.EXCERPT.getKey());
            return new Flow.Builder()
                .createFrom(flow)
                .metadata(newMetadata)
                .build();
        }
        return flow;
    }

    private static ActionsSummary determineSummaryFrom(final List<OasPathItem> paths) {
        if (paths == null || paths.isEmpty()) {
            return new ActionsSummary.Builder().build();
        }

        final AtomicInteger total = new AtomicInteger(0);

        final Map<String, Integer> tagCounts = paths.stream()//
            .flatMap(p -> OasModelHelper.getOperationMap(p).values().stream())//
            .peek(o -> total.incrementAndGet())//
            .flatMap(o -> OasModelHelper.sanitizeTags(o.tags))//
            .collect(//
                Collectors.groupingBy(//
                    Function.identity(), //
                    Collectors.reducing(0, (e) -> 1, Integer::sum)//
                ));

        return new ActionsSummary.Builder()//
            .totalActions(total.intValue())//
            .actionCountByTags(tagCounts)//
            .build();
    }
}
