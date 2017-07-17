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
package io.syndesis.credential;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;

import io.syndesis.dao.manager.DataManager;
import io.syndesis.model.connection.Connection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.social.connect.ConnectionFactory;
import org.springframework.social.connect.support.OAuth1ConnectionFactory;
import org.springframework.social.connect.support.OAuth2ConnectionFactory;
import org.springframework.social.oauth1.AuthorizedRequestToken;
import org.springframework.social.oauth1.OAuth1Operations;
import org.springframework.social.oauth1.OAuth1Parameters;
import org.springframework.social.oauth1.OAuth1Version;
import org.springframework.social.oauth1.OAuthToken;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.OAuth2Operations;
import org.springframework.social.oauth2.OAuth2Parameters;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public final class Credentials {

    public static final String CACHE_NAME = "credentials";

    private static final Pattern API_BASE_PATH = Pattern.compile("(/[^/]+/[^/]+)/.*");

    private static final MultiValueMap<String, String> EMPTY = new LinkedMultiValueMap<>();

    private final CredentialProviderLocator credentialProviderLocator;

    private final DataManager dataManager;

    private final Cache state;

    @Autowired
    public Credentials(final CredentialProviderLocator credentialProviderLocator, final DataManager dataManager,
        final CacheManager cacheManager) {
        this.credentialProviderLocator = credentialProviderLocator;
        this.dataManager = dataManager;
        state = cacheManager.getCache(CACHE_NAME);
    }

    public Acquisition acquire(final String connectionId, final String providerId, final URI returnUrl,
        final NativeWebRequest request) {

        final ConnectionFactory<?> connectionFactory = factoryFor(providerId);

        final String redirectUrl;
        final CredentialFlowState.Builder flowStateBuilder = new CredentialFlowState.Builder().returnUrl(returnUrl)
            .connectionId(connectionId).providerId(providerId);

        if (connectionFactory instanceof OAuth1ConnectionFactory) {
            redirectUrl = prepareOAuth1((OAuth1ConnectionFactory<?>) connectionFactory, flowStateBuilder, request);
        } else if (connectionFactory instanceof OAuth2ConnectionFactory) {
            redirectUrl = prepareOAuth2((OAuth2ConnectionFactory<?>) connectionFactory, flowStateBuilder, request);
        } else {
            throw new IllegalStateException("Unsupported connection factory implementation: " + connectionFactory);
        }

        final CredentialFlowState flowState = flowStateBuilder.build();
        state.put(flowState.getKey(), flowState);

        return new Acquisition.Builder().type(Acquisition.Type.REDIRECT).url(redirectUrl).build();
    }

    public AcquisitionMethod acquisitionMethodFor(final String providerId) {
        try {
            return acquisitionMethodFor(factoryFor(providerId));
        } catch (final IllegalArgumentException ignored) {
            return AcquisitionMethod.NONE;
        }
    }

    public URI finishAcquisition(final String oauthState, final NativeWebRequest request) {
        final CredentialFlowState flowState = state.get(oauthState, CredentialFlowState.class);

        if (flowState == null) {
            throw new EntityNotFoundException("Flow state could not be found, this might mean that the state "
                + "parameter was not passed correctly or that the allowed timeout has been reached");
        }

        final String connectionId = flowState.getConnectionId();
        final Connection connection = dataManager.fetch(Connection.class, connectionId);

        final String providerId = flowState.getProviderId();
        final ConnectionFactory<?> connectionFactory = factoryFor(providerId);

        final Connection updatedConnection;
        if (connectionFactory instanceof OAuth1ConnectionFactory) {
            updatedConnection = finishOAuth1((OAuth1ConnectionFactory<?>) connectionFactory, connection, flowState,
                request);
        } else if (connectionFactory instanceof OAuth2ConnectionFactory) {
            updatedConnection = finishOAuth2((OAuth2ConnectionFactory<?>) connectionFactory, connection, flowState,
                request);
        } else {
            throw new IllegalStateException("Unsupported connection factory implementation: " + connectionFactory);
        }

        dataManager.update(updatedConnection);

        return flowState.getReturnUrl();
    }

    protected <T> Applicator<T> applicatorFor(final String providerId) {
        @SuppressWarnings("unchecked")
        final Applicator<T> applicator = (Applicator<T>) credentialProviderLocator.getApplicator(providerId);

        return applicator;
    }

    protected ConnectionFactory<?> factoryFor(final String providerId) {
        return credentialProviderLocator.getConnectionFactory(providerId);
    }

    protected Connection finishOAuth1(final OAuth1ConnectionFactory<?> oauth1, final Connection connection,
        final CredentialFlowState flowState, final NativeWebRequest request) {
        final String providerId = flowState.getProviderId();

        final String verifier = request.getParameter("oauth_verifier");
        final AuthorizedRequestToken requestToken = new AuthorizedRequestToken(flowState.getToken(), verifier);
        final OAuthToken accessToken = oauth1.getOAuthOperations().exchangeForAccessToken(requestToken, null);

        final Applicator<OAuthToken> applicator = applicatorFor(providerId);

        return applicator.applyTo(connection, accessToken);
    }

    protected Connection finishOAuth2(final OAuth2ConnectionFactory<?> oauth2, final Connection connection,
        final CredentialFlowState flowState, final NativeWebRequest request) {
        final String providerId = flowState.getProviderId();

        final String code = request.getParameter("code");

        final AccessGrant accessGrant = oauth2.getOAuthOperations().exchangeForAccess(code,
            callbackUrlFor(request, EMPTY), null);

        final Applicator<AccessGrant> applicator = applicatorFor(providerId);

        return applicator.applyTo(connection, accessGrant);
    }

    protected static AcquisitionMethod acquisitionMethodFor(final ConnectionFactory<?> connectionFactory) {
        final String providerId = connectionFactory.getProviderId();

        return new AcquisitionMethod.Builder().label(labelFor(providerId)).icon(iconFor(providerId))
            .type(typeOf(connectionFactory)).description(descriptionFor(providerId)).build();
    }

    protected static String callbackUrlFor(final NativeWebRequest request,
        final MultiValueMap<String, String> additionalParams) {
        final HttpServletRequest httpServletRequest = request.getNativeRequest(HttpServletRequest.class);

        final URI requestUri = URI.create(httpServletRequest.getRequestURL().toString());

        final String path = requestUri.getPath();

        final String callbackPath = API_BASE_PATH.matcher(path).replaceFirst("$1/credentials/callback");

        try {
            final URI base = new URI(requestUri.getScheme(), requestUri.getHost(), callbackPath, null);

            return UriComponentsBuilder.fromUri(base).queryParams(additionalParams).build().toUriString();
        } catch (final URISyntaxException e) {
            throw new IllegalStateException("Unable to generate callback URI", e);
        }
    }

    protected static String descriptionFor(final String providerId) {
        return providerId;
    }

    protected static String iconFor(final String providerId) {
        return providerId;
    }

    protected static String labelFor(final String providerId) {
        return providerId;
    }

    protected static String prepareOAuth1(final OAuth1ConnectionFactory<?> oauth1,
        final CredentialFlowState.Builder flowStateBuilder, final NativeWebRequest request) {
        final OAuth1Operations oauthOperations = oauth1.getOAuthOperations();
        final OAuth1Parameters parameters = new OAuth1Parameters();

        final String stateKey = UUID.randomUUID().toString();
        flowStateBuilder.key(stateKey);

        final OAuthToken oAuthToken;
        final OAuth1Version oAuthVersion = oauthOperations.getVersion();

        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        parameters.set("state", stateKey);

        if (oAuthVersion == OAuth1Version.CORE_10) {
            parameters.setCallbackUrl(callbackUrlFor(request, params));

            oAuthToken = oauthOperations.fetchRequestToken(null, null);
        } else if (oAuthVersion == OAuth1Version.CORE_10_REVISION_A) {
            oAuthToken = oauthOperations.fetchRequestToken(callbackUrlFor(request, params), null);
        } else {
            throw new IllegalStateException("Unsupported OAuth 1 version: " + oAuthVersion);
        }
        flowStateBuilder.token(oAuthToken);

        return oauthOperations.buildAuthorizeUrl(oAuthToken.getValue(), parameters);
    }

    protected static String prepareOAuth2(final OAuth2ConnectionFactory<?> oauth2,
        final CredentialFlowState.Builder flowStateBuilder, final NativeWebRequest request) {
        final OAuth2Parameters parameters = new OAuth2Parameters();

        final String callbackUrl = callbackUrlFor(request, EMPTY);
        parameters.setRedirectUri(callbackUrl);

        final String scope = oauth2.getScope();
        parameters.setScope(scope);

        final String stateKey = oauth2.generateState();
        flowStateBuilder.key(stateKey);
        parameters.add("state", stateKey);

        final OAuth2Operations oauthOperations = oauth2.getOAuthOperations();

        return oauthOperations.buildAuthorizeUrl(parameters);
    }

    protected static AcquisitionMethod.Type typeOf(final ConnectionFactory<?> connectionFactory) {
        Assert.notNull(connectionFactory, "ConnectionFactory is required");

        if (connectionFactory instanceof OAuth1ConnectionFactory) {
            return AcquisitionMethod.Type.OAUTH1;
        } else if (connectionFactory instanceof OAuth2ConnectionFactory) {
            return AcquisitionMethod.Type.OAUTH2;
        }

        throw new IllegalArgumentException("Unknown ConnectionFactory type: " + connectionFactory.getClass());
    }
}
