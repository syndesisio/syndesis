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
package io.syndesis.server.api.generator.swagger;

import java.net.URI;
import java.util.Optional;

import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import io.swagger.models.auth.ApiKeyAuthDefinition;
import io.swagger.models.auth.BasicAuthDefinition;
import io.swagger.models.auth.OAuth2Definition;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.ConfigurationProperty.PropertyValue;
import io.syndesis.common.model.connection.ConnectorSettings;

import org.junit.Test;

import static io.syndesis.server.api.generator.swagger.PropertyGenerators.createHostUri;
import static io.syndesis.server.api.generator.swagger.PropertyGenerators.determineHost;

import static org.assertj.core.api.Assertions.assertThat;

public class PropertyGeneratorsTest {

    @Test
    public void shouldConsiderOnlyAuthorizationCodeOAuthFlows() {
        final Swagger swagger = new Swagger()
            .securityDefinition("oauth-username-password", new OAuth2Definition().password("https://api.example.com/token"))
            .securityDefinition("oauth-implicit", new OAuth2Definition().implicit("https://api.example.com/authz"))
            .securityDefinition("oauth-authorization-code", new OAuth2Definition().accessCode("https://api.example.com/token", "https://api.example.com/authz"))
            .securityDefinition("basic-auth", new BasicAuthDefinition())
            .securityDefinition("api-key", new ApiKeyAuthDefinition());

        final ConfigurationProperty template = new ConfigurationProperty.Builder().build();
        final ConnectorSettings settings = new ConnectorSettings.Builder().build();
        final Optional<ConfigurationProperty> authenticationTypes = PropertyGenerators.authenticationType
            .propertyGenerator()
            .generate(swagger, template, settings);

        assertThat(authenticationTypes)
            .contains(new ConfigurationProperty.Builder()
                .addEnum(PropertyValue.Builder.of("oauth2:oauth-authorization-code", "OAuth 2.0 - oauth-authorization-code"))
                .addEnum(PropertyValue.Builder.of("basic:basic-auth", "HTTP Basic Authentication - basic-auth"))
                .addEnum(PropertyValue.Builder.of("apiKey:api-key", "API Key - api-key"))
                .build());
    }

    @Test
    public void shouldCreateHostUri() {
        assertThat(createHostUri("scheme", "host", -1)).isEqualTo("scheme://host");
        assertThat(createHostUri("scheme", "host", 8080)).isEqualTo("scheme://host:8080");
    }

    @Test
    public void shouldDefaultToNoSecurityIfNoSupportedSecurityDefinitionsFound() {
        final Swagger swagger = new Swagger()
            .securityDefinition("oauth-username-password", new OAuth2Definition().password("https://api.example.com/token"))
            .securityDefinition("oauth-implicit", new OAuth2Definition().implicit("https://api.example.com/authz"));

        final ConfigurationProperty template = new ConfigurationProperty.Builder().build();
        final ConnectorSettings settings = new ConnectorSettings.Builder().build();
        final Optional<ConfigurationProperty> authenticationTypes = PropertyGenerators.authenticationType
            .propertyGenerator()
            .generate(swagger, template, settings);

        assertThat(authenticationTypes)
            .contains(new ConfigurationProperty.Builder()
                .defaultValue("none")
                .addEnum(ConfigurationProperty.PropertyValue.Builder.of("none", "No Security"))
                .build());
    }

    @Test
    public void shouldDetermineFromHostsContainingPorts() {
        assertThat(determineHost(new Swagger().host("54.152.43.92:8080").scheme(Scheme.HTTPS))).isEqualTo("https://54.152.43.92:8080");
    }

    @Test
    public void shouldDetermineHostFromSpecification() {
        assertThat(determineHost(new Swagger().host("api.example.com").scheme(Scheme.HTTPS))).isEqualTo("https://api.example.com");
        assertThat(determineHost(new Swagger().host("api.example.com").scheme(Scheme.HTTP).scheme(Scheme.HTTPS)))
            .isEqualTo("https://api.example.com");
        assertThat(determineHost(new Swagger().host("api.example.com").scheme(Scheme.HTTP))).isEqualTo("http://api.example.com");
    }

