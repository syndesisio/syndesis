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
package io.syndesis.github.backend;

import io.syndesis.core.Tokens;

import java.net.HttpURLConnection;

import org.eclipse.egit.github.core.client.GitHubClient;

import static org.eclipse.egit.github.core.client.IGitHubConstants.AUTH_TOKEN;

/**
 * GitHub client which sets a KeyCloak authentication token
 *
 * Note that this client needs to be recreated afresh for request, as the tokens
 * are different for each request.
 *
 * @author roland
 * @since 09/03/2017
 */
public class KeycloakProviderTokenAwareGitHubClient extends GitHubClient {

    public KeycloakProviderTokenAwareGitHubClient() {
        super();
    }

    public KeycloakProviderTokenAwareGitHubClient(String hostname) {
        super(hostname);
    }

    @Override
    protected HttpURLConnection configureRequest(final HttpURLConnection request) {
        super.configureRequest(request);
        request.setRequestProperty(HEADER_AUTHORIZATION, AUTH_TOKEN + ' ' + Tokens.fetchProviderTokenFromKeycloak(Tokens.TokenProvider.GITHUB));
        return request;
    }
}
