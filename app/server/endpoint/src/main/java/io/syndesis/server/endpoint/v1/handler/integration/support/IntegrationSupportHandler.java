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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
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

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.syndesis.common.model.ChangeEvent;
import io.syndesis.common.model.Dependency;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.ModelExport;
import io.syndesis.common.model.Schema;
import io.syndesis.common.model.WithId;
import io.syndesis.common.model.WithName;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.extension.Extension;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.IntegrationDeployment;
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

import static org.springframework.util.StreamUtils.nonClosing;

@Path("/integration-support")
@Api(value = "integration-support")
@Component
@SuppressWarnings({ "PMD.ExcessiveImports", "PMD.GodClass", "PMD.StdCyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.CyclomaticComplexity" })
public class IntegrationSupportHandler {

    private static final String EXPORT_MODEL_INFO_FILE_NAME = "model-info.json";
    private static final String EXPORT_MODEL_FILE_NAME = "model.json";

    private static final Logger LOG = LoggerFactory.getLogger(IntegrationSupportHandler.class);
    private static final String IMPORTED_SUFFIX = "-imported-";

    private static final BiFunction<Extension, String, Extension> RENAME_EXTENSION = (e, n) -> new Extension.Builder().createFrom(e).name(n).build();
    private static final BiFunction<Connection, String, Connection> RENAME_CONNECTION = (c, n) -> new Connection.Builder().createFrom(c).name(n).build();

    private final Migrator migrator;
    private final SqlJsonDB jsondb;
    private final IntegrationProjectGenerator projectGenerator;
    private final DataManager dataManager;
    private final IntegrationResourceManager resourceManager;
    private final IntegrationHandler integrationHandler;
    private final FileDataManager extensionDataManager;

