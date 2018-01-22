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
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import io.syndesis.core.Json;
import io.syndesis.core.KeyGenerator;
import io.syndesis.core.MavenProperties;
import io.syndesis.dao.extension.ExtensionDataManager;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.integration.support.Strings;
import io.syndesis.model.Dependency;
import io.syndesis.model.Split;
import io.syndesis.model.action.ConnectorAction;
import io.syndesis.model.action.ConnectorDescriptor;
import io.syndesis.model.action.ExtensionAction;
import io.syndesis.model.action.ExtensionDescriptor;
import io.syndesis.model.connection.ConfigurationProperty;
import io.syndesis.model.connection.Connection;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.extension.Extension;
import io.syndesis.model.filter.ExpressionFilterStep;
import io.syndesis.model.filter.FilterPredicate;
import io.syndesis.model.filter.RuleFilterStep;
import io.syndesis.model.integration.Integration;
import io.syndesis.model.integration.IntegrationDeployment;
import io.syndesis.model.integration.IntegrationDeploymentSpec;
import io.syndesis.model.integration.SimpleStep;
import io.syndesis.model.integration.Step;
import io.syndesis.project.converter.ProjectGeneratorProperties.Templates;
import io.syndesis.project.converter.visitor.ConnectorStepVisitor;
import io.syndesis.project.converter.visitor.DataMapperStepVisitor;
import io.syndesis.project.converter.visitor.ExpressionFilterStepVisitor;
import io.syndesis.project.converter.visitor.ExtensionStepVisitor;
import io.syndesis.project.converter.visitor.RuleFilterStepVisitor;
import io.syndesis.project.converter.visitor.StepVisitorFactoryRegistry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class DefaultProjectGeneratorTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultProjectGeneratorTest.class);
    private static final String CONNECTORS_VERSION = ResourceBundle.getBundle("test").getString("connectors.version");
    private static final String CAMEL_VERSION = ResourceBundle.getBundle("test").getString("camel.version");

    private static final MavenProperties MAVEN_PROPERTIES = new MavenProperties()
        .addRepository("maven.central", "https://repo1.maven.org/maven2")
        .addRepository("redhat.ga", "https://maven.repository.redhat.com/ga")
        .addRepository("jboss.ea", "https://repository.jboss.org/nexus/content/groups/ea");

    private static final ConnectorAction PERIODIC_TIMER_ACTION;
    private static final Connector TIMER_CONNECTOR;
    private static final ConnectorAction HTTP_GET_ACTION;
    private static final ConnectorAction HTTP_POST_ACTION;
    private static final Connector HTTP_CONNECTOR;
    private static final ConnectorAction TWITTER_MENTION_ACTION;
    private static final Connector TWITTER_CONNECTOR;

    private final StepVisitorFactoryRegistry registry;
    private final String basePath;
    private final List<Templates.Resource> additionalResources;
    private final ConcurrentMap<String , Object> resources;

    static {
        PERIODIC_TIMER_ACTION = new ConnectorAction.Builder()
            .id(KeyGenerator.createKey())
            .descriptor(new ConnectorDescriptor.Builder()
                .connectorId("timer")
                .camelConnectorPrefix("periodic-timer-connector")
                .camelConnectorGAV("io.syndesis:timer-connector:" + CONNECTORS_VERSION)
                .build())
            .build();

        TIMER_CONNECTOR = new Connector.Builder()
            .id("timer")
            .putProperty(
                "period",
                new ConfigurationProperty.Builder()
                    .kind("property")
                    .secret(false)
                    .componentProperty(false)
                    .build())
            .addAction(PERIODIC_TIMER_ACTION)
            .build();


        HTTP_GET_ACTION = new ConnectorAction.Builder()
            .id(KeyGenerator.createKey())
            .descriptor(new ConnectorDescriptor.Builder()
                .connectorId("http")
                .camelConnectorPrefix("http-get-connector")
                .camelConnectorGAV("io.syndesis:http-get-connector:" + CONNECTORS_VERSION)
                .build())
            .build();


        HTTP_POST_ACTION = new ConnectorAction.Builder()
            .id(KeyGenerator.createKey())
            .descriptor(new ConnectorDescriptor.Builder()
                .connectorId("http")
                .camelConnectorPrefix("http-post-connector")
                .camelConnectorGAV("io.syndesis:http-post-connector:" + CONNECTORS_VERSION)
                .build())
            .build();

        HTTP_CONNECTOR = new Connector.Builder()
            .id("http")
            .putProperty(
                "httpUri",
                new ConfigurationProperty.Builder()
                    .kind("property")
                    .secret(false)
                    .componentProperty(false)
                    .build())
            .putProperty(
                "username",
                new ConfigurationProperty.Builder()
                    .kind("property")
                    .secret(true)
                    .componentProperty(false)
                    .build())
            .putProperty(
                "password",
                new ConfigurationProperty.Builder()
                    .kind("property")
                    .secret(true)
                    .build())
            .putProperty(
                "token",
                new ConfigurationProperty.Builder()
                    .kind("property")
                    .secret(true)
                    .componentProperty(false)
                    .build())
            .addAction(HTTP_GET_ACTION)
            .addAction(HTTP_POST_ACTION)
            .build();

        TWITTER_MENTION_ACTION = new ConnectorAction.Builder()
            .id("twitter-mention-action")
            .descriptor(new ConnectorDescriptor.Builder()
                .componentScheme("twitter-timeline")
                .putConfiguredProperty("timelineType", "MENTIONS")
                .putConfiguredProperty("delay", "30000")
                .build())
            .build();

        TWITTER_CONNECTOR = new Connector.Builder()
            .id("twitter")
            .putProperty(
                "accessToken",
                new ConfigurationProperty.Builder()
                    .kind("property")
                    .secret(true)
                    .componentProperty(true)
                    .build())
            .putProperty(
                "accessTokenSecret",
                new ConfigurationProperty.Builder()
                    .kind("property")
                    .secret(true)
                    .build())
            .putProperty(
                "consumerKey",
                new ConfigurationProperty.Builder()
                    .kind("property")
                    .secret(true)
                    .build())
            .putProperty(
                "consumerSecret",
                new ConfigurationProperty.Builder()
                    .kind("property")
                    .secret(true)
                    .build())
            .componentScheme("twitter")
            .addDependency(Dependency.maven("io.syndesis:camel-component-proxy:" + CONNECTORS_VERSION))
            .addDependency(Dependency.maven("org.apache.camel:camel-twitter:" + CAMEL_VERSION))
            .addAction(TWITTER_MENTION_ACTION)
            .build();
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

    public DefaultProjectGeneratorTest(String basePath, List<Templates.Resource> additionalResources) {
        this.basePath = basePath;
        this.additionalResources = additionalResources;
        this.resources = new ConcurrentHashMap<>();
        this.registry = new StepVisitorFactoryRegistry(
            new DataMapperStepVisitor.Factory(),
            new ConnectorStepVisitor.ConnectorFactory(),
            new ConnectorStepVisitor.EndpointFactory(),
            new RuleFilterStepVisitor.Factory(),
            new ExpressionFilterStepVisitor.Factory(),
            new ExtensionStepVisitor.Factory()
        );
    }

    // ************************************************
    // Tests
    // ************************************************

    @Test
    public void testConvert() throws Exception {
        Step step1 = new SimpleStep.Builder()
            .stepKind("endpoint")
            .connection(new Connection.Builder()
                .id(KeyGenerator.createKey())
                .connector(TIMER_CONNECTOR)
                .build())
            .putConfiguredProperty("period", "5000")
            .action(PERIODIC_TIMER_ACTION)
            .build();

        Step step2 = new SimpleStep.Builder()
            .stepKind("endpoint")
            .connection(new Connection.Builder()
                .id(KeyGenerator.createKey())
                .connector(HTTP_CONNECTOR)
                .build())
            .putConfiguredProperty("httpUri", "http://localhost:8080/hello")
            .action(HTTP_GET_ACTION)
            .build();

        Step step3 = new SimpleStep.Builder()
            .stepKind("log")
            .putConfiguredProperty("message", "Hello World! ${body}")
            .build();

        Step step4 = new SimpleStep.Builder()
            .stepKind("endpoint")
            .connection(new Connection.Builder()
                .id(KeyGenerator.createKey())
                .connector(HTTP_CONNECTOR)
                .build())
            .putConfiguredProperty("httpUri", "http://localhost:8080/bye")
            .action(HTTP_POST_ACTION)
            .build();

        IntegrationDeployment integrationDeployment = newIntegrationDeployment(step1, step2, step3, step4);
        ProjectGeneratorProperties generatorProperties = new ProjectGeneratorProperties(new MavenProperties());
        generatorProperties.getTemplates().setOverridePath(this.basePath);
        generatorProperties.getTemplates().getAdditionalResources().addAll(this.additionalResources);

        Path runtimeDir = generate(integrationDeployment, generatorProperties);

        assertFileContents(generatorProperties, runtimeDir.resolve("src/main/java/io/syndesis/example/Application.java"), "test-Application.java");
        assertFileContents(generatorProperties, runtimeDir.resolve("src/main/resources/application.properties"), "test-application.properties");
        assertFileContents(generatorProperties, runtimeDir.resolve("src/main/resources/syndesis.yml"), "test-convert-syndesis.yml");
        assertFileContents(generatorProperties, runtimeDir.resolve("pom.xml"), "test-convert-pom.xml");

        for (Templates.Resource additionalResource : generatorProperties.getTemplates().getAdditionalResources()) {
            assertFileContents(generatorProperties, runtimeDir.resolve(additionalResource.getDestination()), "test-" + additionalResource.getSource());
        }
    }

    @Test
    public void testConverterWithSecrets() throws Exception {
        Step step1 = new SimpleStep.Builder()
            .stepKind("endpoint")
            .connection(new Connection.Builder()
                .id(KeyGenerator.createKey())
                .connector(TIMER_CONNECTOR)
                .build())
            .putConfiguredProperty("period", "5000")
            .action(PERIODIC_TIMER_ACTION)
            .build();

        Step step2 = new SimpleStep.Builder()
            .stepKind("endpoint")
            .connection(new Connection.Builder()
                .id(KeyGenerator.createKey())
                .connector(HTTP_CONNECTOR)
                .build())
            .putConfiguredProperty("httpUri", "http://localhost:8080/hello")
            .putConfiguredProperty("username", "admin")
            .putConfiguredProperty("password", "admin")
            .putConfiguredProperty("token", "mytoken")
            .action(HTTP_GET_ACTION)
            .build();

        IntegrationDeployment integrationDeployment = newIntegrationDeployment(step1, step2);

        ProjectGeneratorProperties generatorProperties = new ProjectGeneratorProperties(MAVEN_PROPERTIES);
        generatorProperties.getTemplates().setOverridePath(this.basePath);
        generatorProperties.getTemplates().getAdditionalResources().addAll(this.additionalResources);
        generatorProperties.setSecretMaskingEnabled(true);

        Path runtimeDir = generate(integrationDeployment,  generatorProperties);

        assertFileContents(generatorProperties, runtimeDir.resolve("src/main/resources/application.properties"), "test-application.properties");
        assertFileContents(generatorProperties, runtimeDir.resolve("src/main/resources/syndesis.yml"), "test-convert-with-secrets-syndesis.yml");
    }

    @Test
    public void testConverterWithSecretsAndMultipleConnectorOfSameType() throws Exception {
        Step step1 = new SimpleStep.Builder()
            .stepKind("endpoint")
            .connection(new Connection.Builder()
                .id(KeyGenerator.createKey())
                .connector(TIMER_CONNECTOR)
                .build())
            .putConfiguredProperty("period", "5000")
            .action(PERIODIC_TIMER_ACTION)
            .build();

        Step step2 = new SimpleStep.Builder()
            .stepKind("endpoint")
            .connection(new Connection.Builder()
                .id(KeyGenerator.createKey())
                .connector(HTTP_CONNECTOR)
                .build())
            .putConfiguredProperty("httpUri", "http://localhost:8080/hello")
            .putConfiguredProperty("username", "admin")
            .putConfiguredProperty("password", "admin")
            .putConfiguredProperty("token", "mytoken")
            .action(HTTP_GET_ACTION)
            .build();

        Step step3 = new SimpleStep.Builder()
            .stepKind("endpoint")
            .connection(new Connection.Builder()
                .id(KeyGenerator.createKey())
                .connector(HTTP_CONNECTOR)
                .build())
            .putConfiguredProperty("httpUri", "http://localhost:8080/bye")
            .putConfiguredProperty("username", "admin")
            .putConfiguredProperty("password", "admin")
            .putConfiguredProperty("token", "mytoken")
            .action(HTTP_GET_ACTION)
            .build();

        IntegrationDeployment integrationDeployment = newIntegrationDeployment(step1, step2, step3);

        ProjectGeneratorProperties generatorProperties = new ProjectGeneratorProperties(MAVEN_PROPERTIES);
        generatorProperties.getTemplates().setOverridePath(this.basePath);
        generatorProperties.getTemplates().getAdditionalResources().addAll(this.additionalResources);
        generatorProperties.setSecretMaskingEnabled( true);

        Path runtimeDir = generate(integrationDeployment, generatorProperties);

        assertFileContents(generatorProperties, runtimeDir.resolve("src/main/resources/application.properties"), "test-application.properties");
        assertFileContents(generatorProperties, runtimeDir.resolve("src/main/resources/syndesis.yml"), "test-convert-with-secrets-and-multiple-connector-of-same-type-syndesis.yml");
    }

    @Ignore("test-integration.json is outdated")
    @Test
    public void testConvertFromJson() throws Exception {
        JsonNode integrationDeploymentJson = new ObjectMapper().readTree(this.getClass().getResourceAsStream("test-integration-deployment.json"));
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new Jdk8Module());
        IntegrationDeployment integrationDeployment = objectMapper.readValue(integrationDeploymentJson.get("data").toString(), IntegrationDeployment.class);


        ProjectGeneratorProperties generatorProperties = new ProjectGeneratorProperties(MAVEN_PROPERTIES);
        generatorProperties.getTemplates().setOverridePath(this.basePath);
        generatorProperties.getTemplates().getAdditionalResources().addAll(this.additionalResources);

        Path runtimePath = generate(integrationDeployment, generatorProperties);

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
                .id(KeyGenerator.createKey())
                .connector(TIMER_CONNECTOR)
                .build())
            .putConfiguredProperty("period", "5000")
            .action(PERIODIC_TIMER_ACTION)
            .build();

        Step step2 = new SimpleStep.Builder()
            .stepKind("mapper")
            .putConfiguredProperty("atlasmapping", "{}")
            .build();

        Step step3 = new SimpleStep.Builder()
            .stepKind("endpoint")
            .connection(new Connection.Builder()
                .id(KeyGenerator.createKey())
                .connector(HTTP_CONNECTOR)
                .build())
            .putConfiguredProperty("httpUri", "http://localhost:8080/bye")
            .action(HTTP_POST_ACTION)
            .build();


        IntegrationDeployment integrationDeployment = newIntegrationDeployment(step1, step2, step3);
        ProjectGeneratorProperties generatorProperties = new ProjectGeneratorProperties(MAVEN_PROPERTIES);
        generatorProperties.getTemplates().setOverridePath(this.basePath);
        generatorProperties.getTemplates().getAdditionalResources().addAll(this.additionalResources);

        Path runtimePath = generate(integrationDeployment, generatorProperties);
        runtimePath.toFile().deleteOnExit();

        assertFileContents(generatorProperties, runtimePath.resolve("src/main/resources/syndesis.yml"), "test-mapper-syndesis.yml");
        assertThat(new String(Files.readAllBytes(runtimePath.resolve("src/main/resources/mapping-step-2.json")), StandardCharsets.UTF_8)).isEqualTo("{}");
    }

    @Test
    public void testWithFilter() throws Exception {
        Step step1 = new SimpleStep.Builder()
            .stepKind("endpoint")
            .connection(new Connection.Builder()
                .id(KeyGenerator.createKey())
                .connector(TIMER_CONNECTOR)
                .build())
            .putConfiguredProperty("period", "5000")
            .action(PERIODIC_TIMER_ACTION)
            .build();

        Step step2 = new RuleFilterStep.Builder()
            .putConfiguredProperty("predicate", FilterPredicate.AND.toString())
            .putConfiguredProperty("rules", "[{ \"path\": \"in.header.counter\", \"op\": \">\", \"value\": \"10\" }]")
            .build();

        Step step3 = new SimpleStep.Builder()
            .stepKind("endpoint")
            .connection(new Connection.Builder()
                .id(KeyGenerator.createKey())
                .connector(HTTP_CONNECTOR)
                .build())
            .putConfiguredProperty("httpUri", "http://localhost:8080/bye")
            .action(HTTP_POST_ACTION)
            .build();

        Step step4 = new ExpressionFilterStep.Builder()
            .putConfiguredProperty("filter", "${body.germanSecondLeagueChampion} equals 'FCN'")
            .build();

        IntegrationDeployment integrationDeployment = newIntegrationDeployment(step1, step2, step3, step4);
        ProjectGeneratorProperties generatorProperties = new ProjectGeneratorProperties(MAVEN_PROPERTIES);
        generatorProperties.getTemplates().setOverridePath(this.basePath);
        generatorProperties.getTemplates().getAdditionalResources().addAll(this.additionalResources);

        Path runtimeDir = generate(integrationDeployment, generatorProperties);

        assertFileContents(generatorProperties, runtimeDir.resolve("src/main/resources/syndesis.yml"), "test-filter-syndesis.yml");
    }

    @SuppressWarnings("PMD.ExcessiveMethodLength")
    @Test
    public void testWithExtension() throws Exception {
        Step step1 = new SimpleStep.Builder()
            .stepKind("endpoint")
            .connection(new Connection.Builder()
                .id(KeyGenerator.createKey())
                .connector(TIMER_CONNECTOR)
                .build())
            .putConfiguredProperty("period", "5000")
            .action(PERIODIC_TIMER_ACTION)
            .build();

        Step step2 = new SimpleStep.Builder()
            .stepKind("extension")
            .extension(new Extension.Builder()
                .id(KeyGenerator.createKey())
                .extensionId("my-extension-1")
                .addDependency(Dependency.maven("org.slf4j:slf4j-api:1.7.11"))
                .addDependency(Dependency.maven("org.slf4j:slf4j-simple:1.7.11"))
                .build())
            .putConfiguredProperty("key-1", "val-1")
            .putConfiguredProperty("key-2", "val-2")
            .action(new ExtensionAction.Builder()
                .id(KeyGenerator.createKey())
                .descriptor(new ExtensionDescriptor.Builder()
                    .kind(ExtensionAction.Kind.ENDPOINT)
                    .entrypoint("direct:extension")
                    .build()
                ).build())
            .build();

        Step step3 = new SimpleStep.Builder()
            .stepKind("extension")
            .extension(new Extension.Builder()
                .id(KeyGenerator.createKey())
                .extensionId("my-extension-2")
                .build())
            .putConfiguredProperty("key-1", "val-1")
            .putConfiguredProperty("key-2", "val-2")
            .action(new ExtensionAction.Builder()
                .id(KeyGenerator.createKey())
                .descriptor(new ExtensionDescriptor.Builder()
                    .kind(ExtensionAction.Kind.BEAN)
                    .entrypoint("com.example.MyExtension::action")
                    .build()
                ).build())
            .build();

        Step step4 = new SimpleStep.Builder()
            .stepKind("extension")
            .extension(new Extension.Builder()
                .id(KeyGenerator.createKey())
                .extensionId("my-extension-3")
                .build())
            .putConfiguredProperty("key-1", "val-1")
            .putConfiguredProperty("key-2", "val-2")
            .action(new ExtensionAction.Builder()
                .id(KeyGenerator.createKey())
                .descriptor(new ExtensionDescriptor.Builder()
                    .kind(ExtensionAction.Kind.STEP)
                    .entrypoint("com.example.MyStep")
                    .build()
                ).build())
            .build();

        Step step5 = new SimpleStep.Builder()
            .stepKind("endpoint")
            .connection(new Connection.Builder()
                .id(KeyGenerator.createKey())
                .connector(HTTP_CONNECTOR)
                .build())
            .putConfiguredProperty("httpUri", "http://localhost:8080/bye")
            .putConfiguredProperty("username", "admin")
            .putConfiguredProperty("password", "admin")
            .putConfiguredProperty("token", "mytoken")
            .action(HTTP_GET_ACTION)
            .build();

        IntegrationDeployment integrationDeployment = newIntegrationDeployment(step1, step2, step3, step4, step5);
        ProjectGeneratorProperties generatorProperties = new ProjectGeneratorProperties(MAVEN_PROPERTIES);
        generatorProperties.getTemplates().setOverridePath(this.basePath);
        generatorProperties.getTemplates().getAdditionalResources().addAll(this.additionalResources);
        generatorProperties.setSecretMaskingEnabled(true);

        Path runtimeDir = generate(integrationDeployment, generatorProperties);

        assertFileContents(generatorProperties, runtimeDir.resolve("src/main/resources/syndesis.yml"), "test-syndesis-extension.yml");
        assertFileContents(generatorProperties, runtimeDir.resolve("src/main/resources/loader.properties"), "test-loader.properties");
        assertFileContents(generatorProperties, runtimeDir.resolve("pom.xml"), "test-pom-extension.xml");
        assertFileContents(generatorProperties, runtimeDir.resolve(".s2i/bin/assemble"), "test-s2i-assemble");

        assertThat(runtimeDir.resolve("extensions/my-extension-1.jar")).exists();
        assertThat(runtimeDir.resolve("extensions/my-extension-2.jar")).exists();
        assertThat(runtimeDir.resolve("extensions/my-extension-3.jar")).exists();
    }

    @Test
    public void testConnectorConvert() throws Exception {
        Step step1 = new SimpleStep.Builder()
            .stepKind("endpoint")
            .connection(new Connection.Builder()
                .id(KeyGenerator.createKey())
                .connector(TIMER_CONNECTOR)
                .build())
            .putConfiguredProperty("period", "5000")
            .action(PERIODIC_TIMER_ACTION)
            .build();
        Step step2 = new SimpleStep.Builder()
            .stepKind("endpoint")
            .connection(new Connection.Builder()
                .id(KeyGenerator.createKey())
                .connector(TWITTER_CONNECTOR)
                .build())
            .action(TWITTER_MENTION_ACTION)
            .putConfiguredProperty("accessToken", "at")
            .putConfiguredProperty("accessTokenSecret", "ats")
            .putConfiguredProperty("consumerKey", "ck")
            .putConfiguredProperty("consumerSecret", "cs")
            .build();

        testConnectorConvertIntegration(newIntegrationDeployment(step1, step2));
    }

    @Test
    public void testConnectorConvertFromIntegration() throws Exception {
        try (InputStream irs = getClass().getResourceAsStream("test-connector-integration-deployment.json")) {
            IntegrationDeployment integrationDeployment = Json.mapper().readValue(irs, IntegrationDeployment.class);

            ProjectGeneratorProperties generatorProperties = new ProjectGeneratorProperties(new MavenProperties());
            generatorProperties.setSecretMaskingEnabled(true);
            generatorProperties.getTemplates().setOverridePath(this.basePath);
            generatorProperties.getTemplates().getAdditionalResources().addAll(this.additionalResources);

            Path runtimeDir = generate(integrationDeployment, generatorProperties);

            assertFileContents(generatorProperties, runtimeDir.resolve("src/main/java/io/syndesis/example/Application.java"), "test-Application.java");
            assertFileContents(generatorProperties, runtimeDir.resolve("src/main/resources/application.properties"), "test-application.properties");
            assertFileContents(generatorProperties, runtimeDir.resolve("src/main/resources/syndesis.yml"), "test-connector-convert-syndesis.yml");
            assertFileContents(generatorProperties, runtimeDir.resolve("pom.xml"), "test-connector-convert-pom.xml");

            for (Templates.Resource additionalResource : generatorProperties.getTemplates().getAdditionalResources()) {
                assertFileContents(generatorProperties, runtimeDir.resolve(additionalResource.getDestination()), "test-" + additionalResource.getSource());
            }
        }
    }

    @Test
    public void testConnectorConvertWithSplitter() throws Exception {
        Step step1 = new SimpleStep.Builder()
            .stepKind("endpoint")
            .connection(new Connection.Builder()
                .id(KeyGenerator.createKey())
                .connector(TIMER_CONNECTOR)
                .build())
            .putConfiguredProperty("period", "5000")
            .action(PERIODIC_TIMER_ACTION)
            .build();
        Step step2 = new SimpleStep.Builder()
            .stepKind("endpoint")
            .connection(new Connection.Builder()
                .id(KeyGenerator.createKey())
                .connector(TWITTER_CONNECTOR)
                .build())
            .action(new ConnectorAction.Builder()
                .id("twitter-mention-action")
                .descriptor(new ConnectorDescriptor.Builder()
                    .componentScheme("twitter-timeline")
                    .putConfiguredProperty("timelineType", "MENTIONS")
                    .putConfiguredProperty("delay", "30000")
                    .split(new Split.Builder()
                        .language("tokenize")
                        .expression(",")
                        .build())
                    .build())
                .build())
            .putConfiguredProperty("accessToken", "at")
            .putConfiguredProperty("accessTokenSecret", "ats")
            .putConfiguredProperty("consumerKey", "ck")
            .putConfiguredProperty("consumerSecret", "cs")
            .build();

        IntegrationDeployment integrationDeployment = newIntegrationDeployment(step1, step2);
        ProjectGeneratorProperties generatorProperties = new ProjectGeneratorProperties(new MavenProperties());
        generatorProperties.setSecretMaskingEnabled(true);
        generatorProperties.getTemplates().setOverridePath(this.basePath);
        generatorProperties.getTemplates().getAdditionalResources().addAll(this.additionalResources);

        Path runtimeDir = generate(integrationDeployment, generatorProperties);

        assertFileContents(generatorProperties, runtimeDir.resolve("src/main/java/io/syndesis/example/Application.java"), "test-Application.java");
        assertFileContents(generatorProperties, runtimeDir.resolve("src/main/resources/application.properties"), "test-application.properties");
        assertFileContents(generatorProperties, runtimeDir.resolve("src/main/resources/syndesis.yml"), "test-connector-convert-with-splitter-syndesis.yml");
        assertFileContents(generatorProperties, runtimeDir.resolve("pom.xml"), "test-connector-convert-pom.xml");

        for (Templates.Resource additionalResource : generatorProperties.getTemplates().getAdditionalResources()) {
            assertFileContents(generatorProperties, runtimeDir.resolve(additionalResource.getDestination()), "test-" + additionalResource.getSource());
        }
    }

    @Test
    public void testConnectorConvertWithSplitterFromIntegration() throws Exception {
        try (
             InputStream irs = getClass().getResourceAsStream("test-connector-with-splitter-integration-deployment.json")) {
            testConnectorConvertIntegration(Json.mapper().readValue(irs, IntegrationDeployment.class));
        }
    }

    @Test
    public void testConnectorConvertWithDefaultSplitter() throws Exception {
        Step step1 = new SimpleStep.Builder()
            .stepKind("endpoint")
            .connection(new Connection.Builder()
                .id(KeyGenerator.createKey())
                .connector(TIMER_CONNECTOR)
                .build())
            .putConfiguredProperty("period", "5000")
            .action(PERIODIC_TIMER_ACTION)
            .build();
        Step step2 = new SimpleStep.Builder()
            .stepKind("endpoint")
            .connection(new Connection.Builder()
                .id(KeyGenerator.createKey())
                .connector(TWITTER_CONNECTOR)
                .build())
            .action(new ConnectorAction.Builder()
                .id("twitter-mention-action")
                .descriptor(new ConnectorDescriptor.Builder()
                    .componentScheme("twitter-timeline")
                    .putConfiguredProperty("timelineType", "MENTIONS")
                    .putConfiguredProperty("delay", "30000")
                    .split(new Split.Builder().build())
                    .build())
                .build())
            .putConfiguredProperty("accessToken", "at")
            .putConfiguredProperty("accessTokenSecret", "ats")
            .putConfiguredProperty("consumerKey", "ck")
            .putConfiguredProperty("consumerSecret", "cs")
            .build();

        IntegrationDeployment integrationDeployment = newIntegrationDeployment(step1, step2);

        ProjectGeneratorProperties generatorProperties = new ProjectGeneratorProperties(new MavenProperties());
        generatorProperties.setSecretMaskingEnabled(true);
        generatorProperties.getTemplates().setOverridePath(this.basePath);
        generatorProperties.getTemplates().getAdditionalResources().addAll(this.additionalResources);

        Path runtimeDir = generate(integrationDeployment, generatorProperties);

        assertFileContents(generatorProperties, runtimeDir.resolve("src/main/java/io/syndesis/example/Application.java"), "test-Application.java");
        assertFileContents(generatorProperties, runtimeDir.resolve("src/main/resources/application.properties"), "test-application.properties");
        assertFileContents(generatorProperties, runtimeDir.resolve("src/main/resources/syndesis.yml"), "test-connector-convert-with-default-splitter-syndesis.yml");
        assertFileContents(generatorProperties, runtimeDir.resolve("pom.xml"), "test-connector-convert-pom.xml");

        for (Templates.Resource additionalResource : generatorProperties.getTemplates().getAdditionalResources()) {
            assertFileContents(generatorProperties, runtimeDir.resolve(additionalResource.getDestination()), "test-" + additionalResource.getSource());
        }
    }

    @Test
    public void testConnectorConvertWithDefaultSplitterFromIntegration() throws Exception {
        try (InputStream irs = getClass().getResourceAsStream("test-connector-with-default-splitter-integration-deployment.json")) {
            testConnectorConvertIntegration(Json.mapper().readValue(irs, IntegrationDeployment.class));
        }
    }

    private void testConnectorConvertIntegration(IntegrationDeployment integrationDeployment) throws Exception {
        ProjectGeneratorProperties generatorProperties = new ProjectGeneratorProperties(new MavenProperties());
        generatorProperties.setSecretMaskingEnabled(true);
        generatorProperties.getTemplates().setOverridePath(this.basePath);
        generatorProperties.getTemplates().getAdditionalResources().addAll(this.additionalResources);

        Path runtimeDir = generate( integrationDeployment, generatorProperties);

        assertFileContents(generatorProperties, runtimeDir.resolve("src/main/java/io/syndesis/example/Application.java"), "test-Application.java");
        assertFileContents(generatorProperties, runtimeDir.resolve("src/main/resources/application.properties"), "test-application.properties");
        assertFileContents(generatorProperties, runtimeDir.resolve("src/main/resources/syndesis.yml"), "test-connector-convert-syndesis.yml");
        assertFileContents(generatorProperties, runtimeDir.resolve("pom.xml"), "test-connector-convert-pom.xml");

        for (Templates.Resource additionalResource : generatorProperties.getTemplates().getAdditionalResources()) {
            assertFileContents(generatorProperties, runtimeDir.resolve(additionalResource.getDestination()), "test-" + additionalResource.getSource());
        }
    }

    // ************************************************
    // Helpers
    // ************************************************

    private Integration newIntegration() {
        return new Integration.Builder()
            .id("test-integration")
            .name("Test Integration")
            .description("This is a test integration!")
            .build();
    }

    private IntegrationDeployment newIntegrationDeployment(Step... steps) {
        for (Step step : steps) {
            step.getConnection().ifPresent(
                resource -> resources.put(resource.getId().get(), resource)
            );
            step.getAction().filter(ConnectorAction.class::isInstance).map(ConnectorAction.class::cast).ifPresent(
                resource -> resources.put(resource.getId().get(), resource)
            );
            step.getExtension().ifPresent(
                resource -> resources.put(resource.getId().get(), resource)
            );
        }

        return new IntegrationDeployment.Builder()
            .integrationId("test-integration")
            .name("Test Integration")
            .spec(new IntegrationDeploymentSpec.Builder()
                .name("Test Integration")
                .description("This is a test integration!")
                .steps(Arrays.asList(steps))
                .build())
            .build();
    }

    private Path generate( IntegrationDeployment integrationDeployment, ProjectGeneratorProperties generatorProperties) throws IOException {
        final DataManager dataManager = mock(DataManager.class);
        final ExtensionDataManager extensionDataManager = mock(ExtensionDataManager.class);
        final ProjectGenerator generator = new DefaultProjectGenerator(generatorProperties, registry, dataManager, extensionDataManager);

        // mock data manager
        when(dataManager.fetch(anyObject(), anyString())).then(invocation -> {
            final String id = invocation.getArgumentAt(1, String.class);
            final Object resource = resources.get(id);

            LOGGER.debug("Resource {}: {}", id, resource);

            return resource;
        });

        // mock extension manager
        when(extensionDataManager.getExtensionBinaryFile(anyString())).then(invocation -> {
            final String id = invocation.getArgumentAt(0, String.class);
            final InputStream is = IOUtils.toInputStream(id, StandardCharsets.UTF_8);

            return is;
        });

        try (InputStream is = generator.generate(integrationDeployment)) {
            Path ret = testFolder.newFolder("integration-runtime").toPath();

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

        final String actual = new String(Files.readAllBytes(actualFilePath), StandardCharsets.UTF_8);
        final String expected = new String(Files.readAllBytes(Paths.get(resource.toURI())), StandardCharsets.UTF_8);

        assertThat(actual).isEqualTo(expected);
    }
}
