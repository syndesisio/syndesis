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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
import io.syndesis.common.model.integration.Integration;
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

        return ApiGeneratorHelper.specificationFrom(resourceManager, integration)
            .map(IntegrationSpecificationHandler::createResponseFrom)
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

        final Integration updated = ApiGeneratorHelper.updateFlowsAndStartAndEndDataShapes(existing, givenIntegration);

        final OpenApi existingApiSpecification = ApiGeneratorHelper.specificationFrom(resourceManager, existing).orElse(null);

        if (Objects.equals(existing.getFlows(), updated.getFlows()) && Objects.equals(existingApiSpecification, apiIntegration.getSpec())) {
            // no changes were made to the flows or to the specification
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

}
