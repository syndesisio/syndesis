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
package io.syndesis.connector.rest.swagger;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.camel.Endpoint;
import org.apache.camel.component.connector.DefaultConnectorComponent;
import org.apache.camel.component.connector.DefaultConnectorEndpoint;

public final class SwaggerConnectorComponent extends DefaultConnectorComponent {

    private String accessToken;

    private Long accessTokenExpiresAt;

    private AuthenticationType authenticationType;

    private boolean authorizeUsingParameters;

    private String clientId;

    private String clientSecret;

    private String password;

    private String refreshToken;

    private Set<Integer> refreshTokenRetryStatuses = Collections.emptySet();

    private String specification;

    private String tokenEndpoint;

    private String username;

    public SwaggerConnectorComponent() {
        this(null);
    }

    public SwaggerConnectorComponent(final String componentSchema) {
        super("swagger-connector", componentSchema, SwaggerConnectorComponent.class);
    }

    public String getAccessToken() {
        return accessToken;
    }

    public Long getAccessTokenExpiresAt() {
        return accessTokenExpiresAt;
    }

    public AuthenticationType getAuthenticationType() {
        return authenticationType;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getPassword() {
        return password;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getRefreshTokenRetryStatuses() {
        return String.join(",", refreshTokenRetryStatuses.stream().map(i -> i.toString()).collect(Collectors.toSet()));
    }

    public Set<Integer> getRefreshTokenRetryStatusesSet() {
        return refreshTokenRetryStatuses;
    }

    public String getSpecification() {
        return specification;
    }

    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public String getUsername() {
        return username;
    }

    public boolean isAuthorizeUsingParameters() {
        return authorizeUsingParameters;
    }

    public void setAccessToken(final String accessToken) {
        this.accessToken = accessToken;
    }

    public void setAccessTokenExpiresAt(final Long accessTokenExpiresAt) {
        this.accessTokenExpiresAt = accessTokenExpiresAt;
    }

    public void setAuthenticationType(final AuthenticationType authenticationType) {
        this.authenticationType = authenticationType;
    }

    public void setAuthorizeUsingParameters(final boolean useParametersForClientCredentials) {
        authorizeUsingParameters = useParametersForClientCredentials;
    }

    public void setClientId(final String clientId) {
        this.clientId = clientId;
    }

    public void setClientSecret(final String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public void setRefreshToken(final String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setRefreshTokenRetryStatuses(final String refreshTokenRetryStatuses) {
        if (refreshTokenRetryStatuses == null) {
            this.refreshTokenRetryStatuses = Collections.emptySet();
        } else {
            this.refreshTokenRetryStatuses = Arrays.asList(refreshTokenRetryStatuses.split("\\s*,\\s*")).stream()//
                .map(String::trim)//
                .filter(s -> !s.isEmpty())//
                .map(Integer::valueOf)//
                .collect(Collectors.toSet());
        }
    }

    public void setSpecification(final String specification) {
        this.specification = specification;
    }

    public void setTokenEndpoint(final String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    @Override
    protected Endpoint createEndpoint(final String uri, final String remaining, final Map<String, Object> parameters) throws Exception {
        final DefaultConnectorEndpoint endpoint = null;

        if (authenticationType == AuthenticationType.oauth2 && refreshToken != null && !refreshTokenRetryStatuses.isEmpty()) {
            return new OAuthRefreshingEndpoint(endpoint, this);
        }

        return endpoint;
    }
}
