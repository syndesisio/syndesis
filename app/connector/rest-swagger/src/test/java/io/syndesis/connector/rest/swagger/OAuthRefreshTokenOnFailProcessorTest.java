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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

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

    SwaggerConnectorComponent component = new SwaggerConnectorComponent();

    HttpOperationFailedException exception = new HttpOperationFailedException("uri", 403, "status", "location", null, null);

    Exchange exchange = new DefaultExchange(new DefaultCamelContext());

    public OAuthRefreshTokenOnFailProcessorTest() {
        exchange.setProperty(Exchange.EXCEPTION_CAUGHT, exception);
        component.setClientId("client-id");
        component.setClientSecret("client-secret");
        component.setTokenEndpoint("token-endpoint");
        component.setAccessToken("access-token");
        component.setRefreshToken("refresh-token");
    }

    @Test
    public void shouldNotThrowExceptionWhenRetryingInitially() throws Exception {
        final OAuthRefreshTokenProcessor processor = createProcessor("{}");
        component.setRefreshTokenRetryStatuses("403");

        assertThatThrownBy(() -> processor.process(exchange)).isSameAs(exception);
    }

    @Test
    public void shouldNotThrowExceptionWhenRetryingWithDifferentRefreshToken() throws Exception {
        final OAuthRefreshTokenProcessor processor = createProcessor("{}");
        component.setRefreshTokenRetryStatuses("403");
        processor.lastRefreshTokenTried.set("different-refresh-token");

        assertThatThrownBy(() -> processor.process(exchange)).isSameAs(exception);
    }

    @Test
    public void shouldNotUpdateComponentAccessAndRefreshTokensWithEmptyValues() throws Exception {
        final OAuthRefreshTokenProcessor processor = createProcessor("{\"access_token\": \"\", \"refresh_token\": \"\"}");
        component.setRefreshTokenRetryStatuses("403");

        assertThatThrownBy(() -> processor.process(exchange)).isSameAs(exception);

        assertThat(component.getAccessToken()).isEqualTo("access-token");
        assertThat(component.getRefreshToken()).isEqualTo("refresh-token");
    }

    @Test
    public void shouldNotUpdateComponentAccessAndRefreshTokensWithNullValues() throws Exception {
        final OAuthRefreshTokenProcessor processor = createProcessor("{\"access_token\": null, \"refresh_token\": null}");
        component.setRefreshTokenRetryStatuses("403");

        assertThatThrownBy(() -> processor.process(exchange)).isSameAs(exception);

        assertThat(component.getAccessToken()).isEqualTo("access-token");
        assertThat(component.getRefreshToken()).isEqualTo("refresh-token");
    }

    @Test
    public void shouldNotUpdateComponentRefreshTokensWithEmptyValues() throws Exception {
        final OAuthRefreshTokenProcessor processor = createProcessor("{\"access_token\": \"new-access-token\", \"refresh_token\": \"\"}");
        component.setRefreshTokenRetryStatuses("403");

        assertThatThrownBy(() -> processor.process(exchange)).isSameAs(exception);

        assertThat(component.getAccessToken()).isEqualTo("new-access-token");
        assertThat(component.getRefreshToken()).isEqualTo("refresh-token");
    }

    @Test
    public void shouldNotUpdateComponentRefreshTokensWithNullValues() throws Exception {
        final OAuthRefreshTokenProcessor processor = createProcessor("{\"access_token\": \"new-access-token\", \"refresh_token\": null}");
        component.setRefreshTokenRetryStatuses("403");

        assertThatThrownBy(() -> processor.process(exchange)).isSameAs(exception);

        assertThat(component.getAccessToken()).isEqualTo("new-access-token");
        assertThat(component.getRefreshToken()).isEqualTo("refresh-token");
    }

    @Test
    public void shouldRefreshSecondTimeIfWeReceivedTheSameRefreshTokenTheFirstTime() throws Exception {
        final OAuthRefreshTokenProcessor processor = createProcessor(
            "{\"access_token\": \"new-access-token\", \"refresh_token\": \"new-refresh-token\"}",
            "{\"access_token\": \"newer-access-token\", \"refresh_token\": \"new-refresh-token\"}");
        component.setRefreshTokenRetryStatuses("403");

        assertThatThrownBy(() -> processor.process(exchange)).isSameAs(exception);

        assertThat(component.getAccessToken()).isEqualTo("new-access-token");
        assertThat(component.getRefreshToken()).isEqualTo("new-refresh-token");

        assertThatThrownBy(() -> processor.process(exchange)).isSameAs(exception);

        assertThat(component.getAccessToken()).isEqualTo("newer-access-token");
        assertThat(component.getRefreshToken()).isEqualTo("new-refresh-token");
    }

    @Test
    public void shouldThrowExceptionIfAlreadyAttemptedWithTheSameRefreshToken() throws Exception {
        component.setRefreshTokenRetryStatuses("403");
        final OAuthRefreshTokenOnFailProcessor processor = new OAuthRefreshTokenOnFailProcessor(component);
        processor.lastRefreshTokenTried.set("refresh-token");

        assertThatThrownBy(() -> processor.process(exchange)).isSameAs(exception);
    }

    @Test
    public void shouldThrowExceptionIfStatusIsNonRetriable() {
        final OAuthRefreshTokenOnFailProcessor processor = new OAuthRefreshTokenOnFailProcessor(component);

        assertThatThrownBy(() -> processor.process(exchange)).isSameAs(exception);

        component.setRefreshTokenRetryStatuses("400,500");

        assertThatThrownBy(() -> processor.process(exchange)).isSameAs(exception);
    }

    @Test
    public void shouldUpdateComponentAccessAndRefreshTokens() throws Exception {
        final OAuthRefreshTokenProcessor processor = createProcessor(
            "{\"access_token\": \"new-access-token\", \"refresh_token\": \"new-refresh-token\"}");
        component.setRefreshTokenRetryStatuses("403");

        assertThatThrownBy(() -> processor.process(exchange)).isSameAs(exception);

        assertThat(component.getAccessToken()).isEqualTo("new-access-token");
        assertThat(component.getRefreshToken()).isEqualTo("new-refresh-token");
    }

    @Test
    public void shouldUpdateComponentAccessToken() throws Exception {
        final OAuthRefreshTokenProcessor processor = createProcessor("{\"access_token\": \"new-access-token\"}");
        component.setRefreshTokenRetryStatuses("403");

        assertThatThrownBy(() -> processor.process(exchange)).isSameAs(exception);

        assertThat(component.getAccessToken()).isEqualTo("new-access-token");
    }

    @Test
    public void shouldUpdateComponentRefreshTokenOnlyIfAccessTokenIsGiven() throws Exception {
        final OAuthRefreshTokenProcessor processor = createProcessor("{\"refresh_token\": \"new-refresh-token\"}");
        component.setRefreshTokenRetryStatuses("403");

        assertThatThrownBy(() -> processor.process(exchange)).isSameAs(exception);

        assertThat(component.getAccessToken()).isEqualTo("access-token");
        assertThat(component.getRefreshToken()).isEqualTo("refresh-token");
    }

    OAuthRefreshTokenOnFailProcessor createProcessor(final String... grantJsons) throws IOException {
        final CloseableHttpClient client = mock(CloseableHttpClient.class);
        final CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        when(response.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK"));

        final HttpEntity first = entity(grantJsons[0]);
        final HttpEntity[] rest = Arrays.stream(grantJsons).skip(1).map(OAuthRefreshTokenOnFailProcessorTest::entity)
            .toArray(HttpEntity[]::new);
        when(response.getEntity()).thenReturn(first, rest);

        when(client.execute(ArgumentMatchers.any(HttpUriRequest.class))).thenReturn(response);

        return new OAuthRefreshTokenOnFailProcessor(component) {
            @Override
            CloseableHttpClient createHttpClient() {
                return client;
            }
        };
    }

    static HttpEntity entity(final String grantJson) {
        return new ByteArrayEntity(grantJson.getBytes(StandardCharsets.US_ASCII), ContentType.APPLICATION_JSON);
    }
}
