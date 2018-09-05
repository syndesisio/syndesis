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
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.openapi.OpenApi;
import io.syndesis.common.util.KeyGenerator;
import io.syndesis.common.util.SyndesisServerException;
import io.syndesis.server.api.generator.APIGenerator;
import io.syndesis.server.api.generator.APIIntegration;
import io.syndesis.server.api.generator.APIValidationContext;
import io.syndesis.server.api.generator.ProvidedApiTemplate;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.endpoint.v1.handler.BaseHandler;
import okio.BufferedSource;
import okio.Okio;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;

@Path("/apis")
@Api(value = "apis")
@Component
public class ApiHandler extends BaseHandler {

    private static final String API_PROVIDER_CONNECTION_ID = "api-provider";
    private static final String API_PROVIDER_START_ACTION_ID = "io.syndesis:api-provider-start";
    private static final String API_PROVIDER_END_ACTION_ID = "io.syndesis:api-provider-end";

    private APIGenerator apiGenerator;

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
        Connection apiProviderConnection = getDataManager().fetch(Connection.class, API_PROVIDER_CONNECTION_ID);
        if (apiProviderConnection == null) {
            throw new IllegalStateException("Cannot find api-provider connection with id: " + API_PROVIDER_CONNECTION_ID);
        }

        String spec = getSpec(apiFormData);
        if (!apiProviderConnection.getConnector().isPresent()) {
            Connector apiProviderConnector = getDataManager().fetch(Connector.class, apiProviderConnection.getConnectorId());
            apiProviderConnection = new Connection.Builder()
                .createFrom(apiProviderConnection)
                .connector(apiProviderConnector)
                .build();
        }

        ProvidedApiTemplate template = new ProvidedApiTemplate(apiProviderConnection, API_PROVIDER_START_ACTION_ID, API_PROVIDER_END_ACTION_ID);

        APIIntegration apiIntegration = apiGenerator.generateIntegration(spec, template);

        if (apiIntegration.getSpec() != null) {
            getDataManager().store(apiIntegration.getSpec(), OpenApi.class);
        }

        return apiIntegration.getIntegration();
    }

    @POST
    @Path("/generator/save")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation("Test resource for testing the creation of APIs without UI")
    public Integration createIntegrationFromAPIAndSave(@MultipartForm final APIFormData apiFormData) {
        Integration integration = createIntegrationFromAPI(apiFormData);

        String name = integration.getName();
        if (name == null) {
            integration = new Integration.Builder()
                .createFrom(integration)
                .name(KeyGenerator.createKey())
                .build();
        }

        getDataManager().store(integration, Integration.class);
        return integration;
    }

    @POST
    @Path("/info")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation("Validates the API and provides a summary of the operations")
    public APISummary info(@MultipartForm final APIFormData apiFormData) {
        String spec = getSpec(apiFormData);
        return apiGenerator.info(spec, APIValidationContext.PROVIDED_API);
    }

    protected String getSpec(APIFormData apiFormData) {
        final String spec;
        try (BufferedSource source = Okio.buffer(Okio.source(apiFormData.getSpecification()))) {
            spec = source.readUtf8();
        } catch (IOException e) {
            throw SyndesisServerException.launderThrowable("Failed to read specification", e);
        }
        return spec;
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
