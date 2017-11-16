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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.ResourceBundle;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import io.syndesis.connector.catalog.ConnectorCatalog;
import io.syndesis.connector.catalog.ConnectorCatalogProperties;
import io.syndesis.core.MavenProperties;
import io.syndesis.dao.extension.ExtensionDataManager;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.integration.support.Strings;
import io.syndesis.model.action.ConnectorAction;
import io.syndesis.model.action.ConnectorDescriptor;
import io.syndesis.model.action.ExtensionAction;
import io.syndesis.model.action.ExtensionDescriptor;
import io.syndesis.model.connection.Connection;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.extension.Extension;
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
import io.syndesis.project.converter.visitor.ExtensionStepVisitor;
import io.syndesis.project.converter.visitor.RuleFilterStepVisitor;
import io.syndesis.project.converter.visitor.StepVisitorFactoryRegistry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class DefaultProjectGeneratorTest {
    private static final String CONNECTORS_VERSION = ResourceBundle.getBundle("test").getString("connectors.version");
    private static MavenProperties mavenProperties = new MavenProperties(map("maven.central", "https://repo1.maven.org/maven2",
        "redhat.ga", "https://maven.repository.redhat.com/ga",
        "jboss.ea", "https://repository.jboss.org/nexus/content/groups/ea"));
    private static final ConnectorCatalogProperties CATALOG_PROPERTIES = new ConnectorCatalogProperties(mavenProperties);
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
    }

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

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
            new DataMapperStepVisitor.Factory(),
            new EndpointStepVisitor.Factory(),
            new RuleFilterStepVisitor.Factory(),
            new ExpressionFilterStepVisitor.Factory(),
            new ExtensionStepVisitor.Factory()
        );

        this.connectors = new HashMap<>();
        this.connectors.putAll(OBJECT_MAPPER.readValue(getClass().getResourceAsStream("test-connectors.json"), CONNECTOR_MAP_TYPE_REF));
    }

    // ************************************************
    // Tests
    // ************************************************

    @Test
    public void testConvert() throws Exception {
        Step step1 = new SimpleStep.Builder()
            .stepKind("endpoint").
                connection(new Connection.Builder()
                    .configuredProperties(map())
                    .build())
            .configuredProperties(map("period",5000))
            .action(new ConnectorAction.Builder()
                .descriptor(new ConnectorDescriptor.Builder()
                    .connectorId("timer")
                    .camelConnectorPrefix("periodic-timer-connector")
                    .camelConnectorGAV("io.syndesis:timer-connector:" + CONNECTORS_VERSION)
                    .build()
                ).build())
            .build();

        Step step2 = new SimpleStep.Builder()
            .stepKind("endpoint")
            .connection(new Connection.Builder()
                .configuredProperties(map())
                .build())
            .configuredProperties(map("httpUri", "http://localhost:8080/hello"))
            .action(new ConnectorAction.Builder()
                .descriptor(new ConnectorDescriptor.Builder()
                    .connectorId("http")
                    .camelConnectorPrefix("http-get-connector")
                    .camelConnectorGAV("io.syndesis:http-get-connector:" + CONNECTORS_VERSION)
                    .build()
                ).build())
            .build();

        Step step3 = new SimpleStep.Builder()
            .stepKind("log")
            .configuredProperties(map("message", "Hello World! ${body}"))
            .build();

        Step step4 = new SimpleStep.Builder()
            .stepKind("endpoint")
            .connection(new Connection.Builder()
                .configuredProperties(Collections.emptyMap())
                .build())
            .configuredProperties(map("httpUri", "http://localhost:8080/bye"))
            .action(new ConnectorAction.Builder()
                .descriptor(new ConnectorDescriptor.Builder()
                    .connectorId("http")
                    .camelConnectorPrefix("http-post-connector")
                    .camelConnectorGAV("io.syndesis:http-post-connector:" + CONNECTORS_VERSION)
                    .build()
                ).build())
            .build();

        Integration integration = new Integration.Builder()
            .id("test-integration")
            .name("Test Integration")
            .description("This is a test integration!")
            .steps( Arrays.asList(step1, step2, step3, step4))
            .build();

        ProjectGeneratorProperties generatorProperties = new ProjectGeneratorProperties(new MavenProperties());
        generatorProperties.getTemplates().setOverridePath(this.basePath);
        generatorProperties.getTemplates().getAdditionalResources().addAll(this.additionalResources);

        Path runtimeDir = generate(integration, generatorProperties);

        assertFileContents(generatorProperties, runtimeDir.resolve("src/main/java/io/syndesis/example/Application.java"), "test-Application.java");
        assertFileContents(generatorProperties, runtimeDir.resolve("src/main/resources/application.properties"), "test-application.properties");
        assertFileContents(generatorProperties, runtimeDir.resolve("src/main/resources/syndesis.yml"), "test-syndesis.yml");
        assertFileContents(generatorProperties, runtimeDir.resolve("pom.xml"), "test-pom.xml");

        for (Templates.Resource additionalResource : generatorProperties.getTemplates().getAdditionalResources()) {
            assertFileContents(generatorProperties, runtimeDir.resolve(additionalResource.getDestination()), "test-" + additionalResource.getSource());
        }
    }

    @Test
    public void testConverterWithPasswordMasking() throws Exception {
        Step step1 = new SimpleStep.Builder()
            .stepKind("endpoint")
            .connection(new Connection.Builder()
                .id("1")
                .configuredProperties(map())
                .build())
            .configuredProperties(map("period",5000))
                .action(new ConnectorAction.Builder()
                    .descriptor(new ConnectorDescriptor.Builder()
                    .connectorId("timer")
                    .camelConnectorPrefix("periodic-timer-connector")
                    .camelConnectorGAV("io.syndesis:timer-connector:" + CONNECTORS_VERSION)
                    .build()
                ).build())
            .build();

        Step step2 = new SimpleStep.Builder()
            .stepKind("endpoint")
            .connection(new Connection.Builder()
                .id("2")
                .configuredProperties(map())
                .build())
            .configuredProperties(map("httpUri", "http://localhost:8080/hello", "username", "admin", "password", "admin", "token", "mytoken"))
            .action(new ConnectorAction.Builder()
                .descriptor(new ConnectorDescriptor.Builder()
                    .connectorId("http")
                    .camelConnectorPrefix("http-get-connector")
                    .camelConnectorGAV("io.syndesis:http-get-connector:" + CONNECTORS_VERSION)
                    .build()
                ).build())
            .build();

        Integration integration = new Integration.Builder()
            .id("test-integration")
            .name("Test Integration")
            .description("This is a test integration!")
            .steps(Arrays.asList(step1, step2))
            .build();

        ProjectGeneratorProperties generatorProperties = new ProjectGeneratorProperties(mavenProperties);
        generatorProperties.getTemplates().setOverridePath(this.basePath);
        generatorProperties.getTemplates().getAdditionalResources().addAll(this.additionalResources);
        generatorProperties.setSecretMaskingEnabled(true);

        Path runtimeDir = generate(integration, generatorProperties);

        assertFileContents(generatorProperties, runtimeDir.resolve("src/main/resources/application.properties"), "test-application.properties");
        assertFileContents(generatorProperties, runtimeDir.resolve("src/main/resources/syndesis.yml"), "test-syndesis-with-secrets.yml");
    }

    @Test
    public void testConverterWithPasswordMaskingAndMultipleConnectorOfSameType() throws Exception {
        Step step1 = new SimpleStep.Builder()
            .stepKind("endpoint")
            .connection(new Connection.Builder()
                .id("1")
                .configuredProperties(map())
                .build())
            .configuredProperties(map("period",5000))
            .action(new ConnectorAction.Builder()
                .descriptor(new ConnectorDescriptor.Builder()
                    .connectorId("timer")
                    .camelConnectorPrefix("periodic-timer-connector")
                    .camelConnectorGAV("io.syndesis:timer-connector:" + CONNECTORS_VERSION)
                    .build()
                ).build())
            .build();

        Step step2 = new SimpleStep.Builder()
            .stepKind("endpoint")
            .connection(new Connection.Builder()
                .id("2")
                .configuredProperties(map())
                .build())
            .configuredProperties(map("httpUri", "http://localhost:8080/hello", "username", "admin", "password", "admin", "token", "mytoken"))
            .action(new ConnectorAction.Builder()
                .descriptor(new ConnectorDescriptor.Builder()
                    .connectorId("http")
                    .camelConnectorPrefix("http-get-connector")
                    .camelConnectorGAV("io.syndesis:http-get-connector:" + CONNECTORS_VERSION)
                    .build()
                ).build())
            .build();

        Step step3 = new SimpleStep.Builder()
            .stepKind("endpoint")
            .connection(new Connection.Builder()
                .id("3")
                .configuredProperties(map())
                .build())
            .configuredProperties(map("httpUri", "http://localhost:8080/bye", "username", "admin", "password", "admin", "token", "mytoken"))
            .action(new ConnectorAction.Builder()
                .descriptor(new ConnectorDescriptor.Builder()
                    .connectorId("http")
                    .camelConnectorPrefix("http-get-connector")
                    .camelConnectorGAV("io.syndesis:http-get-connector:" + CONNECTORS_VERSION)
                    .build()
                ).build())
            .build();

        Integration integration = new Integration.Builder()
            .id("test-integration")
            .name("Test Integration")
            .description("This is a test integration!")
            .steps(Arrays.asList(step1, step2, step3))
            .build();

        ProjectGeneratorProperties generatorProperties = new ProjectGeneratorProperties(mavenProperties);
        generatorProperties.getTemplates().setOverridePath(this.basePath);
        generatorProperties.getTemplates().getAdditionalResources().addAll(this.additionalResources);
        generatorProperties.setSecretMaskingEnabled(true);

        Path runtimeDir = generate(integration, generatorProperties);

        assertFileContents(generatorProperties, runtimeDir.resolve("src/main/resources/application.properties"), "test-application.properties");
        assertFileContents(generatorProperties, runtimeDir.resolve("src/main/resources/syndesis.yml"), "test-syndesis-with-secrets-and-multiple-connector-of-same-type.yml");
    }

    @Test
    public void testConvertFromJson() throws Exception {
        JsonNode json = new ObjectMapper().readTree(this.getClass().getResourceAsStream("test-integration.json"));
        Integration integration = new ObjectMapper().registerModule(new Jdk8Module()).readValue(json.get("data").toString(), Integration.class);

        ProjectGeneratorProperties generatorProperties = new ProjectGeneratorProperties(mavenProperties);
        generatorProperties.getTemplates().setOverridePath(this.basePath);
        generatorProperties.getTemplates().getAdditionalResources().addAll(this.additionalResources);

        Path runtimePath = generate(integration, generatorProperties);

        assertFileContents(generatorProperties, runtimePath.resolve("src/main/java/io/syndesis/example/Application.java"), "test-Application.java");
        assertFileContents(generatorProperties, runtimePath.resolve("src/main/resources/application.properties"), "test-pull-push-application.properties");
        assertFileContents(generatorProperties, runtimePath.resolve("src/main/resources/syndesis.yml"), "test-pull-push-syndesis.yml");
        assertFileContents(generatorProperties, runtimePath.resolve("pom.xml"), "test-pull-push-pom.xml");
    }

    @Test
    public void testMapper() throws Exception {
        Step step1 = new SimpleStep.Builder()
            .stepKind("endpoint")
            .connection(new Connection.Builder()
                .configuredProperties(map())
                .build())
            .configuredProperties(map("period", 5000))
            .action(new ConnectorAction.Builder()
                .descriptor(new ConnectorDescriptor.Builder()
                    .connectorId("timer")
                    .camelConnectorPrefix("periodic-timer-connector")
                    .camelConnectorGAV("io.syndesis:timer-connector:" + CONNECTORS_VERSION)
                    .build()
                ).build())
            .build();

        Step step2 = new SimpleStep.Builder()
            .stepKind("mapper")
            .configuredProperties(map("atlasmapping", "{}"))
            .build();

        Step step3 = new SimpleStep.Builder()
            .stepKind("endpoint")
            .connection(new Connection.Builder()
                .configuredProperties(Collections.emptyMap())
                .build())
            .configuredProperties(map("httpUri", "http://localhost:8080/bye"))
            .action(new ConnectorAction.Builder()
                .descriptor(new ConnectorDescriptor.Builder()
                    .connectorId("http")
                    .camelConnectorPrefix("http-post-connector")
                    .camelConnectorGAV("io.syndesis:http-post-connector:" + CONNECTORS_VERSION)
                    .build()
                ).build())
            .build();

        Integration integration = new Integration.Builder()
            .id("test-integration")
            .name("Test Integration")
            .steps( Arrays.asList(step1, step2, step3))
            .build();

        ProjectGeneratorProperties generatorProperties = new ProjectGeneratorProperties(mavenProperties);
        generatorProperties.getTemplates().setOverridePath(this.basePath);
        generatorProperties.getTemplates().getAdditionalResources().addAll(this.additionalResources);

        Path runtimePath = generate(integration, generatorProperties);
        runtimePath.toFile().deleteOnExit();

        assertFileContents(generatorProperties, runtimePath.resolve("src/main/resources/syndesis.yml"), "test-mapper-syndesis.yml");
        assertThat(new String(Files.readAllBytes(runtimePath.resolve("src/main/resources/mapping-step-2.json")))).isEqualTo("{}");
    }

    @Test
    public void testWithFilter() throws Exception {
        Step step1 = new SimpleStep.Builder()
            .stepKind("endpoint")
            .connection(new Connection.Builder()
                .configuredProperties(map())
                .build())
            .configuredProperties(map("period", 5000))
            .action(new ConnectorAction.Builder()
                .descriptor(new ConnectorDescriptor.Builder()
                    .connectorId("timer")
                    .camelConnectorPrefix("periodic-timer-connector")
                    .camelConnectorGAV("io.syndesis:timer-connector:" + CONNECTORS_VERSION)
                    .build()
                ).build())
            .build();

        Step step2 = new RuleFilterStep.Builder()
            .configuredProperties(map(
                "predicate", FilterPredicate.AND.toString(),
                "rules", "[{ \"path\": \"in.header.counter\", \"op\": \">\", \"value\": \"10\" }]"
            ))
            .build();

        Step step3 = new SimpleStep.Builder()
            .stepKind("endpoint")
            .connection(new Connection.Builder()
                .configuredProperties(Collections.emptyMap())
                .build())
            .configuredProperties(map("httpUri", "http://localhost:8080/bye"))
            .action(new ConnectorAction.Builder()
                .descriptor(new ConnectorDescriptor.Builder()
                    .connectorId("http")
                    .camelConnectorPrefix("http-post-connector")
                    .camelConnectorGAV("io.syndesis:http-post-connector:" + CONNECTORS_VERSION)
                    .build()
                ).build())
            .build();

        Step step4 = new ExpressionFilterStep.Builder()
            .configuredProperties(map("filter", "${body.germanSecondLeagueChampion} equals 'FCN'"))
            .build();

        Integration integration = new Integration.Builder()
            .id("test-integration")
            .name("Test Integration")
            .steps( Arrays.asList(step1, step2, step3, step4))
            .build();

        ProjectGeneratorProperties generatorProperties = new ProjectGeneratorProperties(mavenProperties);
        generatorProperties.getTemplates().setOverridePath(this.basePath);
        generatorProperties.getTemplates().getAdditionalResources().addAll(this.additionalResources);

        Path runtimeDir = generate(integration, generatorProperties);

        assertFileContents(generatorProperties, runtimeDir.resolve("src/main/resources/syndesis.yml"), "test-filter-syndesis.yml");
    }

    @SuppressWarnings("PMD.ExcessiveMethodLength")
    @Test
    public void testWithExtension() throws Exception {
        Step step1 = new SimpleStep.Builder()
            .stepKind("endpoint")
            .connection(new Connection.Builder()
                .id("1")
                .configuredProperties(map())
                .build())
            .configuredProperties(map("period",5000))
            .action(new ConnectorAction.Builder()
                .descriptor(new ConnectorDescriptor.Builder()
                    .connectorId("timer")
                    .camelConnectorPrefix("periodic-timer-connector")
                    .camelConnectorGAV("io.syndesis:timer-connector:" + CONNECTORS_VERSION)
                    .build()
                ).build())
            .build();

        Step step2 = new SimpleStep.Builder()
            .stepKind("extension")
            .extension(new Extension.Builder()
                .extensionId("my-extension-1")
                .addDependency("org.slf4j:slf4j-api:1.7.11")
                .addDependency("org.slf4j:slf4j-simple:1.7.11")
                .build()
            )
            .configuredProperties(map("key-1", "val-1", "key-2", "val-2"))
            .action(new ExtensionAction.Builder()
                .descriptor(new ExtensionDescriptor.Builder()
                    .kind(ExtensionAction.Kind.ENDPOINT)
                    .entrypoint("direct:extension")
                    .build()
                ).build())
            .build();

        Step step3 = new SimpleStep.Builder()
            .stepKind("extension")
            .extension(new Extension.Builder()
                .extensionId("my-extension-2")
                .build()
            )
            .configuredProperties(map("key-1", "val-1", "key-2", "val-2"))
            .action(new ExtensionAction.Builder()
                .descriptor(new ExtensionDescriptor.Builder()
                    .kind(ExtensionAction.Kind.BEAN)
                    .entrypoint("com.example.MyExtension::action")
                    .build()
                ).build())
            .build();

        Step step4 = new SimpleStep.Builder()
            .stepKind("extension")
            .extension(new Extension.Builder()
                .extensionId("my-extension-3")
                .build()
            )
            .configuredProperties(map("key-1", "val-1", "key-2", "val-2"))
            .action(new ExtensionAction.Builder()
                .descriptor(new ExtensionDescriptor.Builder()
                    .kind(ExtensionAction.Kind.STEP)
                    .entrypoint("com.example.MyStep")
                    .build()
                ).build())
            .build();

        Step step5 = new SimpleStep.Builder()
            .stepKind("endpoint")
            .connection(new Connection.Builder()
                .id("3")
                .configuredProperties(map())
                .build())
            .configuredProperties(map("httpUri", "http://localhost:8080/bye", "username", "admin", "password", "admin", "token", "mytoken"))
            .action(new ConnectorAction.Builder()
                .descriptor(new ConnectorDescriptor.Builder()
                    .connectorId("http")
                    .camelConnectorPrefix("http-get-connector")
                    .camelConnectorGAV("io.syndesis:http-get-connector:" + CONNECTORS_VERSION)
                    .build()
                ).build())
            .build();

        Integration integration = new Integration.Builder()
            .id("test-integration")
            .name("Test Integration")
            .description("This is a test integration!")
            .steps(Arrays.asList(step1, step2, step3, step4, step5))
            .build();

        ProjectGeneratorProperties generatorProperties = new ProjectGeneratorProperties(mavenProperties);
        generatorProperties.getTemplates().setOverridePath(this.basePath);
        generatorProperties.getTemplates().getAdditionalResources().addAll(this.additionalResources);
        generatorProperties.setSecretMaskingEnabled(true);

        Path runtimeDir = generate(integration, generatorProperties);

        assertFileContents(generatorProperties, runtimeDir.resolve("src/main/resources/syndesis.yml"), "test-syndesis-extension.yml");
        assertFileContents(generatorProperties, runtimeDir.resolve("src/main/resources/loader.properties"), "test-loader.properties");
        assertFileContents(generatorProperties, runtimeDir.resolve("pom.xml"), "test-pom-extension.xml");
        assertFileContents(generatorProperties, runtimeDir.resolve(".s2i/bin/assemble"), "test-s2i-assemble");

        assertThat(Files.exists(runtimeDir.resolve("extensions/my-extension-1.jar")));
        assertThat(Files.exists(runtimeDir.resolve("extensions/my-extension-2.jar")));
        assertThat(Files.exists(runtimeDir.resolve("extensions/my-extension-3.jar")));
    }

    // ************************************************
    // Helpers
    // ************************************************

    private Path generate(Integration integration, ProjectGeneratorProperties generatorProperties) throws IOException {
        final DataManager dataManager = mock(DataManager.class);
        final ExtensionDataManager extensionDataManager = mock(ExtensionDataManager.class);
        final ProjectGenerator generator = new DefaultProjectGenerator(generatorProperties, new ConnectorCatalog(CATALOG_PROPERTIES), registry, dataManager, Optional.of(extensionDataManager));

        // Mock data manager
        connectors.forEach((k, v) -> when(dataManager.fetch(Connector.class, k)).thenReturn(v));

        // mock extension manager
        when(extensionDataManager.getExtensionBinaryFile("my-extension-1")).thenReturn(IOUtils.toInputStream("my-extension-1", StandardCharsets.UTF_8));
        when(extensionDataManager.getExtensionBinaryFile("my-extension-2")).thenReturn(IOUtils.toInputStream("my-extension-2", StandardCharsets.UTF_8));
        when(extensionDataManager.getExtensionBinaryFile("my-extension-3")).thenReturn(IOUtils.toInputStream("my-extension-3", StandardCharsets.UTF_8));

        try (InputStream is = generator.generate(integration)) {
            Path ret = testFolder.newFolder("integration-runtime").toPath();

            try (TarArchiveInputStream tis = new TarArchiveInputStream(is)) {
                TarArchiveEntry tarEntry = tis.getNextTarEntry();
                // tarIn is a TarArchiveInputStream
                while (tarEntry != null) {// create a file with the same name as the tarEntry
                    File destPath = new File(ret.toFile(), tarEntry.getName());
                    if (tarEntry.isDirectory()) {
                        destPath.mkdirs();
                    } else {
                        destPath.getParentFile().mkdirs();
                        destPath.createNewFile();
                        byte[] btoRead = new byte[8129];
                        BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(destPath));
                        int len = tis.read(btoRead);
                        while (len != -1) {
                            bout.write(btoRead, 0, len);
                            len = tis.read(btoRead);
                        }
                        bout.close();
                    }
                    tarEntry = tis.getNextTarEntry();
                }
            }

            return ret;
        }
    }

    private void assertFileContents(ProjectGeneratorProperties generatorProperties, Path actualFilePath, String expectedFileName) throws URISyntaxException, IOException {
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

        final String actual = new String(Files.readAllBytes(actualFilePath));
        final String expected = new String(Files.readAllBytes(Paths.get(resource.toURI())), StandardCharsets.UTF_8);

        assertThat(actual).isEqualTo(expected);
    }

    // Helper method to help constuct maps with concise syntax
    private static Map<String, String> map(Object... values) {
        HashMap<String, String> rc = new HashMap<>();
        for (int i = 0; i + 1 < values.length; i += 2) {
            rc.put(values[i].toString(), values[i + 1].toString());
        }
        return rc;
    }
}
