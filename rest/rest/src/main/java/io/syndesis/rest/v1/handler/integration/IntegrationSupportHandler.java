/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.rest.v1.handler.integration;

import io.swagger.annotations.Api;
import io.syndesis.core.Json;
import io.syndesis.model.ModelData;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.model.ModelExport;
import io.syndesis.model.Schema;
import io.syndesis.model.connection.Connection;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.integration.Integration;
import io.syndesis.project.converter.ProjectGenerator;
import io.syndesis.rest.v1.handler.exception.RestErrorResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Path("/integration-support")
@Api(value = "integration-support")
@Component
public class IntegrationSupportHandler {

    public static final String EXPORT_MODEL_FILE_NAME = "model.json";
    private static final Logger LOG = LoggerFactory.getLogger(IntegrationSupportHandler.class);

    private final ProjectGenerator projectConverter;
    private final DataManager dataManager;
    private final IntegrationHandler integrationHandler;

    public IntegrationSupportHandler(ProjectGenerator projectConverter, final DataManager dataManager, IntegrationHandler integrationHandler) {
        this.projectConverter = projectConverter;
        this.dataManager = dataManager;
        this.integrationHandler = integrationHandler;
    }

    @POST
    @Path("/generate/pom.xml")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public byte[] projectPom(Integration integration) throws IOException {
        return projectConverter.generatePom(integration);
    }


    @POST
    @Path("/import")
    public Response importIntegration(@Context SecurityContext sec, InputStream is) {
        try (ZipInputStream zis = new ZipInputStream(is)) {
            int imported = 0;
            while (true) {
                ZipEntry entry = zis.getNextEntry();
                if( entry == null ) {
                    break;
                }
                if (EXPORT_MODEL_FILE_NAME.equals(entry.getName())) {
                    ModelExport models = Json.mapper().readValue(new FilterInputStream(zis) {
                        @Override
                        public void close() throws IOException {
                            // We want to avoid closing zis
                        }
                    }, ModelExport.class);

                    imported += importModels(sec, models);
                }
                zis.closeEntry();
            }
            if (imported==0) {
                LOG.info("Could not import integration: No integration data model found.");
                return RestErrorResponses.badRequest(
                    "Does not look like an integration export.",
                    "No integration data model found");
            }
            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (IOException e) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Could not import integration: " + e, e);
            }
            return RestErrorResponses.badRequest(e);
        }
    }

    public int importModels(SecurityContext sec, ModelExport export) throws IOException {

        validateForImport(export);

        // Now do the actual import.
        int count = 0;
        for (ModelData<?> model : export.models()) {
            switch (model.getKind()) {
                case Integration:

                    Integration integration = (Integration) model.getData();
                    integration = new Integration.Builder()
                        .createFrom(integration)
                        .desiredStatus(Integration.Status.Draft)
                        .build();

                    // Do we need to create it?
                    if (dataManager.fetch(Integration.class, integration.getId().get()) == null) {
                        LOG.info("Creating integration: "+integration.getName());
                        integrationHandler.create(sec, integration);
                    } else {
                        LOG.info("Updating integration: "+integration.getName());
                        integrationHandler.update(integration.getId().get(), integration);
                    }
                    count ++;
                    break;

                case Connection:

                    // We only create connections, never update.
                    Connection connection = (Connection) model.getData();
                    if (dataManager.fetch(Connection.class, connection.getId().get()) == null) {
                        dataManager.create(connection);
                    }

                    break;
                case Connector:

                    // We only create connectors, never update.
                    Connector connector = (Connector) model.getData();
                    if (dataManager.fetch(Connector.class, connector.getId().get()) == null) {
                        dataManager.create(connector);
                    }
                    break;

                default:

                    if (LOG.isInfoEnabled()) {
                        LOG.info("Cannot import unsupported model kind: " + model.getKind());
                    }
                    break;

            }
        }
        return count;
    }

    public void validateForImport(ModelExport export) throws IOException {
        // First do some validation of the models..
        if (!Schema.VERSION.equals(export.schemaVersion())) {
            throw new IOException("Cannot import an export at schema version level: " + export.schemaVersion());
        }

        for (ModelData<?> model : export.models()) {
            switch (model.getKind()) {
                case Integration:
                case Connection:
                case Connector:
                    break;
                default:
                    throw new IOException("Cannot import unsupported model kind: " + model.getKind());

            }
        }
    }


}
