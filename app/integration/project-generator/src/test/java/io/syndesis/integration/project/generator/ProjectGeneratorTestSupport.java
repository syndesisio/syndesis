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
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;

import io.syndesis.integration.api.IntegrationProjectGenerator;
import io.syndesis.integration.api.IntegrationResourceManager;
import io.syndesis.model.Dependency;
import io.syndesis.model.action.ConnectorAction;
import io.syndesis.model.action.ConnectorDescriptor;
import io.syndesis.model.connection.ConfigurationProperty;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.extension.Extension;
import io.syndesis.model.integration.Integration;
import io.syndesis.model.integration.IntegrationDeployment;
import io.syndesis.model.integration.Step;

@SuppressWarnings("PMD.ExcessiveImports")
public class ProjectGeneratorTestSupport {
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
    @Rule
    public TestName testName = new TestName();

    // *****************************
    // Static definitions
    // *****************************

    protected static final String SYNDESIS_CONNECTORS_VERSION;
    protected static final String CAMEL_VERSION;
    protected static final ConnectorAction PERIODIC_TIMER_ACTION;
    protected static final Connector TIMER_CONNECTOR;
    protected static final ConnectorAction HTTP_GET_ACTION;
    protected static final ConnectorAction HTTP_POST_ACTION;
    protected static final Connector HTTP_CONNECTOR;
    protected static final ConnectorAction TWITTER_MENTION_ACTION;
    protected static final Connector TWITTER_CONNECTOR;

    static {
        SYNDESIS_CONNECTORS_VERSION = ResourceBundle.getBundle("test").getString("syndesis-connectors.version");
        CAMEL_VERSION = ResourceBundle.getBundle("test").getString("camel.version");

        PERIODIC_TIMER_ACTION = new ConnectorAction.Builder()
            .id("periodic-timer-action")
            .descriptor(new ConnectorDescriptor.Builder()
                .connectorId("timer")
                .camelConnectorPrefix("periodic-timer-connector")
                .camelConnectorGAV("io.syndesis.connector:connector-timer:" + SYNDESIS_CONNECTORS_VERSION)
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
            .id("http-get-action")
            .descriptor(new ConnectorDescriptor.Builder()
                .connectorId("http")
                .camelConnectorPrefix("http-get-connector")
                .camelConnectorGAV("io.syndesis.connector:connector-http-get:" + SYNDESIS_CONNECTORS_VERSION)
                .build())
            .build();


        HTTP_POST_ACTION = new ConnectorAction.Builder()
            .id("http-post-action")
            .descriptor(new ConnectorDescriptor.Builder()
                .connectorId("http")
                .camelConnectorPrefix("http-post-connector")
                .camelConnectorGAV("io.syndesis.connector:connector-http-post:" + SYNDESIS_CONNECTORS_VERSION)
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
            .addDependency(Dependency.maven("io.syndesis.integration:integration-component-proxy:" + SYNDESIS_CONNECTORS_VERSION))
            .addDependency(Dependency.maven("org.apache.camel:camel-twitter:" + CAMEL_VERSION))
            .addAction(TWITTER_MENTION_ACTION)
            .build();
    }

    // *****************************
    // Helpers
    // *****************************

    protected IntegrationDeployment newIntegration(TestResourceManager resourceManager, Step... steps) {
        for (int i = 0; i < steps.length; i++) {
            steps[i].getConnection().filter(r -> r.getId().isPresent()).ifPresent(
                resource -> resourceManager.put(resource.getId().get(), resource)
            );
            steps[i].getAction().filter(ConnectorAction.class::isInstance).map(ConnectorAction.class::cast).filter(r -> r.getId().isPresent()).ifPresent(
                resource -> resourceManager.put(resource.getId().get(), resource)
            );
            steps[i].getExtension().filter(r -> r.getId().isPresent()).ifPresent(
                resource -> resourceManager.put(resource.getId().get(), resource)
            );

            steps[i] = new Step.Builder().createFrom(steps[i]).putMetadata(Step.METADATA_STEP_INDEX, Integer.toString(i + 1)).build();
        }

        return new IntegrationDeployment.Builder()
            .spec(new Integration.Builder()
                .id("test-integration")
                .name("Test Integration")
                .description("This is a test integration!")
                .steps(Arrays.asList(steps))
                .build())
            .build();
    }

    protected Path generate(IntegrationDeployment deployment, ProjectGeneratorConfiguration generatorConfiguration, TestResourceManager resourceManager) throws IOException {
        final IntegrationProjectGenerator generator = new ProjectGenerator(generatorConfiguration, resourceManager);

        try (InputStream is = generator.generate(deployment)) {
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

    protected void assertFileContents(ProjectGeneratorConfiguration generatorConfiguration, Path actualFilePath, String expectedFileName) throws URISyntaxException, IOException {
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

    // *****************************
    // Resources
    // *****************************

    protected class TestResourceManager implements IntegrationResourceManager {
        private final ConcurrentMap<String , Object> resources;

        public TestResourceManager() {
            resources = new ConcurrentHashMap<>();
        }

        public void put(String id, Object resource) {
            resources.put(id, resource);
        }

        @Override
        public Optional<Connector> loadConnector(String id) {
            return Optional.ofNullable(resources.get(id))
                .filter(Connector.class::isInstance)
                .map(Connector.class::cast);
        }

        @Override
        public Optional<Extension> loadExtension(String id) {
            return Optional.ofNullable(resources.get(id))
                .filter(Extension.class::isInstance)
                .map(Extension.class::cast);
        }

        @Override
        public Optional<InputStream> loadExtensionBLOB(String id) {
            final InputStream is = IOUtils.toInputStream(id, StandardCharsets.UTF_8);

            return Optional.of(is);
        }

        @Override
        public String decrypt(String encrypted) {
            return encrypted;
        }
    }
}
