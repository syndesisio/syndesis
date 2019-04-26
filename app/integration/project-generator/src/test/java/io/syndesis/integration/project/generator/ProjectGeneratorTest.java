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
package io.syndesis.integration.project.generator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.Dependency;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.ResourceIdentifier;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.action.StepAction;
import io.syndesis.common.model.action.StepDescriptor;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.extension.Extension;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.model.integration.step.template.TemplateStepLanguage;
import io.syndesis.common.model.openapi.OpenApi;
import io.syndesis.common.util.KeyGenerator;
import io.syndesis.integration.api.IntegrationProjectGenerator;

@SuppressWarnings({ "PMD.ExcessiveImports", "PMD.ExcessiveMethodLength" })
@RunWith(Parameterized.class)
public class ProjectGeneratorTest {
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
    @Rule
    public TestName testName = new TestName();

    private final String basePath;
    private final List<ProjectGeneratorConfiguration.Templates.Resource> additionalResources;

    private final List<Throwable> errors;

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
                    new ProjectGeneratorConfiguration.Templates.Resource("deployment.yml", "src/main/fabric8/deployment.yml"),
                    new ProjectGeneratorConfiguration.Templates.Resource("settings.xml", "configuration/settings.xml")
                )
            }
        });
    }

    public ProjectGeneratorTest(String basePath, List<ProjectGeneratorConfiguration.Templates.Resource> additionalResources) {
        this.basePath = basePath;
        this.additionalResources = additionalResources;
        this.errors = new ArrayList<>();
    }

    // ***************************
    // Tests
    // ***************************

    @Test
    public void testGenerateProject() throws Exception {
        TestResourceManager resourceManager = new TestResourceManager();

        Integration integration = resourceManager.newIntegration(
            new Step.Builder()
                .stepKind(StepKind.endpoint)
                .connection(new Connection.Builder()
                    .id("timer-connection")
                    .connector(TestConstants.TIMER_CONNECTOR)
                    .build())
                .putConfiguredProperty("period", "5000")
                .action(TestConstants.PERIODIC_TIMER_ACTION)
                .build(),
            new Step.Builder()
                .stepKind(StepKind.mapper)
                .putConfiguredProperty("atlasmapping", "{}")
                .build(),
            new Step.Builder()
                .stepKind(StepKind.ruleFilter)
                .putConfiguredProperty("predicate", "AND")
                .putConfiguredProperty("rules", "[{ \"path\": \"in.header.counter\", \"op\": \">\", \"value\": \"10\" }]")
                .build(),
            new Step.Builder()
                .stepKind(StepKind.extension)
                .extension(new Extension.Builder()
                    .id("my-extension-1")
                    .extensionId("my-extension-1")
                    .addDependency(Dependency.maven("org.slf4j:slf4j-api:1.7.11"))
                    .addDependency(Dependency.maven("org.slf4j:slf4j-simple:1.7.11"))
                    .addDependency(Dependency.maven("org.apache.camel:camel-spring-boot-starter:2.10.0"))
                    .build())
                .putConfiguredProperty("key-1", "val-1")
                .putConfiguredProperty("key-2", "val-2")
                .action(new StepAction.Builder()
                    .id("my-extension-1-action-1")
                    .descriptor(new StepDescriptor.Builder()
                        .kind(StepAction.Kind.ENDPOINT)
                        .entrypoint("direct:extension")
                        .build()
                    ).build())
                .build(),
            new Step.Builder()
                .stepKind(StepKind.extension)
                .extension(new Extension.Builder()
                    .id("my-extension-2")
                    .extensionId("my-extension-2")
                    .build())
                .putConfiguredProperty("key-1", "val-1")
                .putConfiguredProperty("key-2", "val-2")
                .action(new StepAction.Builder()
                    .id("my-extension-1-action-1")
                    .descriptor(new StepDescriptor.Builder()
                        .kind(StepAction.Kind.BEAN)
                        .entrypoint("com.example.MyExtension::action")
                        .build()
                    ).build())
                .build(),
            new Step.Builder()
                .stepKind(StepKind.extension)
                .extension(new Extension.Builder()
                    .id("my-extension-3")
                    .extensionId("my-extension-3")
                    .build())
                .putConfiguredProperty("key-1", "val-1")
                .putConfiguredProperty("key-2", "val-2")
                .action(new StepAction.Builder()
                    .id("my-extension-2-action-1")
                    .descriptor(new StepDescriptor.Builder()
                        .kind(StepAction.Kind.STEP)
                        .entrypoint("com.example.MyStep")
                        .build()
                    ).build())
                .build(),
            new Step.Builder()
                .stepKind(StepKind.endpoint)
                .connection(new Connection.Builder()
                    .id("http-connection")
                    .connector(TestConstants.HTTP_CONNECTOR)
                    .build())
                .putConfiguredProperty("httpUri", "http://localhost:8080/hello")
                .putConfiguredProperty("username", "admin")
                .putConfiguredProperty("password", "admin")
                .putConfiguredProperty("token", "mytoken")
                .action(TestConstants.HTTP_GET_ACTION)
                .build()
        );

        ProjectGeneratorConfiguration configuration = new ProjectGeneratorConfiguration();
        configuration.getTemplates().setOverridePath(this.basePath);
        configuration.getTemplates().getAdditionalResources().addAll(this.additionalResources);
        configuration.setSecretMaskingEnabled(true);

        Path runtimeDir = generate(integration, configuration, resourceManager);

        assertFileContents(configuration, runtimeDir.resolve("pom.xml"), "pom.xml");
        assertFileContentsJson(configuration, runtimeDir.resolve("src/main/resources/syndesis/integration/integration.json"), "integration.json");
        assertFileContents(configuration, runtimeDir.resolve("src/main/resources/application.properties"), "application.properties");
        assertFileContents(configuration, runtimeDir.resolve("src/main/resources/loader.properties"), "loader.properties");
        assertFileContents(configuration, runtimeDir.resolve(".s2i/bin/assemble"), "assemble");
        assertFileContents(configuration, runtimeDir.resolve("prometheus-config.yml"), "prometheus-config.yml");

        assertThat(runtimeDir.resolve("extensions/my-extension-1.jar")).exists();
        assertThat(runtimeDir.resolve("extensions/my-extension-2.jar")).exists();
        assertThat(runtimeDir.resolve("extensions/my-extension-3.jar")).exists();
        assertThat(runtimeDir.resolve("src/main/resources/mapping-flow-0-step-1.json")).exists();

        // lets validate configuration when activity tracing is enabled.
        try( Stream<Path> stream = Files.walk(testFolder.getRoot().toPath().resolve("integration-project")) ) {
            stream.sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        }
        configuration.setActivityTracing(true);
        runtimeDir = generate(integration, configuration, resourceManager);
        assertFileContents(configuration, runtimeDir.resolve("src/main/resources/application.properties"), "application-tracing.properties");

        assertThat(errors).isEmpty();
    }

    @Test
    public void testGenerateProjectErrorHandling() throws Exception {
        TestResourceManager resourceManager = new TestResourceManager();

        Integration integration = resourceManager.newIntegration(
                new Step.Builder()
                        .stepKind(StepKind.endpoint)
                        .connection(new Connection.Builder()
                                .id("timer-connection")
                                .connector(TestConstants.TIMER_CONNECTOR)
                                .build())
                        .putConfiguredProperty("period", "5000")
                        .action(TestConstants.PERIODIC_TIMER_ACTION)
                        .build(),
                new Step.Builder()
                        .stepKind(StepKind.log)
                        .build()
        );

        ProjectGeneratorConfiguration configuration = new ProjectGeneratorConfiguration();
        configuration.getTemplates().setOverridePath(this.basePath);
        configuration.getTemplates().getAdditionalResources().addAll(this.additionalResources);
        configuration.getTemplates().getAdditionalResources().add(
                new ProjectGeneratorConfiguration.Templates.Resource("file-that-does-not-exist.yml", "deployment.yml"));
        configuration.setSecretMaskingEnabled(true);

        generate(integration, configuration, resourceManager);
        await().atMost(5000L, TimeUnit.MILLISECONDS).until(() -> !errors.isEmpty());
        assertThat(errors.get(0)).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testGenerateApplicationProperties() throws IOException {

        // ******************
        // NEW STYLE
        // ******************

        final ConnectorAction newAction = new ConnectorAction.Builder()
            .id(KeyGenerator.createKey())
            .descriptor(new ConnectorDescriptor.Builder()
                .connectorId("new")
                .componentScheme("http4")
                .build())
            .build();
        final Connector newConnector = new Connector.Builder()
            .id("new")
            .addAction(newAction)
            .putProperty("username",
                new ConfigurationProperty.Builder()
                    .componentProperty(false)
                    .secret(true)
                    .build())
            .putProperty("password",
                new ConfigurationProperty.Builder()
                    .componentProperty(false)
                    .secret(true)
                    .build())
            .putProperty("token",
                new ConfigurationProperty.Builder()
                    .componentProperty(true)
                    .secret(true)
                    .build())
            .build();

        // ******************
        // Integration
        // ******************

        Step s1 = new Step.Builder()
            .stepKind(StepKind.endpoint)
            .connection(new Connection.Builder()
                .id(KeyGenerator.createKey())
                .connector(newConnector)
                .build())
            .putConfiguredProperty("username", "my-username-2")
            .putConfiguredProperty("password", "my-password-2")
            .putConfiguredProperty("token", "my-token-2")
            .action(newAction)
            .build();

        TestResourceManager resourceManager = new TestResourceManager();
        ProjectGeneratorConfiguration configuration = new ProjectGeneratorConfiguration();
        ProjectGenerator generator = new ProjectGenerator(configuration, resourceManager, TestConstants.MAVEN_PROPERTIES);
        Integration integration = new Integration.Builder()
            .createFrom(resourceManager.newIntegration(s1))
            .putConfiguredProperty("integration", "property")
            .build();
        Properties properties = generator.generateApplicationProperties(integration);

        assertThat(properties).containsOnly(
            entry("integration", "property"),
            entry("flow-0.http4-0.token", "my-token-2"),
            entry("flow-0.http4-0.username", "my-username-2"),
            entry("flow-0.http4-0.password", "my-password-2")
        );
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGenerateApplicationPropertiesOldStyle() throws IOException {

        // ******************
        // OLD STYLE
        // ******************

        final ConnectorAction oldAction = new ConnectorAction.Builder()
            .id(KeyGenerator.createKey())
            .descriptor(new ConnectorDescriptor.Builder()
                .connectorId("old")
                .build())
            .build();
        final Connector oldConnector = new Connector.Builder()
            .id("old")
            .addAction(oldAction)
            .putProperty("username",
                new ConfigurationProperty.Builder()
                    .componentProperty(false)
                    .secret(true)
                    .build())
            .putProperty("password",
                new ConfigurationProperty.Builder()
                    .componentProperty(false)
                    .secret(true)
                    .build())
            .putProperty("token",
                new ConfigurationProperty.Builder()
                    .componentProperty(true)
                    .secret(true)
                    .build())
            .build();

        // ******************
        // Integration
        // ******************

        Step s1 = new Step.Builder()
            .stepKind(StepKind.endpoint)
            .connection(new Connection.Builder()
                .id(KeyGenerator.createKey())
                .connector(oldConnector)
                .build())
            .putConfiguredProperty("username", "my-username-1")
            .putConfiguredProperty("password", "my-password-1")
            .putConfiguredProperty("token", "my-token-1")
            .action(oldAction)
            .build();

        TestResourceManager resourceManager = new TestResourceManager();
        ProjectGeneratorConfiguration configuration = new ProjectGeneratorConfiguration();
        ProjectGenerator generator = new ProjectGenerator(configuration, resourceManager, TestConstants.MAVEN_PROPERTIES);
        Integration integration = new Integration.Builder()
            .createFrom(resourceManager.newIntegration(s1))
            .putConfiguredProperty("integration", "property")
            .build();
        generator.generateApplicationProperties(integration);
    }


    @Test
    public void testGenerateApplicationWithRestDSL() throws Exception {
        TestResourceManager resourceManager = new TestResourceManager();

        // ******************
        // OpenApi
        // ******************

        URL location = ProjectGeneratorTest.class.getResource("/petstore.json");
        byte[] content = Files.readAllBytes(Paths.get(location.toURI()));

        resourceManager.put("petstore", new OpenApi.Builder().document(content).id("petstore").build());


        // ******************
        // Integration
        // ******************

        Step s1 = new Step.Builder()
            .stepKind(StepKind.endpoint)
            .action(new ConnectorAction.Builder()
                .id(KeyGenerator.createKey())
                .descriptor(new ConnectorDescriptor.Builder()
                    .connectorId("new")
                    .componentScheme("direct")
                    .putConfiguredProperty("name", "start")
                    .build())
                .build())
            .connection(new Connection.Builder()
                .connector(
                    new Connector.Builder()
                        .id("api-provider")
                        .addDependency(new Dependency.Builder()
                            .type(Dependency.Type.MAVEN)
                            .id("io.syndesis.connector:connector-api-provider:1.x.x")
                            .build())
                        .build())
                .build())
            .build();
        Step s2 = new Step.Builder()
            .stepKind(StepKind.endpoint)
            .action(new ConnectorAction.Builder()
                .id(KeyGenerator.createKey())
                .descriptor(new ConnectorDescriptor.Builder()
                    .connectorId("new")
                    .componentScheme("log")
                    .putConfiguredProperty("loggerName", "end")
                    .build())
                .build())
            .build();

        Integration integration = new Integration.Builder()
            .id("test-integration")
            .name("Test Integration")
            .description("This is a test integration!")
            .addResource(new ResourceIdentifier.Builder()
                .kind(Kind.OpenApi)
                .id("petstore")
                .build())
            .addFlow(new Flow.Builder()
                .steps(Arrays.asList(s1, s2))
                .build())
            .build();

        ProjectGeneratorConfiguration configuration = new ProjectGeneratorConfiguration();
        configuration.getTemplates().setOverridePath(this.basePath);
        configuration.getTemplates().getAdditionalResources().addAll(this.additionalResources);
        configuration.setSecretMaskingEnabled(true);

        Path runtimeDir = generate(integration, configuration, resourceManager);

        assertThat(runtimeDir.resolve("src/main/java/io/syndesis/example/Application.java")).exists();
        assertThat(runtimeDir.resolve("src/main/java/io/syndesis/example/RestRoute.java")).exists();
        assertThat(runtimeDir.resolve("src/main/java/io/syndesis/example/RestRouteConfiguration.java")).exists();

        assertFileContents(configuration, runtimeDir.resolve("src/main/java/io/syndesis/example/RestRoute.java"), "RestRoute.java");
        assertFileContents(configuration, runtimeDir.resolve("src/main/java/io/syndesis/example/RestRouteConfiguration.java"), "RestRouteConfiguration.java");
        assertThat(errors).isEmpty();
    }

    // *****************************
    // Helpers
    // *****************************

    // ***************************
    // Tests
    // ***************************

    @Test
    public void testDependencyCollection() throws Exception {
        TestResourceManager resourceManager = new TestResourceManager();

        Integration integration = resourceManager.newIntegration(
            new Step.Builder()
                .stepKind(StepKind.endpoint)
                .connection(new Connection.Builder()
                    .id("timer-connection")
                    .connector(TestConstants.TIMER_CONNECTOR)
                    .build())
                .putConfiguredProperty("period", "5000")
                .action(TestConstants.PERIODIC_TIMER_ACTION)
                .build(),
            new Step.Builder()
                .stepKind(StepKind.mapper)
                .putConfiguredProperty("atlasmapping", "{}")
                .build(),
            new Step.Builder()
                .stepKind(StepKind.ruleFilter)
                .putConfiguredProperty("predicate", "AND")
                .putConfiguredProperty("rules", "[{ \"path\": \"in.header.counter\", \"op\": \">\", \"value\": \"10\" }]")
                .addDependency(Dependency.maven("org.myStep:someLib1:1.0"))
                .addDependency(Dependency.maven("org.myStep:someLib2:1.0"))
                .addDependency(Dependency.maven("org.myStep:someLib3:1.0"))
                .build(),
            new Step.Builder()
                .stepKind(StepKind.endpoint)
                .connection(new Connection.Builder()
                    .id("http-connection")
                    .connector(TestConstants.HTTP_CONNECTOR)
                    .build())
                .putConfiguredProperty("httpUri", "http://localhost:8080/hello")
                .putConfiguredProperty("username", "admin")
                .putConfiguredProperty("password", "admin")
                .putConfiguredProperty("token", "mytoken")
                .action(TestConstants.HTTP_GET_ACTION)
                .build()
        );

        Collection<Dependency> dependencies = resourceManager.collectDependencies(integration);
        /*
         * Should return
         * - 3 step dependencies from the rule filter step
         */
        assertEquals(3, dependencies.size());
    }

    @Test
    public void testGenerateTemplateStepProjectDependencies() throws Exception {
        TestResourceManager resourceManager = new TestResourceManager();

        Integration integration = resourceManager.newIntegration(
            new Step.Builder()
                .stepKind(StepKind.endpoint)
                .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                                    .putConfiguredProperty("name", "Test-Connector")
                                    .build())
                        .build())
                .build(),
            new Step.Builder()
                .id("templating")
                .stepKind(StepKind.template)
                .action(new StepAction.Builder()
                        .descriptor(new StepDescriptor.Builder()
                                    .kind(StepAction.Kind.STEP)
                                    .inputDataShape(new DataShape.Builder()
                                                    .kind(DataShapeKinds.JSON_SCHEMA)
                                                    .build())
                                    .outputDataShape(new DataShape.Builder()
                                                     .kind(DataShapeKinds.JSON_SCHEMA)
                                                     .build())
                                    .build())
                        .build())
                .putConfiguredProperty("template", "{{text}}")
                .putConfiguredProperty("language", TemplateStepLanguage.MUSTACHE.toString())
                .build(),
            new Step.Builder()
                .stepKind(StepKind.endpoint)
                .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                                    .putConfiguredProperty("name", "result")
                                    .build())
                        .build())
                .build()
        );

        ProjectGeneratorConfiguration configuration = new ProjectGeneratorConfiguration();
        configuration.getTemplates().setOverridePath(this.basePath);
        configuration.getTemplates().getAdditionalResources().addAll(this.additionalResources);
        configuration.setSecretMaskingEnabled(true);

        Path runtimeDir = generate(integration, configuration, resourceManager);

        assertFileContents(configuration, runtimeDir.resolve("pom.xml"), "pom.xml");
        assertThat(errors).isEmpty();
    }

    private Path generate(Integration integration, ProjectGeneratorConfiguration generatorConfiguration, TestResourceManager resourceManager) throws IOException {
        Path destination = testFolder.newFolder("integration-project").toPath();

        generate(destination, integration, generatorConfiguration, resourceManager);

        return destination;
    }

    private void assertFileContents(ProjectGeneratorConfiguration generatorConfiguration, Path actualFilePath, String expectedFileName) throws URISyntaxException, IOException {
        URL resource = null;
        String overridePath = generatorConfiguration.getTemplates().getOverridePath();
        String methodName = testName.getMethodName();

        int index = methodName.indexOf('[');
        if (index != -1) {
            methodName = methodName.substring(0, index);
        }

        if (!StringUtils.isEmpty(overridePath)) {
            resource = ProjectGeneratorTest.class.getResource(methodName + "/" + overridePath + "/" + expectedFileName);
        }
        if (resource == null) {
            resource = ProjectGeneratorTest.class.getResource(methodName + "/" + expectedFileName);
        }
        if (resource == null) {
            throw new IllegalArgumentException("Unable to find the required resource (" + expectedFileName + ")");
        }

        final String actual = new String(Files.readAllBytes(actualFilePath), StandardCharsets.UTF_8).trim();
        final String expected = new String(Files.readAllBytes(Paths.get(resource.toURI())), StandardCharsets.UTF_8).trim();

        assertThat(actual).isEqualTo(expected);
    }

    private void assertFileContentsJson(ProjectGeneratorConfiguration generatorConfiguration, Path actualFilePath, String expectedFileName) throws URISyntaxException, IOException, JSONException {
        URL resource = null;
        String overridePath = generatorConfiguration.getTemplates().getOverridePath();
        String methodName = testName.getMethodName();

        int index = methodName.indexOf('[');
        if (index != -1) {
            methodName = methodName.substring(0, index);
        }

        if (!StringUtils.isEmpty(overridePath)) {
            resource = ProjectGeneratorTest.class.getResource(methodName + "/" + overridePath + "/" + expectedFileName);
        }
        if (resource == null) {
            resource = ProjectGeneratorTest.class.getResource(methodName + "/" + expectedFileName);
        }
        if (resource == null) {
            throw new IllegalArgumentException("Unable to find te required resource (" + expectedFileName + ")");
        }

        final String actual = new String(Files.readAllBytes(actualFilePath), StandardCharsets.UTF_8).trim();
        final String expected = new String(Files.readAllBytes(Paths.get(resource.toURI())), StandardCharsets.UTF_8).trim();

        JSONAssert.assertEquals(expected, actual, JSONCompareMode.STRICT);
    }

    // ***************************
    // Tests
    // ***************************

    private void generate(Path destination, Integration integration, ProjectGeneratorConfiguration generatorConfiguration, TestResourceManager resourceManager) throws IOException {
        final IntegrationProjectGenerator generator = new ProjectGenerator(generatorConfiguration, resourceManager,TestConstants.MAVEN_PROPERTIES);

        try (InputStream is = generator.generate(integration, errors::add)) {
            try (TarArchiveInputStream tis = new TarArchiveInputStream(is)) {
                TarArchiveEntry tarEntry = tis.getNextTarEntry();

                // tarIn is a TarArchiveInputStream
                while (tarEntry != null) {
                    // create a file with the same name as the tarEntry
                    File destPath = new File(destination.toFile(), tarEntry.getName());
                    if (tarEntry.isDirectory()) {
                        destPath.mkdirs();
                    } else {
                        destPath.getParentFile().mkdirs();
                        destPath.createNewFile();

                        try(BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(destPath))) {
                            IOUtils.copy(tis, bout);
                        }
                    }
                    tarEntry = tis.getNextTarEntry();
                }
            }
        }
    }
}
