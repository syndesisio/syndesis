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

import com.fasterxml.jackson.databind.ObjectWriter;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import io.syndesis.common.util.CollectionsUtils;
import io.syndesis.common.util.Json;
import io.syndesis.common.util.MavenProperties;
import io.syndesis.common.util.Names;
import io.syndesis.common.util.Optionals;
import io.syndesis.common.util.Predicates;
import io.syndesis.integration.api.IntegrationProjectGenerator;
import io.syndesis.integration.api.IntegrationResourceManager;
import io.syndesis.integration.project.generator.mvn.MavenGav;
import io.syndesis.integration.project.generator.mvn.PomContext;
import io.syndesis.common.model.Dependency;
import io.syndesis.common.model.WithConfiguredProperties;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.syndesis.integration.project.generator.ProjectGeneratorHelper.addResource;
import static io.syndesis.integration.project.generator.ProjectGeneratorHelper.addTarEntry;
import static io.syndesis.integration.project.generator.ProjectGeneratorHelper.compile;
import static io.syndesis.integration.project.generator.ProjectGeneratorHelper.mandatoryDecrypt;
import static io.syndesis.integration.project.generator.ProjectGeneratorHelper.sanitize;

@SuppressWarnings("PMD.ExcessiveImports")
public class ProjectGenerator implements IntegrationProjectGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectGenerator.class);

    private final ProjectGeneratorConfiguration configuration;
    private final IntegrationResourceManager resourceManager;
    private final Mustache applicationJavaMustache;
    private final Mustache applicationPropertiesMustache;
    private final Mustache pomMustache;
    private final MavenProperties mavenProperties;

    public ProjectGenerator(ProjectGeneratorConfiguration configuration, IntegrationResourceManager resourceManager, MavenProperties mavenProperties) throws IOException {
        this.configuration = configuration;
        this.resourceManager = resourceManager;
        this.mavenProperties = mavenProperties;

        MustacheFactory mf = new DefaultMustacheFactory();

        this.applicationJavaMustache = compile(mf, configuration, "Application.java.mustache", "Application.java");
        this.applicationPropertiesMustache = compile(mf, configuration, "application.properties.mustache", "application.properties");
        this.pomMustache = compile(mf, configuration, "pom.xml.mustache", "pom.xml");
    }

    @Override
    @SuppressWarnings("resource")
    public InputStream generate(final Integration integrationDefinition) throws IOException {
        final Integration integration = sanitize(integrationDefinition, resourceManager);
        final PipedInputStream is = new PipedInputStream();
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final PipedOutputStream os = new PipedOutputStream(is);

        executor.execute(generateAddProjectTarEntries(integration, os));

        return is;
    }

    @SuppressWarnings("PMD")
    @Override
    public Properties generateApplicationProperties(final Integration integrationDefinition) {
        final Integration integration = sanitize(integrationDefinition, resourceManager);
        final Properties properties = new Properties();
        final List<? extends Step> steps = integration.getSteps();

        for (int i = 0; i < steps.size(); i++) {
            final Step step = steps.get(i);

            // Check if a step is of supported type.
            if(StepKind.endpoint != step.getStepKind()) {
                continue;
            }

            // Check if a step has the required options
            if(step.getAction().filter(ConnectorAction.class::isInstance).isPresent() && step.getConnection().isPresent()) {
                final String index = Integer.toString(i + 1);
                final Connection connection = step.getConnection().get();
                final ConnectorAction action = ConnectorAction.class.cast(step.getAction().get());
                final ConnectorDescriptor descriptor = action.getDescriptor();
                final Connector connector = resourceManager.loadConnector(connection).orElseThrow(
                    () -> new IllegalArgumentException("No connector with id: " + connection.getConnectorId())
                );

                if (connector.getComponentScheme().isPresent() || descriptor.getComponentScheme().isPresent()) {
                    // Grab the component scheme from the component descriptor or
                    // from the connector
                    final String componentScheme = Optionals.first(descriptor.getComponentScheme(), connector.getComponentScheme()).get();
                    final Map<String, ConfigurationProperty> configurationProperties = CollectionsUtils.aggregate(connector.getProperties(), action.getProperties());

                    // Workaround for https://github.com/syndesisio/syndesis/issues/1713
                    for (Map.Entry<String, ConfigurationProperty> entry: configurationProperties.entrySet()) {
                        if (entry.getValue() != null && entry.getValue().getDefaultValue() != null && !entry.getValue().getDefaultValue().isEmpty()) {
                            if (connector.isSecret(entry.getKey()) || action.isSecret(entry.getKey())) {
                                addDecryptedKeyProperty(properties, index, componentScheme, entry.getKey(), entry.getValue().getDefaultValue());
                            }
                        }
                    }
                    for (Map.Entry<String, String> entry: connection.getConfiguredProperties().entrySet()) {
                        if (connector.isSecret(entry) || action.isSecret(entry)) {
                            addDecryptedKeyProperty(properties, index, componentScheme, entry.getKey(), entry.getValue());
                        }
                    }
                    for (Map.Entry<String, String> entry: step.getConfiguredProperties().entrySet()) {
                        if (connector.isSecret(entry) || action.isSecret(entry)) {
                            addDecryptedKeyProperty(properties, index, componentScheme, entry.getKey(), entry.getValue());
                        }
                    }
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
                        .forEach(
                            e -> {
                                addDecryptedKeyProperty(properties, index, componentScheme, e.getKey(), e.getValue());
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
                        .forEach(
                            e -> {
                                String propKeyPrefix = String.format("%s.configurations.%s", componentScheme, componentScheme);
                                addDecryptedKeyProperty(properties, index, propKeyPrefix, e.getKey(), e.getValue());
                            }
                        );

                }
            }
        }

        return properties;
    }

    @Override
    public byte[] generatePom(final Integration integration) throws IOException {
        final Set<MavenGav> dependencies = resourceManager.collectDependencies(integration).stream()
            .filter(Dependency::isMaven)
            .map(Dependency::getId)
            .map(MavenGav::new)
            .filter(ProjectGeneratorHelper::filterDefaultDependencies)
            .collect(Collectors.toCollection(TreeSet::new));

        return ProjectGeneratorHelper.generate(
            new PomContext(
                integration.getId().orElse(""),
                integration.getName(),
                integration.getDescription().orElse(null),
                dependencies,
                    mavenProperties),
            pomMustache
        );
    }

    @SuppressWarnings("PMD.ExcessiveParameterList")
    private void addDecryptedKeyProperty(Properties properties, String index, String propKeyPrefix, String propertyKey, String propertyVal) {
        String key = String.format("%s-%s.%s", propKeyPrefix, index, propertyKey);
        String val = mandatoryDecrypt(resourceManager, propertyKey, propertyVal);

        properties.put(key, val);
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
    private Runnable generateAddProjectTarEntries(Integration integration, OutputStream os) {
        return () -> {
            try (
                TarArchiveOutputStream tos = new TarArchiveOutputStream(os)) {
                tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);

                ObjectWriter writer = Json.writer();

                addTarEntry(tos, "src/main/java/io/syndesis/example/Application.java", ProjectGeneratorHelper.generate(integration, applicationJavaMustache));
                addTarEntry(tos, "src/main/resources/application.properties", ProjectGeneratorHelper.generate(integration, applicationPropertiesMustache));
                addTarEntry(tos, "src/main/resources/syndesis/integration/integration.json", writer.with(writer.getConfig().getDefaultPrettyPrinter()).writeValueAsBytes(integration));
                addTarEntry(tos, "pom.xml", generatePom(integration));

                addResource(tos, ".s2i/bin/assemble", "s2i/assemble");
                addExtensions(tos, integration);
                addResource(tos, "prometheus-config.yml", "templates/prometheus-config.yml");
                addResource(tos, "configuration/settings.xml", "templates/settings.xml");
                addAdditionalResources(tos);

                List<Step> steps = integration.getSteps();
                for (int i = 0; i < steps.size(); i++) {
                    Step step = steps.get(i);
                    if (StepKind.mapper == step.getStepKind()) {
                        final Map<String, String> properties = step.getConfiguredProperties();
                        final String mapping = properties.get("atlasmapping");

                        if (mapping != null) {
                            final String index = Integer.toString(i+1);
                            final String resource = "mapping-step-"  +index + ".json";

                            addTarEntry(tos, "src/main/resources/" + resource, mapping.getBytes(StandardCharsets.UTF_8));
                        }
                    }
                }

                LOGGER.info("Integration [{}]: Project files written to output stream", Names.sanitize(integration.getName()));
            } catch (IOException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error(String.format("Exception while creating runtime build tar for deployment %s : %s",
                            integration.getName(), e.toString()), e);
                }
            }
        };
    }

    private void addExtensions(TarArchiveOutputStream tos, Integration integration) throws IOException {
        final Set<String> extensions = resourceManager.collectDependencies(integration).stream()
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
