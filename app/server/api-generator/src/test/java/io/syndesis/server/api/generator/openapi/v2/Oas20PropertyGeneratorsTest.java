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
package io.syndesis.server.api.generator.openapi.v2;

import java.net.URI;
import java.util.ArrayList;
import java.util.Optional;

import io.apicurio.datamodels.core.models.Extension;
import io.apicurio.datamodels.openapi.v2.models.Oas20Document;
import io.apicurio.datamodels.openapi.v2.models.Oas20SecurityScheme;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.ConfigurationProperty.PropertyValue;
import io.syndesis.common.model.connection.ConnectorSettings;
import io.syndesis.server.api.generator.openapi.OpenApiModelInfo;
import io.syndesis.server.api.generator.openapi.OpenApiSecurityScheme;
import io.syndesis.server.api.generator.openapi.util.OasModelHelper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Oas20PropertyGeneratorsTest {

    private static final Oas20PropertyGenerators GENERATOR = new Oas20PropertyGenerators();

    @Test
    public void shouldConsiderOnlyAuthorizationCodeOAuthFlows() {
        final Oas20Document openApiDoc = new Oas20Document();
        openApiDoc.securityDefinitions = openApiDoc.createSecurityDefinitions();
        openApiDoc.securityDefinitions.addSecurityScheme("oauth-username-password", oauth2SecurityScheme("oauth-username-password", "password", "https://api.example.com/token", null));
        openApiDoc.securityDefinitions.addSecurityScheme("oauth-implicit", oauth2SecurityScheme("oauth-implicit", "implicit", null, "https://api.example.com/authz"));
        openApiDoc.securityDefinitions.addSecurityScheme("oauth-authorization-code", oauth2SecurityScheme("oauth-authorization-code", "accessCode","https://api.example.com/token", "https://api.example.com/authz"));
        openApiDoc.securityDefinitions.addSecurityScheme("basic-auth", basicAuthSecurityScheme("basic-auth"));
        openApiDoc.securityDefinitions.addSecurityScheme("api-key", apiKeySecurityScheme("api-key"));

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
        final Oas20Document openApiDoc = new Oas20Document();
        openApiDoc.securityDefinitions = openApiDoc.createSecurityDefinitions();
        openApiDoc.securityDefinitions.addSecurityScheme("oauth-username-password", oauth2SecurityScheme("oauth-username-password", "password", "https://api.example.com/token", null));
        openApiDoc.securityDefinitions.addSecurityScheme("oauth-implicit", oauth2SecurityScheme("oauth-implicit", "implicit", null, "https://api.example.com/authz"));

        final ConfigurationProperty template = new ConfigurationProperty.Builder().build();
        final ConnectorSettings settings = new ConnectorSettings.Builder().build();
        final Optional<ConfigurationProperty> authenticationTypes = GENERATOR.forProperty("authenticationType")
            .generate(new OpenApiModelInfo.Builder().model(openApiDoc).build(), template, settings);

        assertThat(authenticationTypes)
            .contains(new ConfigurationProperty.Builder()
                .defaultValue("none")
                .addEnum(ConfigurationProperty.PropertyValue.Builder.of("none", "No Security"))
                .build());
    }

    @Test
    public void shouldDetermineFromHostsContainingPorts() {
        Oas20Document openApiDoc = new Oas20Document();
        openApiDoc.host = "54.152.43.92:8080";
        openApiDoc.schemes = new ArrayList<>();
        openApiDoc.schemes.add("https");
        assertThat(GENERATOR.determineHost(new OpenApiModelInfo.Builder().model(openApiDoc).build())).isEqualTo("https://54.152.43.92:8080");
    }

    @Test
    public void shouldDetermineHostFromSpecification() {
        Oas20Document openApiDoc = new Oas20Document();
        openApiDoc.host = "api.example.com";
        openApiDoc.schemes = new ArrayList<>();
        openApiDoc.schemes.add("https");
        assertThat(GENERATOR.determineHost(new OpenApiModelInfo.Builder().model(openApiDoc).build())).isEqualTo("https://api.example.com");

        openApiDoc.schemes.add("http");
        assertThat(GENERATOR.determineHost(new OpenApiModelInfo.Builder().model(openApiDoc).build())).isEqualTo("https://api.example.com");

        openApiDoc.schemes.remove("https");
        assertThat(GENERATOR.determineHost(new OpenApiModelInfo.Builder().model(openApiDoc).build())).isEqualTo("http://api.example.com");
    }

    @Test
    public void shouldDetermineHostFromSpecificationUrl() {
        final URI specificationUrl = URI.create("https://api.example.com/swagger.json");
        Oas20Document openApiDoc = new Oas20Document();
        Extension extension = new Extension();
        extension.name = OasModelHelper.URL_EXTENSION;
        extension.value = specificationUrl;
        openApiDoc.addExtension(OasModelHelper.URL_EXTENSION, extension);

        assertThat(GENERATOR.determineHost(new OpenApiModelInfo.Builder().model(openApiDoc).build())).isEqualTo("https://api.example.com");
        openApiDoc.schemes = new ArrayList<>();
        openApiDoc.schemes.add("http");
        assertThat(GENERATOR.determineHost(new OpenApiModelInfo.Builder().model(openApiDoc).build())).isEqualTo("http://api.example.com");

        openApiDoc.host = "api2.example.com";
        assertThat(GENERATOR.determineHost(new OpenApiModelInfo.Builder().model(openApiDoc).build())).isEqualTo("http://api2.example.com");
    }

    @Test
    public void shouldDetermineSecurityDefinitionToUseFromTheConfiguredAuthenticationType() {
        final Oas20SecurityScheme securityScheme = basicAuthSecurityScheme("username-password");

        final Oas20Document openApiDoc = new Oas20Document();
        openApiDoc.securityDefinitions = openApiDoc.createSecurityDefinitions();
        openApiDoc.securityDefinitions.addSecurityScheme("username-password", securityScheme);

        final ConnectorSettings settings = new ConnectorSettings.Builder()
            .putConfiguredProperty("authenticationType", "basic:username-password")
            .build();

        final Optional<Oas20SecurityScheme> got = GENERATOR.securityDefinition(
            new OpenApiModelInfo.Builder().model(openApiDoc).build(), settings, OpenApiSecurityScheme.BASIC);
        assertThat(got).containsSame(securityScheme);
    }

    @Test
    public void shouldDetermineSecurityDefinitionToUseFromTheConfiguredAuthenticationTypeWithName() {
        final Oas20SecurityScheme securityScheme = basicAuthSecurityScheme("username-password");

        final Oas20Document openApiDoc = new Oas20Document();
        openApiDoc.securityDefinitions = openApiDoc.createSecurityDefinitions();
        openApiDoc.securityDefinitions.addSecurityScheme("username-password", securityScheme);

        final ConnectorSettings settings = new ConnectorSettings.Builder()
            .putConfiguredProperty("authenticationType", "basic:username-password")
            .build();

        final Optional<Oas20SecurityScheme> got = GENERATOR.securityDefinition(
            new OpenApiModelInfo.Builder().model(openApiDoc).build(), settings, OpenApiSecurityScheme.BASIC);
        assertThat(got).containsSame(securityScheme);
    }

    @Test
    public void shouldReturnNullIfNoHostGivenAnywhere() {
        Oas20Document openApiDoc = new Oas20Document();
        assertThat(GENERATOR.determineHost(new OpenApiModelInfo.Builder().model(openApiDoc).build())).isNull();

        openApiDoc.schemes = new ArrayList<>();
        openApiDoc.schemes.add("http");
        assertThat(GENERATOR.determineHost(new OpenApiModelInfo.Builder().model(openApiDoc).build())).isNull();

        openApiDoc = new Oas20Document();
        openApiDoc.host = "host";
        assertThat(GENERATOR.determineHost(new OpenApiModelInfo.Builder().model(openApiDoc).build())).isNull();
    }

    @Test
    public void shouldReturnNullIfNoHttpSchemesFound() {
        Oas20Document openApiDoc = new Oas20Document();
        openApiDoc.schemes = new ArrayList<>();
        openApiDoc.schemes.add("ws");
        openApiDoc.schemes.add("wss");

        assertThat(GENERATOR.determineHost(new OpenApiModelInfo.Builder().model(openApiDoc).build())).isNull();
    }

    @Test
    public void shouldTakeOnlyAuthorizationCodeOAuthFlowUrls() {
        final Oas20Document openApiDoc = new Oas20Document();
        openApiDoc.securityDefinitions = openApiDoc.createSecurityDefinitions();
        openApiDoc.securityDefinitions.addSecurityScheme("oauth-username-password",
            oauth2SecurityScheme("oauth-username-password", "password", "https://wrong.example.com/token", null));
        openApiDoc.securityDefinitions.addSecurityScheme("oauth-implicit",
            oauth2SecurityScheme("oauth-implicit", "implicit", null, "https://wrong.example.com/authz"));
        openApiDoc.securityDefinitions.addSecurityScheme("oauth-authorization-code",
            oauth2SecurityScheme("oauth-authorization-code", "accessCode","https://api.example.com/token", "https://api.example.com/authz"));

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

    private static Oas20SecurityScheme oauth2SecurityScheme(String name, String flow, String tokenUrl, String authorizationUrl) {
        Oas20SecurityScheme securityScheme = new Oas20SecurityScheme(name);
        securityScheme.type = OpenApiSecurityScheme.OAUTH2.getName();
        securityScheme.tokenUrl = tokenUrl;
        securityScheme.authorizationUrl = authorizationUrl;
        securityScheme.flow = flow;
        return  securityScheme;
    }

    private static Oas20SecurityScheme basicAuthSecurityScheme(String name) {
        Oas20SecurityScheme securityScheme = new Oas20SecurityScheme(name);
        securityScheme.type = OpenApiSecurityScheme.BASIC.getName();
        return  securityScheme;
    }

    private static Oas20SecurityScheme apiKeySecurityScheme(String name) {
        Oas20SecurityScheme securityScheme = new Oas20SecurityScheme(name);
        securityScheme.type = OpenApiSecurityScheme.API_KEY.getName();
        return  securityScheme;
    }
}
