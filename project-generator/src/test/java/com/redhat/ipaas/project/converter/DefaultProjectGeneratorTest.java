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
package com.redhat.ipaas.project.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.redhat.ipaas.connector.catalog.ConnectorCatalog;
import com.redhat.ipaas.connector.catalog.ConnectorCatalogProperties;
import com.redhat.ipaas.model.connection.Action;
import com.redhat.ipaas.model.connection.Connection;
import com.redhat.ipaas.model.connection.Connector;
import com.redhat.ipaas.model.integration.Integration;
import com.redhat.ipaas.model.integration.Step;

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

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultProjectGeneratorTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new Jdk8Module());
    private static final TypeReference<HashMap<String, Connector>> CONNECTOR_MAP_TYPE_REF = new TypeReference<HashMap<String, Connector>>() {
    };

    private Map<String, Connector> connectors = new HashMap<>();

    @Before
    public void setUp() throws IOException {
        connectors.putAll(OBJECT_MAPPER.readValue(this.getClass().getResourceAsStream("test-connectors.json"), CONNECTOR_MAP_TYPE_REF));
    }

    @Test
    public void testConvert() throws Exception {
        Step step1 = new Step.Builder().stepKind("endpoint").connection(new Connection.Builder().configuredProperties(map()).build()).configuredProperties(map("period",5000)).action(new Action.Builder().connectorId("timer").camelConnectorPrefix("periodic-timer").camelConnectorGAV("com.redhat.ipaas:timer-connector:0.2.1").build()).build();
        Step step2 = new Step.Builder().stepKind("endpoint").connection(new Connection.Builder().configuredProperties(map()).build()).configuredProperties(map("httpUri", "http://localhost:8080/hello")).action(new Action.Builder().connectorId("http").camelConnectorPrefix("http-get").camelConnectorGAV("com.redhat.ipaas:http-get-connector:0.2.1").build()).build();
        Step step3 = new Step.Builder().stepKind("log").configuredProperties(map("message", "Hello World! ${body}")).build();
        Step step4 = new Step.Builder().stepKind("endpoint").connection(new Connection.Builder().configuredProperties(Collections.emptyMap()).build()).configuredProperties(map("httpUri", "http://localhost:8080/bye")).action(new Action.Builder().connectorId("http").camelConnectorPrefix("http-post").camelConnectorGAV("com.redhat.ipaas:http-post-connector:0.2.1").build()).build();

        GenerateProjectRequest request = ImmutableGenerateProjectRequest
            .builder()
            .integration(new Integration.Builder()
                .id("test-integration")
                .name("Test Integration")
                .description("This is a test integration!")
                .gitRepo("https://ourgithhost.somewhere/test.git")
                .steps( Arrays.asList(step1, step2, step3, step4))
                .build())
            .connectors(connectors)
            .build();

        Map<String, byte[]> files = new DefaultProjectGenerator(new ConnectorCatalog(new ConnectorCatalogProperties()), new ProjectGeneratorProperties()).generate(request);

        assertFileContents(files.get("README.md"), "test-README.md");
        assertFileContents(files.get("src/main/java/com/redhat/ipaas/example/Application.java"), "test-Application.java");
        assertFileContents(files.get("src/main/resources/application.yml"), "test-application.yml");
        assertFileContents(files.get("src/main/resources/funktion.yml"), "test-funktion.yml");
        assertFileContents(files.get("pom.xml"), "test-pom.xml");
    }


    @Test
    public void testConverterWithPasswordMasking() throws Exception {
        Step step1 = new Step.Builder().stepKind("endpoint").connection(new Connection.Builder().configuredProperties(map()).build()).configuredProperties(map("period",5000)).action(new Action.Builder().connectorId("timer").camelConnectorPrefix("periodic-timer").camelConnectorGAV("com.redhat.ipaas:timer-connector:0.2.1").build()).build();
        Step step2 = new Step.Builder().stepKind("endpoint").connection(new Connection.Builder().configuredProperties(map()).build()).configuredProperties(map("httpUri", "http://localhost:8080/hello", "username", "admin", "password", "admin")).action(new Action.Builder().connectorId("http").camelConnectorPrefix("http-get").camelConnectorGAV("com.redhat.ipaas:http-get-connector:0.2.1").build()).build();

        GenerateProjectRequest request = ImmutableGenerateProjectRequest
            .builder()
            .integration(new Integration.Builder()
                .id("test-integration")
                .name("Test Integration")
                .description("This is a test integration!")
                .gitRepo("https://ourgithhost.somewhere/test.git")
                .steps( Arrays.asList(step1, step2))
                .build())
            .connectors(connectors)
            .build();

        ProjectGeneratorProperties generatorProperties = new ProjectGeneratorProperties();
        generatorProperties.setSecretMaskingEnabled(true);

        Map<String, byte[]> files = new DefaultProjectGenerator(new ConnectorCatalog(new ConnectorCatalogProperties()), generatorProperties).generate(request);


        assertFileContents(files.get("src/main/resources/application.yml"), "test-application.yml");
        assertFileContents(files.get("src/main/resources/funktion.yml"), "test-funktion-with-secrets.yml");
    }


    @Test
    public void testConvertFromJson() throws Exception {

        JsonNode json = new ObjectMapper().readTree(this.getClass().getResourceAsStream("test-integration.json"));

        GenerateProjectRequest request = ImmutableGenerateProjectRequest
            .builder()
            .integration(new ObjectMapper().registerModule(new Jdk8Module()).readValue(json.get("data").toString(), Integration.class))
            .connectors(connectors)
            .build();

        Map<String, byte[]> files = new DefaultProjectGenerator(new ConnectorCatalog(new ConnectorCatalogProperties()), new ProjectGeneratorProperties()).generate(request);

        assertFileContents(files.get("README.md"), "test-pull-push-README.md");
        assertFileContents(files.get("src/main/java/com/redhat/ipaas/example/Application.java"), "test-Application.java");
        assertFileContents(files.get("src/main/resources/application.yml"), "test-pull-push-application.yml");
        assertFileContents(files.get("src/main/resources/funktion.yml"), "test-pull-push-funktion.yml");
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
    private static HashMap<String, ? extends String> map(Object... values) {
        HashMap<String, String> rc = new HashMap<String, String>();
        for (int i = 0; i + 1 < values.length; i += 2) {
            rc.put(values[i].toString(), values[i + 1].toString());
        }
        return rc;
    }

}
