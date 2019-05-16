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
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.util.Json;
import io.syndesis.common.util.Resources;
import io.syndesis.integration.runtime.IntegrationRouteBuilder;
import io.syndesis.integration.runtime.IntegrationStepHandler;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.model.ModelCamelContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.stubbing.Scenario;

import static org.assertj.core.api.Assertions.assertThat;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.status;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

public class RestSwaggerConnectorIntegrationTest {

    private static final String DOGGIE = "{\"id\":123,\"category\":{\"id\":1,\"name\":\"Hounds\"},\"name\":\"Doggie the dog\",\"photoUrls\":[\"doggie.png\"],\"tags\":[{\"id\":1,\"name\":\"drooler\"}],\"status\":\"available\"}";

    private static final DataShape JSON_SCHEMA_SHAPE = new DataShape.Builder()
        .kind(DataShapeKinds.JSON_SCHEMA)
        .build();

    private static final DataShape NONE_SHAPE = new DataShape.Builder()
        .kind(DataShapeKinds.NONE)
        .build();

    private static final Connector REST_OPENAPI_CONNECTOR = loadConnector();

    @Rule
    public HttpErrorDetailRule httpErrorDetail = new HttpErrorDetailRule();

    @Rule
    public WireMockRule wiremock = new WireMockRule(WireMockConfiguration.options().dynamicPort());

    private Connection connection;

    private CamelContext context;

    @Before
    public void setup() throws Exception {
        connection = new Connection.Builder()
            .putConfiguredProperty("host", "http://localhost:" + wiremock.port())
            .putConfiguredProperty("specification", readSpecification())
            .connector(REST_OPENAPI_CONNECTOR)
            .build();

        final RouteBuilder builder = createRouteBuilder();

        context = builder.getContext();
        builder.addRoutesToCamelContext(context);

        context.start();
    }

    @Test
    public void shouldInvokeRemoteApis() throws Exception {
        wiremock.givenThat(get("/v2/pet/123")
            .willReturn(ok(DOGGIE)
                .withHeader("Content-Type", "application/json")));

        assertThat(context.createProducerTemplate().requestBody("direct:getPetById",
            "{\"parameters\":{\"petId\":\"123\"}}", String.class))
                .isEqualTo(DOGGIE);

        wiremock.verify(getRequestedFor(urlEqualTo("/v2/pet/123"))
            .withHeader("Accept", equalTo("application/json"))
            .withRequestBody(WireMock.equalTo("")));
    }

    @Test
    public void shouldNotBeInfluencedByHttpUriHeaderValue() throws Exception {
        wiremock.givenThat(get("/v2/pet/123")
            .willReturn(ok(DOGGIE)
                .withHeader("Content-Type", "application/json")));

        assertThat(context.createProducerTemplate().requestBodyAndHeaders("direct:getPetById",
            "{\"parameters\":{\"petId\":\"123\"}}",
            Collections.singletonMap(Exchange.HTTP_URI, "bogus"), String.class))
                .isEqualTo(DOGGIE);

        wiremock.verify(getRequestedFor(urlEqualTo("/v2/pet/123"))
            .withHeader("Accept", equalTo("application/json"))
            .withRequestBody(WireMock.equalTo("")));
    }

    @Test
    public void shouldNotPassHeadersAlreadyPresentOnExchange() throws Exception {
        wiremock.givenThat(get("/v2/pet/123")
            .willReturn(ok(DOGGIE)
                .withHeader("Content-Type", "application/json")));

        final Map<String, Object> headers = new HashMap<>();
        headers.put("Host", "outside");
        headers.put("Accept", "application/xml");
        headers.put("Forwarded", "for=1.2.3.4;proto=http;by=4.3.2.1");
        headers.put("Cookie", "cupcake=chocolate");
        headers.put("Authorization", "Bearer supersecret");

        assertThat(context.createProducerTemplate().requestBodyAndHeaders("direct:getPetById",
            "{\"parameters\":{\"petId\":\"123\"}}", headers, String.class))
                .isEqualTo(DOGGIE);

        wiremock.verify(getRequestedFor(urlEqualTo("/v2/pet/123"))
            .withHeader("Host", equalTo("localhost:" + wiremock.port()))
            .withHeader("Accept", equalTo("application/json"))
            .withoutHeader("Forwarded")
            .withoutHeader("Cookie")
            .withoutHeader("Authorization")
            .withRequestBody(WireMock.equalTo("")));
    }

