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
package io.syndesis.server.endpoint.v1.handler.integration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.ApiOperation;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.openapi.OpenApi;
import io.syndesis.server.api.generator.APIGenerator;
import io.syndesis.server.api.generator.APIIntegration;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.endpoint.v1.handler.api.ApiGeneratorHelper;
import io.syndesis.server.endpoint.v1.handler.api.ApiHandler;

import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

public final class IntegrationSpecificationHandler {

    private final APIGenerator apiGenerator;

    private final DataManager dataManager;

    private final IntegrationHandler integrationHandler;

    public IntegrationSpecificationHandler(final IntegrationHandler integrationHandler) {
        this.integrationHandler = integrationHandler;
        dataManager = integrationHandler.getDataManager();
        apiGenerator = integrationHandler.apiGenerator;
    }

    @PUT
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation("For an integration that is generated from a specification updates it so it conforms to the updated specification")
    public void updateSpecification(@PathParam("id") final String id, @MultipartForm final ApiHandler.APIFormData apiFormData) {
        final Integration existing = integrationHandler.getIntegration(id);

        final APIIntegration apiIntegration = ApiGeneratorHelper.generateIntegrationUpdateFrom(existing, apiFormData, dataManager, apiGenerator);

        final Integration givenIntegration = apiIntegration.getIntegration();

        final Integration updated = updateFlowsAndStartAndEndDataShapes(existing, givenIntegration);

        if (existing.getFlows().equals(updated.getFlows())) {
            // no changes were made to the flows
            return;
        }

        // store the OpenAPI resource, we keep the old one as it might
        // be referenced from Integration's stored in IntegrationDeployent's
        // this gives us a rollback mechanism
        dataManager.store(apiIntegration.getSpec(), OpenApi.class);

        // perform the regular update
        integrationHandler.update(id, updated);
    }

    static Integration updateFlowsAndStartAndEndDataShapes(final Integration existing, final Integration given) {
        // will contain updated flows
        final List<Flow> updatedFlows = new ArrayList<>(given.getFlows().size());

        for (final Flow givenFlow : given.getFlows()) {
            final String flowId = givenFlow.getId().get();

            final Optional<Flow> maybeExistingFlow = existing.findFlowById(flowId);
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

            final Flow updatedFlow = existingFlow.builder().name(givenFlow.getName()).description(givenFlow.getDescription()).steps(updatedSteps).build();
            updatedFlows.add(updatedFlow);
        }

        return existing.builder().flows(updatedFlows)
            // we replace all resources counting that the only resource
            // present is the OpenAPI specification
            .resources(given.getResources()).build();
    }
}
