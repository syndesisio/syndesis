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
package io.syndesis.rest.v1.handler.integration.support;

import static io.syndesis.rest.v1.handler.integration.IntegrationHandler.addEntry;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
import io.syndesis.core.Json;
import io.syndesis.core.Names;
import io.syndesis.dao.file.FileDataManager;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.dao.manager.operators.IdPrefixFilter;
import io.syndesis.dao.manager.operators.ReverseFilter;
import io.syndesis.integration.api.IntegrationProjectGenerator;
import io.syndesis.integration.api.IntegrationResourceManager;
import io.syndesis.model.ChangeEvent;
import io.syndesis.model.Dependency;
import io.syndesis.model.Kind;
import io.syndesis.model.ListResult;
import io.syndesis.model.ModelData;
import io.syndesis.model.ModelExport;
import io.syndesis.model.Schema;
import io.syndesis.model.buletin.IntegrationBulletinBoard;
import io.syndesis.model.connection.Connection;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.extension.Extension;
import io.syndesis.model.integration.Integration;
import io.syndesis.model.integration.IntegrationDeployment;
import io.syndesis.model.integration.Step;
import io.syndesis.rest.util.PaginationFilter;
import io.syndesis.rest.util.ReflectiveSorter;
import io.syndesis.rest.v1.handler.connection.ConnectionHandler;
import io.syndesis.rest.v1.handler.integration.DeletedFilter;
import io.syndesis.rest.v1.handler.integration.IntegrationHandler;
import io.syndesis.rest.v1.handler.integration.model.IntegrationOverview;
import io.syndesis.rest.v1.operations.PaginationOptionsFromQueryParams;
import io.syndesis.rest.v1.operations.SortOptionsFromQueryParams;

@Path("/integration-support")
@Api(value = "integration-support")
@Component
@SuppressWarnings({ "PMD.ExcessiveImports", "PMD.GodClass", "PMD.StdCyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.CyclomaticComplexity" })
public class IntegrationSupportHandler {

    public static final String EXPORT_MODEL_FILE_NAME = "model.json";
    private static final Logger LOG = LoggerFactory.getLogger(IntegrationSupportHandler.class);

    private final IntegrationProjectGenerator projectGenerator;
    private final DataManager dataManager;
    private final IntegrationResourceManager resourceManager;
    private final IntegrationHandler integrationHandler;
    private final ConnectionHandler connectionHandler;
    private final FileDataManager extensionDataManager;

