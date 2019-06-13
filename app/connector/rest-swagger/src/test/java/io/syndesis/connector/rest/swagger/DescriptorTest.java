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
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.syndesis.common.model.Dependency;
import io.syndesis.common.model.Dependency.Type;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.ConfigurationProperty.PropertyValue;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.util.Json;
import io.syndesis.connector.rest.swagger.auth.apikey.ApiKey.Placement;

import org.apache.camel.CamelContext;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DescriptorTest {

    private static final String CAMEL_VERSION = CamelContext.class.getPackage().getImplementationVersion();

    private static final String SYNDESIS_VERSION = ResourceBundle.getBundle("version").getString("version");

    @Test
    public void descriptorShouldConformToExpected() throws IOException {
        final Connector expected = new Connector.Builder()
            .id("rest-swagger")
            .connectorFactory(ConnectorFactory.class.getName())
            .actions(new ArrayList<>())
            .icon("")
            .description("Meta connector to generate swagger client connectors")
            .name("OpenAPI client")
            .componentScheme("rest-swagger")
            .putProperty("accessToken", new ConfigurationProperty.Builder()
                .displayName("OAuth access token")
                .description("OAuth Access token")
                .type("string")
                .javaType("java.lang.String")
                .order(4)
                .secret(Boolean.TRUE)
                .build())
            .putProperty("accessTokenExpiresAt", new ConfigurationProperty.Builder()
                .description("Seconds in UTC when the access token expires")
                .javaType("java.lang.Long")
                .type("hidden")
                .build())
            .putProperty("authenticationParameterName", new ConfigurationProperty.Builder()
                .description("Name of the API key parameter")
                .displayName("API key parameter name")
                .javaType("java.lang.String")
                .order(2)
                .required(true)
                .type("hidden")
                .build())
            .putProperty("authenticationParameterPlacement", new ConfigurationProperty.Builder()
                .displayName("Placement of the API key parameter")
                .addAllEnum(Stream.of(Placement.values())
                    .sorted()
                    .map(p -> new PropertyValue.Builder().label(p.toString()).value(p.toString()).build())::iterator)
                .javaType("java.lang.String")
                .order(4)
                .required(true)
                .type("hidden")
                .build())
            .putProperty("authenticationParameterValue", new ConfigurationProperty.Builder()
                .description("Value of the API key authentication parameter")
                .displayName("API key")
                .javaType("java.lang.String")
                .order(3)
                .required(true)
                .secret(true)
                .type("string")
                .build())
            .putProperty("authenticationType", new ConfigurationProperty.Builder()
                .displayName("Authentication type")
                .type("hidden")
                .javaType("java.lang.String")
                .order(1)
                .addAllEnum(Stream.of(AuthenticationType.values())
                    .sorted()
                    .map(v -> PropertyValue.Builder.of(v.name(), v.name()))
                    .collect(Collectors.toList()))
                .build())
            .putProperty("authorizationEndpoint", new ConfigurationProperty.Builder()
                .displayName("OAuth Authorization Endpoint URL")
                .description("URL for the start of the OAuth flow")
                .type("string")
                .javaType("java.lang.String")
                .required(Boolean.TRUE)
                .order(6)
                .build())
            .putProperty("authorizeUsingParameters", new ConfigurationProperty.Builder()
                .description("Should the implementation send client id and client secret when performing OAuth flow")
                .javaType("java.lang.String")
                .type("hidden")
                .build())
            .putProperty("basePath", new ConfigurationProperty.Builder()
                .displayName("Base path")
                .description(
                    "API basePath for example /v2. Default is unset if set overrides the value present in OpenAPI document.")
                .type("string")
                .javaType("java.lang.String")
                .order(11)
                .build())
            .putProperty("clientId", new ConfigurationProperty.Builder()
                .displayName("OAuth Client ID")
                .description("OAuth Client ID, sometimes called Consumer Key")
                .type("string")
                .javaType("java.lang.String")
                .order(2)
                .build())
            .putProperty("clientSecret", new ConfigurationProperty.Builder()
                .displayName("OAuth Client Secret")
                .description("OAuth Client Secret, sometimes called Consumer Secret")
                .type("string")
                .javaType("java.lang.String")
                .secret(Boolean.TRUE)
                .order(3)
                .build())
            .putProperty("host", new ConfigurationProperty.Builder()
                .displayName("Host")
                .description(
                    "Scheme hostname and port to direct the HTTP requests to in the form of https://hostname:port.")
                .type("string")
                .javaType("java.lang.String")
                .required(Boolean.TRUE)
                .order(10)
                .build())
            .putProperty("oauthScopes", new ConfigurationProperty.Builder()
                .displayName("OAuth Scopes")
                .description("OAuth scopes needed for the API.")
                .type("string")
                .javaType("java.lang.String")
                .order(8)
                .build())
            .putProperty("operationId", new ConfigurationProperty.Builder()
                .displayName("Operation ID")
                .description("ID of the operation from the OpenAPI document.")
                .required(Boolean.TRUE)
                .type("hidden")
                .javaType("java.lang.String")
                .build())
            .putProperty("password", new ConfigurationProperty.Builder()
                .displayName("Password")
                .description("Password to authenticate with.")
                .type("string")
                .javaType("java.lang.String")
                .required(Boolean.TRUE)
                .secret(Boolean.TRUE)
                .order(3)
                .build())
            .putProperty("refreshToken", new ConfigurationProperty.Builder()
                .displayName("OAuth Refresh token")
                .description("OAuth Refresh token.")
                .type("string")
                .javaType("java.lang.String")
                .secret(Boolean.TRUE)
                .order(5)
                .build())
            .putProperty("refreshTokenRetryStatuses", new ConfigurationProperty.Builder()
                .displayName("HTTP statuses for refreshing OAuth token")
                .description(
                    "Comma separated list of HTTP statuses for which to refresh the OAuth access token using the refresh token.")
                .type("hidden")
                .javaType("java.lang.String")
                .build())
            .putProperty("specification", new ConfigurationProperty.Builder()
                .displayName("Document")
                .description("OpenAPI document defining the service.")
                .required(Boolean.TRUE)
                .type("hidden")
                .javaType("java.lang.String")
                .build())
            .putProperty("tokenEndpoint", new ConfigurationProperty.Builder()
                .displayName("OAuth Token Endpoint URL")
                .description("URL to fetch the OAuth Access token.")
                .type("string")
                .javaType("java.lang.String")
                .order(7)
                .build())
            .putProperty("tokenStrategy", new ConfigurationProperty.Builder()
                .displayName("OAuth Token strategy")
                .type("hidden")
                .javaType("java.lang.String")
                .build())
            .putProperty("username", new ConfigurationProperty.Builder()
                .displayName("Username")
                .description("Username to authenticate with.")
                .type("string")
                .javaType("java.lang.String")
                .required(Boolean.TRUE)
                .order(2)
                .build())
            .putConfiguredProperty("componentName", "connector-rest-swagger-http4")
            .addConnectorCustomizer(SpecificationResourceCustomizer.class.getName())
            .addConnectorCustomizer(AuthenticationCustomizer.class.getName())
            .addConnectorCustomizer(RequestCustomizer.class.getName())
            .addConnectorCustomizer(ResponseCustomizer.class.getName())
            .putMetadata("hide-from-connection-pages", "true")
            .addDependencies(
                mavenDependency("io.syndesis.connector:connector-rest-swagger:" + SYNDESIS_VERSION),
                mavenDependency("org.apache.camel:camel-rest-swagger:" + CAMEL_VERSION),
                mavenDependency("org.apache.camel:camel-http4:" + CAMEL_VERSION))
            .build();

        final Connector defined;
        try (InputStream stream = DescriptorTest.class
            .getResourceAsStream("/META-INF/syndesis/connector/rest-swagger.json")) {
            defined = Json.readFromStream(stream, Connector.class);
        }

        assertThat(defined).isEqualTo(expected);
    }

    static Dependency mavenDependency(final String id) {
        return new Dependency.Builder()
            .id(id)
            .type(Type.MAVEN)
            .build();
    }
}