    @Test
    public void shouldDetermineHostFromSpecificationUrl() {
        final URI specificationUrl = URI.create("https://api.example.com/swagger.json");
        assertThat(determineHost(new Swagger().vendorExtension(BaseSwaggerConnectorGenerator.URL_EXTENSION, specificationUrl)))
            .isEqualTo("https://api.example.com");
        assertThat(
            determineHost(new Swagger().vendorExtension(BaseSwaggerConnectorGenerator.URL_EXTENSION, specificationUrl).scheme(Scheme.HTTP)))
                .isEqualTo("http://api.example.com");
        assertThat(determineHost(new Swagger().vendorExtension(BaseSwaggerConnectorGenerator.URL_EXTENSION, specificationUrl)
            .host("api2.example.com").scheme(Scheme.HTTP))).isEqualTo("http://api2.example.com");
    }

    @Test
    public void shouldDetermineSecurityDefinitionToUseFromTheConfiguredAuthenticationType() {
        final BasicAuthDefinition securityDefinition = new BasicAuthDefinition();

        final Swagger swagger = new Swagger()
            .securityDefinition("username-password", securityDefinition);

        final ConnectorSettings settings = new ConnectorSettings.Builder()
            .putConfiguredProperty(PropertyGenerators.authenticationType.name(), "basic:username-password")
            .build();

        final Optional<BasicAuthDefinition> got = PropertyGenerators.securityDefinition(swagger, settings, BasicAuthDefinition.class);
        assertThat(got).containsSame(securityDefinition);
    }

    @Test
    public void shouldDetermineSecurityDefinitionToUseFromTheConfiguredAuthenticationTypeWithName() {
        final BasicAuthDefinition securityDefinition = new BasicAuthDefinition();

        final Swagger swagger = new Swagger()
            .securityDefinition("username-password", securityDefinition);

        final ConnectorSettings settings = new ConnectorSettings.Builder()
            .putConfiguredProperty(PropertyGenerators.authenticationType.name(), "basic:username-password")
            .build();

        final Optional<BasicAuthDefinition> got = PropertyGenerators.securityDefinition(swagger, settings, BasicAuthDefinition.class);
        assertThat(got).containsSame(securityDefinition);
    }

    @Test
    public void shouldReturnNullIfNoHostGivenAnywhere() {
        assertThat(determineHost(new Swagger())).isNull();
        assertThat(determineHost(new Swagger().scheme(Scheme.HTTP))).isNull();
        assertThat(determineHost(new Swagger().host("host"))).isNull();
    }

    @Test
    public void shouldReturnNullIfNoHttpSchemesFound() {
        assertThat(determineHost(new Swagger().scheme(Scheme.WS).scheme(Scheme.WSS))).isNull();
    }

    @Test
    public void shouldTakeOnlyAuthorizationCodeOAuthFlowUrls() {
        final Swagger swagger = new Swagger()
            .securityDefinition("oauth-username-password", new OAuth2Definition().password("https://wrong.example.com/token"))
            .securityDefinition("oauth-implicit", new OAuth2Definition().implicit("https://wrong.example.com/authz"))
            .securityDefinition("oauth-authorization-code",
                new OAuth2Definition().accessCode("https://api.example.com/token", "https://api.example.com/authz"));

        final ConfigurationProperty template = new ConfigurationProperty.Builder().build();
        final ConnectorSettings settings = new ConnectorSettings.Builder()
            .putConfiguredProperty(PropertyGenerators.authenticationType.name(), "oauth2:oauth-authorization-code")
            .build();
        final Optional<ConfigurationProperty> authorizationEndpoint = PropertyGenerators.authorizationEndpoint
            .propertyGenerator()
            .generate(swagger, template, settings);

        assertThat(authorizationEndpoint)
            .contains(new ConfigurationProperty.Builder()
                .defaultValue("https://api.example.com/token")
                .build());

        final Optional<ConfigurationProperty> tokenEndpoint = PropertyGenerators.tokenEndpoint
            .propertyGenerator()
            .generate(swagger, template, settings);

        assertThat(tokenEndpoint)
            .contains(new ConfigurationProperty.Builder()
                .defaultValue("https://api.example.com/authz")
                .build());
    }
}
