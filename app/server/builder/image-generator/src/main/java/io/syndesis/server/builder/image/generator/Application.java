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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.model.openapi.OpenApi;
import io.syndesis.common.util.MavenProperties;
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

import io.syndesis.common.util.Json;
import io.syndesis.common.util.SuppressFBWarnings;
import io.syndesis.server.dao.init.ReadApiClientData;
import io.syndesis.server.dao.manager.DaoConfiguration;
import io.syndesis.integration.api.IntegrationProjectGenerator;
import io.syndesis.integration.api.IntegrationResourceManager;
import io.syndesis.integration.project.generator.ProjectGenerator;
import io.syndesis.integration.project.generator.ProjectGeneratorAutoConfiguration;
import io.syndesis.integration.project.generator.ProjectGeneratorConfiguration;
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


    private void generateIntegrationProject(File project) throws IOException {
        final ReadApiClientData reader = new ReadApiClientData();
        final ArrayList<Step> steps = new ArrayList<>();

        String deploymentText;
        try (InputStream is = resourceLoader.getResource("io/syndesis/server/dao/deployment.json").getInputStream()) {
            deploymentText = reader.from(is);
        }

        final List<ModelData<?>> modelList = reader.readDataFromString(deploymentText);
        for (final ModelData<?> model : modelList) {
            if (model.getKind() == Kind.Connector) {
                final Connector connector = (Connector) model.getData();
                for (final Action action : connector.getActions()) {
                    steps.add(
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
                steps.add(
                    new Step.Builder()
                        .stepKind(StepKind.endpoint)
                        .connection(new Connection.Builder()
                            .connectorId("connector-" + template.getId())
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
                    Connector connector = Json.reader().forType(Connector.class).readValue(resource.getInputStream());

                    if (connector != null) {
                        for (final Action action : connector.getActions()) {
                            steps.add(
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
                }
            }
        } catch (FileNotFoundException ignored) {
            // ignore
        }


        Integration integration = new Integration.Builder()
            .id("Integration")
            .name("Integration")
            .description("This integration is used to prime the .m2 repo")
            .addFlow(new Flow.Builder().steps(steps).build())
            .build();

        generate(integration, project);
    }

    @SuppressWarnings("PMD.UseProperClassLoader")
    private void generate(Integration integration, File targetDir) throws IOException {
        ProjectGeneratorConfiguration configuration = new ProjectGeneratorConfiguration();
        IntegrationProjectGenerator generator = new ProjectGenerator(configuration, new EmptyIntegrationResourceManager(), mavenProperties);

        Path dir =targetDir.toPath();
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
        @Override
        public Optional<Connector> loadConnector(String id) {
            return Optional.empty();
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

}
