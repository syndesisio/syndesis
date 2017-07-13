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
package io.syndesis.project.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import io.syndesis.connector.catalog.ConnectorCatalog;
import io.syndesis.connector.catalog.ConnectorCatalogProperties;
import io.syndesis.model.connection.Action;
import io.syndesis.model.connection.Connection;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.filter.FilterPredicate;
import io.syndesis.model.filter.FilterRule;
import io.syndesis.model.filter.FilterStep;
import io.syndesis.model.filter.FilterType;
import io.syndesis.model.integration.Integration;
import io.syndesis.model.integration.SimpleStep;
import io.syndesis.model.integration.Step;

import io.syndesis.project.converter.visitor.DataMapperStepVisitor;
import io.syndesis.project.converter.visitor.EndpointStepVisitor;
import io.syndesis.project.converter.visitor.FilterStepVisitor;
import io.syndesis.project.converter.visitor.StepVisitorFactoryRegistry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultProjectGeneratorTest {

	private static Properties properties = new Properties();

    static {
            System.setProperty("groovy.grape.report.downloads", "true");
            System.setProperty("ivy.message.logger.level", "3");
            try {
                properties.load(DefaultProjectGeneratorTest.class.getResourceAsStream("test.properties"));
            } catch (IOException e) {
                Assert.fail("Can't read the test.properties");
            }
    }

    private static final String CONNECTOR_VERSION=properties.getProperty("syndesis-connectors.version");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new Jdk8Module());
    private static final TypeReference<HashMap<String, Connector>> CONNECTOR_MAP_TYPE_REF = new TypeReference<HashMap<String, Connector>>() {
    };

    private final StepVisitorFactoryRegistry registry = new StepVisitorFactoryRegistry(Arrays.asList(new DataMapperStepVisitor.Factory(),
        new EndpointStepVisitor.Factory(),
        new FilterStepVisitor.Factory()
    ));

    private Map<String, Connector> connectors = new HashMap<>();

    @Before
    public void setUp() throws IOException {
        connectors.putAll(OBJECT_MAPPER.readValue(this.getClass().getResourceAsStream("test-connectors.json"), CONNECTOR_MAP_TYPE_REF));
    }

    @Test
    public void testConvert() throws Exception {
        Step step1 = new SimpleStep.Builder().stepKind("endpoint").connection(new Connection.Builder().configuredProperties(map()).build()).configuredProperties(map("period",5000)).action(new Action.Builder().connectorId("timer").camelConnectorPrefix("periodic-timer").camelConnectorGAV("io.syndesis:timer-connector:0.4.5").build()).build();
        Step step2 = new SimpleStep.Builder().stepKind("endpoint").connection(new Connection.Builder().configuredProperties(map()).build()).configuredProperties(map("httpUri", "http://localhost:8080/hello")).action(new Action.Builder().connectorId("http").camelConnectorPrefix("http-get").camelConnectorGAV("io.syndesis:http-get-connector:0.4.5").build()).build();
        Step step3 = new SimpleStep.Builder().stepKind("log").configuredProperties(map("message", "Hello World! ${body}")).build();
        Step step4 = new SimpleStep.Builder().stepKind("endpoint").connection(new Connection.Builder().configuredProperties(Collections.emptyMap()).build()).configuredProperties(map("httpUri", "http://localhost:8080/bye")).action(new Action.Builder().connectorId("http").camelConnectorPrefix("http-post").camelConnectorGAV("io.syndesis:http-post-connector:0.4.5").build()).build();

        GenerateProjectRequest request = ImmutableGenerateProjectRequest
            .builder()
            .gitHubUser("noob")
            .gitHubRepoName("test")
            .integration(new Integration.Builder()
                .id("test-integration")
                .name("Test Integration")
                .description("This is a test integration!")
                .steps( Arrays.asList(step1, step2, step3, step4))
                .build())
            .connectors(connectors)
            .build();
        Map<String, byte[]> files = new DefaultProjectGenerator(new ConnectorCatalog(new ConnectorCatalogProperties()), new ProjectGeneratorProperties(), registry).generate(request);

        assertFileContents(files.get("README.md"), "test-README.md");
        assertFileContents(files.get("src/main/java/io/syndesis/example/Application.java"), "test-Application.java");
        assertFileContents(files.get("src/main/resources/application.properties"), "test-application.properties");
        assertFileContents(files.get("src/main/resources/syndesis.yml"), "test-syndesis.yml");
        assertFileContents(files.get("pom.xml"), "test-pom.xml");
    }


    @Test
    public void testConverterWithPasswordMasking() throws Exception {
        Step step1 = new SimpleStep.Builder().stepKind("endpoint").connection(new Connection.Builder().configuredProperties(map()).build()).configuredProperties(map("period",5000)).action(new Action.Builder().connectorId("timer").camelConnectorPrefix("periodic-timer").camelConnectorGAV("io.syndesis:timer-connector:0.4.5").build()).build();
        Step step2 = new SimpleStep.Builder().stepKind("endpoint").connection(new Connection.Builder().configuredProperties(map()).build()).configuredProperties(map("httpUri", "http://localhost:8080/hello", "username", "admin", "password", "admin", "token", "mytoken")).action(new Action.Builder().connectorId("http").camelConnectorPrefix("http-get").camelConnectorGAV("io.syndesis:http-get-connector:0.4.5").build()).build();

        GenerateProjectRequest request = ImmutableGenerateProjectRequest
            .builder()
            .integration(new Integration.Builder()
                .id("test-integration")
                .name("Test Integration")
                .description("This is a test integration!")
                .steps( Arrays.asList(step1, step2))
                .build())
            .connectors(connectors)
            .gitHubUser("noob")
            .gitHubRepoName("test")
            .build();

        ProjectGeneratorProperties generatorProperties = new ProjectGeneratorProperties();
        generatorProperties.setSecretMaskingEnabled(true);

        Map<String, byte[]> files = new DefaultProjectGenerator(new ConnectorCatalog(new ConnectorCatalogProperties()), generatorProperties, registry).generate(request);


        assertFileContents(files.get("src/main/resources/application.properties"), "test-application.properties");
        assertFileContents(files.get("src/main/resources/syndesis.yml"), "test-syndesis-with-secrets.yml");
    }


    @Test
    public void testConvertFromJson() throws Exception {

        JsonNode json = new ObjectMapper().readTree(this.getClass().getResourceAsStream("test-integration.json"));

        GenerateProjectRequest request = ImmutableGenerateProjectRequest
            .builder()
            .gitHubUser("noob")
            .gitHubRepoName("test")
            .integration(new ObjectMapper().registerModule(new Jdk8Module()).readValue(json.get("data").toString(), Integration.class))
            .connectors(connectors)
            .build();

        Map<String, byte[]> files = new DefaultProjectGenerator(new ConnectorCatalog(new ConnectorCatalogProperties()), new ProjectGeneratorProperties(), registry).generate(request);

        assertFileContents(files.get("README.md"), "test-pull-push-README.md");
        assertFileContents(files.get("src/main/java/io/syndesis/example/Application.java"), "test-Application.java");
        assertFileContents(files.get("src/main/resources/application.properties"), "test-pull-push-application.properties");
        assertFileContents(files.get("src/main/resources/syndesis.yml"), "test-pull-push-syndesis.yml");
        assertFileContents(files.get("pom.xml"), "test-pull-push-pom.xml");
    }


    private static void assertFileContents(byte[] actualContents, String expectedFileName) throws Exception {
        assertThat(new String(actualContents)).isEqualTo(
            new String(Files.readAllBytes(
                Paths.get(DefaultProjectGeneratorTest.class.getResource(expectedFileName).toURI())
            ), StandardCharsets.UTF_8)
        );
    }

    // Helper method to help constuct maps with concise syntax
    private static HashMap<String, String> map(Object... values) {
        HashMap<String, String> rc = new HashMap<String, String>();
        for (int i = 0; i + 1 < values.length; i += 2) {
            rc.put(values[i].toString(), values[i + 1].toString());
        }
        return rc;
    }

    @Test
    public void testMapper() throws Exception {
        Step step1 = new SimpleStep.Builder().stepKind("endpoint").connection(new Connection.Builder().configuredProperties(map()).build()).configuredProperties(map("period",5000)).action(new Action.Builder().connectorId("timer").camelConnectorPrefix("periodic-timer").camelConnectorGAV("io.syndesis:timer-connector:0.4.5").build()).build();
        Map<String, String> props = new HashMap<>();
        props.put("atlasmapping", "{}");
        Step step2 = new SimpleStep.Builder().stepKind("mapper").configuredProperties(props).build();
        Step step3 = new SimpleStep.Builder().stepKind("endpoint").connection(new Connection.Builder().configuredProperties(Collections.emptyMap()).build()).configuredProperties(map("httpUri", "http://localhost:8080/bye")).action(new Action.Builder().connectorId("http").camelConnectorPrefix("http-post").camelConnectorGAV("io.syndesis:http-post-connector:0.4.5").build()).build();

        GenerateProjectRequest request = ImmutableGenerateProjectRequest
            .builder()
            .gitHubUser("noob")
            .gitHubRepoName("test")
            .integration(new Integration.Builder()
                .id("test-integration")
                .name("Test Integration")
                .steps( Arrays.asList(step1, step2, step3))
                .build())
            .connectors(connectors)
            .build();

        Map<String, byte[]> files = new DefaultProjectGenerator(new ConnectorCatalog(new ConnectorCatalogProperties()), new ProjectGeneratorProperties(), registry).generate(request);

        assertFileContents(files.get("src/main/resources/syndesis.yml"), "test-mapper-syndesis.yml");
        assertThat(new String(files.get("src/main/resources/mapping-step-2.json"))).isEqualTo("{}");
    }


    @Test
    public void testWithFiler() throws Exception {
        Step step1 = new SimpleStep.Builder().stepKind("endpoint").connection(new Connection.Builder().configuredProperties(map()).build()).configuredProperties(map("period",5000)).action(new Action.Builder().connectorId("timer").camelConnectorPrefix("periodic-timer").camelConnectorGAV("io.syndesis:timer-connector:0.4.5").build()).build();
        //Step step1 = new SimpleStep.Builder().stepKind("endpoint").connection(new Connection.Builder().configuredProperties(map()).build()).action(new Action.Builder().connectorId("twitter").camelConnectorPrefix("twitter-mention").camelConnectorGAV("io.syndesis:twitter-mention-connector:0.4.5").build()).build();
        Step step2 = new FilterStep.Builder().stepKind("filter")
            .type(FilterType.RULE)
            .predicate(FilterPredicate.AND)
            .addRule(new FilterRule.Builder()
            .path("$in.header.counter")
                .op(">")
                .value("10")
            .build()).build();

        Step step3 = new SimpleStep.Builder().stepKind("endpoint").connection(new Connection.Builder().configuredProperties(Collections.emptyMap()).build()).configuredProperties(map("httpUri", "http://localhost:8080/bye")).action(new Action.Builder().connectorId("http").camelConnectorPrefix("http-post").camelConnectorGAV("io.syndesis:http-post-connector:0.4.5").build()).build();

        GenerateProjectRequest request = ImmutableGenerateProjectRequest
            .builder()
            .gitHubUser("noob")
            .gitHubRepoName("test")
            .integration(new Integration.Builder()
                .id("test-integration")
                .name("Test Integration")
                .steps( Arrays.asList(step1, step2, step3))
                .build())
            .connectors(connectors)
            .build();

        Map<String, byte[]> files = new DefaultProjectGenerator(new ConnectorCatalog(new ConnectorCatalogProperties()), new ProjectGeneratorProperties(), registry).generate(request);

        assertFileContents(files.get("src/main/resources/syndesis.yml"), "test-filter-syndesis.yml");
    }
}
