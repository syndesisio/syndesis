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

import java.io.InputStream;
import java.util.Objects;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.syndesis.common.model.api.APISummary;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.openapi.OpenApi;
import io.syndesis.integration.api.IntegrationResourceManager;
import io.syndesis.server.api.generator.APIGenerator;
import io.syndesis.server.api.generator.APIIntegration;
import io.syndesis.server.api.generator.APIValidationContext;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.endpoint.v1.handler.BaseHandler;

import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.springframework.stereotype.Component;

@Path("/apis")
@Api(value = "apis")
@Component
public class ApiHandler extends BaseHandler {

    private final APIGenerator apiGenerator;

    private final IntegrationResourceManager resourceManager;

    public static class APIFormData {

        @FormParam("specification")
        private InputStream specification;

        public InputStream getSpecification() {
            return specification;
        }

        public void setSpecification(final InputStream specification) {
            this.specification = specification;
        }
    }

    public static class APIUpdateFormData extends APIFormData {
        @FormParam("integration")
        private Integration integration;

        public Integration getIntegration() {
            return integration;
        }

        public void setIntegration(final Integration integration) {
            this.integration = integration;
        }
    }

    public ApiHandler(final DataManager dataMgr, final IntegrationResourceManager resourceManager, final APIGenerator apiGenerator) {
        super(dataMgr);
        this.resourceManager = resourceManager;
        this.apiGenerator = apiGenerator;
    }

    @POST
    @Path("/generator")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation("Provides a integration from a API specification. Does not store it in the database")
    public Integration createIntegrationFromAPI(@MultipartForm final APIFormData apiFormData) {
        final APIIntegration apiIntegration = ApiGeneratorHelper.generateIntegrationFrom(apiFormData, getDataManager(), apiGenerator);

        if (apiIntegration.getSpec() != null) {
            // TODO we should probably reconsider this, even if the user
            // cancels the the integration creation, so no integration
            // is stored the specification is stored in the database
            getDataManager().store(apiIntegration.getSpec(), OpenApi.class);
        }

        return apiIntegration.getIntegration();
    }

    @POST
    @Path("/info")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation("Validates the API and provides a summary of the operations")
    public APISummary info(@MultipartForm final APIFormData apiFormData) {
        final String spec = ApiGeneratorHelper.getSpec(apiFormData);
        return apiGenerator.info(spec, APIValidationContext.PROVIDED_API);
    }

    @PUT
    @Path("/generator")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation("Update the provided integration from a API specification. Does not store it in the database")
    public Response updateIntegrationFromSpecification(@MultipartForm final APIUpdateFormData apiUpdateFormData) {
        final DataManager dataManager = getDataManager();
        final Integration existing = apiUpdateFormData.integration;

        final APIIntegration apiIntegration = ApiGeneratorHelper.generateIntegrationUpdateFrom(existing, apiUpdateFormData, dataManager, apiGenerator);

        final Integration givenIntegration = apiIntegration.getIntegration();

        final Integration updated = ApiGeneratorHelper.updateFlowsAndStartAndEndDataShapes(existing, givenIntegration);

        final OpenApi existingApiSpecification = ApiGeneratorHelper.specificationFrom(resourceManager, existing).orElse(null);

        if (Objects.equals(existing.getFlows(), updated.getFlows()) && Objects.equals(existingApiSpecification, apiIntegration.getSpec())) {
            // no changes were made to the flows or to the specification
            return Response.notModified().build();
        }

        // store the OpenAPI resource, we keep the old one as it might
        // be referenced from Integration's stored in IntegrationDeployent's
        // this gives us a rollback mechanism
        final OpenApi openApi = apiIntegration.getSpec();
        dataManager.store(openApi, OpenApi.class);

        return Response.accepted(updated).build();
    }
}
