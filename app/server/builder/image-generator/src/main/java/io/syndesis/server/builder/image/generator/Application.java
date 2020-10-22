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
package io.syndesis.server.builder.image.generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;

import io.syndesis.common.model.Kind;
import io.syndesis.common.model.ModelData;
import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.connection.ConnectorTemplate;
import io.syndesis.common.model.extension.Extension;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.model.openapi.OpenApi;
import io.syndesis.common.util.MavenProperties;
import io.syndesis.common.util.SuppressFBWarnings;
import io.syndesis.common.util.json.JsonUtils;
import io.syndesis.integration.api.IntegrationProjectGenerator;
import io.syndesis.integration.api.IntegrationResourceManager;
import io.syndesis.integration.project.generator.ProjectGenerator;
import io.syndesis.integration.project.generator.ProjectGeneratorAutoConfiguration;
import io.syndesis.integration.project.generator.ProjectGeneratorConfiguration;
import io.syndesis.server.dao.init.ReadApiClientData;
import io.syndesis.server.dao.manager.DaoConfiguration;

@EnableConfigurationProperties(SpringMavenProperties.class)
@SpringBootApplication(
    exclude = {
        DaoConfiguration.class,
        ProjectGeneratorAutoConfiguration.class
    }
)
@SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity"})
public class Application implements ApplicationRunner {
    @Autowired
    private ResourceLoader resourceLoader;
    @Autowired
    private MavenProperties mavenProperties;

    @Value("${to:image}")
    private String to;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    @SuppressFBWarnings("DM_EXIT")
    public void run(ApplicationArguments args) {
        try {
            System.out.println("To: "+to); //NOPMD
            generateIntegrationProject(new File(to));
        } catch (IOException e) {
            e.printStackTrace(); //NOPMD
            System.exit(1); //NOPMD
        }
    }

    private GatheredDependencies collectAllSteps() throws IOException {
        final ReadApiClientData reader = new ReadApiClientData();
        final GatheredDependencies deps = new GatheredDependencies();

        String deploymentText;
        try (InputStream is = resourceLoader.getResource("io/syndesis/server/dao/deployment.json").getInputStream()) {
            deploymentText = reader.from(is);
        }

        final List<ModelData<?>> modelList = reader.readDataFromString(deploymentText);
        final Map<String, String> schemeIdMap = new HashMap<>();
        for (final ModelData<?> model : modelList) {
            if (model.getKind() == Kind.Connector) {
                final Connector connector = (Connector) model.getData();
                for (final Action action : connector.getActions()) {
                    deps.steps.add(
                        new Step.Builder()
                            .stepKind(StepKind.endpoint)
                            .connection(new Connection.Builder()
                                .connector(connector)
                                .connectorId(connector.getId().get())
                                .build())
                            .action(action)
                            .build()
                    );
                }
            }

            if (model.getKind() == Kind.ConnectorTemplate) {
                final ConnectorTemplate template = (ConnectorTemplate) model.getData();
                schemeIdMap.put(template.getComponentScheme(), template.getId().get());
                deps.steps.add(
                    new Step.Builder()
                        .stepKind(StepKind.endpoint)
                        .connection(new Connection.Builder()
                            .connectorId(template.getId().get())
                            .build())
                        .action(new ConnectorAction.Builder()
                            .descriptor(new ConnectorDescriptor.Builder()
                                .componentScheme(template.getComponentScheme())
                                .build())
                            .build())
                        .build()
                );
            }
        }

        try {
            final ResourcePatternResolver resolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
            final Resource[] resources = resolver.getResources("classpath:/META-INF/syndesis/connector/*.json");

            if (resources != null) {
                for (Resource resource: resources) {
                    Connector connector = JsonUtils.reader().forType(Connector.class).readValue(resource.getInputStream());

                    if (connector != null) {
                        // template IDs are mapped to their actual connectors, to add dependencies
                        final String templateId = schemeIdMap.get(connector.getComponentScheme().orElse(null));
                        if (templateId != null) {
                            deps.connectorMap.put(templateId, connector);
                        }

                        deps.steps.add(
                            new Step.Builder()
                                .stepKind(StepKind.endpoint)
                                .connection(new Connection.Builder()
                                    .connector(connector)
                                    .connectorId(connector.getId().get())
                                    .build())
                                .build()
                        );
                    }
                }
            }
        } catch (FileNotFoundException ignored) {
            // ignore
        }

        return deps;
    }

    private void generateIntegrationProject(File project) throws IOException {
        final GatheredDependencies deps = collectAllSteps();

        StringBuilder parentPom = new StringBuilder(5000);
        parentPom.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://maven.apache.org/POM/4.0.0\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\"><modelVersion>4.0.0</modelVersion> <groupId>io.syndesis.integrations</groupId><artifactId>project</artifactId><version>0.1-SNAPSHOT</version><packaging>pom</packaging><modules>\n");
        int i = 0;
        for (final Step step : deps.steps) {
          final Integration integration = new Integration.Builder()
              .id("Integration")
              .name("Integration")
              .description("This integration is used to prime the .m2 repo")
              .addFlow(new Flow.Builder().addStep(step).build())
              .build();

          final String artifactId = "p" + i++;
          final File module = new File(project, artifactId);

          generate(integration, module, deps.connectorMap, artifactId);
          parentPom.append("<module>").append(artifactId).append("</module>\n");
        }

        parentPom.append("</modules></project>");

        Files.write(project.toPath().resolve("pom.xml"), parentPom.toString().getBytes(StandardCharsets.UTF_8));
    }

    @SuppressWarnings("PMD.UseProperClassLoader")
    private void generate(Integration integration, File targetDir, Map<String, Connector> connectorMap, String artifactId) throws IOException {
        ProjectGeneratorConfiguration configuration = new ProjectGeneratorConfiguration();
        configuration.setArtifactId(artifactId);
        IntegrationProjectGenerator generator = new ProjectGenerator(configuration, new EmptyIntegrationResourceManager(connectorMap), mavenProperties);

        Path dir = targetDir.toPath();
        Files.createDirectories(dir);
        Files.write(dir.resolve("pom.xml"), generator.generatePom(integration));

        dir = dir.resolve("src/main/java/io/syndesis/example");
        Files.createDirectories(dir);

        ClassPathResource resource = new ClassPathResource("io/syndesis/integration/project/generator/templates/Application.java.mustache");
        try (InputStream is = resource.getInputStream() )  {
            Files.write(dir.resolve("Application.java"), IOUtils.toByteArray(is));
        }
    }

    private static class EmptyIntegrationResourceManager implements IntegrationResourceManager {

        private final Map<String, Connector> connectorMap;

        public EmptyIntegrationResourceManager(Map<String, Connector> connectorMap) {
            this.connectorMap = connectorMap;
        }

        @Override
        public Optional<Connector> loadConnector(String id) {
            return Optional.ofNullable(connectorMap.get(id));
        }

        @Override
        public Optional<Extension> loadExtension(String id) {
            return Optional.empty();
        }

        @Override
        public List<Extension> loadExtensionsByTag(String tag) {
            return Collections.emptyList();
        }

        @Override
        public Optional<InputStream> loadExtensionBLOB(String extensionId) {
            return Optional.empty();
        }

        @Override
        public Optional<OpenApi> loadOpenApiDefinition(String s) {
            return Optional.empty();
        }

        @Override
        public String decrypt(String encrypted) {
            return encrypted;
        }
    }

    private static class GatheredDependencies {
        List<Step> steps = new ArrayList<>();
        Map<String, Connector> connectorMap = new HashMap<>();
    }

}