    @Test
    public void shouldOutputUnifiedDataShapePayload() {
        wiremock.givenThat(get("/v2/pet/123")
            .willReturn(ok(DOGGIE)
                .withHeader("Content-Type", "application/json")));

        assertThat(context.createProducerTemplate().requestBody("direct:unifiedResponse",
            "{\"parameters\":{\"petId\":\"123\"}}", String.class))
                .isEqualTo(
                    "{\"parameters\":{\"Status\":200,\"Content-Type\":\"application/json\"},\"body\":" + DOGGIE + "}");

        wiremock.verify(getRequestedFor(urlEqualTo("/v2/pet/123"))
            .withHeader("Accept", equalTo("application/json"))
            .withRequestBody(WireMock.equalTo("")));
    }

    @Test
    public void shouldPassAuthenticationParameterHeader() throws Exception {
        wiremock.givenThat(get("/v2/user/logout")
            .withHeader("apiKey", equalTo("supersecret"))
            .willReturn(ok()));

        assertThat(context.createProducerTemplate().requestBody("direct:headerAuth", null, String.class))
            .isEqualTo("");

        wiremock.verify(getRequestedFor(urlEqualTo("/v2/user/logout"))
            .withHeader("apiKey", equalTo("supersecret"))
            .withRequestBody(WireMock.equalTo("")));
    }

    @Test
    public void shouldPassAuthenticationQueryParameter() throws Exception {
        wiremock.givenThat(get("/v2/secured/user/logout?api_key=supersecret")
            .willReturn(ok()));

        assertThat(context.createProducerTemplate().requestBody("direct:queryParamAuth", null, String.class))
            .isEqualTo("");

        wiremock.verify(getRequestedFor(urlEqualTo("/v2/secured/user/logout?api_key=supersecret"))
            .withRequestBody(WireMock.equalTo("")));
    }

    @Test
    public void shouldPassAuthenticationQueryParameterAlongWithOtherParameters() throws Exception {
        wiremock.givenThat(get("/v2/secured/pet/findByStatus?api_key=supersecret&status=available")
            .willReturn(ok()));

        assertThat(context.createProducerTemplate().requestBody("direct:additionalQueryParamAuth", "{\"parameters\":{\"status\":\"available\"}}", String.class))
            .isEqualTo("");

        wiremock.verify(getRequestedFor(urlEqualTo("/v2/secured/pet/findByStatus?api_key=supersecret&status=available"))
            .withRequestBody(WireMock.equalTo("")));
    }

    @Test
    public void shouldPassBasicAuthorizationHeader() throws Exception {
        wiremock.givenThat(get("/v2/user/logout")
            .withBasicAuth("luser", "supersecret")
            .willReturn(ok()));

        assertThat(context.createProducerTemplate().requestBody("direct:basicAuth", null, String.class))
            .isEqualTo("");

        wiremock.verify(getRequestedFor(urlEqualTo("/v2/user/logout"))
            .withBasicAuth(new BasicCredentials("luser", "supersecret"))
            .withRequestBody(WireMock.equalTo("")));
    }

    @Test
    public void shouldPassOauthBearerTokenInAuthorizationHeader() throws Exception {
        wiremock.givenThat(get("/v2/user/logout")
            .withHeader("Authorization", equalTo("Bearer access-token"))
            .willReturn(ok()));

        assertThat(context.createProducerTemplate().requestBody("direct:oauth", null, String.class))
            .isEqualTo("");

        wiremock.verify(getRequestedFor(urlEqualTo("/v2/user/logout"))
            .withHeader("Authorization", equalTo("Bearer access-token"))
            .withRequestBody(WireMock.equalTo("")));
    }

    @Test
    public void shouldPassQueryParameters() throws Exception {
        final String doggieArray = "[" + DOGGIE + "]";
        wiremock.givenThat(get("/v2/pet/findByStatus?status=available")
            .withQueryParam("status", equalTo("available"))
            .willReturn(ok(doggieArray)
                .withHeader("Content-Type", "application/json")));

        assertThat(context.createProducerTemplate().requestBody("direct:findPetsByStatus",
            "{\"parameters\":{\"status\":\"available\"}}", String.class))
                .isEqualTo(doggieArray);

        wiremock.verify(getRequestedFor(urlEqualTo("/v2/pet/findByStatus?status=available"))
            .withQueryParam("status", equalTo("available"))
            .withHeader("Accept", equalTo("application/json"))
            .withRequestBody(WireMock.equalTo("")));
    }

