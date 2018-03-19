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
import java.util.List;
import java.util.Properties;

import io.syndesis.common.model.Dependency;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.action.StepAction;
import io.syndesis.common.model.action.StepDescriptor;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.extension.Extension;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.util.KeyGenerator;
import io.syndesis.common.util.MavenProperties;
import io.syndesis.integration.api.IntegrationProjectGenerator;
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

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({ "PMD.ExcessiveImports", "PMD.ExcessiveMethodLength" })
@RunWith(Parameterized.class)
public class ProjectGeneratorTest {
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
    @Rule
    public TestName testName = new TestName();

    private final String basePath;
    private final List<ProjectGeneratorConfiguration.Templates.Resource> additionalResources;

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
        assertThat(runtimeDir.resolve("src/main/resources/mapping-step-2.json")).exists();
    }


    @Test
    public void testGenerateApplicationProperties() throws IOException {

        // ******************
        // OLD STYLE
        // ******************

        final ConnectorAction oldAction = new ConnectorAction.Builder()
            .id(KeyGenerator.createKey())
            .descriptor(new ConnectorDescriptor.Builder()
                .connectorId("old")
                .camelConnectorPrefix("old")
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
        Step s2 = new Step.Builder()
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
        ProjectGenerator generator = new ProjectGenerator(configuration, resourceManager, getMavenProperties());
        Integration integration = resourceManager.newIntegration(s1, s2);
        Properties properties = generator.generateApplicationProperties(integration);

        assertThat(properties.size()).isEqualTo(6);
        assertThat(properties.getProperty("old.configurations.old-1.token")).isEqualTo("my-token-1");
        assertThat(properties.getProperty("old-1.username")).isEqualTo("my-username-1");
        assertThat(properties.getProperty("old-1.password")).isEqualTo("my-password-1");
        assertThat(properties.getProperty("http4-2.token")).isEqualTo("my-token-2");
        assertThat(properties.getProperty("http4-2.username")).isEqualTo("my-username-2");
        assertThat(properties.getProperty("http4-2.password")).isEqualTo("my-password-2");
    }
    // *****************************
    // Helpers
    // *****************************

    private Path generate(Integration integration, ProjectGeneratorConfiguration generatorConfiguration, TestResourceManager resourceManager) throws IOException {
        final IntegrationProjectGenerator generator = new ProjectGenerator(generatorConfiguration, resourceManager, getMavenProperties());

        try (InputStream is = generator.generate(integration)) {
            Path ret = testFolder.newFolder("integration-project").toPath();

            try (TarArchiveInputStream tis = new TarArchiveInputStream(is)) {
                TarArchiveEntry tarEntry = tis.getNextTarEntry();

                // tarIn is a TarArchiveInputStream
                while (tarEntry != null) {
                    // create a file with the same name as the tarEntry
                    File destPath = new File(ret.toFile(), tarEntry.getName());
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

            return ret;
        }
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
            throw new IllegalArgumentException("Unable to find te required resource (" + expectedFileName + ")");
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

    protected MavenProperties getMavenProperties() {
        MavenProperties mavenProperties = new MavenProperties();
        mavenProperties.addRepository("maven.central", "https://repo1.maven.org/maven2");
        mavenProperties.addRepository("redhat.ga", "https://maven.repository.redhat.com/ga");
        mavenProperties.addRepository("jboss.ea", "https://repository.jboss.org/nexus/content/groups/ea");
        return mavenProperties;
    }
}