    public IntegrationSupportHandler(
        final Migrator migrator,
        final SqlJsonDB jsondb,
        final IntegrationProjectGenerator projectGenerator,
        final DataManager dataManager,
        final IntegrationResourceManager resourceManager,
        final IntegrationHandler integrationHandler,
        final FileDataManager extensionDataManager) {
        this.migrator = migrator;
        this.jsondb = jsondb;

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
        CloseableJsonDB memJsonDB = MemorySqlJsonDB.create(jsondb.getIndexes());
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
                    CloseableJsonDB memJsonDB = MemorySqlJsonDB.create(jsondb.getIndexes());
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

    public List<ChangeEvent> importModels(SecurityContext sec, ModelExport export, JsonDB given) throws IOException {

        // Apply per version migrations to get the schema upgraded on the import.
        int from = export.schemaVersion();
        int to = Schema.VERSION;

        if( from > to ) {
            throw new IOException("Cannot import an export at schema version level: " + export.schemaVersion());
        }

        for (int i = from; i < to; i++) {
            int version = i + 1;
            migrator.migrate(given, version);
        }

        ArrayList<ChangeEvent> result = new ArrayList<>();
        // id->new-name map
        Map<String, String> renamedIds = new HashMap<>();

        // Import the extensions..
        importModels(new JsonDbDao<Extension>(given) {
            @Override
            public Class<Extension> getType() {
                return Extension.class;
            }
        }, RENAME_EXTENSION, renamedIds, result);

        // NOTE: connectors are imported without renaming and ignoring renamed ids
        // as a matter of fact, the lambda should never be called
        importModels(new JsonDbDao<Connector>(given) {
            @Override
            public Class<Connector> getType() {
                return Connector.class;
            }
        }, (c, n) -> new Connector.Builder().createFrom(c).name(c.getName()).build(), new HashMap<>(), result);

        importModels(new JsonDbDao<Connection>(given) {
            @Override
            public Class<Connection> getType() {
                return Connection.class;
            }
        }, RENAME_CONNECTION, renamedIds, result);

        importIntegrations(sec, new JsonDbDao<Integration>(given) {
            @Override
            public Class<Integration> getType() {
                return Integration.class;
            }
        }, renamedIds, result);


        return result;
    }

    private void importIntegrations(SecurityContext sec, JsonDbDao<Integration> export, Map<String, String> renamedIds, List<ChangeEvent> result) {
        for (Integration integration : export.fetchAll().getItems()) {
            Integration.Builder builder = new Integration.Builder()
                .createFrom(integration)
                .isDeleted(false)
                .updatedAt(System.currentTimeMillis());

            // Do we need to create it?
            String id = integration.getId().get();
            Integration previous = dataManager.fetch(Integration.class, id);
            resolveDuplicateNames(integration, builder, renamedIds);
            if (previous == null) {
                LOG.info("Creating integration: {}", integration.getName());
                integrationHandler.create(sec, builder.build());
                result.add(ChangeEvent.of("created", integration.getKind().getModelName(), id));
            } else {
                LOG.info("Updating integration: {}", integration.getName());
                integrationHandler.update(id, builder.version(previous.getVersion()+1).build());
                result.add(ChangeEvent.of("updated", integration.getKind().getModelName(), id));
            }
        }
    }

    private void resolveDuplicateNames(Integration integration, Integration.Builder builder, Map<String, String> renamedIds) {

        // check for duplicate integration name
        String integrationName = integration.getName();
        final Set<String> names = getAllPropertyValues(Integration.class, Integration::getName, i -> !i.isDeleted());
        names.addAll(getAllPropertyValues(IntegrationDeployment.class, d -> d.getSpec().getName(), d -> !d.getSpec().isDeleted()));
        if (names.contains(integrationName)) {
            integrationName = getNextAvailableName(integrationName, names);
            builder.name(integrationName);
        }

        // sync renames of other objects
        // connections
        builder.connections(integration.getConnections().stream()
                .map(c -> renameIfNeeded(c, renamedIds, RENAME_CONNECTION))
                .collect(Collectors.toList()));

        // steps
        builder.steps(integration.getSteps().stream()
                .map(s -> new Step.Builder().createFrom(s)
                        // step connections
                        .connection(s.getConnection().map(c -> renameIfNeeded(c, renamedIds, RENAME_CONNECTION)))
                        // step extensions
                        .extension(s.getExtension().map(e -> renameIfNeeded(e, renamedIds, RENAME_EXTENSION)))
                        .build())
                .collect(Collectors.toList()));
    }

    private <T extends WithId<T> & WithName> T renameIfNeeded(T model, Map<String, String> renames, BiFunction<T,
            String, T> nameFunc) {
        final String name = renames.get(model.getId().get());
        return name != null ? nameFunc.apply(model, name) : model;
    }

    private <T extends WithId<T>> Set<String> getAllPropertyValues(Class<T> model, Function<T, String> propertyFunc) {
        return getAllPropertyValues(model, propertyFunc, d -> true);
    }

    private <T extends WithId<T>> Set<String> getAllPropertyValues(Class<T> model, Function<T, String> propertyFunc, Function<T, Boolean> filterFunc) {
        final ListResult<T> deployments = dataManager.fetchAll(model);
        return deployments.getItems().stream()
                .filter(d -> filterFunc.apply(d))
                .map(d -> propertyFunc.apply(d))
                .collect(Collectors.toSet());
    }

    private String getNextAvailableName(String name, Set<String> names) {
        String newName = null;
        for (int i = 1; newName == null; i++) {
            final String candidate = name + IMPORTED_SUFFIX + i;
            if (!names.contains(candidate)) {
                newName = candidate;
            }
        }
        return newName;
    }

    private <T extends WithId<T> & WithName> void importModels(JsonDbDao<T> export, BiFunction<T, String, T> renameFunc,
                                                               Map<String, String> renames, List<ChangeEvent> result) {
        final Set<String> names = getAllPropertyValues(export.getType(), WithName::getName);
        for (T item : export.fetchAll().getItems()) {
            Kind kind = item.getKind();
            String id = item.getId().get();
            if (dataManager.fetch(export.getType(), id) == null) {

                // resolve duplicate names
                String name = item.getName();
                if (names.contains(name)) {

                    // rename item
                    name = getNextAvailableName(name, names);
                    item = renameFunc.apply(item, name);

                    names.add(name);
                    renames.put(id, name);
                }

                // create new item
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