    @Test
    public void shouldPassRequestBodies() throws Exception {
        wiremock.givenThat(put("/v2/user/luser")
            .willReturn(ok()));

        assertThat(context.createProducerTemplate().requestBody("direct:updateUser",
            "{\"parameters\":{\"username\":\"luser\"},\"body\":{\"id\":321,\"name\":\"Test User\"}}", String.class))
                .isEqualTo("");

        wiremock.verify(putRequestedFor(urlEqualTo("/v2/user/luser"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withRequestBody(WireMock.equalToJson("{\"id\":321,\"name\":\"Test User\"}")));
    }

    @Test
    public void shouldRefreshOAuthTokenBeforeItExpires() throws Exception {
        wiremock.givenThat(post("/oauth/authorize")
            .withRequestBody(equalTo("refresh_token=refresh-token&grant_type=refresh_token"))
            .willReturn(ok()
                .withBody("{\"access_token\":\"new-token\"}")));

        wiremock.givenThat(get("/v2/user/logout")
            .withHeader("Authorization", equalTo("Bearer new-token"))
            .willReturn(ok()));

        assertThat(context.createProducerTemplate().requestBody("direct:oauth-refresh", null, String.class))
            .isEqualTo("");

        wiremock.verify(getRequestedFor(urlEqualTo("/v2/user/logout"))
            .withHeader("Authorization", equalTo("Bearer new-token"))
            .withRequestBody(WireMock.equalTo("")));
    }

    @Test
    public void shouldRefreshOAuthTokenOnHttpStatus() throws Exception {
        wiremock.givenThat(post("/oauth/authorize").inScenario("oauth-retry")
            .withRequestBody(equalTo("refresh_token=refresh-token&grant_type=refresh_token"))
            .willReturn(ok()
                .withBody("{\"access_token\":\"refreshed-token\"}"))
            .whenScenarioStateIs(Scenario.STARTED)
            .willSetStateTo("Expecting request"));

        wiremock.givenThat(get("/v2/pet/123").inScenario("oauth-retry")
            .withHeader("Authorization", equalTo("Bearer refreshed-token"))
            .willReturn(status(401))
            .whenScenarioStateIs("Expecting request")
            .willSetStateTo("Expecting retry"));

        wiremock.givenThat(post("/oauth/authorize").inScenario("oauth-retry")
            .withRequestBody(equalTo("refresh_token=refresh-token&grant_type=refresh_token"))
            .willReturn(ok()
                .withBody("{\"access_token\":\"new-token\"}"))
            .whenScenarioStateIs("Expecting retry")
            .willSetStateTo("Retried"));

        wiremock.givenThat(get("/v2/pet/123").inScenario("oauth-retry")
            .withHeader("Authorization", equalTo("Bearer new-token"))
            .willReturn(ok(DOGGIE)
                .withHeader("Content-Type", "application/json"))
            .whenScenarioStateIs("Retried"));

        assertThat(context.createProducerTemplate().requestBody("direct:oauth-retry",
            "{\"parameters\":{\"petId\":\"123\"}}", String.class))
                .isEqualTo(DOGGIE);

        wiremock.verify(getRequestedFor(urlEqualTo("/v2/pet/123"))
            .withHeader("Authorization", equalTo("Bearer new-token"))
            .withRequestBody(WireMock.equalTo("")));
    }

    @After
    public void teardown() throws Exception {
        context.stop();
    }

    private Flow additionalQueryParameterAuthenticationFlow() {
        final Step queryParameterAuth = operation("apiKeyFindPetsByStatus", JSON_SCHEMA_SHAPE, NONE_SHAPE).builder()
            .connection(connection.builder()
                .putConfiguredProperty("authenticationType", AuthenticationType.apiKey.name())
                .putConfiguredProperty("authenticationParameterName", "api_key")
                .putConfiguredProperty("authenticationParameterValue", "ENC:_key_")
                .putConfiguredProperty("authenticationParameterPlacement", "query")
                .build())
            .build();

        return new Flow.Builder()
            .addStep(direct("additionalQueryParamAuth"))
            .addStep(queryParameterAuth)
            .build();
    }

    private Flow basicAuthorizationFlow() {
        final Step apiBasicAuth = operation("logoutUser", NONE_SHAPE, NONE_SHAPE).builder()
            .connection(connection.builder()
                .putConfiguredProperty("authenticationType", AuthenticationType.basic.name())
                .putConfiguredProperty("username", "luser")
                .putConfiguredProperty("password", "ENC:_password_")
                .build())
            .build();

        return new Flow.Builder()
            .addStep(direct("basicAuth"))
            .addStep(apiBasicAuth)
            .build();
    }

    private Integration createIntegration() {
        return new Integration.Builder()
            .addFlow(testFlow("getPetById", JSON_SCHEMA_SHAPE, JSON_SCHEMA_SHAPE))
            .addFlow(testFlow("updateUser", JSON_SCHEMA_SHAPE, NONE_SHAPE))
            .addFlow(testFlow("findPetsByStatus", JSON_SCHEMA_SHAPE, JSON_SCHEMA_SHAPE))
            .addFlow(basicAuthorizationFlow())
            .addFlow(oAuthAuthorizationFlow())
            .addFlow(testFlow("getPetById", "unifiedResponse", JSON_SCHEMA_SHAPE, new DataShape.Builder()
                .kind(DataShapeKinds.JSON_SCHEMA)
                .specification("{\"$id\":\"io:syndesis:wrapped\"}")
                .build()))
            .addFlow(oAuthRefreshFlow())
            .addFlow(oAuthRetryFlow())
            .addFlow(headerAuthenticationFlow())
            .addFlow(queryParameterAuthenticationFlow())
            .addFlow(additionalQueryParameterAuthenticationFlow())
            .build();
    }

    private RouteBuilder createRouteBuilder() {
        return new IntegrationRouteBuilder("", Resources.loadServices(IntegrationStepHandler.class)) {

            @Override
            public void configure() throws Exception {
                errorHandler(defaultErrorHandler().maximumRedeliveries(1));

                super.configure();
            }

            @Override
            protected ModelCamelContext createContainer() {
                final Properties properties = new Properties();

                properties.put("flow-3.rest-swagger-1.password", "supersecret");

                properties.put("flow-4.rest-swagger-1.accessToken", "access-token");

                properties.put("flow-6.rest-swagger-1.clientSecret", "client-secret");
                properties.put("flow-6.rest-swagger-1.accessToken", "access-token");
                properties.put("flow-6.rest-swagger-1.refreshToken", "refresh-token");

                properties.put("flow-7.rest-swagger-1.clientSecret", "client-secret");
                properties.put("flow-7.rest-swagger-1.accessToken", "access-token");
                properties.put("flow-7.rest-swagger-1.refreshToken", "refresh-token");

                properties.put("flow-8.rest-swagger-1.authenticationParameterValue", "supersecret");

                properties.put("flow-9.rest-swagger-1.authenticationParameterValue", "supersecret");

                properties.put("flow-10.rest-swagger-1.authenticationParameterValue", "supersecret");

                final PropertiesComponent propertiesComponent = new PropertiesComponent();
                propertiesComponent.setInitialProperties(properties);

                final SimpleRegistry registry = new SimpleRegistry();
                registry.put("properties", propertiesComponent);

                final ModelCamelContext leanContext = new DefaultCamelContext(registry);
                leanContext.disableJMX();

                return leanContext;
            }

            @Override
            protected Integration loadIntegration() {
                return createIntegration();
            }
        };
    }

    private Flow headerAuthenticationFlow() {
        final Step headerParameterAuth = operation("logoutUser", NONE_SHAPE, NONE_SHAPE).builder()
            .connection(connection.builder()
                .putConfiguredProperty("authenticationType", AuthenticationType.apiKey.name())
                .putConfiguredProperty("authenticationParameterName", "apiKey")
                .putConfiguredProperty("authenticationParameterValue", "ENC:_key_")
                .putConfiguredProperty("authenticationParameterPlacement", "header")
                .build())
            .build();

        return new Flow.Builder()
            .addStep(direct("headerAuth"))
            .addStep(headerParameterAuth)
            .build();
    }

    private Flow oAuthAuthorizationFlow() {
        final Step apiWithOauth = operation("logoutUser", NONE_SHAPE, NONE_SHAPE).builder()
            .connection(connection.builder()
                .putConfiguredProperty("authenticationType", AuthenticationType.oauth2.name())
                .putConfiguredProperty("accessToken", "ENC:_access_token_")
                .build())
            .build();

        return new Flow.Builder()
            .addStep(direct("oauth"))
            .addStep(apiWithOauth)
            .build();
    }

    private Flow oAuthRefreshFlow() {
        final Step apiWithOauth = operation("logoutUser", NONE_SHAPE, NONE_SHAPE).builder()
            .connection(connection.builder()
                .putConfiguredProperty("authenticationType", AuthenticationType.oauth2.name())
                .putConfiguredProperty("accessToken", "ENC:_access_token_")
                .putConfiguredProperty("clientId", "client-id")
                .putConfiguredProperty("clientSecret", "ENC:_client_secret_")
                .putConfiguredProperty("refreshToken", "ENC:_refresh_token_")
                .putConfiguredProperty("accessTokenExpiresAt", String.valueOf(System.currentTimeMillis()))
                .putConfiguredProperty("authorizationEndpoint",
                    "http://localhost:" + wiremock.port() + "/oauth/authorize")
                .build())
            .build();

        return new Flow.Builder()
            .addStep(direct("oauth-refresh"))
            .addStep(apiWithOauth)
            .build();
    }

    private Flow oAuthRetryFlow() {
        final Step apiWithOauth = operation("getPetById", JSON_SCHEMA_SHAPE, JSON_SCHEMA_SHAPE).builder()
            .connection(connection.builder()
                .putConfiguredProperty("authenticationType", AuthenticationType.oauth2.name())
                .putConfiguredProperty("accessToken", "ENC:_access_token_")
                .putConfiguredProperty("clientId", "client-id")
                .putConfiguredProperty("clientSecret", "ENC:_client_secret_")
                .putConfiguredProperty("refreshToken", "ENC:_refresh_token_")
                .putConfiguredProperty("refreshTokenRetryStatuses", "401")
                .putConfiguredProperty("accessTokenExpiresAt", String.valueOf(Long.MAX_VALUE))
                .putConfiguredProperty("authorizationEndpoint",
                    "http://localhost:" + wiremock.port() + "/oauth/authorize")
                .build())
            .build();

        return new Flow.Builder()
            .addStep(direct("oauth-retry"))
            .addStep(apiWithOauth)
            .build();
    }

    private Step operation(final String operationId, final DataShape inputDataShape, final DataShape outputDataShape) {
        return new Step.Builder()
            .stepKind(StepKind.endpoint)
            .action(new ConnectorAction.Builder()
                .descriptor(new ConnectorDescriptor.Builder()
                    .putConfiguredProperty("operationId", operationId)
                    .componentScheme(REST_OPENAPI_CONNECTOR.getComponentScheme().get())
                    .connectorId(REST_OPENAPI_CONNECTOR.getId().get())
                    .inputDataShape(inputDataShape)
                    .outputDataShape(outputDataShape)
                    .build())
                .build())
            .connection(connection)
            .build();
    }

    private Flow queryParameterAuthenticationFlow() {
        final Step queryParameterAuth = operation("apiKeyLogoutUser", NONE_SHAPE, NONE_SHAPE).builder()
            .connection(connection.builder()
                .putConfiguredProperty("authenticationType", AuthenticationType.apiKey.name())
                .putConfiguredProperty("authenticationParameterName", "api_key")
                .putConfiguredProperty("authenticationParameterValue", "ENC:_key_")
                .putConfiguredProperty("authenticationParameterPlacement", "query")
                .build())
            .build();

        return new Flow.Builder()
            .addStep(direct("queryParamAuth"))
            .addStep(queryParameterAuth)
            .build();
    }

    private Flow testFlow(final String operationId, final DataShape inputDataShape, final DataShape outputDataShape) {
        return testFlow(operationId, operationId, inputDataShape, outputDataShape);
    }

    private Flow testFlow(final String operationId, final String triggerName, final DataShape inputDataShape,
        final DataShape outputDataShape) {
        return new Flow.Builder()
            .addStep(direct(triggerName))
            .addStep(operation(operationId, inputDataShape, outputDataShape))
            .build();
    }

    private static Step direct(final String name) {
        return new Step.Builder()
            .stepKind(StepKind.endpoint)
            .action(new ConnectorAction.Builder()
                .descriptor(new ConnectorDescriptor.Builder()
                    .componentScheme("direct")
                    .putConfiguredProperty("name", name)
                    .build())
                .build())
            .build();
    }

    private static Connector loadConnector() {
        try (InputStream json = RestSwaggerConnectorIntegrationTest.class
            .getResourceAsStream("/META-INF/syndesis/connector/rest-swagger.json")) {
            return Json.readFromStream(json, Connector.class);
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
    }

    private static String readSpecification() {
        try {
            return Resources.getResourceAsText("petstore.json");
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
    }

}
