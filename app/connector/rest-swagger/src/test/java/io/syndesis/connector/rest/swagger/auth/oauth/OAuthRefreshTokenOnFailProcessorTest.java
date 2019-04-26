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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.syndesis.connector.rest.swagger.Configuration;

import org.apache.camel.Exchange;
import org.apache.camel.http.common.HttpOperationFailedException;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicStatusLine;
import org.junit.Test;
import org.mockito.ArgumentMatchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OAuthRefreshTokenOnFailProcessorTest {

    private final HttpOperationFailedException exception = new HttpOperationFailedException("uri", 403, "status", "location", null, null);

    private final Exchange exchange = new DefaultExchange(new DefaultCamelContext());

    public OAuthRefreshTokenOnFailProcessorTest() {
        exchange.setProperty(Exchange.EXCEPTION_CAUGHT, exception);
    }

    @Test
    public void shouldNotThrowExceptionWhenRetryingInitially() throws Exception {
        final OAuthRefreshTokenProcessor processor = createProcessor("{}");

        assertThatThrownBy(() -> processor.process(exchange)).isSameAs(exception);
    }

    @Test
    public void shouldNotThrowExceptionWhenRetryingWithDifferentRefreshToken() throws Exception {
        final OAuthRefreshTokenProcessor processor = createProcessor("{}");
        processor.lastRefreshTokenTried.set("different-refresh-token");

        assertThatThrownBy(() -> processor.process(exchange)).isSameAs(exception);
    }

    @Test
    public void shouldNotUpdateComponentAccessAndRefreshTokensWithEmptyValues() throws Exception {
        final OAuthRefreshTokenProcessor processor = createProcessor("{\"access_token\": \"\", \"refresh_token\": \"\"}");

        assertThatThrownBy(() -> processor.process(exchange)).isSameAs(exception);

        assertThat(processor.state.getAccessToken()).isEqualTo("access-token");
        assertThat(processor.state.getRefreshToken()).isEqualTo("refresh-token");
    }

    @Test
    public void shouldNotUpdateComponentAccessAndRefreshTokensWithNullValues() throws Exception {
        final OAuthRefreshTokenProcessor processor = createProcessor("{\"access_token\": null, \"refresh_token\": null}");

        assertThatThrownBy(() -> processor.process(exchange)).isSameAs(exception);

        assertThat(processor.state.getAccessToken()).isEqualTo("access-token");
        assertThat(processor.state.getRefreshToken()).isEqualTo("refresh-token");
    }

    @Test
    public void shouldNotUpdateComponentRefreshTokensWithEmptyValues() throws Exception {
        final OAuthRefreshTokenProcessor processor = createProcessor(
            "{\"access_token\": \"new-access-token\", \"refresh_token\": \"\"}");

        assertThatThrownBy(() -> processor.process(exchange)).isSameAs(exception);

        assertThat(processor.state.getAccessToken()).isEqualTo("new-access-token");
        assertThat(processor.state.getRefreshToken()).isEqualTo("refresh-token");
    }

    @Test
    public void shouldNotUpdateComponentRefreshTokensWithNullValues() throws Exception {
        final OAuthRefreshTokenProcessor processor = createProcessor(
            "{\"access_token\": \"new-access-token\", \"refresh_token\": null}");

        assertThatThrownBy(() -> processor.process(exchange)).isSameAs(exception);

        assertThat(processor.state.getAccessToken()).isEqualTo("new-access-token");
        assertThat(processor.state.getRefreshToken()).isEqualTo("refresh-token");
    }

    @Test
    public void shouldRefreshSecondTimeIfWeReceivedTheSameRefreshTokenTheFirstTime() throws Exception {
        final OAuthRefreshTokenProcessor processor = createProcessor(
            "{\"access_token\": \"new-access-token\", \"refresh_token\": \"new-refresh-token\"}",
            "{\"access_token\": \"newer-access-token\", \"refresh_token\": \"new-refresh-token\"}");

        assertThatThrownBy(() -> processor.process(exchange)).isSameAs(exception);

        assertThat(processor.state.getAccessToken()).isEqualTo("new-access-token");
        assertThat(processor.state.getRefreshToken()).isEqualTo("new-refresh-token");

        assertThatThrownBy(() -> processor.process(exchange)).isSameAs(exception);

        assertThat(processor.state.getAccessToken()).isEqualTo("newer-access-token");
        assertThat(processor.state.getRefreshToken()).isEqualTo("new-refresh-token");
    }

    @Test
    public void shouldThrowExceptionIfAlreadyAttemptedWithTheSameRefreshToken() throws Exception {
        final Configuration configuration = configuration("403");

        final OAuthRefreshTokenOnFailProcessor processor = new OAuthRefreshTokenOnFailProcessor(OAuthState.createFrom(configuration), configuration);
        processor.lastRefreshTokenTried.set("refresh-token");

        assertThatThrownBy(() -> processor.process(exchange)).isSameAs(exception);
    }

    @Test
    public void shouldThrowExceptionIfStatusIsNonRetriable() {
        final Configuration configuration = configuration("400");

        final OAuthRefreshTokenOnFailProcessor processor = new OAuthRefreshTokenOnFailProcessor(OAuthState.createFrom(configuration), configuration);

        assertThatThrownBy(() -> processor.process(exchange)).isSameAs(exception);
    }

    @Test
    public void shouldUpdateComponentAccessAndRefreshTokens() throws Exception {
        final OAuthRefreshTokenProcessor processor = createProcessor(
            "{\"access_token\": \"new-access-token\", \"refresh_token\": \"new-refresh-token\"}");

        assertThatThrownBy(() -> processor.process(exchange)).isSameAs(exception);

        assertThat(processor.state.getAccessToken()).isEqualTo("new-access-token");
        assertThat(processor.state.getRefreshToken()).isEqualTo("new-refresh-token");
    }

    @Test
    public void shouldUpdateComponentAccessToken() throws Exception {
        final OAuthRefreshTokenProcessor processor = createProcessor("{\"access_token\": \"new-access-token\"}");

        assertThatThrownBy(() -> processor.process(exchange)).isSameAs(exception);

        assertThat(processor.state.getAccessToken()).isEqualTo("new-access-token");
    }

    @Test
    public void shouldUpdateComponentRefreshTokenOnlyIfAccessTokenIsGiven() throws Exception {
        final OAuthRefreshTokenProcessor processor = createProcessor("{\"refresh_token\": \"new-refresh-token\"}");

        assertThatThrownBy(() -> processor.process(exchange)).isSameAs(exception);

        assertThat(processor.state.getAccessToken()).isEqualTo("access-token");
        assertThat(processor.state.getRefreshToken()).isEqualTo("refresh-token");
    }

    private static Configuration configuration(final String retryStatuses) {
        final Map<String, Object> initial = new HashMap<>();
        initial.put("clientId", "client-id");
        initial.put("clientSecret", "client-secret");
        initial.put("accessToken", "access-token");
        initial.put("refreshToken", "refresh-token");
        initial.put("accessTokenExpiresAt", Long.valueOf(-1));
        initial.put("authorizationEndpoint", "token-endpoint");
        initial.put("authorizeUsingParameters", Boolean.FALSE);
        initial.put("refreshTokenRetryStatuses", retryStatuses);

        return new Configuration(initial, null, null, null);
    }

    @SuppressWarnings("resource")
    private static OAuthRefreshTokenOnFailProcessor createProcessor(final String... grantJsons) throws IOException {
        final CloseableHttpClient client = mock(CloseableHttpClient.class);
        final CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        when(response.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK"));

        final HttpEntity first = entity(grantJsons[0]);
        final HttpEntity[] rest = Arrays.stream(grantJsons).skip(1).map(OAuthRefreshTokenOnFailProcessorTest::entity)
            .toArray(HttpEntity[]::new);
        when(response.getEntity()).thenReturn(first, rest);

        when(client.execute(ArgumentMatchers.any(HttpUriRequest.class))).thenReturn(response);

        final Configuration configuration = configuration("403");

        final OAuthRefreshTokenOnFailProcessor processor = new OAuthRefreshTokenOnFailProcessor(OAuthState.createFrom(configuration), configuration) {
            @Override
            CloseableHttpClient createHttpClient() {
                return client;
            }
        };

        return processor;
    }

    private static HttpEntity entity(final String grantJson) {
        return new ByteArrayEntity(grantJson.getBytes(StandardCharsets.US_ASCII), ContentType.APPLICATION_JSON);
    }
}
