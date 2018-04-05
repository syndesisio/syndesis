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
package io.syndesis.server.endpoint.v1.handler.integration.support;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.validation.constraints.NotNull;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.syndesis.common.model.ChangeEvent;
import io.syndesis.common.model.Dependency;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.ModelExport;
import io.syndesis.common.model.Schema;
import io.syndesis.common.model.WithId;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.extension.Extension;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.IntegrationOverview;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.util.Json;
import io.syndesis.common.util.Names;
import io.syndesis.integration.api.IntegrationProjectGenerator;
import io.syndesis.integration.api.IntegrationResourceManager;
import io.syndesis.server.dao.file.FileDataManager;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.endpoint.v1.handler.integration.IntegrationHandler;
import io.syndesis.server.jsondb.CloseableJsonDB;
import io.syndesis.server.jsondb.JsonDB;
import io.syndesis.server.jsondb.dao.JsonDbDao;
import io.syndesis.server.jsondb.dao.Migrator;
import io.syndesis.server.jsondb.impl.MemorySqlJsonDB;
import io.syndesis.server.jsondb.impl.SqlJsonDB;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static org.springframework.util.StreamUtils.nonClosing;

@Path("/integration-support")
@Api(value = "integration-support")
@Component
@SuppressWarnings({ "PMD.ExcessiveImports", "PMD.GodClass", "PMD.StdCyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.CyclomaticComplexity" })
public class IntegrationSupportHandler {

    public static final String EXPORT_MODEL_INFO_FILE_NAME = "model-info.json";
    public static final String EXPORT_MODEL_FILE_NAME = "model.json";
    private static final Logger LOG = LoggerFactory.getLogger(IntegrationSupportHandler.class);

    private final Migrator migrator;
    private final SqlJsonDB jsonDB;
    private final IntegrationProjectGenerator projectGenerator;
    private final DataManager dataManager;
    private final IntegrationResourceManager resourceManager;
    private final IntegrationHandler integrationHandler;
    private final FileDataManager extensionDataManager;

