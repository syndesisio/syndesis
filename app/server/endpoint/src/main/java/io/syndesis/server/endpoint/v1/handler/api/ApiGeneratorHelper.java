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
package io.syndesis.server.endpoint.v1.handler.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.ResourceIdentifier;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.openapi.OpenApi;
import io.syndesis.common.util.SyndesisServerException;
import io.syndesis.integration.api.IntegrationResourceManager;
import io.syndesis.server.api.generator.APIGenerator;
import io.syndesis.server.api.generator.APIIntegration;
import io.syndesis.server.api.generator.ProvidedApiTemplate;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.endpoint.v1.handler.api.ApiHandler.APIFormData;

import okio.BufferedSource;
import okio.Okio;

public final class ApiGeneratorHelper {

    private static final String API_PROVIDER_CONNECTION_ID = "api-provider";
    private static final String API_PROVIDER_END_ACTION_ID = "io.syndesis:api-provider-end";
    private static final String API_PROVIDER_START_ACTION_ID = "io.syndesis:api-provider-start";

    public static APIIntegration generateIntegrationFrom(final APIFormData apiFormData, final DataManager dataManager, final APIGenerator apiGenerator) {
        Connection apiProviderConnection = dataManager.fetch(Connection.class, API_PROVIDER_CONNECTION_ID);
        if (apiProviderConnection == null) {
            throw new IllegalStateException("Cannot find api-provider connection with id: " + API_PROVIDER_CONNECTION_ID);
        }

        final String spec = getSpec(apiFormData);
        if (!apiProviderConnection.getConnector().isPresent()) {
            final Connector apiProviderConnector = dataManager.fetch(Connector.class, apiProviderConnection.getConnectorId());
            apiProviderConnection = new Connection.Builder().createFrom(apiProviderConnection).connector(apiProviderConnector).build();
        }

        final ProvidedApiTemplate template = new ProvidedApiTemplate(apiProviderConnection, API_PROVIDER_START_ACTION_ID, API_PROVIDER_END_ACTION_ID);

        return apiGenerator.generateIntegration(spec, template);
    }

    public static Optional<OpenApi> specificationFrom(IntegrationResourceManager resourceManager, final Integration integration) {
        final Optional<ResourceIdentifier> specification = integration.getResources().stream().filter(r -> r.getKind() == Kind.OpenApi)
            .max(ResourceIdentifier.LATEST_VERSION);

        return specification
            .flatMap(s -> s.getId()
                .flatMap(resourceManager::loadOpenApiDefinition));
    }

    public static APIIntegration generateIntegrationUpdateFrom(final Integration existing, final APIFormData apiFormData, final DataManager dataManager,
        final APIGenerator apiGenerator) {
        final APIIntegration newApiIntegration = generateIntegrationFrom(apiFormData, dataManager, apiGenerator);

        final Integration newIntegration = newApiIntegration.getIntegration();

        final Integration updatedIntegration = newIntegration.builder()
            .id(existing.getId())
            .build();

        return new APIIntegration(updatedIntegration, newApiIntegration.getSpec());
    }

    public static Integration updateFlowsAndStartAndEndDataShapes(final Integration existing, final Integration given) {
        // will contain updated flows
        final List<Flow> updatedFlows = new ArrayList<>(given.getFlows().size());

        for (final Flow givenFlow : given.getFlows()) {
            final Optional<String> maybeOperationId = givenFlow.getMetadata(OpenApi.OPERATION_ID);
            if (!maybeOperationId.isPresent()) {
                continue;
            }

            final String operationId = maybeOperationId.get();

            final Optional<Flow> maybeExistingFlow = existing.findFlowBy(operationIdEquals(operationId));
            if (!maybeExistingFlow.isPresent()) {
                // this is a flow generated from a new operation or it
                // has it's operation id changed, either way we only
                // need to add it, since we don't know what flow we need
                // to update
                updatedFlows.add(givenFlow);
                continue;
            }

            final List<Step> givenSteps = givenFlow.getSteps();
            if (givenSteps.size() != 2) {
                throw new IllegalArgumentException("Expecting to get exactly two steps per flow");
            }

            // this is a freshly minted flow from the specification
            // there should be only two steps (start and end) in the
            // flow
            final Step givenStart = givenSteps.get(0);
            final Optional<DataShape> givenStartDataShape = givenStart.outputDataShape();

            // generated flow has only a start and an end, start is at 0
            // and the end is at 1
            final Step givenEnd = givenSteps.get(1);
            final Optional<DataShape> givenEndDataShape = givenEnd.inputDataShape();

            final Flow existingFlow = maybeExistingFlow.get();
            final List<Step> existingSteps = existingFlow.getSteps();

            // readability
            final int start = 0;
            final int end = existingSteps.size() - 1;

            // now we update the data shapes of the start and end steps
            final Step existingStart = existingSteps.get(start);
            final Step updatedStart = existingStart.updateOutputDataShape(givenStartDataShape);

            final Step existingEnd = existingSteps.get(end);
            final Step updatedEnd = existingEnd.updateInputDataShape(givenEndDataShape);

            final List<Step> updatedSteps = new ArrayList<>(existingSteps);
            updatedSteps.set(start, updatedStart);
            updatedSteps.set(end, updatedEnd);

            final Flow updatedFlow = existingFlow.builder()
                .name(givenFlow.getName())
                .description(givenFlow.getDescription())
                .steps(updatedSteps)
                .build();
            updatedFlows.add(updatedFlow);
        }

        return existing.builder().flows(updatedFlows)
            // we replace all resources counting that the only resource
            // present is the OpenAPI specification
            .resources(given.getResources()).build();
    }

    private static Predicate<Flow> operationIdEquals(String operationId) {
        return f -> f.getMetadata(OpenApi.OPERATION_ID).map(id -> id.equals(operationId)).orElse(false);
    }

    static String getSpec(final APIFormData apiFormData) {
        try (BufferedSource source = Okio.buffer(Okio.source(apiFormData.getSpecification()))) {
            return source.readUtf8();
        } catch (final IOException e) {
            throw SyndesisServerException.launderThrowable("Failed to read specification", e);
        }
    }

}
