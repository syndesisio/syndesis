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
package io.syndesis.rest.v1.handler.connection;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.syndesis.core.SyndesisServerException;
import io.syndesis.dao.icon.IconDataAccessObject;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.connection.ConnectorSettings;
import io.syndesis.model.connection.ConnectorSummary;
import io.syndesis.model.icon.Icon;
import okio.BufferedSource;
import okio.Okio;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.springframework.context.ApplicationContext;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

@Api(tags = {"custom-connector", "connector-template"})
public final class CustomConnectorHandler extends BaseConnectorGeneratorHandler {

    private final IconDataAccessObject iconDao;

    /* default */ CustomConnectorHandler(final DataManager dataManager, final ApplicationContext applicationContext, final IconDataAccessObject iconDao) {
        super(dataManager, applicationContext);
        this.iconDao = iconDao;
    }

    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation("Creates a new Connector based on the ConnectorTemplate identified by the provided `id`  and the data given in `connectorSettings`")
    @ApiResponses(@ApiResponse(code = 200, response = Connector.class, message = "Newly created Connector"))
    public Connector create(final ConnectorSettings connectorSettings) {

        final Connector connector = withGeneratorAndTemplate(connectorSettings.getConnectorTemplateId(),
            (generator, template) -> generator.generate(template, connectorSettings));

        return getDataManager().create(connector);
    }

    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation("Creates a new Connector based on the ConnectorTemplate identified by the provided `id` and the data given in `connectorSettings` multipart part, plus optional `icon` file")
    @ApiResponses(@ApiResponse(code = 200, response = Connector.class, message = "Newly created Connector"))
    public Connector create(@MultipartForm CustomConnectorFormData customConnectorFormData) {
        if (customConnectorFormData.getConnectorSettings() == null) {
            throw new IllegalArgumentException("Missing connectorSettings parameter");
        }

        Connector generatedConnector = withGeneratorAndTemplate(customConnectorFormData.getConnectorSettings().getConnectorTemplateId(),
            (generator, template) -> generator.generate(template, customConnectorFormData.getConnectorSettings()));

        if (customConnectorFormData.getIconInputStream() != null) {
            try {
                String guessedMediaType = URLConnection.guessContentTypeFromStream(new BufferedInputStream(customConnectorFormData.getIconInputStream()));
                if (!guessedMediaType.startsWith("image/")) {
                    throw new IllegalArgumentException("Invalid file contents for an image");
                }
                MediaType mediaType = MediaType.valueOf(guessedMediaType);
                Icon.Builder iconBuilder = new Icon.Builder()
                    .mediaType(mediaType.toString());

                Icon icon = getDataManager().create(iconBuilder.build());
                iconDao.write(icon.getId().get(), customConnectorFormData.getIconInputStream());

                generatedConnector = new Connector.Builder().createFrom(generatedConnector).icon("db:" + icon.getId().get()).build();
            } catch (IOException e) {
                throw new IllegalArgumentException("Error while reading multipart request", e);
            }
        }

        return getDataManager().create(generatedConnector);
    }

    @POST
    @Path("/info")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation("Provides a summary of the connector as it would be built using a ConnectorTemplate identified by the provided `connector-template-id` and the data given in `connectorSettings`")
    public ConnectorSummary info(final ConnectorSettings connectorSettings) {
        return withGeneratorAndTemplate(connectorSettings.getConnectorTemplateId(),
            (generator, template) -> generator.info(template, connectorSettings));
    }

    @POST
    @Path("/info")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation("Provides a summary of the connector as it would be built using a ConnectorTemplate identified by the provided `connector-template-id` and the data given in `connectorSettings`")
    public ConnectorSummary info(@MultipartForm final SwaggerConnectorFormData swaggerConnectorFormData) {
        try {
            final String swaggerSpecification;
            try (BufferedSource source = Okio.buffer(Okio.source(swaggerConnectorFormData.getSwaggerSpecification()))) {
                swaggerSpecification = source.readUtf8();
            }

            final ConnectorSettings connectorSettings = new ConnectorSettings.Builder()
                .createFrom(swaggerConnectorFormData.getConnectorSettings())
                .putConfiguredProperty("specification", swaggerSpecification)
                .build();

            return withGeneratorAndTemplate(connectorSettings.getConnectorTemplateId(),
                (generator, template) -> generator.info(template, connectorSettings));
        } catch (IOException e) {
            throw SyndesisServerException.launderThrowable("Failed to read Swagger specification", e);
        }
    }

    public static class CustomConnectorFormData {
        @FormParam("connectorSettings")
        private ConnectorSettings connectorSettings;

        @FormParam("icon")
        private InputStream iconInputStream;

        public CustomConnectorFormData() {
        }

        public CustomConnectorFormData(ConnectorSettings connectorSettings, InputStream iconInputStream) {
            this.connectorSettings = connectorSettings;
            this.iconInputStream = iconInputStream;
        }

        public ConnectorSettings getConnectorSettings() {
            return connectorSettings;
        }

        public void setConnectorSettings(ConnectorSettings connectorSettings) {
            this.connectorSettings = connectorSettings;
        }

        public InputStream getIconInputStream() {
            return iconInputStream;
        }

        public void setIconInputStream(InputStream iconInputStream) {
            this.iconInputStream = iconInputStream;
        }
    }

    public static class SwaggerConnectorFormData {
        @FormParam("connectorSettings")
        private ConnectorSettings connectorSettings;

        @FormParam("swaggerSpecification")
        private InputStream swaggerSpecification;

        public SwaggerConnectorFormData() {
        }

        public SwaggerConnectorFormData(ConnectorSettings connectorSettings, InputStream swaggerSpecification) {
            this.connectorSettings = connectorSettings;
            this.swaggerSpecification = swaggerSpecification;
        }

        public ConnectorSettings getConnectorSettings() {
            return connectorSettings;
        }

        public void setConnectorSettings(ConnectorSettings connectorSettings) {
            this.connectorSettings = connectorSettings;
        }

        public InputStream getSwaggerSpecification() {
            return swaggerSpecification;
        }

        public void setSwaggerSpecification(InputStream swaggerSpecification) {
            this.swaggerSpecification = swaggerSpecification;
        }
    }
}
