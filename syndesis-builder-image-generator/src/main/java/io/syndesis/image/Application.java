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
package io.syndesis.image;

import io.syndesis.connector.catalog.ConnectorCatalog;
import io.syndesis.connector.catalog.ConnectorCatalogProperties;
import io.syndesis.dao.init.ModelData;
import io.syndesis.dao.init.ReadApiClientData;
import io.syndesis.dao.manager.DaoConfiguration;
import io.syndesis.model.Kind;
import io.syndesis.model.connection.Action;
import io.syndesis.model.connection.Connection;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.integration.Integration;
import io.syndesis.model.integration.SimpleStep;
import io.syndesis.model.integration.Step;
import io.syndesis.project.converter.DefaultProjectGenerator;
import io.syndesis.project.converter.GenerateProjectRequest;
import io.syndesis.project.converter.ProjectGeneratorProperties;
import io.syndesis.project.converter.visitor.StepVisitorFactoryRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication(exclude = {DaoConfiguration.class})
public class Application implements ApplicationRunner {

    @Value("${to:image}")
    private String to;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            System.out.println("To: "+to);
            generateIntegrationProject(new File(to));
            System.exit(0);
        } catch (IOException e) {
            System.exit(1);
        }
    }


    static private void generateIntegrationProject(File project) throws IOException {
        ArrayList<Step> steps = new ArrayList<>();
        HashMap<String, Connector> connectors = new HashMap<String, Connector>();

        String deploymentText;
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("io/syndesis/dao/deployment.json")) {
            ReadApiClientData readApiClientData = new ReadApiClientData();
            deploymentText = readApiClientData.from(is);
        }

        final ReadApiClientData reader = new ReadApiClientData();
        final List<ModelData<?>> modelList = reader.readDataFromString(deploymentText);
        for (final ModelData<?> model : modelList) {
            if (model.getKind() == Kind.Connector) {
                final Connector connector = (Connector) model.getData();
                connectors.put(connector.getId().get(), connector);
                for (final Action action : connector.getActions()) {
                    steps.add(
                        new SimpleStep.Builder()
                            .stepKind("endpoint").
                            connection(new Connection.Builder()
                                .configuredProperties(map())
                                .connectorId(connector.getId())
                                .build())
                            .configuredProperties(map())
                            .action(action)
                            .build()
                    );
                }
            }
        }

        GenerateProjectRequest request = new GenerateProjectRequest.Builder()
            .integration(new Integration.Builder()
                .id("integration")
                .name("Integration")
                .description("This integration is used to prime the .m2 repo")
                .steps( steps )
                .build())
            .connectors(connectors)
            .build();
        generate(request, project);
    }

    static private void generate(GenerateProjectRequest request, File targetDir) throws IOException {

        ProjectGeneratorProperties generatorProperties = new ProjectGeneratorProperties();
        ConnectorCatalogProperties catalogProperties = new ConnectorCatalogProperties();
        catalogProperties.setMavenRepos(new HashMap<>());
        StepVisitorFactoryRegistry registry = new StepVisitorFactoryRegistry(Arrays.asList());
        DefaultProjectGenerator generator = new DefaultProjectGenerator(new ConnectorCatalog(catalogProperties), generatorProperties, registry);

        Path dir =targetDir.toPath();
        Files.createDirectories( dir);
        Files.write(dir.resolve("pom.xml"), generator.generatePom(request.getIntegration()));

        dir = dir.resolve("src/main/java/io/syndesis/example");
        Files.createDirectories( dir);

        String resourceFile = "io/syndesis/project/converter/templates/Application.java.mustache";
        try (InputStream is = Application.class.getClassLoader().getResourceAsStream(resourceFile) )  {
            Files.write(dir.resolve("Application.java"), readAllBytes(is));
        }

    }

    public static byte[] readAllBytes(InputStream is) throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024 * 4];
            for (int len; (len = is.read(buffer)) != -1; )
                os.write(buffer, 0, len);
            os.flush();
            return os.toByteArray();
        }
    }


    // Helper method to help construct maps with concise syntax
    static private Map<String, String> map(Object... values) {
        HashMap<String, String> rc = new HashMap<>();
        for (int i = 0; i + 1 < values.length; i += 2) {
            rc.put(values[i].toString(), values[i + 1].toString());
        }
        return rc;
    }
}
