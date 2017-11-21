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
package io.syndesis.rest.v1.handler.setup;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.syndesis.core.SuppressFBWarnings;
import io.syndesis.credential.Credentials;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.model.connection.Connector;

import org.springframework.boot.autoconfigure.social.SocialProperties;
import org.springframework.stereotype.Component;

/**
 * This rest endpoint handles working with global oauth settings.
 */
@Path("/setup/oauth-apps")
@Api(value = "oauth-apps")
@Component
public class OAuthAppHandler {

    private final DataManager dataMgr;

    public OAuthAppHandler(DataManager dataMgr) {
        this.dataMgr = dataMgr;
    }

    // Since this a a view model DTO, and not a domain model lets define it here instead
    // of placing it into the model module.
    @SuppressFBWarnings(
        value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD",
        justification = "All fields are encode by jackson and sent to the UI")
    public static final class OAuthApp extends SocialProperties {
        public String id;
        public String name;
        public String icon;
        public String clientId;
        public String clientSecret;

        @Override
        @JsonIgnore
        public String getAppId() {
            return clientId;
        }

        @Override
        @JsonIgnore
        public String getAppSecret() {
            return clientSecret;
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "")
    public List<OAuthApp> get() {
        ArrayList<OAuthApp> apps = new ArrayList<>();

        List<Connector> items = dataMgr.fetchAll(Connector.class).getItems();
        items.forEach(connector -> {
            if (isOauthConnector(connector)) {
                apps.add(createOAuthApp(connector));
            }
        });

        return apps;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/{id}")
    public OAuthApp get(@PathParam("id") @ApiParam(required = true) String id) {

        Connector connector = dataMgr.fetch(Connector.class, id);
        if( connector == null ) {
            throw new EntityNotFoundException();
        }
        if (isOauthConnector(connector)) {
            return createOAuthApp(connector);
        }

        throw new EntityNotFoundException();
    }

    private static OAuthApp createOAuthApp(Connector connector) {
        OAuthApp app = new OAuthApp();
        app.id = connector.getId().get();
        app.name = connector.getName();
        app.icon = connector.getIcon();
        app.clientId = connector.propertyTaggedWith(Credentials.CLIENT_ID_TAG).orElse(null);
        app.clientSecret = connector.propertyTaggedWith(Credentials.CLIENT_SECRET_TAG).orElse(null);
        return app;
    }

    @PUT
    @Path(value = "/{id}")
    @Consumes("application/json")
    public void update(@NotNull @PathParam("id") String id, @NotNull @Valid OAuthApp app) {
        final Connector connector = dataMgr.fetch(Connector.class, id);
        if (connector == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        final Connector updated = new Connector.Builder().createFrom(connector)
            .putOrRemoveConfiguredPropertyTaggedWith(Credentials.CLIENT_ID_TAG, app.clientId)
            .putOrRemoveConfiguredPropertyTaggedWith(Credentials.CLIENT_SECRET_TAG, app.clientSecret)
            .build();

        dataMgr.update(updated);
    }

    private static boolean isOauthConnector(Connector connector) {
        return connector.getProperties().values().stream().anyMatch(x -> {
                return x.getTags().contains(Credentials.CLIENT_ID_TAG);
            }
        );
    }

}
