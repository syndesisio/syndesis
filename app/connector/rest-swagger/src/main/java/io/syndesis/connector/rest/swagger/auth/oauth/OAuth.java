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
package io.syndesis.connector.rest.swagger.auth.oauth;

import io.syndesis.connector.rest.swagger.Configuration;
import io.syndesis.connector.rest.swagger.SwaggerProxyComponent;
import io.syndesis.connector.rest.swagger.auth.SetAuthorizationHeader;
import io.syndesis.integration.component.proxy.Processors;

public final class OAuth {

    private OAuth() {
        // utility class
    }

    public static void setup(final SwaggerProxyComponent component, final Configuration configuration) {
        final boolean canProcessRefresh = canProcessRefresh(configuration);
        final boolean retriesOnAuthenticationErrors = retriesOnAuthenticationErrors(configuration);

        final OAuthState state = OAuthState.createFrom(configuration);

        if (canProcessRefresh && !retriesOnAuthenticationErrors) {
            Processors.addBeforeProducer(component, new OAuthRefreshTokenProcessor(state, configuration));
        } else if (retriesOnAuthenticationErrors) {
            Processors.addBeforeProducer(component, new OAuthRefreshTokenProcessor(state, configuration));

            final OAuthRefreshTokenOnFailProcessor refreshOnFailure = new OAuthRefreshTokenOnFailProcessor(state, configuration);
            component.overrideEndpoint(endpoint -> new OAuthRefreshingEndpoint(component, endpoint, refreshOnFailure));
        } else {
            final String authorizationHeaderValue = "Bearer " + configuration.stringOption("accessToken");
            Processors.addBeforeProducer(component, new SetAuthorizationHeader(authorizationHeaderValue));
        }
    }

    private static boolean canProcessRefresh(final Configuration configuration) {
        final String clientId = configuration.stringOption("clientId");
        final String clientSecret = configuration.stringOption("clientSecret");
        final String refreshToken = configuration.stringOption("refreshToken");
        final String authorizationEndpoint = configuration.stringOption("authorizationEndpoint");
        final boolean authorizeUsingParameters = configuration.booleanOption("authorizeUsingParameters");

        final boolean hasBasicRefreshOptions = refreshToken != null && authorizationEndpoint != null;
        final boolean hasParametersIfNeeded = authorizeUsingParameters && clientId != null && clientSecret != null;

        return hasBasicRefreshOptions && (!authorizeUsingParameters || hasParametersIfNeeded);
    }

    private static boolean retriesOnAuthenticationErrors(final Configuration configuration) {
        final String statuses = configuration.stringOption("refreshTokenRetryStatuses");

        return statuses != null && !statuses.isEmpty();
    }
}
