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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.syndesis.common.model.api.APISummary;
import io.syndesis.server.credential.Credentials;
import io.syndesis.server.dao.file.FileDataManager;
import io.syndesis.server.dao.file.IconDao;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.dao.manager.EncryptionComponent;
import io.syndesis.server.inspector.Inspectors;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.filter.FilterOptions;
import io.syndesis.common.model.filter.Op;
import io.syndesis.common.model.icon.Icon;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.server.endpoint.v1.handler.BaseHandler;
import io.syndesis.server.endpoint.v1.operations.Deleter;
import io.syndesis.server.endpoint.v1.operations.Getter;
import io.syndesis.server.endpoint.v1.operations.Lister;
import io.syndesis.server.endpoint.v1.operations.Updater;
import io.syndesis.server.endpoint.v1.state.ClientSideState;
import io.syndesis.server.verifier.Verifier;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Path("/connectors")
@Api(value = "connectors")
@Component
public class ConnectorHandler extends BaseHandler implements Lister<Connector>, Getter<Connector>, Updater<Connector>, Deleter<Connector> {

    private final ApplicationContext applicationContext;
    private final IconDao iconDao;
    private final FileDataManager extensionDataManager;
    private final Credentials credentials;
    private final EncryptionComponent encryptionComponent;
    private final Inspectors inspectors;
    private final ClientSideState state;
    private final Verifier verifier;

    public ConnectorHandler(final DataManager dataMgr, final Verifier verifier, final Credentials credentials, final Inspectors inspectors,
                            final ClientSideState state, final EncryptionComponent encryptionComponent, final ApplicationContext applicationContext,
                            final IconDao iconDao, final FileDataManager extensionDataManager) {
        super(dataMgr);
        this.verifier = verifier;
        this.credentials = credentials;
        this.inspectors = inspectors;
        this.state = state;
        this.encryptionComponent = encryptionComponent;
        this.applicationContext = applicationContext;
        this.iconDao = iconDao;
        this.extensionDataManager = extensionDataManager;
    }

    @Path("/{id}/credentials")
    public ConnectorCredentialHandler credentials(@NotNull final @PathParam("id") String connectorId) {
        return new ConnectorCredentialHandler(credentials, state, connectorId);
    }

    @Path("/custom")
    public CustomConnectorHandler customConnectorHandler() {
        return new CustomConnectorHandler(getDataManager(), applicationContext, iconDao);
    }

    @Override
    public void delete(final String id) {
        Deleter.super.delete(id);

        getDataManager().fetchIdsByPropertyValue(Connection.class, "connectorId", id)
            .forEach(connectionId -> getDataManager().delete(Connection.class, connectionId));
    }

    @Override
    public Connector get(final String id) {
        final Connector connector = augmentedWithUsage(Getter.super.get(id));

        final Optional<String> connectorGroupId = connector.getConnectorGroupId();
        if (!connectorGroupId.map(applicationContext::containsBean).orElse(false)) {
            return connector;
        }

        final APISummary summary = new APISummary.Builder().createFrom(connector).build();

        return connector.builder().actionsSummary(summary.getActionsSummary()).build();
    }

    @Path("/{id}/actions")
    public ConnectorActionHandler getActions(@PathParam("id") final String connectorId) {
        return new ConnectorActionHandler(getDataManager(), connectorId);
    }

