/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.connector.generator.swagger;

import java.io.IOException;
import java.util.Optional;

import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;
import io.swagger.parser.SwaggerParser;
import io.syndesis.connector.generator.ActionsSummary;
import io.syndesis.connector.generator.ConnectorSummary;
import io.syndesis.model.connection.ConfigurationProperty;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.connection.ConnectorSettings;

import org.junit.Test;

import static io.syndesis.connector.generator.swagger.TestHelper.resource;

import static org.assertj.core.api.Assertions.assertThat;

public class SwaggerConnectorGeneratorTest extends SwaggerConnectorGeneratorBaseTest {

    @Test
    public void shouldCreatePropertyParametersFromPetstoreSwagger() throws IOException {
        final String specification = resource("/swagger/petstore.swagger.json");
        final Swagger swagger = new SwaggerParser().parse(specification);

        final Parameter petIdPathParameter = swagger.getPath("/pet/{petId}").getGet().getParameters().get(0);

        final Optional<ConfigurationProperty> maybeConfigurationProperty = SwaggerConnectorGenerator
            .createPropertyFromParameter(petIdPathParameter);

        final ConfigurationProperty expected = new ConfigurationProperty.Builder()//
            .componentProperty(false)//
            .deprecated(false)//
            .description("ID of pet to return")//
            .displayName("petId")//
            .group("producer")//
            .javaType(Long.class.getName())//
            .kind("property")//
            .required(true)//
            .secret(false)//
            .type("integer")//
            .build();

        assertThat(maybeConfigurationProperty).hasValue(expected);
    }

    @Test
    public void shouldCreateSecurityConfigurationFromReverbSwagger() throws IOException {
        final String specification = resource("/swagger/reverb.swagger.json");

        final ConnectorSettings connectorSettings = new ConnectorSettings.Builder()//
            .name("Reverb API")//
            .description("Invokes Reverb API")//
            .icon("fa-music")//
            .putConfiguredProperty("specification", specification)//
            .build();

        final Connector generated = new SwaggerConnectorGenerator().generate(SWAGGER_TEMPLATE, connectorSettings);

        assertThat(generated.getProperties().keySet()).contains("accessToken", "accessTokenUrl", "clientId", "clientSecret");
        assertThat(generated.getProperties().get("authenticationType").getEnum())
            .containsExactly(new ConfigurationProperty.PropertyValue.Builder().value("oauth2").label("OAuth 2.0").build());
    }

    @Test
    public void shouldIncorporateGivenConfiguredProperties() throws IOException {
        final String specification = resource("/swagger/reverb.swagger.json");

        final ConnectorSettings connectorSettings = new ConnectorSettings.Builder()//
            .name("Reverb API")//
            .description("Invokes Reverb API")//
            .icon("fa-music")//
            .putConfiguredProperty("specification", specification)//
            .putConfiguredProperty("accessTokenUrl", "http://some.token.url").build();

        final Connector connector = new SwaggerConnectorGenerator().generate(SWAGGER_TEMPLATE, connectorSettings);

        assertThat(connector.getConfiguredProperties()).containsEntry("accessTokenUrl", "http://some.token.url");
    }

    @Test
    public void shouldProvideInfoFromPetstoreSwagger() throws IOException {
        final String specification = resource("/swagger/petstore.swagger.json");

        final ConnectorSettings connectorSettings = new ConnectorSettings.Builder()//
            .putConfiguredProperty("specification", specification)//
            .build();

        final ConnectorSummary summary = new SwaggerConnectorGenerator().info(SWAGGER_TEMPLATE, connectorSettings);

        final ActionsSummary actionsSummary = new ActionsSummary.Builder().totalActions(20).putActionCountByTag("store", 4)
            .putActionCountByTag("user", 8).putActionCountByTag("pet", 8).build();

        final ConnectorSummary expected = new ConnectorSummary.Builder()//
            .name("Swagger Petstore")//
            .actionsSummary(actionsSummary)//
            .build();
        assertThat(summary).isEqualToIgnoringGivenFields(expected, "description", "properties");
        assertThat(summary.getDescription()).startsWith("This is a sample server Petstore server");
        assertThat(summary.getProperties().keySet()).contains("host", "basePath", "authenticationType", "clientId", "clientSecret",
            "accessToken", "accessTokenUrl", "operationId", "specification");
    }

}
