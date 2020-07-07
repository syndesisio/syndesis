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

package io.syndesis.test.integration.project;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.extension.Extension;
import io.syndesis.common.model.openapi.OpenApi;
import io.syndesis.common.util.MavenProperties;
import io.syndesis.integration.api.IntegrationResourceManager;
import io.syndesis.integration.project.generator.ProjectGenerator;
import io.syndesis.integration.project.generator.ProjectGeneratorConfiguration;
import io.syndesis.test.SyndesisTestEnvironment;
import io.syndesis.test.integration.source.IntegrationSource;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 */
public abstract class AbstractMavenProjectBuilder<T extends AbstractMavenProjectBuilder<T>> implements ProjectBuilder {

    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractMavenProjectBuilder.class);

    private final String name;
    private final String syndesisVersion;

    private Path outputDir;
    private final ProjectGeneratorConfiguration projectGeneratorConfiguration = new ProjectGeneratorConfiguration();
    private final MavenProperties mavenProperties = new MavenProperties();

    protected AbstractMavenProjectBuilder(String name, String syndesisVersion) {
        this.name = name;
        this.syndesisVersion = syndesisVersion;
        withOutputDirectory(SyndesisTestEnvironment.getOutputDirectory());
    }

    @Override
    public Project build(IntegrationSource source) {
        try {
            Path projectDir = Files.createTempDirectory(outputDir, name);

            LOG.info(String.format("Building integration project in directory: '%s'", projectDir.toAbsolutePath()));

            ProjectGenerator projectGenerator = new ProjectGenerator(projectGeneratorConfiguration,
                    new StaticIntegrationResourceManager(source),
                    mavenProperties);
            try (TarArchiveInputStream in = new TarArchiveInputStream(projectGenerator.generate(source.get(), System.out::println))) {
                ArchiveEntry archiveEntry = in.getNextEntry();
                while (archiveEntry != null) {
                    Path fileOrDirectory = projectDir.resolve(archiveEntry.getName());
                    if (archiveEntry.isDirectory()) {
                        if (!Files.exists(fileOrDirectory)) {
                            Files.createDirectories(fileOrDirectory);
                        }
                    } else {
                        Path parent = fileOrDirectory.getParent();
                        if (parent != null) {
                            Files.createDirectories(parent);
                        }
                        Files.copy(in, fileOrDirectory);
                    }

                    archiveEntry = in.getNextEntry();
                }
            }

            customizePomFile(source, projectDir.resolve("pom.xml"));
            customizeIntegrationFile(source, projectDir.resolve("src").resolve("main").resolve("resources").resolve("syndesis").resolve("integration").resolve("integration.json"));

            // auto add secrets and other integration test settings to application properties
            Files.write(projectDir.resolve("src").resolve("main").resolve("resources").resolve("application.properties"),
                    getApplicationProperties(source).getBytes(Charset.forName("utf-8")), StandardOpenOption.APPEND);
            return new Project.Builder().projectPath(projectDir).build();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create integration project", e);
        }
    }

    protected void customizePomFile(IntegrationSource source, Path pomFile) throws IOException {
        // overwrite the syndesis version in generated pom.xml as project export may use another version as required in test.
        if (Files.exists(pomFile)) {
            List<String> pomLines = Files.readAllLines(pomFile, Charset.forName("utf-8"));
            StringBuilder newPom = new StringBuilder();
            for (String line : pomLines) {
                newPom.append(customizePomLine(line)).append(System.lineSeparator());
            }
            Files.write(pomFile, newPom.toString().getBytes(Charset.forName("utf-8")));
        }
    }

    protected String customizePomLine(String line) {
        if (line.trim().startsWith("<syndesis.version>") && line.trim().endsWith("</syndesis.version>")) {
            return line.substring(0, line.indexOf('<')) +
                    String.format("<syndesis.version>%s</syndesis.version>", syndesisVersion) +
                    line.substring(line.lastIndexOf('>') + 1);
        }

        return line;
    }

    @SuppressWarnings("unchecked")
    private T self() {
        return (T) this;
    }

    private String getApplicationProperties(IntegrationSource source) throws IOException {
        ProjectGenerator projectGenerator = new ProjectGenerator(projectGeneratorConfiguration, new StaticIntegrationResourceManager(source), mavenProperties);
        Properties applicationProperties = customizeApplicationProperties(projectGenerator.generateApplicationProperties(source.get()));

        StringWriter writer = new StringWriter();
        applicationProperties.store(writer, "Auto added integration test properties");

        return writer.toString();
    }

    protected Properties customizeApplicationProperties(Properties applicationProperties) {
        applicationProperties.put("management.server.port", String.valueOf(SyndesisTestEnvironment.getManagementPort()));
        return applicationProperties;
    }

    public final T withOutputDirectory(String tmpDir) {
        try {
            this.outputDir = Files.createDirectories(Paths.get(tmpDir));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create temp directory", e);
        }
        return self();
    }

    static class StaticIntegrationResourceManager implements IntegrationResourceManager {
        private final IntegrationSource source;

        StaticIntegrationResourceManager(IntegrationSource source) {
            this.source = source;
        }

        @Override
        public Optional<Connector> loadConnector(String id) {
            return Optional.empty();
        }

        @Override
        public Optional<Extension> loadExtension(String id) {
            return Optional.of(new Extension.Builder().id(id).build());
        }

        @Override
        public List<Extension> loadExtensionsByTag(String tag) {
            return Collections.emptyList();
        }

        @Override
        public Optional<InputStream> loadExtensionBLOB(String id) {
            InputStream is = null;
            LOG.info("Uploading extension BLOB from file {}", id);
            try {
                is = new ByteArrayInputStream(IOUtils.toByteArray(new FileInputStream(id)));
            } catch (Exception e) {
                LOG.error("Error while uploading extension BLOB", e);
            }
            return Optional.ofNullable(is);
        }

        @Override
        public Optional<OpenApi> loadOpenApiDefinition(String id) {
            return Optional.ofNullable(source.getOpenApis().get(":" + id));
        }

        @Override
        public String decrypt(String encrypted) {
            return "secret";
        }
    }

    /**
     * Obtains the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Obtains the projectGeneratorConfiguration.
     */
    public ProjectGeneratorConfiguration getProjectGeneratorConfiguration() {
        return projectGeneratorConfiguration;
    }

    /**
     * Obtains the mavenProperties.
     */
    public MavenProperties getMavenProperties() {
        return mavenProperties;
    }
}
