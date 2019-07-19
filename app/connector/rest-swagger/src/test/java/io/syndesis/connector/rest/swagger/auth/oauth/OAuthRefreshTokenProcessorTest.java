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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.http.common.HttpOperationFailedException;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicStatusLine;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import io.syndesis.connector.rest.swagger.Configuration;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;

public class OAuthRefreshTokenProcessorTest {
    private final long currentTime = 1000000000;

    private final HttpOperationFailedException exception = new HttpOperationFailedException("uri", 403, "status", "location", null, null);

    private final Exchange exchange = new DefaultExchange(new DefaultCamelContext());

    public OAuthRefreshTokenProcessorTest() {
        exchange.setProperty(Exchange.EXCEPTION_CAUGHT, exception);
    }

    @Test
    public void shouldAllowOverideExpires() throws Exception {
        final OAuthRefreshTokenProcessor processor = createProcessor(
            "{\"access_token\": \"new-access-token\", \"refresh_token\": \"new-refresh-token\", \"expires_in\": 3600}");
        processor.state.update("access-token", currentTime, null);
        processor.isFirstTime.set(Boolean.FALSE);
        processor.expiresInOverride = 1800L;

        processor.process(exchange);

        assertThat(processor.state.getAccessToken()).isEqualTo("new-access-token");
        assertThat(processor.state.getRefreshToken()).isEqualTo("new-refresh-token");
        assertThat(processor.state.getAccessTokenExpiresAt()).isEqualTo(currentTime + 1800000L);
    }

    @Test
    public void shouldNotRefreshAccessTokenIfItHasntExpired() throws Exception {
        final OAuthRefreshTokenProcessor processor = createProcessor(
            "{\"access_token\": \"new-access-token\", \"refresh_token\": \"new-refresh-token\", \"expires_in\": 3600}");
        processor.state.update("access-token", currentTime + OAuthRefreshTokenProcessor.AHEAD_OF_TIME_REFRESH_MILIS + 1000, null);
        processor.isFirstTime.set(Boolean.FALSE);

        processor.process(exchange);

        assertThat(processor.state.getAccessToken()).isEqualTo("access-token");
        assertThat(processor.state.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(processor.state.getAccessTokenExpiresAt()).isEqualTo(currentTime + OAuthRefreshTokenProcessor.AHEAD_OF_TIME_REFRESH_MILIS + 1000);
    }

    @Test
    public void shouldRefreshAccessTokenBeforeItExpiresUsingAheadOfTimeRefresh() throws Exception {
        final OAuthRefreshTokenProcessor processor = createProcessor(
            "{\"access_token\": \"new-access-token\", \"refresh_token\": \"new-refresh-token\", \"expires_in\": 3600}");
        processor.state.update("access-token", currentTime + OAuthRefreshTokenProcessor.AHEAD_OF_TIME_REFRESH_MILIS, null);
        processor.isFirstTime.set(Boolean.FALSE);

        processor.process(exchange);

        assertThat(processor.state.getAccessToken()).isEqualTo("new-access-token");
        assertThat(processor.state.getRefreshToken()).isEqualTo("new-refresh-token");
        assertThat(processor.state.getAccessTokenExpiresAt()).isEqualTo(currentTime + 3600000L);
    }

    @Test
    public void shouldRefreshAccessTokenIfItHasExpired() throws Exception {
        final OAuthRefreshTokenProcessor processor = createProcessor(
            "{\"access_token\": \"new-access-token\", \"refresh_token\": \"new-refresh-token\", \"expires_in\": 3600}");
        processor.state.update("access-token", currentTime, null);
        processor.isFirstTime.set(Boolean.FALSE);

        processor.process(exchange);

        assertThat(processor.state.getAccessToken()).isEqualTo("new-access-token");
        assertThat(processor.state.getRefreshToken()).isEqualTo("new-refresh-token");
        assertThat(processor.state.getAccessTokenExpiresAt()).isEqualTo(currentTime + 3600000L);
    }

    @Test
    public void shouldRefreshAccessTokenIfItsTheFirstTimeApiIsInvoked() throws Exception {
        final OAuthRefreshTokenProcessor processor = createProcessor(
            "{\"access_token\": \"new-access-token\", \"refresh_token\": \"new-refresh-token\", \"expires_in\": 3600}");
        processor.state.update("access-token", currentTime + OAuthRefreshTokenProcessor.AHEAD_OF_TIME_REFRESH_MILIS + 1000, null);

        processor.process(exchange);

        assertThat(processor.state.getAccessToken()).isEqualTo("new-access-token");
        assertThat(processor.state.getRefreshToken()).isEqualTo("new-refresh-token");
        assertThat(processor.state.getAccessTokenExpiresAt()).isEqualTo(currentTime + 3600000L);
    }

    @SuppressWarnings("resource")
    private OAuthRefreshTokenProcessor createProcessor(final String grantJson) throws IOException {
        final CloseableHttpClient client = mock(CloseableHttpClient.class);
        final CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        when(response.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK"));
        when(response.getEntity()).thenReturn(new ByteArrayEntity(grantJson.getBytes(StandardCharsets.US_ASCII), ContentType.APPLICATION_JSON));

        when(client.execute(ArgumentMatchers.any(HttpUriRequest.class))).thenReturn(response);

        final Map<String, Object> initial = new HashMap<>();
        initial.put("clientId", "client-id");
        initial.put("clientSecret", "client-secret");
        initial.put("accessToken", "access-token");
        initial.put("refreshToken", "refresh-token");
        initial.put("accessTokenExpiresAt", Long.valueOf(-1));
        initial.put("authorizationEndpoint", "token-endpoint");

        final ComponentProxyCustomizer customizer = mock(ComponentProxyCustomizer.class);
        final Configuration configuration = new Configuration(initial, customizer, null, null);

        final OAuthRefreshTokenProcessor processor = new OAuthRefreshTokenProcessor(OAuthState.createFrom(configuration), configuration) {
            @Override
            CloseableHttpClient createHttpClient() {
                return client;
            }

            @Override
            long now() {
                return currentTime;
            }
        };

        return processor;
    }
}
