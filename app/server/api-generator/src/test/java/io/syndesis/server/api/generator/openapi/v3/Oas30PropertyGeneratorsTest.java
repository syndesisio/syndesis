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
package io.syndesis.server.api.generator.openapi.v3;

import java.net.URI;
import java.util.ArrayList;
import java.util.Optional;

import io.apicurio.datamodels.core.models.Extension;
import io.apicurio.datamodels.core.models.common.Server;
import io.apicurio.datamodels.core.models.common.ServerVariable;
import io.apicurio.datamodels.openapi.v3.models.Oas30Document;
import io.apicurio.datamodels.openapi.v3.models.Oas30SecurityScheme;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.ConfigurationProperty.PropertyValue;
import io.syndesis.common.model.connection.ConnectorSettings;
import io.syndesis.server.api.generator.openapi.OpenApiModelInfo;
import io.syndesis.server.api.generator.openapi.OpenApiSecurityScheme;
import io.syndesis.server.api.generator.openapi.util.OasModelHelper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Oas30PropertyGeneratorsTest {

    private static final Oas30PropertyGenerators GENERATOR = new Oas30PropertyGenerators();

    @Test
    public void shouldConsiderOnlyAuthorizationCodeOAuthFlows() {
        final Oas30Document openApiDoc = new Oas30Document();
        openApiDoc.components = openApiDoc.createComponents();
        openApiDoc.components.addSecurityScheme("oauth-username-password", oauth2SecurityScheme("oauth-username-password", "password", "https://api.example.com/token", null));
        openApiDoc.components.addSecurityScheme("oauth-implicit", oauth2SecurityScheme("oauth-implicit", "implicit", null, "https://api.example.com/authz"));
        openApiDoc.components.addSecurityScheme("oauth-authorization-code", oauth2SecurityScheme("oauth-authorization-code", "authorizationCode","https://api.example.com/token", "https://api.example.com/authz"));
        openApiDoc.components.addSecurityScheme("basic-auth", basicAuthSecurityScheme("basic-auth"));
        openApiDoc.components.addSecurityScheme("api-key", apiKeySecurityScheme("api-key"));

        final ConfigurationProperty template = new ConfigurationProperty.Builder().build();
        final ConnectorSettings settings = new ConnectorSettings.Builder().build();
        final Optional<ConfigurationProperty> authenticationTypes = GENERATOR.forProperty("authenticationType")
            .generate(new OpenApiModelInfo.Builder().model(openApiDoc).build(), template, settings);

        assertThat(authenticationTypes)
            .contains(new ConfigurationProperty.Builder()
                .addEnum(PropertyValue.Builder.of("oauth2:oauth-authorization-code", "OAuth 2.0 - oauth-authorization-code"))
                .addEnum(PropertyValue.Builder.of("basic:basic-auth", "HTTP Basic Authentication - basic-auth"))
                .addEnum(PropertyValue.Builder.of("apiKey:api-key", "API Key - api-key"))
                .build());
    }

    @Test
    public void shouldDefaultToNoSecurityIfNoSupportedSecurityDefinitionsFound() {
        final Oas30Document openApiDoc = new Oas30Document();
        openApiDoc.components = openApiDoc.createComponents();
        openApiDoc.components.addSecurityScheme("oauth-username-password", oauth2SecurityScheme("oauth-username-password", "password", "https://api.example.com/token", null));
        openApiDoc.components.addSecurityScheme("oauth-implicit", oauth2SecurityScheme("oauth-implicit", "implicit", null, "https://api.example.com/authz"));

        final ConfigurationProperty template = new ConfigurationProperty.Builder().build();
        final ConnectorSettings settings = new ConnectorSettings.Builder().build();
        final Optional<ConfigurationProperty> authenticationTypes = GENERATOR.forProperty("authenticationType")
            .generate(new OpenApiModelInfo.Builder().model(openApiDoc).build(), template, settings);

        assertThat(authenticationTypes)
            .contains(new ConfigurationProperty.Builder()
                .defaultValue("none")
                .addEnum(PropertyValue.Builder.of("none", "No Security"))
                .build());
    }

    @Test
    public void shouldDetermineFromHostsContainingPorts() {
        Oas30Document openApiDoc = new Oas30Document();
        openApiDoc.addServer("https://54.152.43.92:8080", "TestServer");
        assertThat(GENERATOR.determineHost(new OpenApiModelInfo.Builder().model(openApiDoc).build())).isEqualTo("https://54.152.43.92");
    }

    @Test
    public void shouldDetermineHostFromSpecification() {
        Oas30Document openApiDoc = new Oas30Document();
        Server server = openApiDoc.addServer("{scheme}://api.example.com", "TestServer");
        ServerVariable schemes = server.createServerVariable("scheme");
        schemes.default_ = "https";
        schemes.enum_ = new ArrayList<>();
        schemes.enum_.add("https");
        server.addServerVariable("scheme", schemes);
        assertThat(GENERATOR.determineHost(new OpenApiModelInfo.Builder().model(openApiDoc).build())).isEqualTo("https://api.example.com");

        schemes.enum_.add("http");
        assertThat(GENERATOR.determineHost(new OpenApiModelInfo.Builder().model(openApiDoc).build())).isEqualTo("https://api.example.com");

        schemes.default_ = "http";
        assertThat(GENERATOR.determineHost(new OpenApiModelInfo.Builder().model(openApiDoc).build())).isEqualTo("http://api.example.com");
    }

    @Test
    public void shouldDetermineHostFromSpecificationUrl() {
        final URI specificationUrl = URI.create("https://api.example.com/swagger.json");
        Oas30Document openApiDoc = new Oas30Document();
        Extension extension = new Extension();
        extension.name = OasModelHelper.URL_EXTENSION;
        extension.value = specificationUrl;
        openApiDoc.addExtension(OasModelHelper.URL_EXTENSION, extension);

        assertThat(GENERATOR.determineHost(new OpenApiModelInfo.Builder().model(openApiDoc).build())).isEqualTo("https://api.example.com");
        Server server = openApiDoc.addServer("{scheme}://api.example.com", "TestServer");
        ServerVariable schemes = server.createServerVariable("scheme");
        schemes.default_ = "http";
        schemes.enum_ = new ArrayList<>();
        schemes.enum_.add("http");
        server.addServerVariable("scheme", schemes);
        assertThat(GENERATOR.determineHost(new OpenApiModelInfo.Builder().model(openApiDoc).build())).isEqualTo("http://api.example.com");

        server.url = "{scheme}://api2.example.com";
        assertThat(GENERATOR.determineHost(new OpenApiModelInfo.Builder().model(openApiDoc).build())).isEqualTo("http://api2.example.com");
    }

    @Test
    public void shouldDetermineSecurityDefinitionToUseFromTheConfiguredAuthenticationType() {
        final Oas30SecurityScheme securityScheme = basicAuthSecurityScheme("username-password");

        final Oas30Document openApiDoc = new Oas30Document();
        openApiDoc.components = openApiDoc.createComponents();
        openApiDoc.components.addSecurityScheme("username-password", securityScheme);

        final ConnectorSettings settings = new ConnectorSettings.Builder()
            .putConfiguredProperty("authenticationType", "basic:username-password")
            .build();

        final Optional<Oas30SecurityScheme> got = GENERATOR.securityDefinition(
            new OpenApiModelInfo.Builder().model(openApiDoc).build(), settings, OpenApiSecurityScheme.BASIC);
        assertThat(got).containsSame(securityScheme);
    }

    @Test
    public void shouldDetermineSecurityDefinitionToUseFromTheConfiguredAuthenticationTypeWithName() {
        final Oas30SecurityScheme securityScheme = basicAuthSecurityScheme("username-password");

        final Oas30Document openApiDoc = new Oas30Document();
        openApiDoc.components = openApiDoc.createComponents();
        openApiDoc.components.addSecurityScheme("username-password", securityScheme);

        final ConnectorSettings settings = new ConnectorSettings.Builder()
            .putConfiguredProperty("authenticationType", "basic:username-password")
            .build();

        final Optional<Oas30SecurityScheme> got = GENERATOR.securityDefinition(
            new OpenApiModelInfo.Builder().model(openApiDoc).build(), settings, OpenApiSecurityScheme.BASIC);
        assertThat(got).containsSame(securityScheme);
    }

    @Test
    public void shouldReturnNullIfNoHostGivenAnywhere() {
        Oas30Document openApiDoc = new Oas30Document();
        assertThat(GENERATOR.determineHost(new OpenApiModelInfo.Builder().model(openApiDoc).build())).isNull();

        openApiDoc.addServer("/v1", "TestServer");
        assertThat(GENERATOR.determineHost(new OpenApiModelInfo.Builder().model(openApiDoc).build())).isNull();
    }

    @Test
    public void shouldReturnNullIfNoHttpSchemesFound() {
        Oas30Document openApiDoc = new Oas30Document();
        Server server = openApiDoc.addServer("{scheme}://api.example.com", "TestServer");
        ServerVariable schemes = server.createServerVariable("scheme");
        schemes.default_ = "ws";
        schemes.enum_ = new ArrayList<>();
        schemes.enum_.add("ws");
        schemes.enum_.add("wss");
        server.addServerVariable("scheme", schemes);
        assertThat(GENERATOR.determineHost(new OpenApiModelInfo.Builder().model(openApiDoc).build())).isNull();
    }

    @Test
    public void shouldTakeOnlyAuthorizationCodeOAuthFlowUrls() {
        final Oas30Document openApiDoc = new Oas30Document();
        openApiDoc.components = openApiDoc.createComponents();
        openApiDoc.components.addSecurityScheme("oauth-username-password",
            oauth2SecurityScheme("oauth-username-password", "password", "https://wrong.example.com/token", null));
        openApiDoc.components.addSecurityScheme("oauth-implicit",
            oauth2SecurityScheme("oauth-implicit", "implicit", null, "https://wrong.example.com/authz"));
        openApiDoc.components.addSecurityScheme("oauth-authorization-code",
            oauth2SecurityScheme("oauth-authorization-code", "authorizationCode","https://api.example.com/token", "https://api.example.com/authz"));

        final ConfigurationProperty template = new ConfigurationProperty.Builder().build();
        final ConnectorSettings settings = new ConnectorSettings.Builder()
            .putConfiguredProperty("authenticationType", "oauth2:oauth-authorization-code")
            .build();
        final Optional<ConfigurationProperty> authorizationEndpoint = GENERATOR.forProperty("authorizationEndpoint")
            .generate(new OpenApiModelInfo.Builder().model(openApiDoc).build(), template, settings);

        assertThat(authorizationEndpoint)
            .contains(new ConfigurationProperty.Builder()
                .defaultValue("https://api.example.com/authz")
                .build());

        final Optional<ConfigurationProperty> tokenEndpoint = GENERATOR.forProperty("tokenEndpoint")
            .generate(new OpenApiModelInfo.Builder().model(openApiDoc).build(), template, settings);

        assertThat(tokenEndpoint)
            .contains(new ConfigurationProperty.Builder()
                .defaultValue("https://api.example.com/token")
                .build());
    }

    private static Oas30SecurityScheme oauth2SecurityScheme(String name, String flow, String tokenUrl, String authorizationUrl) {
        Oas30SecurityScheme securityScheme = new Oas30SecurityScheme(name);
        securityScheme.type = OpenApiSecurityScheme.OAUTH2.getName();

        securityScheme.flows = securityScheme.createOAuthFlows();

        if ("authorizationCode".equals(flow)) {
            securityScheme.flows.authorizationCode = securityScheme.flows.createAuthorizationCodeOAuthFlow();
            securityScheme.flows.authorizationCode.tokenUrl = tokenUrl;
            securityScheme.flows.authorizationCode.authorizationUrl = authorizationUrl;
        }

        if ("clientCredentials".equals(flow)) {
            securityScheme.flows.clientCredentials = securityScheme.flows.createClientCredentialsOAuthFlow();
            securityScheme.flows.clientCredentials.tokenUrl = tokenUrl;
            securityScheme.flows.clientCredentials.authorizationUrl = authorizationUrl;
        }

        if ("password".equals(flow)) {
            securityScheme.flows.password = securityScheme.flows.createPasswordOAuthFlow();
            securityScheme.flows.password.tokenUrl = tokenUrl;
            securityScheme.flows.password.authorizationUrl = authorizationUrl;
        }

        if ("implicit".equals(flow)) {
            securityScheme.flows.implicit = securityScheme.flows.createImplicitOAuthFlow();
            securityScheme.flows.implicit.tokenUrl = tokenUrl;
            securityScheme.flows.implicit.authorizationUrl = authorizationUrl;
        }

        return  securityScheme;
    }

    private static Oas30SecurityScheme basicAuthSecurityScheme(String name) {
        Oas30SecurityScheme securityScheme = new Oas30SecurityScheme(name);
        securityScheme.type = OpenApiSecurityScheme.BASIC.getName();
        return  securityScheme;
    }

    private static Oas30SecurityScheme apiKeySecurityScheme(String name) {
        Oas30SecurityScheme securityScheme = new Oas30SecurityScheme(name);
        securityScheme.type = OpenApiSecurityScheme.API_KEY.getName();
        return  securityScheme;
    }
}
