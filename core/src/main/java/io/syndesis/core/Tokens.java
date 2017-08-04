/**
 * Copyright (C) 2016 Red Hat, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.keycloak.TokenVerifier;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;

public final class Tokens {

    private static final ThreadLocal<String> OAUTH_TOKEN = new InheritableThreadLocal<>();

    private Tokens() {
        // utility class
    }

    public static String getAuthenticationToken() {
        String stringToken = OAUTH_TOKEN.get();
        if (stringToken != null) {
            return stringToken;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("Cannot set authorization header because there is no authenticated principal");
        }
        if (!KeycloakAuthenticationToken.class.isAssignableFrom(authentication.getClass())) {
            throw new IllegalStateException(String.format("Cannot set authorization header because Authentication is of type %s but %s is required",
                authentication.getClass(), KeycloakAuthenticationToken.class));
        }

        KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) authentication;
        return token.getAccount().getKeycloakSecurityContext().getTokenString();
    }

    public static boolean isTokenExpired(String token) {
        TokenVerifier verifier = TokenVerifier.create(token);
        try {
            return verifier.getToken().isExpired();
        } catch (VerificationException e) {
            return true;
        }
    }

    private static String getIssuer(String token) {
        TokenVerifier verifier = TokenVerifier.create(token);
        try {
            return verifier.getToken().getIssuer();
        } catch (VerificationException e) {
            throw SyndesisServerException.launderThrowable(e);
        }
    }

    public static String getProviderToken(String providerId) {
        String keycloakTokenAsString = getAuthenticationToken();

        String issuer = getIssuer(keycloakTokenAsString);

        String tokenEndpointUrl = issuer + "/broker/" + providerId + "/token";
        final String authHeader = "Bearer " + keycloakTokenAsString;

        ClientRequestFilter authFilter = (requestContext) -> requestContext.getHeaders().add(HttpHeaders.AUTHORIZATION, authHeader);
        Client client = ClientBuilder.newBuilder().register(authFilter).build();
        UriBuilder authBase = UriBuilder.fromUri(tokenEndpointUrl);
        WebTarget tokenEndpoint = client.target(authBase);
        Response response = tokenEndpoint.request().get();

        String responseBody = response.readEntity(String.class);

        int status = response.getStatus();
        if (status != 200) {
            throw new IllegalStateException(
                String.format(
                    "Unable to retrieve token for provider %s from URL %s, status code %d, received body: %s",
                    providerId,
                    tokenEndpointUrl,
                    status,
                    responseBody
                )
            );
        }

        String contentTypeHeader = response.getHeaderString("Content-Type");
        if (!"application/json".equals(contentTypeHeader)) {
            throw new IllegalStateException(
                String.format(
                    "Unable to retrieve token for provider %s from URL %s, expected Content-Type application/json, received %s",
                    providerId,
                    tokenEndpointUrl,
                    contentTypeHeader
                )
            );
        }

        ObjectMapper om = new ObjectMapper();
        try {
            AccessTokenResponse accessToken = om.readValue(responseBody, AccessTokenResponse.class);
            return accessToken.getToken();
        } catch (IOException e) {
            throw SyndesisServerException.launderThrowable(e);
        }
    }

    public static void setAuthenticationToken(String token) {
        OAUTH_TOKEN.set(token);
    }

}
