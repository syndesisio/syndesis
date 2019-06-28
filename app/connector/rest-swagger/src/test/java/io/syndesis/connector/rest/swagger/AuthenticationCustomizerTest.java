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

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import io.syndesis.integration.component.proxy.ComponentProxyComponent;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.TypeConverter;
import org.apache.camel.spi.AsyncProcessorAwaitManager;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;

public class AuthenticationCustomizerTest {

    @Rule
    public WireMockRule wiremock = new WireMockRule(WireMockConfiguration.options().dynamicPort());

    @Test
    public void shouldSetBasicAuthorizationHeader() throws Exception {
        final AuthenticationCustomizer customizer = new AuthenticationCustomizer();

        final Map<String, Object> options = new HashMap<>();
        options.put("authenticationType", AuthenticationType.basic);
        options.put("username", "username");
        options.put("password", "{{password}}");

        final ComponentProxyComponent component = new SwaggerProxyComponent("test", "test");
        final CamelContext context = mock(CamelContext.class);
        component.setCamelContext(context);

        when(context.resolvePropertyPlaceholders("username")).thenReturn("username");
        when(context.resolvePropertyPlaceholders("{{password}}")).thenReturn("dolphins");

        customizer.customize(component, options);

        assertThat(options).doesNotContainKeys("authenticationType", "username", "password");

        assertAuthorizationHeaderSetTo(component, "Basic dXNlcm5hbWU6ZG9scGhpbnM=");
    }

    @Test
    public void shouldSetOAuth2AuthorizationHeader() throws Exception {
        final AuthenticationCustomizer customizer = new AuthenticationCustomizer();

        final Map<String, Object> options = new HashMap<>();
        options.put("authenticationType", AuthenticationType.oauth2);
        options.put("accessToken", "{{accessToken}}");

        final ComponentProxyComponent component = new SwaggerProxyComponent("test", "test");
        final CamelContext context = mock(CamelContext.class);
        component.setCamelContext(context);

        when(context.resolvePropertyPlaceholders("{{accessToken}}")).thenReturn("access-token");

        customizer.customize(component, options);

        assertThat(options).doesNotContainKeys("authenticationType", "accessToken");

        assertAuthorizationHeaderSetTo(component, "Bearer access-token");
    }

    @Test
    public void shouldSetParameterAuthenticationHeader() throws Exception {
        final AuthenticationCustomizer customizer = new AuthenticationCustomizer();

        final Map<String, Object> options = new HashMap<>();
        options.put("authenticationType", AuthenticationType.apiKey);
        options.put("authenticationParameterName", "apiKey");
        options.put("authenticationParameterValue", "{{key}}");
        options.put("authenticationParameterPlacement", "header");

        final ComponentProxyComponent component = new SwaggerProxyComponent("test", "test");
        final CamelContext context = mock(CamelContext.class);
        component.setCamelContext(context);

        when(context.resolvePropertyPlaceholders("apiKey")).thenReturn("apiKey");
        when(context.resolvePropertyPlaceholders("{{key}}")).thenReturn("dolphins");
        when(context.resolvePropertyPlaceholders("header")).thenReturn("header");

        customizer.customize(component, options);

        assertThat(options).doesNotContainKeys("authenticationParameterName", "authenticationParameterValue", "authenticationParameterPlacement");

        assertHeaderSetTo(component, "apiKey", "dolphins");
    }

    @Test
    public void shouldSetRefreshOAuth2Token() throws Exception {
        wiremock.givenThat(post("/oauth/authorize")
            .withRequestBody(equalTo("refresh_token=refresh-token&grant_type=refresh_token"))
            .willReturn(ok()
                .withBody("{\"access_token\":\"new-access-token\"}")));

        final AuthenticationCustomizer customizer = new AuthenticationCustomizer();

        final Map<String, Object> options = new HashMap<>();
        options.put("authenticationType", AuthenticationType.oauth2);
        options.put("accessToken", "{{accessToken}}");
        options.put("refreshToken", "{{refreshToken}}");
        final String authorizationEndpoint = "http://localhost:" + wiremock.port() + "/oauth/authorize";
        options.put("authorizationEndpoint", authorizationEndpoint);

        final ComponentProxyComponent component = new SwaggerProxyComponent("test", "test");
        final CamelContext context = mock(CamelContext.class);
        component.setCamelContext(context);

        when(context.resolvePropertyPlaceholders("{{accessToken}}")).thenReturn("access-token");
        when(context.resolvePropertyPlaceholders("{{refreshToken}}")).thenReturn("refresh-token");
        when(context.resolvePropertyPlaceholders(authorizationEndpoint)).thenReturn(authorizationEndpoint);
        final TypeConverter typeConverter = mock(TypeConverter.class);
        when(context.getTypeConverter()).thenReturn(typeConverter);

        customizer.customize(component, options);

        assertThat(options).doesNotContainKeys("authenticationType", "accessToken", "refreshToken", "authorizationEndpoint");

        assertAuthorizationHeaderSetTo(component, "Bearer new-access-token");
    }

    @Test
    public void shouldSupportAllAuthenticatioValues() {
        final ComponentProxyComponent component = new SwaggerProxyComponent("test", "test");
        final CamelContext context = mock(CamelContext.class);
        component.setCamelContext(context);

        final AuthenticationCustomizer customizer = new AuthenticationCustomizer();

        Stream.of(AuthenticationType.values()).forEach(authenticationType -> {
            final Map<String, Object> options = new HashMap<>();
            options.put("authenticationType", authenticationType.name());

            customizer.customize(component, options);
            // no IllegalStateException thrown
        });
    }

    @Test
    public void shouldSupportNamedAuthenticatioValues() {
        final ComponentProxyComponent component = new SwaggerProxyComponent("test", "test");
        final CamelContext context = mock(CamelContext.class);
        component.setCamelContext(context);

        final AuthenticationCustomizer customizer = new AuthenticationCustomizer();

        Stream.of(AuthenticationType.values()).forEach(authenticationType -> {
            final Map<String, Object> options = new HashMap<>();
            options.put("authenticationType", authenticationType.name() + ":name");

            customizer.customize(component, options);
            // no IllegalStateException thrown
        });
    }

    private static void assertAuthorizationHeaderSetTo(final ComponentProxyComponent component, final String value) throws Exception {
        assertHeaderSetTo(component, "Authorization", value);
    }

    private static void assertHeaderSetTo(final ComponentProxyComponent component, final String headerName, final String headerValue) throws Exception {
        final Processor processor = component.getBeforeProducer();

        final Exchange exchange = mock(Exchange.class);
        final Message message = mock(Message.class);
        when(exchange.getIn()).thenReturn(message);
        when(exchange.getOut()).thenReturn(message);
        when(exchange.getPattern()).thenReturn(ExchangePattern.InOut);

        final CamelContext context = mock(CamelContext.class);
        when(exchange.getContext()).thenReturn(context);

        final AsyncProcessorAwaitManager async = mock(AsyncProcessorAwaitManager.class);
        when(context.getAsyncProcessorAwaitManager()).thenReturn(async);

        processor.process(exchange);

        verify(message).setHeader(headerName, headerValue);
    }

}
