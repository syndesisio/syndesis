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

import io.syndesis.common.model.connection.Connector;
import io.syndesis.server.credential.Credentials;

public final class OAuthApp {

    private String authorizationUrl;

    private String clientId;

    private String clientSecret;

    private String icon;

    private String id;

    private String name;

    private String scopes;

    private String tokenUrl;

    public OAuthApp() {
    }

    OAuthApp(final Connector connector) {
        id = connector.getId().get();
        name = connector.getName();
        icon = connector.getIcon();
        clientId = connector.propertyTaggedWith(Credentials.CLIENT_ID_TAG).orElse(null);
        clientSecret = connector.propertyTaggedWith(Credentials.CLIENT_SECRET_TAG).orElse(null);
        authorizationUrl = connector.propertyTaggedWith(Credentials.AUTHORIZATION_URL_TAG).orElse(null);
        tokenUrl = connector.propertyTaggedWith(Credentials.ACCESS_TOKEN_URL_TAG).orElse(null);
        scopes = connector.propertyTaggedWith(Credentials.SCOPE_TAG).orElse(null);
    }

    public String getAuthorizationUrl() {
        return authorizationUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getIcon() {
        return icon;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getScopes() {
        return scopes;
    }

    public String getTokenUrl() {
        return tokenUrl;
    }

    public void setAuthorizationUrl(final String authorizationUrl) {
        this.authorizationUrl = authorizationUrl;
    }

    public void setClientId(final String clientId) {
        this.clientId = clientId;
    }

    public void setClientSecret(final String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public void setIcon(final String icon) {
        this.icon = icon;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setScopes(final String scopes) {
        this.scopes = scopes;
    }

    public void setTokenUrl(final String tokenUrl) {
        this.tokenUrl = tokenUrl;
    }

}
