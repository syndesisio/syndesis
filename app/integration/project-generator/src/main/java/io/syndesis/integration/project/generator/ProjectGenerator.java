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
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import io.syndesis.core.Json;
import io.syndesis.core.Names;
import io.syndesis.core.Optionals;
import io.syndesis.core.Predicates;
import io.syndesis.integration.api.IntegrationProjectGenerator;
import io.syndesis.integration.api.IntegrationResourceManager;
import io.syndesis.integration.project.generator.mvn.MavenGav;
import io.syndesis.integration.project.generator.mvn.PomContext;
import io.syndesis.model.Dependency;
import io.syndesis.model.WithConfiguredProperties;
import io.syndesis.model.action.ConnectorAction;
import io.syndesis.model.action.ConnectorDescriptor;
import io.syndesis.model.connection.Connection;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.integration.IntegrationDeployment;
import io.syndesis.model.integration.Step;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.syndesis.integration.project.generator.ProjectGeneratorHelper.addResource;
import static io.syndesis.integration.project.generator.ProjectGeneratorHelper.addTarEntry;
import static io.syndesis.integration.project.generator.ProjectGeneratorHelper.compile;
import static io.syndesis.integration.project.generator.ProjectGeneratorHelper.resolveConnector;

@SuppressWarnings("PMD.ExcessiveImports")
public class ProjectGenerator implements IntegrationProjectGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectGenerator.class);

    private final ProjectGeneratorConfiguration configuration;
    private final IntegrationResourceManager resourceManager;
    private final Mustache applicationJavaMustache;
    private final Mustache applicationPropertiesMustache;
    private final Mustache pomMustache;

    public ProjectGenerator(ProjectGeneratorConfiguration configuration, IntegrationResourceManager resourceManager) throws IOException {
        this.configuration = configuration;
        this.resourceManager = resourceManager;

        MustacheFactory mf = new DefaultMustacheFactory();

        this.applicationJavaMustache = compile(mf, configuration, "Application.java.mustache", "Application.java");
        this.applicationPropertiesMustache = compile(mf, configuration, "application.properties.mustache", "application.properties");
        this.pomMustache = compile(mf, configuration, "pom.xml.mustache", "pom.xml");
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
    public Properties generateApplicationProperties(IntegrationDeployment deployment) {
        final Properties properties = new Properties();
        final List<? extends Step> steps = deployment.getSpec().getSteps();

        // ****************************************
        //
        // Connectors
        //
        // ****************************************

        steps.stream()
            .filter(step -> step.getStepKind().equals("endpoint"))
            .filter(step -> step.getAction().filter(ConnectorAction.class::isInstance).isPresent())
            .filter(step -> step.getConnection().isPresent())
            .forEach(step -> {
                final String index = step.getMetadata(Step.METADATA_STEP_INDEX).orElseThrow(() -> new IllegalArgumentException("Missing index for step:" + step));
                final Connection connection = step.getConnection().get();
                final ConnectorAction action = ConnectorAction.class.cast(step.getAction().get());
                final ConnectorDescriptor descriptor = action.getDescriptor();
                final Connector connector = resolveConnector(connection, resourceManager);

                if (connector.getComponentScheme().isPresent() || descriptor.getComponentScheme().isPresent()) {
                    // Grab the component scheme from the component descriptor or
                    // from the connector
                    final String componentScheme = Optionals.first(descriptor.getComponentScheme(), connector.getComponentScheme()).get();

                    Stream.of(connector, connection, step)
                        .filter(WithConfiguredProperties.class::isInstance)
                        .map(WithConfiguredProperties.class::cast)
                        .map(WithConfiguredProperties::getConfiguredProperties)
                        .flatMap(map -> map.entrySet().stream())
                        .filter(Predicates.or(connector::isSecret, action::isSecret))
                        .distinct()
                        .forEach(
                            e -> {
                                String key = String.format("%s-%s.%s", componentScheme, index, e.getKey());
                                String val = resourceManager.decrypt(e.getValue());

                                properties.put(key, val);
                            }
                        );
                } else {
                    // The component scheme is defined as camel connector prefix
                    // for 'old' style connectors.
                    final String componentScheme = descriptor.getCamelConnectorPrefix();

                    // endpoint secrets
                    Stream.of(connector, connection, step)
                        .filter(WithConfiguredProperties.class::isInstance)
                        .map(WithConfiguredProperties.class::cast)
                        .map(WithConfiguredProperties::getConfiguredProperties)
                        .flatMap(map -> map.entrySet().stream())
                        .filter(Predicates.or(connector::isEndpointProperty, action::isEndpointProperty))
                        .filter(Predicates.or(connector::isSecret, action::isSecret))
                        .distinct()
                        .forEach(
                            e -> {
                                String key = String.format("%s-%s.%s", componentScheme, index, e.getKey());
                                String val = resourceManager.decrypt(e.getValue());

                                properties.put(key, val);
                            }
                        );

                    // Component properties triggers connectors aliasing so we
                    // can have multiple instances of the same connectors
                    Stream.of(connector, connection, step)
                        .filter(WithConfiguredProperties.class::isInstance)
                        .map(WithConfiguredProperties.class::cast)
                        .map(WithConfiguredProperties::getConfiguredProperties)
                        .flatMap(map -> map.entrySet().stream())
                        .filter(Predicates.or(connector::isComponentProperty, action::isComponentProperty))
                        .distinct()
                        .forEach(
                            e -> {
                                String key = String.format("%s.configurations.%s-%s.%s", componentScheme, componentScheme, index, e.getKey());
                                String val = resourceManager.decrypt(e.getValue());

                                properties.put(key, val);
                            }
                        );
                }
            });

        return properties;
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
                configuration.getMavenProperties()),
            pomMustache
        );
    }

    private void addAdditionalResources(TarArchiveOutputStream tos) throws IOException {
        for (ProjectGeneratorConfiguration.Templates.Resource additionalResource : configuration.getTemplates().getAdditionalResources()) {
            String overridePath = configuration.getTemplates().getOverridePath();
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
                addTarEntry(tos, additionalResource.getDestination(), Files.readAllBytes(Paths.get(resource.toURI())));
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

                addTarEntry(tos, "src/main/java/io/syndesis/example/Application.java", ProjectGeneratorHelper.generate(deployment, applicationJavaMustache));
                addTarEntry(tos, "src/main/resources/application.properties", ProjectGeneratorHelper.generate(deployment, applicationPropertiesMustache));
                addTarEntry(tos, "src/main/resources/syndesis/integration/integration.json", Json.mapper().writerWithDefaultPrettyPrinter().writeValueAsBytes(deployment));
                addTarEntry(tos, "pom.xml", generatePom(deployment));
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

                            addTarEntry(tos, "src/main/resources/" + resource, mapping.getBytes(StandardCharsets.UTF_8));
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

    private void addExtensions(TarArchiveOutputStream tos, IntegrationDeployment deployment) throws IOException {
        final Set<String> extensions = resourceManager.collectDependencies(deployment).stream()
            .filter(Dependency::isExtension)
            .map(Dependency::getId)
            .collect(Collectors.toCollection(TreeSet::new));

        if (!extensions.isEmpty()) {
            addTarEntry(tos, "src/main/resources/loader.properties", generateExtensionLoader(extensions));

            for (String extensionId : extensions) {
                addTarEntry(
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
                    .map(id -> configuration.getSyndesisExtensionPath() + "/" + id + ".jar")
                    .collect(Collectors.joining(",")))
                .append('\n')
                .toString()
                .getBytes(StandardCharsets.UTF_8);
        }

        return new byte[] {};
    }
}
