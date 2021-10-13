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
package io.syndesis.server.api.generator.openapi;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.openapi.models.OasOperation;
import io.apicurio.datamodels.openapi.v2.models.Oas20Document;
import io.apicurio.datamodels.openapi.v2.models.Oas20Info;
import io.apicurio.datamodels.openapi.v2.models.Oas20PathItem;
import io.apicurio.datamodels.openapi.v2.models.Oas20SecurityScheme;
import io.syndesis.common.model.Dependency;
import io.syndesis.common.model.action.ActionsSummary;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.api.APISummary;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.connection.ConnectorSettings;
import io.syndesis.common.util.json.JsonUtils;
import io.syndesis.server.api.generator.APIValidationContext;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import static io.syndesis.server.api.generator.openapi.TestHelper.reformatJson;
import static io.syndesis.server.api.generator.openapi.TestHelper.resource;
import static org.assertj.core.api.Assertions.assertThat;

public class OpenApiConnectorGeneratorTest {

    private final OpenApiConnectorGenerator generator;

    public OpenApiConnectorGeneratorTest() {
        try (InputStream stream = OpenApiConnectorGeneratorTest.class.getResourceAsStream("/META-INF/syndesis/connector/rest-swagger.json")) {
            final Connector restSwagger = JsonUtils.readFromStream(stream, Connector.class);

            generator = new OpenApiConnectorGenerator(restSwagger) {
                @Override
                protected ConnectorDescriptor createDescriptor(String connectorId, OpenApiModelInfo info, OasOperation operation) {
                    return new ConnectorDescriptor.Builder().build();
                }
            };
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    public void includesRestSwaggerConnectorCustomizers() throws IOException {
        final ConnectorSettings connectorSettings = createReverbSettings();
        final Connector connector = generator.generate(ApiConnectorTemplate.SWAGGER_TEMPLATE, connectorSettings);

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
        final Connector connector = generator.generate(ApiConnectorTemplate.SWAGGER_TEMPLATE, connectorSettings);

        assertThat(connector.getDependencies()).isNotEmpty();
        assertThat(connector.getDependencies())
            .anyMatch(d -> d.getType() == Dependency.Type.MAVEN && d.getId().startsWith("io.syndesis.connector:connector-rest-swagger"));
    }

    @Test
    public void includesRestSwaggerConnectorFactory() throws IOException {
        final ConnectorSettings connectorSettings = createReverbSettings();
        final Connector connector = generator.generate(ApiConnectorTemplate.SWAGGER_TEMPLATE, connectorSettings);

        assertThat(connector.getConnectorFactory()).isPresent();
        assertThat(connector.getConnectorFactory()).hasValue("io.syndesis.connector.rest.swagger.ConnectorFactory");
    }

    @Test
    public void shouldCreateSecurityConfigurationFromConcurSwagger() throws IOException {
        final String specification = resource("/openapi/v2/concur.json");

        final ConnectorSettings connectorSettings = new ConnectorSettings.Builder()
            .name("Concur List API")
            .description("Invokes Concur List API")
            .icon("fa-globe")
            .putConfiguredProperty("specification", specification)
            .putConfiguredProperty("authenticationType", "oauth2:concur_oauth2")
            .build();

        final Connector generated = generator.generate(ApiConnectorTemplate.SWAGGER_TEMPLATE, connectorSettings);

        assertThat(generated.getProperties().keySet()).contains("accessToken", "authorizationEndpoint", "tokenEndpoint", "clientId",
            "clientSecret", "tokenStrategy", "authorizeUsingParameters");
        assertThat(generated.getProperties().get("tokenStrategy").getDefaultValue()).isEqualTo("AUTHORIZATION_HEADER");
        assertThat(generated.getProperties().get("authorizeUsingParameters").getDefaultValue()).isEqualTo("true");
    }

    @Test
    public void shouldCreateSecurityConfigurationFromReverbSwagger() throws IOException {
        final String specification = resource("/openapi/v2/reverb.yaml");

        final ConnectorSettings connectorSettings = new ConnectorSettings.Builder()
            .name("Reverb API")
            .description("Invokes Reverb API")
            .icon("fa-music")
            .putConfiguredProperty("specification", specification)
            .putConfiguredProperty("authenticationType", "oauth2:oauth2")
            .build();

        final Connector generated = generator.generate(ApiConnectorTemplate.SWAGGER_TEMPLATE, connectorSettings);

        assertThat(generated.getProperties().keySet()).contains("accessToken", "authorizationEndpoint", "tokenEndpoint", "clientId",
            "clientSecret");
        assertThat(generated.getProperties().get("authenticationType").getEnum())
            .containsExactly(new ConfigurationProperty.PropertyValue.Builder().value("oauth2:oauth2").label("OAuth 2.0 - oauth2").build());
    }

    @Test
    public void shouldDetermineConnectorDescription() {
        final Oas20Document openApiDoc = new Oas20Document();

        assertThat(generator.determineConnectorDescription(ApiConnectorTemplate.SWAGGER_TEMPLATE, createSettingsFrom(openApiDoc)))
            .isEqualTo("unspecified");

        final Oas20Info info = (Oas20Info) openApiDoc.createInfo();
        openApiDoc.info = info;
        assertThat(generator.determineConnectorDescription(ApiConnectorTemplate.SWAGGER_TEMPLATE, createSettingsFrom(openApiDoc)))
            .isEqualTo("unspecified");

        info.description = "description";
        assertThat(generator.determineConnectorDescription(ApiConnectorTemplate.SWAGGER_TEMPLATE, createSettingsFrom(openApiDoc)))
            .isEqualTo("description");
    }

    @Test
    public void shouldDetermineConnectorName() {
        final Oas20Document openApiDoc = new Oas20Document();

        assertThat(generator.determineConnectorName(ApiConnectorTemplate.SWAGGER_TEMPLATE, createSettingsFrom(openApiDoc))).isEqualTo("unspecified");

        final Oas20Info info = (Oas20Info) openApiDoc.createInfo();
        openApiDoc.info = info;
        assertThat(generator.determineConnectorName(ApiConnectorTemplate.SWAGGER_TEMPLATE, createSettingsFrom(openApiDoc))).isEqualTo("unspecified");

        info.title = "title";
        assertThat(generator.determineConnectorName(ApiConnectorTemplate.SWAGGER_TEMPLATE, createSettingsFrom(openApiDoc))).isEqualTo("title");
    }

    @Test
    public void shouldGeneratePropertiesForChosenAuthenticationType() throws JsonProcessingException {
        final Oas20Document openApiDoc = new Oas20Document();
        openApiDoc.securityDefinitions = openApiDoc.createSecurityDefinitions();

        Oas20SecurityScheme scheme1 = openApiDoc.securityDefinitions.createSecurityScheme("one");
        scheme1.in = "query";
        scheme1.type = "apiKey";

        Oas20SecurityScheme scheme2 = openApiDoc.securityDefinitions.createSecurityScheme("two");
        scheme2.in = "header";
        scheme2.type = "apiKey";

        openApiDoc.securityDefinitions.addSecurityScheme("one", scheme1);
        openApiDoc.securityDefinitions.addSecurityScheme("two", scheme2);
        final String specification = JsonUtils.writer().writeValueAsString(Library.writeNode(openApiDoc));

        final ConnectorSettings connectorSettings = new ConnectorSettings.Builder()
            .putConfiguredProperty("specification", specification)
            .putConfiguredProperty("authenticationType", "apiKey:two")
            .build();

        final APISummary summary = generator.info(ApiConnectorTemplate.SWAGGER_TEMPLATE, connectorSettings);

        assertThat(summary.getConfiguredProperties())
            .containsEntry("specification", specification)
            .containsEntry("authenticationType", "apiKey:two");

        final Map<String, ConfigurationProperty> properties = summary.getProperties();
        assertThat(properties.keySet()).containsOnly("authenticationParameterName", "authenticationParameterPlacement",
            "authenticationParameterValue", "authenticationType", "basePath", "host", "specification");
    }

    @Test
    public void shouldIncorporateGivenConfiguredProperties() throws IOException {
        final String specification = resource("/openapi/v2/reverb.yaml");

        final ConnectorSettings connectorSettings = new ConnectorSettings.Builder()//
            .name("Reverb API")//
            .description("Invokes Reverb API")//
            .icon("fa-music")//
            .putConfiguredProperty("specification", specification)//
            .putConfiguredProperty("tokenEndpoint", "http://some.token.url").build();

        final Connector connector = generator.generate(ApiConnectorTemplate.SWAGGER_TEMPLATE, connectorSettings);

        assertThat(connector.getConfiguredProperties()).containsEntry("tokenEndpoint", "http://some.token.url");
    }

    @Test
    public void shouldMakeNonUniqueOperationIdsUnique() {
        final Oas20Document openApiDoc = new Oas20Document();

        openApiDoc.paths = openApiDoc.createPaths();
        Oas20PathItem pathItem = (Oas20PathItem) openApiDoc.paths.createPathItem("/path");
        pathItem.get = pathItem.createOperation("get");
        pathItem.get.operationId = "foo";
        pathItem.post = pathItem.createOperation("post");
        pathItem.post.operationId = "foo";
        pathItem.put = pathItem.createOperation("put");
        pathItem.put.operationId = "bar";

        openApiDoc.paths.addPathItem("/path", pathItem);

        final Connector generated = generator.configureConnector(ApiConnectorTemplate.SWAGGER_TEMPLATE,
            new Connector.Builder().id("connector1").build(),
            createSettingsFrom(openApiDoc));
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

        final APISummary summary = generator.info(ApiConnectorTemplate.SWAGGER_TEMPLATE, connectorSettings);
        assertThat(summary).isNotNull();
    }

    @Test
    public void shouldNotFailOnTrivialyEmptyOperations() {
        final ConnectorSettings connectorSettings = new ConnectorSettings.Builder()//
            .putConfiguredProperty("specification",
                "{\"swagger\": \"2.0\",\"info\": {\"version\": \"0.0.0\",\"title\": \"title\",\"description\": \"description\"},\"paths\": {\"/operation\": {\"get\": {\"responses\": {\"200\": {\"description\": \"OK\"}}}}}}")//
            .build();

        final APISummary summary = generator.info(ApiConnectorTemplate.SWAGGER_TEMPLATE, connectorSettings);
        assertThat(summary).isNotNull();
    }

    @Test
    public void shouldNotProvideAuthenticationPropertiesWithMultipleSecurityDefinitionsMatching() {
        final Oas20Document openApiDoc = new Oas20Document();
        openApiDoc.securityDefinitions = openApiDoc.createSecurityDefinitions();

        Oas20SecurityScheme scheme1 = openApiDoc.securityDefinitions.createSecurityScheme("one");
        scheme1.in = "query";
        scheme1.type = "apiKey";

        Oas20SecurityScheme scheme2 = openApiDoc.securityDefinitions.createSecurityScheme("two");
        scheme2.in = "header";
        scheme2.type = "apiKey";

        openApiDoc.securityDefinitions.addSecurityScheme("one", scheme1);
        openApiDoc.securityDefinitions.addSecurityScheme("two", scheme2);
        final String specification = Library.writeDocumentToJSONString(openApiDoc);

        final ConnectorSettings connectorSettings = new ConnectorSettings.Builder()
            .putConfiguredProperty("specification", specification)
            .build();

        final APISummary summary = generator.info(ApiConnectorTemplate.SWAGGER_TEMPLATE, connectorSettings);

        assertThat(summary.getProperties().keySet()).containsOnly("authenticationType", "basePath", "host", "specification");
    }

    @Test
    public void shouldParseSpecificationWithSecurityRequirements() throws JSONException {
        final OpenApiModelInfo info = OpenApiConnectorGenerator.parseSpecification(new ConnectorSettings.Builder()
            .putConfiguredProperty("specification", "{\"swagger\":\"2.0\",\"paths\":{\"/api\":{\"get\":{\"security\":[{\"secured\":[]}]}}}}")
            .build(),
            APIValidationContext.CONSUMED_API);

        final Oas20Document model = info.getV2Model();
        assertThat(model.paths.getPathItem("/api").get.security).hasSize(1);
        assertThat(model.paths.getPathItem("/api").get.security.get(0).getSecurityRequirementNames()).containsExactly("secured");
        assertThat(model.paths.getPathItem("/api").get.security.get(0).getScopes("secured")).isEmpty();

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
        final String specification = resource("/openapi/v2/petstore.json");

        final ConnectorSettings connectorSettings = new ConnectorSettings.Builder()//
            .putConfiguredProperty("specification", specification)//
            .build();

        final APISummary summary = generator.info(ApiConnectorTemplate.SWAGGER_TEMPLATE, connectorSettings);

        final ActionsSummary actionsSummary = new ActionsSummary.Builder().totalActions(20).putActionCountByTag("store", 4)
            .putActionCountByTag("user", 8).putActionCountByTag("pet", 8).build();

        final APISummary expected = new APISummary.Builder()//
            .name("Swagger Petstore")//
            .addActionsSummary(actionsSummary)//
            .build();
        assertThat(summary).isEqualToIgnoringGivenFields(expected, "icon", "description", "properties", "warnings", "configuredProperties");
        assertThat(summary.getIcon()).matches(s -> s.isPresent() && s.get().startsWith("data:image"));
        assertThat(summary.getDescription()).startsWith("This is a sample server Petstore server");
        assertThat(summary.getProperties().keySet()).containsOnly("authenticationParameterName", "authenticationParameterPlacement",
            "authenticationParameterValue", "authenticationType", "basePath", "host", "specification");
        assertThat(summary.getConfiguredProperties().keySet()).containsOnly("specification");
        assertThat(reformatJson(summary.getConfiguredProperties().get("specification"))).isEqualTo(reformatJson(specification));
    }

    @Test
    public void shouldReportErrorsFromInvalidPetstoreSwagger() throws IOException {
        final String specification = resource("/openapi/v2/invalid/invalid-scheme.petstore.json");

        final ConnectorSettings connectorSettings = new ConnectorSettings.Builder()//
            .putConfiguredProperty("specification", specification)//
            .build();

        final APISummary summary = generator.info(ApiConnectorTemplate.SWAGGER_TEMPLATE, connectorSettings);

        assertThat(summary.getErrors()).hasSize(1);
        assertThat(summary.getWarnings()).hasSize(1);
    }

    private static ConnectorSettings createReverbSettings() throws IOException {
        return new ConnectorSettings.Builder()
            .name("Reverb API")
            .description("Invokes Reverb API")
            .icon("fa-music")
            .putConfiguredProperty("specification", resource("/openapi/v2/reverb.yaml"))
            .build();
    }

    private static ConnectorSettings createSettingsFrom(final Oas20Document openApiDoc) {
        return new ConnectorSettings.Builder()//
            .putConfiguredProperty("specification", Library.writeDocumentToJSONString(openApiDoc))//
            .build();
    }

}
