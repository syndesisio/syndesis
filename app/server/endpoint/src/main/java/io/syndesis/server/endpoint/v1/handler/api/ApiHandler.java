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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.syndesis.common.model.api.APISummary;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.openapi.OpenApi;
import io.syndesis.server.api.generator.APIGenerator;
import io.syndesis.server.api.generator.APIIntegration;
import io.syndesis.server.api.generator.APIValidationContext;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.endpoint.v1.handler.BaseHandler;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;

@Path("/apis")
@Api(value = "apis")
@Component
public class ApiHandler extends BaseHandler {

    private final APIGenerator apiGenerator;

    public ApiHandler(final DataManager dataMgr, APIGenerator apiGenerator) {
        super(dataMgr);
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
        String spec = ApiGeneratorHelper.getSpec(apiFormData);
        return apiGenerator.info(spec, APIValidationContext.PROVIDED_API);
    }

    public static class APIFormData {

        @FormParam("specification")
        private InputStream specification;

        public void setSpecification(InputStream specification) {
            this.specification = specification;
        }

        public InputStream getSpecification() {
            return specification;
        }
    }

}