    public IntegrationSupportHandler(
        final IntegrationProjectGenerator projectGenerator,
        final DataManager dataManager,
        final IntegrationResourceManager resourceManager,
        final IntegrationHandler integrationHandler,
        final ConnectionHandler connectionHandler,
        final FileDataManager extensionDataManager) {

        this.projectGenerator = projectGenerator;
        this.dataManager = dataManager;
        this.resourceManager = resourceManager;
        this.integrationHandler = integrationHandler;
        this.connectionHandler = connectionHandler;
        this.extensionDataManager = extensionDataManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/overviews")
    public ListResult<IntegrationOverview> getOverviews(@Context  UriInfo uriInfo) {

        Stream<Integration> stream = getDataManager().fetchAll(Integration.class,
            new DeletedFilter(),
            new ReflectiveSorter<>(Integration.class, new SortOptionsFromQueryParams(uriInfo)),
            new PaginationFilter<>(new PaginationOptionsFromQueryParams(uriInfo))
        ).getItems().stream();

        return ListResult.of(stream.map(integration -> {

            String id = integration.getId().get();
            List<IntegrationDeployment> deployments = getDataManager().fetchAll(IntegrationDeployment.class,
                new IdPrefixFilter<>(id +":"), ReverseFilter.getInstance())
                .getItems();

            Optional<IntegrationBulletinBoard> bulletins = Optional
                .ofNullable(getDataManager().fetch(IntegrationBulletinBoard.class, id));

            // find the deployment we want published..
            return new IntegrationOverview(integration, bulletins, deployments.stream().findFirst());
        }).collect(Collectors.toList()));
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
    public StreamingOutput export(@NotNull @QueryParam("id") @ApiParam(required = true) List<String> requestedIds) {

        List<String> ids = requestedIds;
        if ( ids ==null || ids.isEmpty() ) {
            throw new ClientErrorException("No 'integration' query parameter specified.", Response.Status.BAD_REQUEST);
        }

        LinkedHashMap<String, ModelData<?>> export = new LinkedHashMap<>();
        LinkedHashSet<String> extensions = new LinkedHashSet<>();

        if( ids.contains("all") ) {
            ids = new ArrayList<>();
            for (Integration integration : dataManager.fetchAll(Integration.class).getItems()) {
                ids.add(integration.getId().get());
            }
        }
        for (String id : ids) {
            Integration integration = integrationHandler.get(id);
            addToExport(export, integration);
            resourceManager.collectDependencies(integration.getSteps()).stream()
                .filter(Dependency::isExtension)
                .map(Dependency::getId)
                .forEach(extensions::add);
        }

        LOG.debug("Extensions: {}", extensions);

        ArrayList<ModelData<?>> models = new ArrayList<>(export.values());
        return out -> {
            try (ZipOutputStream tos = new ZipOutputStream(out) ) {
                ModelExport exportObject = ModelExport.of(Schema.VERSION, models);
                addEntry(tos, EXPORT_MODEL_FILE_NAME, Json.writer().writeValueAsBytes(exportObject));
                for (String extensionId : extensions) {
                    addEntry(tos, "extensions/" + Names.sanitize(extensionId) + ".jar", IOUtils.toByteArray(
                        extensionDataManager.getExtensionBinaryFile(extensionId)
                    ));
                }
            }
        };
    }

    private void addToExport(Map<String, ModelData<?>> export, Integration integration) {
        addToExport(export, new ModelData<>(Kind.Integration, integration));
        for (Step step : integration.getSteps()) {
            Optional<Connection> c = step.getConnection();
            if (c.isPresent()) {
                Connection connection = c.get();
                addToExport(export, new ModelData<>(Kind.Connection, connection));
                Connector connector = integrationHandler.getDataManager().fetch(Connector.class, connection.getConnectorId().get());
                if (connector != null) {
                    addToExport(export, new ModelData<>(Kind.Connector, connector));
                }
            }
            Optional<Extension> e = step.getExtension();
            if (e.isPresent()) {
                Extension extension = e.get();
                addToExport(export, new ModelData<>(Kind.Extension, extension));
            }
        }
    }

    private static void addToExport(Map<String, ModelData<?>> export, ModelData<?> model) {
        try {
            String key = model.getKind().getModelName()+":"+model.getData().getId();
            if ( !export.containsKey(key) )  {
                export.put(key, model);
            }
        } catch (IOException e) {
            // This should not no occur since the model does not need to be deserialized
            throw new IllegalStateException(e);
        }
    }

    @POST
    @Path("/import")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ChangeEvent> importIntegration(@Context SecurityContext sec, InputStream is) {
        try (ZipInputStream zis = new ZipInputStream(is)) {
            HashSet<String> extensionIds = new HashSet<>();
            ArrayList<ChangeEvent> changeEvents = new ArrayList<>();
            while (true) {
                ZipEntry entry = zis.getNextEntry();
                if( entry == null ) {
                    break;
                }
                if (EXPORT_MODEL_FILE_NAME.equals(entry.getName())) {
                    ModelExport models = Json.reader().forType(ModelExport.class).readValue(zis);
                    changeEvents.addAll(importModels(sec, models));
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

    public List<ChangeEvent> importModels(SecurityContext sec, ModelExport export) throws IOException {
        ArrayList<ChangeEvent> result = new ArrayList<>();
        validateForImport(export);

        // Now do the actual import.
        for (ModelData<?> model : export.models()) {
            switch (model.getKind()) {
                case Integration: {
                    Integration integration = (Integration) model.getData();
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
                    integrationHandler.updateBulletinBoard(id);
                    break;
                }
                case Connection: {

                    // We only create connections, never update.
                    Connection connection = (Connection) model.getData();
                    String id = connection.getId().get();
                    if (dataManager.fetch(Connection.class, id) == null) {
                        dataManager.create(connection);
                        connectionHandler.updateBulletinBoard(id);
                        result.add(ChangeEvent.of("created", connection.getKind().getModelName(), id));
                    }

                    break;
                }
                case Connector: {

                    // We only create connectors, never update.
                    Connector connector = (Connector) model.getData();
                    String id = connector.getId().get();
                    if (dataManager.fetch(Connector.class, id) == null) {
                        dataManager.create(connector);
                        result.add(ChangeEvent.of("created", connector.getKind().getModelName(), id));
                    }
                    break;
                }
                case Extension: {

                    // We only create extensions, never update.
                    Extension extension = (Extension) model.getData();
                    String id = extension.getId().get();
                    if (dataManager.fetch(Extension.class, id) == null) {
                        dataManager.create(extension);
                        result.add(ChangeEvent.of("created", extension.getKind().getModelName(), id));
                    }
                    break;
                }
                default: {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Cannot import unsupported model kind: " + model.getKind());
                    }
                    break;
                }
            }
        }
        return result;
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
                case Extension:
                    break;
                default:
                    throw new IOException("Cannot import unsupported model kind: " + model.getKind());

            }
        }
    }
}
