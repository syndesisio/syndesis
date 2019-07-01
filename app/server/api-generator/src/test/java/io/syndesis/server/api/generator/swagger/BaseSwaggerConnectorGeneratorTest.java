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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.swagger.models.Info;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.auth.ApiKeyAuthDefinition;
import io.swagger.models.auth.In;
import io.swagger.models.parameters.Parameter;
import io.syndesis.common.model.Dependency;
import io.syndesis.common.model.action.ActionsSummary;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.api.APISummary;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.connection.ConnectorSettings;
import io.syndesis.common.util.Json;
import io.syndesis.common.util.openapi.OpenApiHelper;
import io.syndesis.server.api.generator.APIValidationContext;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static io.syndesis.server.api.generator.swagger.TestHelper.reformatJson;
import static io.syndesis.server.api.generator.swagger.TestHelper.resource;

import static org.assertj.core.api.Assertions.assertThat;

public class BaseSwaggerConnectorGeneratorTest extends AbstractSwaggerConnectorTest {

    private final BaseSwaggerConnectorGenerator generator;

    public BaseSwaggerConnectorGeneratorTest() {
        try (InputStream stream = SwaggerUnifiedShapeGeneratorExampleTests.class.getResourceAsStream("/META-INF/syndesis/connector/rest-swagger.json")) {
            final Connector restSwagger = Json.readFromStream(stream, Connector.class);

            generator = new BaseSwaggerConnectorGenerator(restSwagger) {
                @Override
                ConnectorDescriptor.Builder createDescriptor(final ObjectNode json, final Swagger swagger, final Operation operation) {
                    return new ConnectorDescriptor.Builder();
                }
            };
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    public void includesRestSwaggerConnectorCustomizers() throws IOException {
        final ConnectorSettings connectorSettings = createReverbSettings();
        final Connector connector = generator.generate(SWAGGER_TEMPLATE, connectorSettings);

        assertThat(connector.getConnectorCustomizers()).isNotEmpty();
        assertThat(connector.getConnectorCustomizers()).contains(
            "io.syndesis.connector.rest.swagger.SpecificationResourceCustomizer",
            "io.syndesis.connector.rest.swagger.AuthenticationCustomizer",
            "io.syndesis.connector.rest.swagger.RequestCustomizer",
            "io.syndesis.connector.rest.swagger.ResponseCustomizer");
    }

    @Test
    public void includesRestSwaggerConnectorDependency() throws IOException {
        final ConnectorSettings connectorSettings = createReverbSettings();
        final Connector connector = generator.generate(SWAGGER_TEMPLATE, connectorSettings);

        assertThat(connector.getDependencies()).isNotEmpty();
        assertThat(connector.getDependencies())
            .anyMatch(d -> d.getType() == Dependency.Type.MAVEN && d.getId().startsWith("io.syndesis.connector:connector-rest-swagger"));
    }

    @Test
    public void includesRestSwaggerConnectorFactory() throws IOException {
        final ConnectorSettings connectorSettings = createReverbSettings();
        final Connector connector = generator.generate(SWAGGER_TEMPLATE, connectorSettings);

        assertThat(connector.getConnectorFactory()).isPresent();
        assertThat(connector.getConnectorFactory()).hasValue("io.syndesis.connector.rest.swagger.ConnectorFactory");
    }

    @Test
    public void shouldCreatePropertyParametersFromPetstoreSwagger() throws IOException {
        final String specification = resource("/swagger/petstore.swagger.json");
        final Swagger swagger = OpenApiHelper.parse(specification);

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

        final ConnectorSettings connectorSettings = new ConnectorSettings.Builder()
            .name("Concur List API")
            .description("Invokes Concur List API")
            .icon("fa-globe")
            .putConfiguredProperty("specification", specification)
            .putConfiguredProperty(PropertyGenerators.authenticationType.name(), "oauth2:concur_oauth2")
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

        final ConnectorSettings connectorSettings = new ConnectorSettings.Builder()
            .name("Reverb API")
            .description("Invokes Reverb API")
            .icon("fa-music")
            .putConfiguredProperty("specification", specification)
            .putConfiguredProperty(PropertyGenerators.authenticationType.name(), "oauth2:oauth2")
            .build();

        final Connector generated = generator.generate(SWAGGER_TEMPLATE, connectorSettings);

        assertThat(generated.getProperties().keySet()).contains("accessToken", "authorizationEndpoint", "tokenEndpoint", "clientId",
            "clientSecret");
        assertThat(generated.getProperties().get(PropertyGenerators.authenticationType.name()).getEnum())
            .containsExactly(new ConfigurationProperty.PropertyValue.Builder().value("oauth2:oauth2").label("OAuth 2.0 - oauth2").build());
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
    public void shouldGeneratePropertiesForChoosenAuthenticationType() {
        final Swagger swagger = new Swagger()
            .securityDefinition("one", new ApiKeyAuthDefinition("query", In.QUERY))
            .securityDefinition("two", new ApiKeyAuthDefinition("query", In.HEADER));
        final String specification = OpenApiHelper.serialize(swagger);

        final ConnectorSettings connectorSettings = new ConnectorSettings.Builder()
            .putConfiguredProperty("specification", specification)
            .putConfiguredProperty(PropertyGenerators.authenticationType.name(), "apiKey:two")
            .build();

        final APISummary summary = generator.info(SWAGGER_TEMPLATE, connectorSettings);

        assertThat(summary.getConfiguredProperties())
            .containsEntry("specification", specification)
            .containsEntry(PropertyGenerators.authenticationType.name(), "apiKey:two");

        final Map<String, ConfigurationProperty> properties = summary.getProperties();
        assertThat(properties.keySet()).containsOnly("authenticationParameterName", "authenticationParameterPlacement",
            "authenticationParameterValue", PropertyGenerators.authenticationType.name(), "basePath", "host", "specification");
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

        final APISummary summary = generator.info(SWAGGER_TEMPLATE, connectorSettings);
        assertThat(summary).isNotNull();
    }

    @Test
    public void shouldNotFailOnTrivialyEmptyOperations() {
        final ConnectorSettings connectorSettings = new ConnectorSettings.Builder()//
            .putConfiguredProperty("specification",
                "{\"swagger\": \"2.0\",\"info\": {\"version\": \"0.0.0\",\"title\": \"title\",\"description\": \"description\"},\"paths\": {\"/operation\": {\"get\": {\"responses\": {\"200\": {\"description\": \"OK\"}}}}}}")//
            .build();

        final APISummary summary = generator.info(SWAGGER_TEMPLATE, connectorSettings);
        assertThat(summary).isNotNull();
    }

    @Test
    public void shouldNotProvideAuthenticationPropertiesWithMultipleSecurityDefinitionsMatching() {
        final Swagger swagger = new Swagger()
            .securityDefinition("one", new ApiKeyAuthDefinition("query", In.QUERY))
            .securityDefinition("two", new ApiKeyAuthDefinition("query", In.HEADER));
        final String specification = OpenApiHelper.serialize(swagger);

        final ConnectorSettings connectorSettings = new ConnectorSettings.Builder()
            .putConfiguredProperty("specification", specification)
            .build();

        final APISummary summary = generator.info(SWAGGER_TEMPLATE, connectorSettings);

        assertThat(summary.getProperties().keySet()).containsOnly(PropertyGenerators.authenticationType.name(), "basePath", "host", "specification");
    }

    @Test
    public void shouldParseSpecificationWithSecurityRequirements() throws JSONException {
        final SwaggerModelInfo info = BaseSwaggerConnectorGenerator.parseSpecification(new ConnectorSettings.Builder()
            .putConfiguredProperty("specification", "{\"swagger\":\"2.0\",\"paths\":{\"/api\":{\"get\":{\"security\":[{\"secured\":[]}]}}}}")
            .build(),
            APIValidationContext.CONSUMED_API);

        final Swagger model = info.getModel();
        assertThat(model.getPath("/api").getGet().getSecurity()).containsOnly(Collections.singletonMap("secured", Collections.emptyList()));

        final String resolvedSpecification = info.getResolvedSpecification();
        JSONAssert.assertEquals(
            "{\"swagger\":\"2.0\",\"paths\":{\"/api\":{\"get\":{\"security\":[{\"secured\":[]}]}}}}",
            resolvedSpecification, JSONCompareMode.STRICT);

        final ObjectNode resolvedJsonGraph = info.getResolvedJsonGraph();
        final JsonNode securityRequirement = resolvedJsonGraph.get("paths").get("/api").get("get").get("security");
        assertThat(securityRequirement).hasSize(1);
        assertThat(securityRequirement.get(0).get("secured")).isEmpty();
    }

    @Test
    public void shouldProvideInfoFromPetstoreSwagger() throws IOException {
        final String specification = resource("/swagger/petstore.swagger.json");

        final ConnectorSettings connectorSettings = new ConnectorSettings.Builder()//
            .putConfiguredProperty("specification", specification)//
            .build();

        final APISummary summary = generator.info(SWAGGER_TEMPLATE, connectorSettings);

        final ActionsSummary actionsSummary = new ActionsSummary.Builder().totalActions(20).putActionCountByTag("store", 4)
            .putActionCountByTag("user", 8).putActionCountByTag("pet", 8).build();

        final APISummary expected = new APISummary.Builder()//
            .name("Swagger Petstore")//
            .actionsSummary(actionsSummary)//
            .build();
        assertThat(summary).isEqualToIgnoringGivenFields(expected, "icon", "description", "properties", "warnings", "configuredProperties");
        assertThat(summary.getIcon()).matches(s -> s.isPresent() && s.get().startsWith("data:image"));
        assertThat(summary.getDescription()).startsWith("This is a sample server Petstore server");
        assertThat(summary.getProperties().keySet()).containsOnly("authenticationParameterName", "authenticationParameterPlacement",
            "authenticationParameterValue", PropertyGenerators.authenticationType.name(), "basePath", "host", "specification");
        assertThat(summary.getConfiguredProperties().keySet()).containsOnly("specification");
        assertThat(reformatJson(summary.getConfiguredProperties().get("specification"))).isEqualTo(reformatJson(specification));
    }

    @Test
    public void shouldReportErrorsFromInvalidPetstoreSwagger() throws IOException {
        final String specification = resource("/swagger/invalid/invalid-scheme.petstore.swagger.json");

        final ConnectorSettings connectorSettings = new ConnectorSettings.Builder()//
            .putConfiguredProperty("specification", specification)//
            .build();

        final APISummary summary = generator.info(SWAGGER_TEMPLATE, connectorSettings);

        assertThat(summary.getErrors()).hasSize(1);
        assertThat(summary.getWarnings()).hasSize(1);
    }

    private static ConnectorSettings createReverbSettings() throws IOException {
        return new ConnectorSettings.Builder()
            .name("Reverb API")
            .description("Invokes Reverb API")
            .icon("fa-music")
            .putConfiguredProperty("specification", resource("/swagger/reverb.swagger.yaml"))
            .build();
    }

    private static ConnectorSettings createSettingsFrom(final Swagger swagger) {
        return new ConnectorSettings.Builder()//
            .putConfiguredProperty("specification", OpenApiHelper.serialize(swagger))//
            .build();
    }

}