    @Path("/{id}/icon")
    public ConnectorIconHandler getConnectorIcon(@NotNull @PathParam("id") final String connectorId) {
        final Connector connector = get(connectorId);

        return new ConnectorIconHandler(getDataManager(), connector, iconDao, extensionDataManager);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/{connectorId}/actions/{actionId}/filters/options")
    public FilterOptions getFilterOptions(@PathParam("connectorId") @ApiParam(required = true) final String connectorId,
                                          @PathParam("actionId") @ApiParam(required = true) final String actionId) {
        final FilterOptions.Builder builder = new FilterOptions.Builder().addOps(Op.DEFAULT_OPTS);
        final Connector connector = getDataManager().fetch(Connector.class, connectorId);

        if (connector == null) {
            return builder.build();
        }

        connector.findActionById(actionId).filter(ConnectorAction.class::isInstance).map(ConnectorAction.class::cast).ifPresent(action -> {
            action.getOutputDataShape().ifPresent(dataShape -> {
                final List<String> paths = inspectors.getPaths(dataShape.getKind().toString(), dataShape.getType(), dataShape.getSpecification(),
                    dataShape.getExemplar());
                builder.addAllPaths(paths);
            });
        });
        return builder.build();
    }

    @Override
    public ListResult<Connector> list(final UriInfo uriInfo) {
        final List<Connector> connectors = Lister.super.list(uriInfo).getItems().stream()
            .map(c -> {
                final APISummary summary = new APISummary.Builder().createFrom(c).build();

                return c.builder().actionsSummary(summary.getActionsSummary()).build();
            })
            .collect(Collectors.toList());

        return ListResult.of(augmentedWithUsage(connectors));
    }

    @Override
    public Kind resourceKind() {
        return Kind.Connector;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{id}/verifier")
    public List<Verifier.Result> verifyConnectionParameters(@NotNull @PathParam("id") final String connectorId, final Map<String, String> props) {
        return verifier.verify(connectorId, encryptionComponent.decrypt(props));
    }

    Connector augmentedWithUsage(final Connector connector) {
        if (connector == null) {
            return null;
        }

        return augmentedWithUsage(Collections.singletonList(connector)).get(0);
    }

    List<Connector> augmentedWithUsage(final List<Connector> connectors) {
        ListResult<Integration> integrationListResult = getDataManager().fetchAll(Integration.class);
        List<Integration> items = integrationListResult.getItems();
        final Map<String, Long> connectorUsage = items.stream()//
            .filter(i -> !i.isDeleted())//
            .flatMap(i -> i.getUsedConnectorIds().stream())//
            .collect(Collectors.groupingBy(String::toString, Collectors.counting()));

        return connectors.stream().map(c -> {
            final int uses = connectorUsage.getOrDefault(c.getId().get(), 0L).intValue();
            return c.builder().uses(uses).build();
        }).collect(Collectors.toList());
    }

    @PUT
    @Path(value = "/{id}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public void update(@NotNull @PathParam("id") @ApiParam(required = true) String id, @MultipartForm ConnectorFormData connectorFormData) {
        if (connectorFormData.getConnector() == null) {
            throw new IllegalArgumentException("Missing connector parameter");
        }

        Connector connectorToUpdate = connectorFormData.getConnector();

        if (connectorFormData.getIconInputStream() != null) {
            try(BufferedInputStream iconStream = new BufferedInputStream(connectorFormData.getIconInputStream())) {
                // URLConnection.guessContentTypeFromStream resets the stream after inspecting the media type so
                // can continue to be used, rather than being consumed.
                String guessedMediaType = URLConnection.guessContentTypeFromStream(iconStream);
                if (!guessedMediaType.startsWith("image/")) {
                    throw new IllegalArgumentException("Invalid file contents for an image");
                }
                MediaType mediaType = MediaType.valueOf(guessedMediaType);
                Icon.Builder iconBuilder = new Icon.Builder().mediaType(mediaType.toString());

                Icon icon = getDataManager().create(iconBuilder.build());
                iconDao.write(icon.getId().get(), iconStream);

                final String oldIconId = connectorToUpdate.getIcon();
                if (oldIconId.toLowerCase(Locale.US).startsWith("db:")) {
                    iconDao.delete(oldIconId.substring(3));
                }
                connectorToUpdate = connectorToUpdate.builder().icon("db:" + icon.getId().get()).build();
            } catch (IOException e) {
                throw new IllegalArgumentException("Error while reading multipart request", e);
            }
        }

        getDataManager().update(connectorToUpdate);
    }

    public static class ConnectorFormData {
        @FormParam("connector")
        private Connector connector;

        @FormParam("icon")
        private InputStream iconInputStream;

        public ConnectorFormData() {
            // allow JAX-RS to construct this class for binding
        }

        public ConnectorFormData(Connector connector, InputStream iconInputStream) {
            this.connector = connector;
            this.iconInputStream = iconInputStream;
        }

        public Connector getConnector() {
            return connector;
        }

        public void setConnector(Connector connector) {
            this.connector = connector;
        }

        public InputStream getIconInputStream() {
            return iconInputStream;
        }

        public void setIconInputStream(InputStream iconInputStream) {
            this.iconInputStream = iconInputStream;
        }
    }
}
