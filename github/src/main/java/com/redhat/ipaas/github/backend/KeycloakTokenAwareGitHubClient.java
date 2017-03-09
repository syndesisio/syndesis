/*
 * Copyright (C) 2017 Red Hat, Inc.
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

package com.redhat.ipaas.github.backend;

import java.net.HttpURLConnection;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * GitHub client which sets a KeyCloak authentication token
 *
 * Note that this client needs to be recreated afresh for request, as the tokens
 * are different for each request.
 *
 * @author roland
 * @since 09/03/2017
 */
public class KeycloakTokenAwareGitHubClient extends GitHubClient {

    public KeycloakTokenAwareGitHubClient(String hostname) {
        super(hostname);
    }

    @Override
    protected String configureUri(final String uri) {
        return uri;
	}

    @Override
    protected HttpURLConnection configureRequest(final HttpURLConnection request) {
        super.configureRequest(request);
        request.setRequestProperty(HEADER_AUTHORIZATION, "Bearer " + getAuthenticationTokenString());
		return request;
	}

    private String getAuthenticationTokenString() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("Cannot set authorization header because there is no authenticated principal");
        } else if (!KeycloakAuthenticationToken.class.isAssignableFrom(authentication.getClass())) {
            throw new IllegalStateException(String.format("Cannot set authorization header because Authentication is of type %s but %s is required",
                                                          new Object[]{authentication.getClass(), KeycloakAuthenticationToken.class}));
        } else {
            KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) authentication;
            return token.getAccount().getKeycloakSecurityContext().getTokenString();
        }
    }
}
