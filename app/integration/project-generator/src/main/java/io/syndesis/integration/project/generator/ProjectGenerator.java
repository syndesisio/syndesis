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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import io.syndesis.core.Json;
import io.syndesis.core.Names;
import io.syndesis.integration.api.IntegrationProjectGenerator;
import io.syndesis.integration.api.IntegrationResourceManager;
import io.syndesis.integration.project.generator.mvn.MavenGav;
import io.syndesis.integration.project.generator.mvn.PomContext;
import io.syndesis.model.Dependency;
import io.syndesis.model.integration.IntegrationDeployment;
import io.syndesis.model.integration.Step;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("PMD.ExcessiveImports")
public class ProjectGenerator implements IntegrationProjectGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectGenerator.class);

    private final MustacheFactory mf = new DefaultMustacheFactory();
    private final ProjectGeneratorConfiguration generatorProperties;
    private final IntegrationResourceManager resourceManager;
    private final Mustache applicationJavaMustache;
    private final Mustache applicationPropertiesMustache;
    private final Mustache pomMustache;

    public ProjectGenerator(ProjectGeneratorConfiguration generatorProperties, IntegrationResourceManager resourceManager) throws IOException {
        this.generatorProperties = generatorProperties;
        this.resourceManager = resourceManager;
        this.applicationJavaMustache = compile(generatorProperties, "Application.java.mustache", "Application.java");
        this.applicationPropertiesMustache = compile(generatorProperties, "application.properties.mustache", "application.properties");
        this.pomMustache = compile(generatorProperties, "pom.xml.mustache", "pom.xml");
    }

    @Override
    @SuppressWarnings("resource")
    public InputStream generate(IntegrationDeployment deployment) throws IOException {
        final PipedInputStream is = new PipedInputStream();
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final PipedOutputStream os = new PipedOutputStream(is);

        executor.execute(generateAddProjectTarEntries(deployment, os));

        return is;
    }

    @Override
    public byte[] generatePom(IntegrationDeployment deployment) throws IOException {
        final Set<MavenGav> dependencies = resourceManager.collectDependencies(deployment).stream()
            .filter(Dependency::isMaven)
            .map(Dependency::getId)
            .map(MavenGav::new)
            .filter(ProjectGeneratorHelper::filterDefaultDependencies)
            .collect(Collectors.toCollection(TreeSet::new));

        return ProjectGeneratorHelper.generate(
            new PomContext(
                deployment.getId().orElse(""),
                deployment.getName(),
                deployment.getSpec().getDescription().orElse(null),
                dependencies,
                generatorProperties.getMavenProperties()),
            pomMustache
        );
    }

    private Mustache compile(ProjectGeneratorConfiguration generatorProperties, String template, String name) throws IOException {
        String overridePath = generatorProperties.getTemplates().getOverridePath();
        URL resource = null;

        if (!StringUtils.isEmpty(overridePath)) {
            resource = getClass().getResource("templates/" + overridePath + "/" + template);
        }
        if (resource == null) {
            resource = getClass().getResource("templates/" + template);
        }
        if (resource == null) {
            throw new IllegalArgumentException(
                String.format("Unable to find te required template (overridePath=%s, template=%s)"
                    , overridePath
                    , template
                )
            );
        }

        try (InputStream stream = resource.openStream()) {
            return mf.compile(new InputStreamReader(stream, StandardCharsets.UTF_8), name);
        }
    }

    private void addAdditionalResources(TarArchiveOutputStream tos) throws IOException {
        for (ProjectGeneratorConfiguration.Templates.Resource additionalResource : generatorProperties.getTemplates().getAdditionalResources()) {
            String overridePath = generatorProperties.getTemplates().getOverridePath();
            URL resource = null;

            if (!StringUtils.isEmpty(overridePath)) {
                resource = getClass().getResource("templates/" + overridePath + "/" + additionalResource.getSource());
            }
            if (resource == null) {
                resource = getClass().getResource("templates/" + additionalResource.getSource());
            }
            if (resource == null) {
                throw new IllegalArgumentException(
                    String.format("Unable to find te required additional resource (overridePath=%s, source=%s)"
                        , overridePath
                        , additionalResource.getSource()
                    )
                );
            }

            try {
                ProjectGeneratorHelper.addTarEntry(tos, additionalResource.getDestination(), Files.readAllBytes(Paths.get(resource.toURI())));
            } catch (URISyntaxException e) {
                throw new IOException(e);
            }
        }
    }

    @SuppressWarnings("PMD.DoNotUseThreads")
    private Runnable generateAddProjectTarEntries(IntegrationDeployment deployment, OutputStream os) {
        return () -> {
            try (
                TarArchiveOutputStream tos = new TarArchiveOutputStream(os)) {
                tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);

                ObjectWriter writer = Json.writer();
                ProjectGeneratorHelper.addTarEntry(tos, "src/main/java/io/syndesis/example/Application.java", ProjectGeneratorHelper.generate(deployment, applicationJavaMustache));
                ProjectGeneratorHelper.addTarEntry(tos, "src/main/resources/application.properties", ProjectGeneratorHelper.generate(deployment, applicationPropertiesMustache));
                ProjectGeneratorHelper.addTarEntry(tos, "src/main/resources/syndesis/integration/integration.json", writer.with(writer.getConfig().getDefaultPrettyPrinter()).writeValueAsBytes(deployment));
                ProjectGeneratorHelper.addTarEntry(tos, "pom.xml", generatePom(deployment));
                addResource(tos, ".s2i/bin/assemble", "s2i/assemble");
                addExtensions(tos, deployment);
                addAdditionalResources(tos);

                for (Step step : deployment.getSpec().getSteps()) {
                    if ("mapper".equals(step.getStepKind())) {
                        final Map<String, String> properties = step.getConfiguredProperties();
                        final String mapping = properties.get("atlasmapping");

                        if (mapping != null) {
                            final String index = step.getMetadata(Step.METADATA_STEP_INDEX).orElseThrow(() -> new IllegalArgumentException("Missing index for step:" + step));
                            final String resource = "mapping-step-"  +index + ".json";

                            ProjectGeneratorHelper.addTarEntry(tos, "src/main/resources/" + resource, mapping.getBytes(StandardCharsets.UTF_8));
                        }
                    }
                }

                LOGGER.info("IntegrationDeployment [{}]: Project files written to output stream", Names.sanitize(deployment.getName()));
            } catch (IOException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error(String.format("Exception while creating runtime build tar for deployment %s : %s",
                        deployment.getName(), e.toString()), e);
                }
            }
        };
    }

    private void addResource(TarArchiveOutputStream tos, String destination, String resource) throws IOException {
        final URL url = getClass().getResource(resource);
        final byte[] bytes = IOUtils.toByteArray(url);

        ProjectGeneratorHelper.addTarEntry(tos, destination, bytes);
    }

    private void addExtensions(TarArchiveOutputStream tos, IntegrationDeployment deployment) throws IOException {
        final Set<String> extensions = resourceManager.collectDependencies(deployment).stream()
            .filter(Dependency::isExtension)
            .map(Dependency::getId)
            .collect(Collectors.toCollection(TreeSet::new));

        if (!extensions.isEmpty()) {
            ProjectGeneratorHelper. addTarEntry(tos, "src/main/resources/loader.properties", generateExtensionLoader(extensions));

            for (String extensionId : extensions) {
                ProjectGeneratorHelper.addTarEntry(
                    tos,
                    "extensions/" + Names.sanitize(extensionId) + ".jar",
                    IOUtils.toByteArray(
                        resourceManager.loadExtensionBLOB(extensionId).orElseThrow(
                            () -> new IllegalStateException("No extension blob for extension with id:" + extensionId)
                        )
                    )
                );
            }
        }
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private byte[] generateExtensionLoader(Set<String> extensions) {
        if (!extensions.isEmpty()) {
            return new StringBuilder()
                .append("loader.path")
                .append('=')
                .append(extensions.stream()
                    .map(Names::sanitize)
                    .map(id -> generatorProperties.getSyndesisExtensionPath() + "/" + id + ".jar")
                    .collect(Collectors.joining(",")))
                .append('\n')
                .toString()
                .getBytes(StandardCharsets.UTF_8);
        }

        return new byte[] {};
    }
}
