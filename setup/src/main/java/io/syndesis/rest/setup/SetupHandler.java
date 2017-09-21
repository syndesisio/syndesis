/*
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
package io.syndesis.rest.setup;

import io.swagger.annotations.Api;
import io.syndesis.rest.setup.keycloak.KeycloakProperties;
import io.syndesis.rest.setup.model.SetupConfigurationRequest;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.IdentityProviderResource;
import org.keycloak.representations.idm.IdentityProviderRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/")
@Api(value = "setup")
@Component
public class SetupHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SetupHandler.class);

    private Keycloak keycloakAdminClient;
    private KeycloakProperties keycloakProperties;

    public SetupHandler(final Keycloak keycloakAdminClient, final KeycloakProperties keycloakProperties) {
        this.keycloakAdminClient = keycloakAdminClient;
        this.keycloakProperties = keycloakProperties;
    }

    @GET
    public Response get() {
        IdentityProviderRepresentation idp = getGitHubIdentityProvider();

        if (idp == null) {
            LOG.error("Missing GitHub identity provider");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        if (isClientIdUnset(idp)) {
            return Response.status(Response.Status.GONE).build();
        }

        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @PUT
    @Consumes("application/json")
    public Response update(@NotNull @Valid SetupConfigurationRequest setupConfiguration) {
        IdentityProviderResource identityProviderResource = getGitHubIdentityProviderResource();

        if (identityProviderResource == null) {
            LOG.error("Missing GitHub identity provider");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        IdentityProviderRepresentation idp = identityProviderResource.toRepresentation();
        if (isClientIdUnset(idp)) {
            return Response.status(Response.Status.GONE).build();
        }

        idp.getConfig().put("clientId", setupConfiguration.getGitHubOAuthConfiguration().getClientId());
        idp.getConfig().put("clientSecret", setupConfiguration.getGitHubOAuthConfiguration().getClientSecret());

        identityProviderResource.update(idp);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    private IdentityProviderResource getGitHubIdentityProviderResource() {
        return keycloakAdminClient.realm(keycloakProperties.getSyndesisRealm()).identityProviders().get(keycloakProperties.getGitHubIdentityProviderId());
    }

    private RealmRepresentation getRealm() {
        return keycloakAdminClient.realm(keycloakProperties.getSyndesisRealm()).toRepresentation();
    }

    private IdentityProviderRepresentation getGitHubIdentityProvider() {
        RealmRepresentation realm = getRealm();
        return getGitHubIdentityProviderFromRealm(realm);
    }

    private IdentityProviderRepresentation getGitHubIdentityProviderFromRealm(RealmRepresentation realm) {
        for (IdentityProviderRepresentation idp : realm.getIdentityProviders()) {
            if (keycloakProperties.getGitHubIdentityProviderId().equals(idp.getProviderId())) {
                return idp;
            }
        }

        return null;
    }

    private boolean isClientIdUnset(IdentityProviderRepresentation idp) {
        return !keycloakProperties.getGithubIdentityProviderUnsetClientId().equals(idp.getConfig().get("clientId"));
    }

}
