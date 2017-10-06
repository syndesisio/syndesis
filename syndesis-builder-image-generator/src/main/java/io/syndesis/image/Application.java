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
import io.syndesis.project.converter.visitor.*;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

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

        String deploymentText = null;
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
        final Map<String, String> repositories = new HashMap<>();
        repositories.put("maven.central", "https://repo1.maven.org/maven2");
        repositories.put("redhat.ga", "https://maven.repository.redhat.com/ga");
        repositories.put("jboss.ea", "https://repository.jboss.org/nexus/content/groups/ea");

        ConnectorCatalogProperties catalogProperties = new ConnectorCatalogProperties();
        catalogProperties.setMavenRepos(repositories);

        // We don't need to generate a real syndesis.yml, so use this dumb step registry.
        StepVisitorFactoryRegistry registry = new StepVisitorFactoryRegistry(
            Arrays.asList(
                new StepVisitorFactory(){
                    @Override
                    public String getStepKind() {
                        return "endpoint";
                    }

                    @Override
                    public StepVisitor create(GeneratorContext generatorContext) {
                        return new StepVisitor(){

                            @Override
                            public io.syndesis.integration.model.steps.Step visit(StepVisitorContext stepContext) {
                                return null;
                            }
                        };
                    }
                }
            )
        );

        try (InputStream is = new DefaultProjectGenerator(new ConnectorCatalog(catalogProperties), generatorProperties, registry).generate(request)) {
            Path ret = Files.createDirectories( targetDir.toPath() );
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
        }
    }


    // Helper method to help constuct maps with concise syntax
    static private Map<String, String> map(Object... values) {
        HashMap<String, String> rc = new HashMap<>();
        for (int i = 0; i + 1 < values.length; i += 2) {
            rc.put(values[i].toString(), values[i + 1].toString());
        }
        return rc;
    }
}
