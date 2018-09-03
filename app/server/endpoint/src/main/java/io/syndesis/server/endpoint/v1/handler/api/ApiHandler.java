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
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.util.KeyGenerator;
import io.syndesis.common.util.SyndesisServerException;
import io.syndesis.server.api.generator.APIGenerator;
import io.syndesis.server.api.generator.ProvidedApiTemplate;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.endpoint.v1.handler.BaseHandler;
import okio.BufferedSource;
import okio.Okio;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiHandler.class);

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
        final String spec;
        try (BufferedSource source = Okio.buffer(Okio.source(apiFormData.getSpecification()))) {
            spec = source.readUtf8();
        } catch (IOException e) {
            throw SyndesisServerException.launderThrowable("Failed to read specification", e);
        }

        Connection apiProviderConnection = getDataManager().fetch(Connection.class, API_PROVIDER_CONNECTION_ID);
        if (apiProviderConnection == null) {
            throw new IllegalStateException("Cannot find api-provider connection with id: " + API_PROVIDER_CONNECTION_ID);
        }
        if (!apiProviderConnection.getConnector().isPresent()) {
            Connector apiProviderConnector = getDataManager().fetch(Connector.class, apiProviderConnection.getConnectorId());
            apiProviderConnection = new Connection.Builder()
                .createFrom(apiProviderConnection)
                .connector(apiProviderConnector)
                .build();
        }

        ProvidedApiTemplate template = new ProvidedApiTemplate(apiProviderConnection, API_PROVIDER_START_ACTION_ID, API_PROVIDER_END_ACTION_ID);

        return apiGenerator.generateIntegration(spec, template);
    }

    @POST
    @Path("/generator/save")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation("Test resource for testing the creation of APIs without UI")
    // TODO: delete me
    public Integration createIntegrationFromAPIAndSave(@MultipartForm final APIFormData apiFormData) {
        Integration integrationData = createIntegrationFromAPI(apiFormData);

        Integration integration = new Integration.Builder()
            .createFrom(integrationData)
            .name("test-" + KeyGenerator.createKey())
            .createdAt(System.currentTimeMillis())
            .build();

        getDataManager().store(integration, Integration.class);
        return integration;
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
