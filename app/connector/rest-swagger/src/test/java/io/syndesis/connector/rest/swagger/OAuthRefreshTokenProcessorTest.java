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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OAuthRefreshTokenProcessorTest {
    SwaggerConnectorComponent component = new SwaggerConnectorComponent();

    long currentTime = 100000;

    HttpOperationFailedException exception = new HttpOperationFailedException("uri", 403, "status", "location", null, null);

    Exchange exchange = new DefaultExchange(new DefaultCamelContext());

    public OAuthRefreshTokenProcessorTest() {
        exchange.setProperty(Exchange.EXCEPTION_CAUGHT, exception);
        component.setClientId("client-id");
        component.setClientSecret("client-secret");
        component.setTokenEndpoint("token-endpoint");
        component.setAccessToken("access-token");
        component.setRefreshToken("refresh-token");
    }

    @Test
    public void shouldRefreshAccessTokenIfItHasExpired() throws Exception {
        component.setAccessTokenExpiresAt(110000L);

        final OAuthRefreshTokenProcessor processor = createProcessor(
            "{\"access_token\": \"new-access-token\", \"refresh_token\": \"new-refresh-token\", \"expires_in\": 3600}");

        processor.process(exchange);

        assertThat(component.getAccessToken()).isEqualTo("new-access-token");
        assertThat(component.getRefreshToken()).isEqualTo("new-refresh-token");
        assertThat(component.getAccessTokenExpiresAt()).isEqualTo(3700000L);
    }

    OAuthRefreshTokenProcessor createProcessor(final String grantJson) throws IOException {
        final CloseableHttpClient client = mock(CloseableHttpClient.class);
        final CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        when(response.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK"));
        when(response.getEntity()).thenReturn(new ByteArrayEntity(grantJson.getBytes(), ContentType.APPLICATION_JSON));

        when(client.execute(ArgumentMatchers.any(HttpUriRequest.class))).thenReturn(response);

        return new OAuthRefreshTokenProcessor(component) {
            @Override
            CloseableHttpClient createHttpClient() {
                return client;
            }

            @Override
            long now() {
                return currentTime;
            }
        };
    }
}
