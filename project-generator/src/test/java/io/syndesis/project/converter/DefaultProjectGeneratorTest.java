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

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import io.syndesis.connector.catalog.ConnectorCatalog;
import io.syndesis.connector.catalog.ConnectorCatalogProperties;
import io.syndesis.integration.support.Strings;
import io.syndesis.model.connection.Action;
import io.syndesis.model.connection.Connection;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.filter.ExpressionFilterStep;
import io.syndesis.model.filter.FilterPredicate;
import io.syndesis.model.filter.RuleFilterStep;
import io.syndesis.model.integration.Integration;
import io.syndesis.model.integration.SimpleStep;
import io.syndesis.model.integration.Step;
import io.syndesis.project.converter.ProjectGeneratorProperties.Templates;
import io.syndesis.project.converter.visitor.DataMapperStepVisitor;
import io.syndesis.project.converter.visitor.EndpointStepVisitor;
import io.syndesis.project.converter.visitor.ExpressionFilterStepVisitor;
import io.syndesis.project.converter.visitor.RuleFilterStepVisitor;
import io.syndesis.project.converter.visitor.StepVisitorFactoryRegistry;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class DefaultProjectGeneratorTest {
    private static final ConnectorCatalogProperties CATALOG_PROPERTIES = new ConnectorCatalogProperties();
    private static Properties properties = new Properties();
    private static final ObjectMapper OBJECT_MAPPER;
    private static final TypeReference<HashMap<String, Connector>> CONNECTOR_MAP_TYPE_REF;

    private final StepVisitorFactoryRegistry registry;
    private final String basePath;
    private final List<Templates.Resource> additionalResources;
    private final Map<String, Connector> connectors;

    static {
            System.setProperty("groovy.grape.report.downloads", "true");
            System.setProperty("ivy.message.logger.level", "3");

            try {
                properties.load(DefaultProjectGeneratorTest.class.getResourceAsStream("test.properties"));
            } catch (IOException e) {
                Assert.fail("Can't read the test.properties");
            }

            OBJECT_MAPPER = new ObjectMapper().registerModule(new Jdk8Module());
            CONNECTOR_MAP_TYPE_REF = new TypeReference<HashMap<String, Connector>>() {
            };
            final Map<String, String> repositories = new HashMap<>();
            repositories.put("maven.central", "https://repo1.maven.org/maven2");
            repositories.put("redhat.ga", "https://maven.repository.redhat.com/ga");
            repositories.put("jboss.ea", "https://repository.jboss.org/nexus/content/groups/ea");
            CATALOG_PROPERTIES.setMavenRepos(repositories);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {
                "",
                Collections.emptyList()
            },
            {
                "redhat",
                Arrays.asList(
                    new Templates.Resource("deployment.yml", "src/main/fabric8/deployment.yml"),
                    new Templates.Resource("settings.xml", "configuration/settings.xml")
                )
            }
        });
    }

    public DefaultProjectGeneratorTest(String basePath, List<Templates.Resource> additionalResources) throws IOException {
        this.basePath = basePath;
        this.additionalResources = additionalResources;
        this.registry = new StepVisitorFactoryRegistry(
            Arrays.asList(
                new DataMapperStepVisitor.Factory(),
                new EndpointStepVisitor.Factory(),
                new RuleFilterStepVisitor.Factory(),
                new ExpressionFilterStepVisitor.Factory()
            )
        );

        this.connectors = new HashMap<>();
        this.connectors.putAll(OBJECT_MAPPER.readValue(getClass().getResourceAsStream("test-connectors.json"), CONNECTOR_MAP_TYPE_REF));
    }

    @Test
    public void testConvert() throws Exception {
        Step step1 = new SimpleStep.Builder().stepKind("endpoint").connection(new Connection.Builder().configuredProperties(map()).build()).configuredProperties(map("period",5000)).action(new Action.Builder().connectorId("timer").camelConnectorPrefix("periodic-timer").camelConnectorGAV("io.syndesis:timer-connector:0.5.0").build()).build();
        Step step2 = new SimpleStep.Builder().stepKind("endpoint").connection(new Connection.Builder().configuredProperties(map()).build()).configuredProperties(map("httpUri", "http://localhost:8080/hello")).action(new Action.Builder().connectorId("http").camelConnectorPrefix("http-get-connector").camelConnectorGAV("io.syndesis:http-get-connector:0.5.0").build()).build();
        Step step3 = new SimpleStep.Builder().stepKind("log").configuredProperties(map("message", "Hello World! ${body}")).build();
        Step step4 = new SimpleStep.Builder().stepKind("endpoint").connection(new Connection.Builder().configuredProperties(Collections.emptyMap()).build()).configuredProperties(map("httpUri", "http://localhost:8080/bye")).action(new Action.Builder().connectorId("http").camelConnectorPrefix("http-post-connector").camelConnectorGAV("io.syndesis:http-post-connector:0.5.0").build()).build();

        GenerateProjectRequest request = new GenerateProjectRequest.Builder()
            .gitHubUserLogin("noob")
            .gitHubRepoName("test")
            .integration(new Integration.Builder()
                .id("test-integration")
                .name("Test Integration")
                .description("This is a test integration!")
                .steps( Arrays.asList(step1, step2, step3, step4))
                .build())
            .connectors(connectors)
            .build();

        ProjectGeneratorProperties generatorProperties = new ProjectGeneratorProperties();
        generatorProperties.getTemplates().setOverridePath(this.basePath);
        generatorProperties.getTemplates().getAdditionalResources().addAll(this.additionalResources);

        Map<String, byte[]> files = new DefaultProjectGenerator(new ConnectorCatalog(CATALOG_PROPERTIES), generatorProperties, registry).generate(request);

        assertFileContents(generatorProperties, files.get("README.md"), "test-README.md");
        assertFileContents(generatorProperties, files.get("src/main/java/io/syndesis/example/Application.java"), "test-Application.java");
        assertFileContents(generatorProperties, files.get("src/main/resources/application.properties"), "test-application.properties");
        assertFileContents(generatorProperties, files.get("src/main/resources/syndesis.yml"), "test-syndesis.yml");
        assertFileContents(generatorProperties, files.get("pom.xml"), "test-pom.xml");

        for (Templates.Resource additionalResource : generatorProperties.getTemplates().getAdditionalResources()) {
            assertFileContents(generatorProperties, files.get(additionalResource.getDestination()), "test-" + additionalResource.getSource());
        }
    }


    @Test
    public void testConverterWithPasswordMasking() throws Exception {
        Step step1 = new SimpleStep.Builder().stepKind("endpoint").connection(new Connection.Builder().configuredProperties(map()).build()).configuredProperties(map("period",5000)).action(new Action.Builder().connectorId("timer").camelConnectorPrefix("periodic-timer").camelConnectorGAV("io.syndesis:timer-connector:0.5.0").build()).build();
        Step step2 = new SimpleStep.Builder().stepKind("endpoint").connection(new Connection.Builder().configuredProperties(map()).build()).configuredProperties(map("httpUri", "http://localhost:8080/hello", "username", "admin", "password", "admin", "token", "mytoken")).action(new Action.Builder().connectorId("http").camelConnectorPrefix("http-get-connector").camelConnectorGAV("io.syndesis:http-get-connector:0.5.0").build()).build();

        GenerateProjectRequest request = new GenerateProjectRequest.Builder()
            .integration(new Integration.Builder()
                .id("test-integration")
                .name("Test Integration")
                .description("This is a test integration!")
                .steps( Arrays.asList(step1, step2))
                .build())
            .connectors(connectors)
            .gitHubUserLogin("noob")
            .gitHubRepoName("test")
            .build();

        ProjectGeneratorProperties generatorProperties = new ProjectGeneratorProperties();
        generatorProperties.getTemplates().setOverridePath(this.basePath);
        generatorProperties.getTemplates().getAdditionalResources().addAll(this.additionalResources);
        generatorProperties.setSecretMaskingEnabled(true);

        Map<String, byte[]> files = new DefaultProjectGenerator(new ConnectorCatalog(CATALOG_PROPERTIES), generatorProperties, registry).generate(request);


        assertFileContents(generatorProperties, files.get("src/main/resources/application.properties"), "test-application.properties");
        assertFileContents(generatorProperties, files.get("src/main/resources/syndesis.yml"), "test-syndesis-with-secrets.yml");
    }


    @Test
    public void testConvertFromJson() throws Exception {

        JsonNode json = new ObjectMapper().readTree(this.getClass().getResourceAsStream("test-integration.json"));

        GenerateProjectRequest request = new GenerateProjectRequest.Builder()
            .gitHubUserLogin("noob")
            .gitHubRepoName("test")
            .integration(new ObjectMapper().registerModule(new Jdk8Module()).readValue(json.get("data").toString(), Integration.class))
            .connectors(connectors)
            .build();


        ProjectGeneratorProperties generatorProperties = new ProjectGeneratorProperties();
        generatorProperties.getTemplates().setOverridePath(this.basePath);
        generatorProperties.getTemplates().getAdditionalResources().addAll(this.additionalResources);

        Map<String, byte[]> files = new DefaultProjectGenerator(new ConnectorCatalog(CATALOG_PROPERTIES), generatorProperties, registry).generate(request);

        assertFileContents(generatorProperties, files.get("README.md"), "test-pull-push-README.md");
        assertFileContents(generatorProperties, files.get("src/main/java/io/syndesis/example/Application.java"), "test-Application.java");
        assertFileContents(generatorProperties, files.get("src/main/resources/application.properties"), "test-pull-push-application.properties");
        assertFileContents(generatorProperties, files.get("src/main/resources/syndesis.yml"), "test-pull-push-syndesis.yml");
        assertFileContents(generatorProperties, files.get("pom.xml"), "test-pull-push-pom.xml");
    }


    private static void assertFileContents(ProjectGeneratorProperties generatorProperties, byte[] actualContents, String expectedFileName) throws URISyntaxException, IOException {
        String overridePath = generatorProperties.getTemplates().getOverridePath();
        URL resource = null;

        if (!Strings.isEmpty(overridePath)) {
            resource = DefaultProjectGeneratorTest.class.getResource(overridePath + "/" + expectedFileName);
        }
        if (resource == null) {
            resource = DefaultProjectGeneratorTest.class.getResource(expectedFileName);
        }
        if (resource == null) {
            throw new IllegalArgumentException("Unable to find te required resource (" + expectedFileName + ")");
        }

        assertThat(new String(actualContents)).isEqualTo(
            new String(Files.readAllBytes(Paths.get(resource.toURI())), StandardCharsets.UTF_8)
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
        Step step1 = new SimpleStep.Builder().stepKind("endpoint").connection(new Connection.Builder().configuredProperties(map()).build()).configuredProperties(map("period",5000)).action(new Action.Builder().connectorId("timer").camelConnectorPrefix("periodic-timer").camelConnectorGAV("io.syndesis:timer-connector:0.5.0").build()).build();
        Map<String, String> props = new HashMap<>();
        props.put("atlasmapping", "{}");
        Step step2 = new SimpleStep.Builder().stepKind("mapper").configuredProperties(props).build();
        Step step3 = new SimpleStep.Builder().stepKind("endpoint").connection(new Connection.Builder().configuredProperties(Collections.emptyMap()).build()).configuredProperties(map("httpUri", "http://localhost:8080/bye")).action(new Action.Builder().connectorId("http").camelConnectorPrefix("http-post-connector").camelConnectorGAV("io.syndesis:http-post-connector:0.5.0").build()).build();

        GenerateProjectRequest request = new GenerateProjectRequest.Builder()
            .gitHubUserLogin("noob")
            .gitHubRepoName("test")
            .integration(new Integration.Builder()
                .id("test-integration")
                .name("Test Integration")
                .steps( Arrays.asList(step1, step2, step3))
                .build())
            .connectors(connectors)
            .build();


        ProjectGeneratorProperties generatorProperties = new ProjectGeneratorProperties();
        generatorProperties.getTemplates().setOverridePath(this.basePath);
        generatorProperties.getTemplates().getAdditionalResources().addAll(this.additionalResources);

        Map<String, byte[]> files = new DefaultProjectGenerator(new ConnectorCatalog(CATALOG_PROPERTIES), generatorProperties, registry).generate(request);

        assertFileContents(generatorProperties, files.get("src/main/resources/syndesis.yml"), "test-mapper-syndesis.yml");
        assertThat(new String(files.get("src/main/resources/mapping-step-2.json"))).isEqualTo("{}");
    }


    @Test
    public void testWithFilter() throws Exception {
        Step step1 = new SimpleStep.Builder().stepKind("endpoint").connection(new Connection.Builder().configuredProperties(map()).build()).configuredProperties(map("period",5000)).action(new Action.Builder().connectorId("timer").camelConnectorPrefix("periodic-timer").camelConnectorGAV("io.syndesis:timer-connector:0.5.0").build()).build();
        //Step step1 = new SimpleStep.Builder().stepKind("endpoint").connection(new Connection.Builder().configuredProperties(map()).build()).action(new Action.Builder().connectorId("twitter").camelConnectorPrefix("twitter-mention").camelConnectorGAV("io.syndesis:twitter-mention-connector:0.5.0").build()).build();
        Map<String, String> props = new HashMap<>();
        props.put("predicate", FilterPredicate.AND.toString());
        props.put("rules", "[{ \"path\": \"in.header.counter\", \"op\": \">\", \"value\": \"10\" }]");
        Step step2 = new RuleFilterStep.Builder().configuredProperties(props).build();

        Step step3 = new SimpleStep.Builder().stepKind("endpoint").connection(new Connection.Builder().configuredProperties(Collections.emptyMap()).build()).configuredProperties(map("httpUri", "http://localhost:8080/bye")).action(new Action.Builder().connectorId("http").camelConnectorPrefix("http-post-connector").camelConnectorGAV("io.syndesis:http-post-connector:0.5.0").build()).build();

        Step step4 = new ExpressionFilterStep.Builder().configuredProperties(map("filter", "${body.germanSecondLeagueChampion} equals 'FCN'")).build();

        GenerateProjectRequest request = new GenerateProjectRequest.Builder()
            .gitHubUserLogin("noob")
            .gitHubRepoName("test")
            .integration(new Integration.Builder()
                .id("test-integration")
                .name("Test Integration")
                .steps( Arrays.asList(step1, step2, step3, step4))
                .build())
            .connectors(connectors)
            .build();

        ProjectGeneratorProperties generatorProperties = new ProjectGeneratorProperties();
        generatorProperties.getTemplates().setOverridePath(this.basePath);
        generatorProperties.getTemplates().getAdditionalResources().addAll(this.additionalResources);

        Map<String, byte[]> files = new DefaultProjectGenerator(new ConnectorCatalog(CATALOG_PROPERTIES), generatorProperties, registry).generate(request);


        assertFileContents(generatorProperties, files.get("src/main/resources/syndesis.yml"), "test-filter-syndesis.yml");
    }
}
