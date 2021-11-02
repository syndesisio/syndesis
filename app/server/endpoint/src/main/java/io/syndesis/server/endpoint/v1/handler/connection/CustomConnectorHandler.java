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
package io.syndesis.server.endpoint.v1.handler.connection;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.syndesis.common.model.api.APISummary;
import io.syndesis.common.util.IOStreams;
import io.syndesis.common.util.SyndesisServerException;
import io.syndesis.server.dao.file.IconDao;
import io.syndesis.server.dao.file.SpecificationResourceDao;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.connection.ConnectorSettings;
import io.syndesis.common.model.icon.Icon;

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
import java.util.Map;
import java.util.Optional;

@Tag(name = "custom-connector")
@Tag(name = "connector-template")
public final class CustomConnectorHandler extends BaseConnectorGeneratorHandler {

    private final IconDao iconDao;

    private final SpecificationResourceDao specificationResourceDao;

    CustomConnectorHandler(final DataManager dataManager, final ApplicationContext applicationContext, final IconDao iconDao, final SpecificationResourceDao specificationResourceDao) {
        super(dataManager, applicationContext);
        this.iconDao = iconDao;
        this.specificationResourceDao = specificationResourceDao;
    }

    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(description = "Creates a new Connector based on the ConnectorTemplate identified by the provided `id` and the data given in `connectorSettings` multipart part, plus optional `icon` file")
    @ApiResponse(responseCode = "200", description = "Newly created Connector")
    public Connector create(@MultipartForm CustomConnectorFormData customConnectorFormData) throws IOException {
        final ConnectorSettings connectorSettings = customConnectorFormData.getConnectorSettings();
        if (connectorSettings == null) {
            throw new IllegalArgumentException("Missing connectorSettings parameter");
        }

        final ConnectorSettings connectorSettingsToUse;
        if (connectorSettings.getConfiguredProperties().containsKey("specification")) {
            connectorSettingsToUse = connectorSettings;
        } else {
            try (InputStream specificationStream = customConnectorFormData.getSpecification()) {
                if (specificationStream == null) {
                    connectorSettingsToUse = connectorSettings;
                } else {
                    @SuppressWarnings("resource")
                    final InputStream buffered = IOStreams.fullyBuffer(specificationStream);

                    connectorSettingsToUse = new ConnectorSettings.Builder().createFrom(connectorSettings).specification(buffered).build();
                }
            }
        }

        Connector generatedConnector = withGeneratorAndTemplate(connectorSettingsToUse.getConnectorTemplateId(),
            (generator, template) -> generator.generate(template, connectorSettingsToUse));

        final InputStream iconInputStream = customConnectorFormData.getIconInputStream();
        if (iconInputStream != null) {
            // URLConnection.guessContentTypeFromStream resets the stream after inspecting the media type so
            // can continue to be used, rather than being consumed.
            try (BufferedInputStream iconStream = new BufferedInputStream(iconInputStream)) {
                String guessedMediaType = URLConnection.guessContentTypeFromStream(iconStream);
                if (!guessedMediaType.startsWith("image/")) {
                    throw new IllegalArgumentException("Invalid file contents for an image");
                }
                MediaType mediaType = MediaType.valueOf(guessedMediaType);
                Icon.Builder iconBuilder = new Icon.Builder()
                    .mediaType(mediaType.toString());

                Icon icon = getDataManager().create(iconBuilder.build());
                iconDao.write(icon.getId().get(), iconStream);

                generatedConnector = new Connector.Builder().createFrom(generatedConnector).icon("db:" + icon.getId().get()).build();
            } catch (IOException e) {
                throw new IllegalArgumentException("Error while reading multipart request", e);
            }
        }

        final Map<String, String> configuredProperties = generatedConnector.getConfiguredProperties();
        if (!configuredProperties.containsKey("specification")) {
            // connector generator has opted not to store the specification within
            // the connector, we need to store the specification as a blob and point
            // to it
            final Optional<InputStream> maybeSpecificationStream = connectorSettingsToUse.getSpecification();
            // but there was a specification provided in settings, which indicates
            // that we should persist it
            if (maybeSpecificationStream.isPresent()) {
                // here we can close the stream
                try (InputStream specificationStream = maybeSpecificationStream.get()) {
                    // by this time we have read the stream provided in connector settings we need to reset it to beginning
                    specificationStream.reset();

                    final String id = generatedConnector.getId().get();
                    specificationResourceDao.write(id, specificationStream);

                    // now we need to point to the specification from the connector
                    generatedConnector = new Connector.Builder().createFrom(generatedConnector)
                        .putConfiguredProperty("specification", "db:" + id)
                        .build();
                }
            }
        }

        return getDataManager().create(generatedConnector);
    }

    @POST
    @Path("/info")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(description = "Provides a summary of the connector as it would be built using a ConnectorTemplate identified by the provided `connector-template-id` and the data given in `connectorSettings`")
    public APISummary info(@MultipartForm final CustomConnectorFormData connectorFormData) {
        try {
            final ConnectorSettings givenConnectorSettings = connectorFormData.getConnectorSettings();

            final ConnectorSettings connectorSettings;
            if (givenConnectorSettings.getConfiguredProperties().containsKey("specification")) {
                connectorSettings = givenConnectorSettings;
            } else {
                try (InputStream specificationStream = connectorFormData.getSpecification()) {
                    if (specificationStream == null) {
                        connectorSettings = givenConnectorSettings;
                    } else {
                        @SuppressWarnings("resource")
                        final InputStream buffered = IOStreams.fullyBuffer(specificationStream);

                        connectorSettings = new ConnectorSettings.Builder().createFrom(givenConnectorSettings)
                            .specification(buffered)
                            .build();
                    }
                }
            }

            return withGeneratorAndTemplate(connectorSettings.getConnectorTemplateId(),
                (generator, template) -> generator.info(template, connectorSettings));
        } catch (IOException e) {
            throw SyndesisServerException.launderThrowable("Failed to read specification", e);
        }
    }

    public static class CustomConnectorFormData {
        @FormParam("connectorSettings")
        private ConnectorSettings connectorSettings;

        @FormParam("icon")
        private InputStream iconInputStream;

        @FormParam("specification")
        private InputStream specification;

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

        public void setSpecification(InputStream specification) {
            this.specification = specification;
        }

        public InputStream getSpecification() {
            return specification;
        }
    }

}