    public IntegrationSupportHandler(
        final Migrator migrator,
        final SqlJsonDB jsonDB,
        final IntegrationProjectGenerator projectGenerator,
        final DataManager dataManager,
        final IntegrationResourceManager resourceManager,
        final IntegrationHandler integrationHandler,
        final FileDataManager extensionDataManager) {
        this.migrator = migrator;
        this.jsonDB = jsonDB;

        this.projectGenerator = projectGenerator;
        this.dataManager = dataManager;
        this.resourceManager = resourceManager;
        this.integrationHandler = integrationHandler;
        this.extensionDataManager = extensionDataManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/overviews")
    public ListResult<IntegrationOverview> getOverviews(@Context  UriInfo uriInfo) {
        return integrationHandler.list(uriInfo);
    }

    @POST
    @Path("/generate/pom.xml")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public byte[] projectPom(Integration integration) throws IOException {
        return projectGenerator.generatePom(integration);
    }

    @GET
    @Path("/export.zip")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput export(@NotNull @QueryParam("id") @ApiParam(required = true) List<String> requestedIds) throws IOException {

        List<String> ids = requestedIds;
        if ( ids ==null || ids.isEmpty() ) {
            throw new ClientErrorException("No 'integration' query parameter specified.", Response.Status.BAD_REQUEST);
        }

        LinkedHashSet<String> extensions = new LinkedHashSet<>();
        CloseableJsonDB memJsonDB = MemorySqlJsonDB.create(jsonDB.getIndexes());
        if( ids.contains("all") ) {
            ids = new ArrayList<>();
            for (Integration integration : dataManager.fetchAll(Integration.class)) {
                ids.add(integration.getId().get());
            }
        }
        for (String id : ids) {
            Integration integration = integrationHandler.getIntegration(id);
            addToExport(memJsonDB, integration);
            resourceManager.collectDependencies(integration.getSteps(), true).stream()
                .filter(Dependency::isExtension)
                .map(Dependency::getId)
                .forEach(extensions::add);
        }
        LOG.debug("Extensions: {}", extensions);

        return out -> {
            try (ZipOutputStream tos = new ZipOutputStream(out) ) {
                ModelExport exportObject = ModelExport.of(Schema.VERSION);
                addEntry(tos, EXPORT_MODEL_INFO_FILE_NAME, Json.writer().writeValueAsBytes(exportObject));
                addEntry(tos, EXPORT_MODEL_FILE_NAME, memJsonDB.getAsByteArray("/"));
                memJsonDB.close();
                for (String extensionId : extensions) {
                    addEntry(tos, "extensions/" + Names.sanitize(extensionId) + ".jar", IOUtils.toByteArray(
                        extensionDataManager.getExtensionBinaryFile(extensionId)
                    ));
                }
            }
        };

    }

    private void addToExport(JsonDB export, Integration integration) {
        addModelToExport(export, integration);
        for (Step step : integration.getSteps()) {
            Optional<Connection> c = step.getConnection();
            if (c.isPresent()) {
                Connection connection = c.get();
                addModelToExport(export, connection);
                Connector connector = integrationHandler.getDataManager().fetch(Connector.class, connection.getConnectorId());
                if (connector != null) {
                    addModelToExport(export, connector);
                }
            }
            Optional<Extension> e = step.getExtension();
            if (e.isPresent()) {
                Extension extension = e.get();
                addModelToExport(export, extension);
            }
        }
    }

    private static <T extends WithId<T>> void addModelToExport(JsonDB export, T model) {
        JsonDbDao<T> dao = new JsonDbDao<T>(export) {
            @Override
            public Class<T> getType() {
                return model.getKind().getModelClass();
            }
        };
        dao.set(model);
    }

    @POST
    @Path("/import")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ChangeEvent> importIntegration(@Context SecurityContext sec, InputStream is) {
        try (ZipInputStream zis = new ZipInputStream(is)) {
            HashSet<String> extensionIds = new HashSet<>();
            ArrayList<ChangeEvent> changeEvents = new ArrayList<>();
            ModelExport modelExport = null;
            while (true) {
                ZipEntry entry = zis.getNextEntry();
                if( entry == null ) {
                    break;
                }

                if (EXPORT_MODEL_INFO_FILE_NAME.equals(entry.getName())) {
                    modelExport = Json.reader().forType(ModelExport.class).readValue(zis);
                }

                if (EXPORT_MODEL_FILE_NAME.equals(entry.getName())) {
                    CloseableJsonDB memJsonDB = MemorySqlJsonDB.create(jsonDB.getIndexes());
                    memJsonDB.set("/", nonClosing(zis));
                    changeEvents.addAll(importModels(sec, modelExport, memJsonDB));
                    memJsonDB.close();
                    for (ChangeEvent changeEvent : changeEvents) {
                        if( changeEvent.getKind().get().equals("extension") ) {
                            extensionIds.add(changeEvent.getId().get());
                        }
                    }
                }

                // import missing extensions that were in the model.
                if( entry.getName().startsWith("extensions/") ) {
                    for (String extensionId : extensionIds) {
                        if( entry.getName().equals("extensions/" + Names.sanitize(extensionId) + ".jar") ) {
                            String path = "/extensions/" + extensionId;
                            InputStream existing = extensionDataManager.getExtensionDataAccess().read(path);
                            if( existing == null ) {
                                extensionDataManager.getExtensionDataAccess().write(path, zis);
                            } else {
                                existing.close();
                            }
                        }
                    }
                }

                zis.closeEntry();
            }
            if (changeEvents.isEmpty()) {
                LOG.info("Could not import integration: No integration data model found.");
                throw new ClientErrorException("Does not look like an integration export.", Response.Status.BAD_REQUEST);
            }
            return changeEvents;
        } catch (IOException e) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Could not import integration: " + e, e);
            }
            throw new ClientErrorException("Could not import integration: " + e, Response.Status.BAD_REQUEST, e);
        }
    }

    public List<ChangeEvent> importModels(SecurityContext sec, ModelExport export, JsonDB jsondb) throws IOException {

        // Apply per version migrations to get the schema upgraded on the import.
        int from = export.schemaVersion();
        int to = Schema.VERSION;

        if( from > to ) {
            throw new IOException("Cannot import an export at schema version level: " + export.schemaVersion());
        }

        for (int i = from; i < to; i++) {
            int version = i + 1;
            migrator.migrate(jsondb, version);
        }

        ArrayList<ChangeEvent> result = new ArrayList<>();
        // Import the extensions..
        importModels(new JsonDbDao<Extension>(jsondb) {
            @Override
            public Class<Extension> getType() {
                return Extension.class;
            }
        }, result);

        importModels(new JsonDbDao<Connector>(jsondb) {
            @Override
            public Class<Connector> getType() {
                return Connector.class;
            }
        }, result);

        importModels(new JsonDbDao<Connection>(jsondb) {
            @Override
            public Class<Connection> getType() {
                return Connection.class;
            }
        }, result);

        importIntegrations(sec, new JsonDbDao<Integration>(jsondb) {
            @Override
            public Class<Integration> getType() {
                return Integration.class;
            }
        }, result);


        return result;
    }

    private void importIntegrations(SecurityContext sec, JsonDbDao<Integration> export, List<ChangeEvent> result) {
        for (Integration integration : export.fetchAll().getItems()) {
            Integration.Builder builder = new Integration.Builder()
                .createFrom(integration)
                .isDeleted(false)
                .updatedAt(System.currentTimeMillis());

            // Do we need to create it?
            String id = integration.getId().get();
            Integration previous = dataManager.fetch(Integration.class, id);
            if (previous == null) {
                LOG.info("Creating integration: {}", integration.getName());
                integrationHandler.create(sec, builder.build());
                result.add(ChangeEvent.of("created", integration.getKind().getModelName(), id));
            } else {
                LOG.info("Updating integration: {}", integration.getName());
                integrationHandler.update(id, builder.version(previous.getVersion()+1).build());
                result.add(ChangeEvent.of("updated", integration.getKind().getModelName(), id));
            }
            break;
        }
    }

    private <T extends WithId<T>> void importModels(JsonDbDao<T> export, List<ChangeEvent> result) {
        for (T item : export.fetchAll().getItems()) {
            Kind kind = item.getKind();
            String id = item.getId().get();
            if (dataManager.fetch(export.getType(), id) == null) {
                dataManager.create(item);

                result.add(ChangeEvent.of("created", kind.getModelName(), id));
            }
        }
    }

    private static void addEntry(ZipOutputStream os, String path, byte[] content) throws IOException {
        ZipEntry entry = new ZipEntry(path);
        entry.setSize(content.length);
        os.putNextEntry(entry);
        os.write(content);
        os.closeEntry();
    }

}
