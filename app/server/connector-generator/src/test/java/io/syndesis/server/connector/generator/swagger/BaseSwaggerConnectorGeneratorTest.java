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
package io.syndesis.server.connector.generator.swagger;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import io.swagger.models.Info;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;
import io.swagger.parser.SwaggerParser;
import io.syndesis.common.model.action.ActionsSummary;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.connection.ConnectorSettings;
import io.syndesis.common.model.connection.ConnectorSummary;
import io.syndesis.server.connector.generator.swagger.util.SwaggerHelper;

import org.junit.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;

import static io.syndesis.server.connector.generator.swagger.TestHelper.reformatJson;
import static io.syndesis.server.connector.generator.swagger.TestHelper.resource;

import static org.assertj.core.api.Assertions.assertThat;

public class BaseSwaggerConnectorGeneratorTest extends AbstractSwaggerConnectorTest {

    private final BaseSwaggerConnectorGenerator generator = new BaseSwaggerConnectorGenerator() {
        @Override
        ConnectorDescriptor.Builder createDescriptor(final ObjectNode json, final Swagger swagger, final Operation operation) {
            return new ConnectorDescriptor.Builder();
        }
    };

    @Test
    public void shouldCreatePropertyParametersFromPetstoreSwagger() throws IOException {
        final String specification = resource("/swagger/petstore.swagger.json");
        final Swagger swagger = new SwaggerParser().parse(specification);

        final Parameter petIdPathParameter = swagger.getPath("/pet/{petId}").getGet().getParameters().get(0);

        final Optional<ConfigurationProperty> maybeConfigurationProperty = BaseSwaggerConnectorGenerator
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
    public void shouldCreateSecurityConfigurationFromConcurSwagger() throws IOException {
        final String specification = resource("/swagger/concur.swagger.json");

        final ConnectorSettings connectorSettings = new ConnectorSettings.Builder()//
            .name("Concur List API")//
            .description("Invokes Concur List API")//
            .icon("fa-globe")//
            .putConfiguredProperty("specification", specification)//
            .build();

        final Connector generated = generator.generate(SWAGGER_TEMPLATE, connectorSettings);

        assertThat(generated.getProperties().keySet()).contains("accessToken", "authorizationEndpoint", "tokenEndpoint", "clientId",
            "clientSecret", "tokenStrategy", "authorizeUsingParameters");
        assertThat(generated.getProperties().get("tokenStrategy").getDefaultValue()).isEqualTo("AUTHORIZATION_HEADER");
        assertThat(generated.getProperties().get("authorizeUsingParameters").getDefaultValue()).isEqualTo("true");
    }

    @Test
    public void shouldCreateSecurityConfigurationFromReverbSwagger() throws IOException {
        final String specification = resource("/swagger/reverb.swagger.yaml");

        final ConnectorSettings connectorSettings = new ConnectorSettings.Builder()//
            .name("Reverb API")//
            .description("Invokes Reverb API")//
            .icon("fa-music")//
            .putConfiguredProperty("specification", specification)//
            .build();

        final Connector generated = generator.generate(SWAGGER_TEMPLATE, connectorSettings);

        assertThat(generated.getProperties().keySet()).contains("accessToken", "authorizationEndpoint", "tokenEndpoint", "clientId",
            "clientSecret");
        assertThat(generated.getProperties().get("authenticationType").getEnum())
            .containsExactly(new ConfigurationProperty.PropertyValue.Builder().value("oauth2").label("OAuth 2.0").build());
    }

    @Test
    public void shouldDetermineConnectorDescription() {
        final Swagger swagger = new Swagger();

        assertThat(generator.determineConnectorDescription(SWAGGER_TEMPLATE, createSettingsFrom(swagger))).isEqualTo("unspecified");

        final Info info = new Info();
        swagger.info(info);
        assertThat(generator.determineConnectorDescription(SWAGGER_TEMPLATE, createSettingsFrom(swagger))).isEqualTo("unspecified");

        info.description("description");
        assertThat(generator.determineConnectorDescription(SWAGGER_TEMPLATE, createSettingsFrom(swagger))).isEqualTo("description");
    }

    @Test
    public void shouldDetermineConnectorName() {
        final Swagger swagger = new Swagger();

        assertThat(generator.determineConnectorName(SWAGGER_TEMPLATE, createSettingsFrom(swagger))).isEqualTo("unspecified");

        final Info info = new Info();
        swagger.info(info);
        assertThat(generator.determineConnectorName(SWAGGER_TEMPLATE, createSettingsFrom(swagger))).isEqualTo("unspecified");

        info.title("title");
        assertThat(generator.determineConnectorName(SWAGGER_TEMPLATE, createSettingsFrom(swagger))).isEqualTo("title");
    }

    @Test
    public void shouldIncorporateGivenConfiguredProperties() throws IOException {
        final String specification = resource("/swagger/reverb.swagger.yaml");

        final ConnectorSettings connectorSettings = new ConnectorSettings.Builder()//
            .name("Reverb API")//
            .description("Invokes Reverb API")//
            .icon("fa-music")//
            .putConfiguredProperty("specification", specification)//
            .putConfiguredProperty("tokenEndpoint", "http://some.token.url").build();

        final Connector connector = generator.generate(SWAGGER_TEMPLATE, connectorSettings);

        assertThat(connector.getConfiguredProperties()).containsEntry("tokenEndpoint", "http://some.token.url");
    }

    @Test
    public void shouldMakeNonUniqueOperationIdsUnique() {
        final Swagger swagger = new Swagger().path("/path", new Path().get(new Operation().operationId("foo"))
            .post(new Operation().operationId("foo")).put(new Operation().operationId("bar")));

        final Connector generated = generator.configureConnector(SWAGGER_TEMPLATE, new Connector.Builder().id("connector1").build(),
            createSettingsFrom(swagger));
        final List<ConnectorAction> actions = generated.getActions();
        assertThat(actions).hasSize(3);
        assertThat(actions.get(0).getId()).hasValueSatisfying(id -> assertThat(id).endsWith("foo"));
        assertThat(actions.get(1).getId()).hasValueSatisfying(id -> assertThat(id).endsWith("foo1"));
        assertThat(actions.get(2).getId()).hasValueSatisfying(id -> assertThat(id).endsWith("bar"));
    }

    @Test
    public void shouldNotFailOnEmptySwagger() {
        final ConnectorSettings connectorSettings = new ConnectorSettings.Builder()//
            .putConfiguredProperty("specification", "{}")//
            .build();

        final ConnectorSummary summary = generator.info(SWAGGER_TEMPLATE, connectorSettings);
        assertThat(summary).isNotNull();
    }

    @Test
    public void shouldNotFailOnTrivialyEmptyOperations() {
        final ConnectorSettings connectorSettings = new ConnectorSettings.Builder()//
            .putConfiguredProperty("specification",
                "{\"swagger\": \"2.0\",\"info\": {\"version\": \"0.0.0\",\"title\": \"title\",\"description\": \"description\"},\"paths\": {\"/operation\": {\"get\": {\"responses\": {\"200\": {\"description\": \"OK\"}}}}}}")//
            .build();

        final ConnectorSummary summary = generator.info(SWAGGER_TEMPLATE, connectorSettings);
        assertThat(summary).isNotNull();
    }

    @Test
    public void shouldProvideInfoFromPetstoreSwagger() throws IOException {
        final String specification = resource("/swagger/petstore.swagger.json");

        final ConnectorSettings connectorSettings = new ConnectorSettings.Builder()//
            .putConfiguredProperty("specification", specification)//
            .build();

        final ConnectorSummary summary = generator.info(SWAGGER_TEMPLATE, connectorSettings);

        final ActionsSummary actionsSummary = new ActionsSummary.Builder().totalActions(20).putActionCountByTag("store", 4)
            .putActionCountByTag("user", 8).putActionCountByTag("pet", 8).build();

        final ConnectorSummary expected = new ConnectorSummary.Builder()//
            .name("Swagger Petstore")//
            .actionsSummary(actionsSummary)//
            .build();
        assertThat(summary).isEqualToIgnoringGivenFields(expected, "icon", "description", "properties", "warnings", "configuredProperties");
        assertThat(summary.getIcon()).startsWith("data:image");
        assertThat(summary.getDescription()).startsWith("This is a sample server Petstore server");
        assertThat(summary.getProperties().keySet()).contains("host", "basePath", "authenticationType", "clientId", "clientSecret",
            "accessToken", "authorizationEndpoint", "oauthScopes", "specification");
        assertThat(summary.getConfiguredProperties().keySet()).containsOnly("specification");
        assertThat(reformatJson(summary.getConfiguredProperties().get("specification"))).isEqualTo(reformatJson(specification));
    }

    @Test
    public void shouldReportErrorsFromInvalidPetstoreSwagger() throws IOException {
        final String specification = resource("/swagger/invalid/invalid-scheme.petstore.swagger.json");

        final ConnectorSettings connectorSettings = new ConnectorSettings.Builder()//
            .putConfiguredProperty("specification", specification)//
            .build();

        final ConnectorSummary summary = generator.info(SWAGGER_TEMPLATE, connectorSettings);

        assertThat(summary.getErrors()).hasSize(1);
        assertThat(summary.getWarnings()).hasSize(1);
    }

    private static ConnectorSettings createSettingsFrom(final Swagger swagger) {
        return new ConnectorSettings.Builder()//
            .putConfiguredProperty("specification", SwaggerHelper.serialize(swagger))//
            .build();
    }
}
