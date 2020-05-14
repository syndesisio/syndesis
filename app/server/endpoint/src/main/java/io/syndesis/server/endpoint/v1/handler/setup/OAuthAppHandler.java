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
package io.syndesis.server.endpoint.v1.handler.setup;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.server.credential.Credentials;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.endpoint.util.PaginationFilter;
import io.syndesis.server.endpoint.v1.operations.PaginationOptionsFromQueryParams;
import org.springframework.stereotype.Component;

/**
 * This rest endpoint handles working with global oauth settings.
 */
@Path("/setup/oauth-apps")
@Tag(name = "oauth-apps")
@Component
public class OAuthAppHandler {

    private final DataManager dataMgr;

    public OAuthAppHandler(final DataManager dataMgr) {
        this.dataMgr = dataMgr;
    }

    @DELETE
    @Consumes("application/json")
    @Path(value = "/{id}")
    public void delete(@NotNull @PathParam("id") @Parameter(required = true) final String id) {
        final Connector connector = dataMgr.fetch(Connector.class, id);

        update(id, OAuthApp.fromConnector(connector).clearValues());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/{id}")
    public OAuthApp get(@NotNull @PathParam("id") @Parameter(required = true) final String id) {

        final Connector connector = dataMgr.fetch(Connector.class, id);
        if (connector == null || !isOauthConnector(connector)) {
            throw new EntityNotFoundException();
        }

        return OAuthApp.fromConnector(connector);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ListResult<OAuthApp> list(
        @Parameter(required = false, description = "Page number to return") @QueryParam("page") @DefaultValue("1") int page,
        @Parameter(required = false, description = "Number of records per page") @QueryParam("per_page") @DefaultValue("20") int perPage
    ) {
        final List<Connector> oauthConnectors = dataMgr.fetchAll(Connector.class, //
            OAuthConnectorFilter.INSTANCE,
            new PaginationFilter<>(new PaginationOptionsFromQueryParams(page, perPage))
        ).getItems();

        final List<OAuthApp> apps = oauthConnectors.stream().map(OAuthApp::fromConnector).collect(Collectors.toList());
        return ListResult.of(apps);
    }

    @PUT
    @Path(value = "/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void update(@NotNull @PathParam("id") final String id, @NotNull @Valid final OAuthApp app) {
        final Connector connector = dataMgr.fetch(Connector.class, id);
        if (connector == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        final Connector updated = app.update(connector);

        dataMgr.update(updated);

        dataMgr.fetchAllByPropertyValue(Connection.class, "connectorId", id)
            .forEach(connection -> toggleDerived(connection, app.isDerived()));
    }

    private void toggleDerived(final Connection connection, final boolean newDerived) {
        final Connection underived = new Connection.Builder().createFrom(connection).isDerived(newDerived).build();

        dataMgr.update(underived);
    }

    private static boolean isOauthConnector(final Connector connector) {
        return connector.getProperties().values().stream().anyMatch(x -> {
            return x.getTags().contains(Credentials.CLIENT_ID_TAG);
        });
    }

}
