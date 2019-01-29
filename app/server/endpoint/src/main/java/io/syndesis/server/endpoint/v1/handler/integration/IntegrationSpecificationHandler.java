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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.ResourceIdentifier;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.openapi.OpenApi;
import io.syndesis.integration.api.IntegrationResourceManager;
import io.syndesis.server.api.generator.APIGenerator;
import io.syndesis.server.api.generator.APIIntegration;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.endpoint.v1.handler.api.ApiGeneratorHelper;
import io.syndesis.server.endpoint.v1.handler.api.ApiHandler;

import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.springframework.stereotype.Component;

@Api("integrations")
@Path("/integrations/{id}/specification")
@Component
public final class IntegrationSpecificationHandler {

    static final String DEFAULT_CONTENT_TYPE = "application/vnd.oai.openapi+json";

    static final String DEFAULT_FILE_NAME = "openapi.json";

    private static final Map<String, String> CONTENT_TYPE_TO_FILE_NAME;

    private static final Response NO_CONTENT = Response.noContent().build();

    private static final Response NOT_FOUND = Response.status(Status.NOT_FOUND).build();

    static {
        final Map<String, String> contentTypeToFileName = new HashMap<>();

        contentTypeToFileName.put(DEFAULT_CONTENT_TYPE, DEFAULT_FILE_NAME);
        contentTypeToFileName.put("application/vnd.oai.openapi", "openapi.yaml");

        CONTENT_TYPE_TO_FILE_NAME = Collections.unmodifiableMap(contentTypeToFileName);
    }

    private final APIGenerator apiGenerator;

    private final DataManager dataManager;

    private final IntegrationHandler integrationHandler;

    private final IntegrationResourceManager resourceManager;

    public IntegrationSpecificationHandler(final IntegrationHandler integrationHandler, final IntegrationResourceManager resourceManager) {
        this.integrationHandler = integrationHandler;
        this.resourceManager = resourceManager;
        dataManager = integrationHandler.getDataManager();
        apiGenerator = integrationHandler.apiGenerator;
    }

    @GET
    @ApiOperation("Responds with specification that defines this integration")
    @ApiResponses({
        @ApiResponse(code = 404, message = "No specification resource defines this integration"),
        @ApiResponse(code = 204, message = "Empty specification provided when definining this integration"),
        @ApiResponse(code = 200, message = "Specification resource follows", responseHeaders = {
            @ResponseHeader(name = "Content-Type", description = "The content type of the specification, e.g. `application/vnd.oai.openapi+json`"),
            @ResponseHeader(name = "Content-Disposition", description = "Contains the `filename` parameter"),
        })
    })
    public Response
        fetch(@NotNull @PathParam("id") @ApiParam(required = true, example = "integration-id", value = "The ID of the integration") final String id) {
        final Integration integration = integrationHandler.getIntegration(id);

        final Optional<ResourceIdentifier> specification = integration.getResources().stream().filter(r -> r.getKind() == Kind.OpenApi)
            .max(ResourceIdentifier.LATEST_VERSION);

        return specification
            .flatMap(s -> s.getId()
                .flatMap(resourceManager::loadOpenApiDefinition)
                .map(IntegrationSpecificationHandler::createResponseFrom))
            .orElse(NOT_FOUND);
    }

    @PUT
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation("For an integration that is generated from a specification updates it so it conforms to the updated specification")
    @ApiImplicitParams(value = {
        @ApiImplicitParam(dataType = "file", name = "specification", required = true, paramType = "form", value = "Next revision of the specification")})
    public void update(@NotNull @PathParam("id") @ApiParam(required = true, example = "integration-id", value = "The ID of the integration") final String id,
        @NotNull @MultipartForm final ApiHandler.APIFormData apiFormData) {
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

    static Response createResponseFrom(final OpenApi document) {
        final byte[] bytes = document.getDocument();
        if (bytes == null || bytes.length == 0) {
            return NO_CONTENT;
        }

        final String documentType = document.getMetadata(HttpHeaders.CONTENT_TYPE).orElse(DEFAULT_CONTENT_TYPE);
        final String fileName = CONTENT_TYPE_TO_FILE_NAME.getOrDefault(documentType, DEFAULT_FILE_NAME);
        return Response
            .ok(bytes, documentType)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
            .build();
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
