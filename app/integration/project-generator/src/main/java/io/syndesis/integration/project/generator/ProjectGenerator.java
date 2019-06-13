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
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import io.swagger.models.Swagger;
import io.syndesis.common.model.Dependency;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.ResourceIdentifier;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.model.openapi.OpenApi;
import io.syndesis.common.util.CollectionsUtils;
import io.syndesis.common.util.Json;
import io.syndesis.common.util.MavenProperties;
import io.syndesis.common.util.Names;
import io.syndesis.common.util.Optionals;
import io.syndesis.common.util.openapi.OpenApiHelper;
import io.syndesis.integration.api.IntegrationErrorHandler;
import io.syndesis.integration.api.IntegrationProjectGenerator;
import io.syndesis.integration.api.IntegrationResourceManager;
import io.syndesis.integration.project.generator.mvn.MavenGav;
import io.syndesis.integration.project.generator.mvn.PomContext;
import org.apache.camel.generator.swagger.RestDslGenerator;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.syndesis.integration.project.generator.ProjectGeneratorHelper.addResource;
import static io.syndesis.integration.project.generator.ProjectGeneratorHelper.addTarEntry;
import static io.syndesis.integration.project.generator.ProjectGeneratorHelper.compile;
import static io.syndesis.integration.project.generator.ProjectGeneratorHelper.mandatoryDecrypt;

@SuppressWarnings("PMD.ExcessiveImports")
public class ProjectGenerator implements IntegrationProjectGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectGenerator.class);

    private final ProjectGeneratorConfiguration configuration;
    private final IntegrationResourceManager resourceManager;
    private final Mustache applicationJavaMustache;
    private final Mustache applicationPropertiesMustache;
    private final Mustache restRoutesMustache;
    private final Mustache pomMustache;
    private final MavenProperties mavenProperties;

    public ProjectGenerator(ProjectGeneratorConfiguration configuration, IntegrationResourceManager resourceManager, MavenProperties mavenProperties) throws IOException {
        this.configuration = configuration;
        this.resourceManager = resourceManager;
        this.mavenProperties = mavenProperties;

        MustacheFactory mf = new DefaultMustacheFactory();

        this.applicationJavaMustache = compile(mf, configuration, "Application.java.mustache", "Application.java");
        this.applicationPropertiesMustache = compile(mf, configuration, "application.properties.mustache", "application.properties");
        this.restRoutesMustache = compile(mf, configuration, "RestRouteConfiguration.java.mustache", "RestRouteConfiguration.java");
        this.pomMustache = compile(mf, configuration, "pom.xml.mustache", "pom.xml");
    }

    @Override
    @SuppressWarnings("resource")
    public InputStream generate(final Integration integrationDefinition, IntegrationErrorHandler errorHandler) throws IOException {
        final Integration integration = resourceManager.sanitize(integrationDefinition);
        final PipedInputStream is = new PipedInputStream();
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final PipedOutputStream os = new PipedOutputStream(is);

        executor.execute(generateAddProjectTarEntries(integration, os, errorHandler));

        return is;
    }

    @SuppressWarnings("PMD")
    @Override
    public Properties generateApplicationProperties(final Integration integrationDefinition) {
        final Integration integration = resourceManager.sanitize(integrationDefinition);
        final Properties properties = new Properties();

        properties.putAll(integration.getConfiguredProperties());

        final List<Flow> flows = integration.getFlows();
        for (int f = 0; f < flows.size(); f++) {
            final Flow flow = flows.get(f);
            final int flowIndex = f;
            final List<Step> steps = flow.getSteps();

            for (int s = 0; s < steps.size(); s++) {
                final Step step = steps.get(s);
                final int stepIndex = s;

                // Check if a step is of supported type.
                if(StepKind.endpoint != step.getStepKind()) {
                    continue;
                }

                // Check if a step has the required options
                if(step.getAction().filter(ConnectorAction.class::isInstance).isPresent() && step.getConnection().isPresent()) {
                    final Connection connection = step.getConnection().get();
                    final ConnectorAction action = (ConnectorAction) step.getAction().get();
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
                                    addDecryptedKeyProperty(properties, flowIndex, stepIndex, componentScheme, entry.getKey(), entry.getValue().getDefaultValue());
                                }
                            }
                        }
                        for (Map.Entry<String, String> entry: connection.getConfiguredProperties().entrySet()) {
                            if (connector.isSecret(entry) || action.isSecret(entry)) {
                                addDecryptedKeyProperty(properties, flowIndex, stepIndex, componentScheme, entry.getKey(), entry.getValue());
                            }
                        }
                        for (Map.Entry<String, String> entry: step.getConfiguredProperties().entrySet()) {
                            if (connector.isSecret(entry) || action.isSecret(entry)) {
                                addDecryptedKeyProperty(properties, flowIndex, stepIndex, componentScheme, entry.getKey(), entry.getValue());
                            }
                        }
                    } else {
                        throw new UnsupportedOperationException("Old style of connectors from camel-connector are not supported anymore, please be sure that integration json satisfy connector.getComponentScheme().isPresent() || descriptor.getComponentScheme().isPresent()");
                    }
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
    private void addDecryptedKeyProperty(Properties properties, Integer flowIndex, int stepIndex, String propKeyPrefix, String propertyKey, String propertyVal) {
        String key = String.format("flow-%d.%s-%d.%s", flowIndex, propKeyPrefix, stepIndex, propertyKey);
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
                    String.format("Unable to find the required additional resource (overridePath=%s, source=%s)"
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

    public static class Scope {
        public ProjectGeneratorConfiguration configuration;
        public Integration integration;

        public Scope(ProjectGeneratorConfiguration configuration, Integration integration) {
            this.configuration = configuration;
            this.integration = integration;
        }

        public ProjectGeneratorConfiguration getConfiguration() {
            return configuration;
        }

        public Integration getIntegration() {
            return integration;
        }
    }

    @SuppressWarnings("PMD.DoNotUseThreads")
    private Runnable generateAddProjectTarEntries(Integration integration, OutputStream os, IntegrationErrorHandler errorHandler) {
        return () -> {
            try (
                TarArchiveOutputStream tos = new TarArchiveOutputStream(os)) {
                tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);

                ObjectWriter writer = Json.writer();

                addTarEntry(tos, "src/main/java/io/syndesis/example/Application.java", ProjectGeneratorHelper.generate(integration, applicationJavaMustache));
                Scope scope = new Scope(configuration, integration);
                addTarEntry(tos, "src/main/resources/application.properties", ProjectGeneratorHelper.generate(scope, applicationPropertiesMustache));
                addTarEntry(tos, "src/main/resources/syndesis/integration/integration.json", writer.with(writer.getConfig().getDefaultPrettyPrinter()).writeValueAsBytes(integration));
                addTarEntry(tos, "pom.xml", generatePom(integration));

                addExtensions(tos, integration);
                addMappingRules(tos, integration);
                addRestDefinition(tos, integration);

                addResource(tos, ".s2i/bin/assemble", "s2i/assemble");
                addResource(tos, "prometheus-config.yml", "templates/prometheus-config.yml");
                addResource(tos, "configuration/settings.xml", "templates/settings.xml");

                addAdditionalResources(tos);

                LOGGER.info("Integration [{}]: Project files written to output stream", Names.sanitize(integration.getName()));
            } catch (Exception e) {
                errorHandler.accept(e);
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Exception while creating runtime build tar for deployment {} : {}", integration.getName(), e.toString(), e);
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

    private void addMappingRules(TarArchiveOutputStream tos, Integration integration) throws IOException {
        final List<Flow> flows = integration.getFlows();
        for (int f = 0; f < flows.size(); f++) {
            final Flow flow = flows.get(f);
            final List<Step> steps = flow.getSteps();

            for (int s = 0; s < steps.size(); s++) {
                final Step step = steps.get(s);

                if (StepKind.mapper == step.getStepKind()) {
                    final Map<String, String> properties = step.getConfiguredProperties();
                    final String mapping = properties.get("atlasmapping");

                    if (mapping != null) {
                        final String resource = "mapping-flow-" + f + "-step-"  + s + ".json";
                        addTarEntry(tos, "src/main/resources/" + resource, mapping.getBytes(StandardCharsets.UTF_8));
                    } else {
                        throw new IllegalStateException("Missing configured property for data mapper mapping definition");
                    }
                }
            }
        }
    }

    private void addRestDefinition(TarArchiveOutputStream tos, Integration integration) throws IOException {
        // assuming that we have a single swagger definition for the moment
        Optional<ResourceIdentifier> rid = integration.getResources().stream().filter(Kind.OpenApi::sameAs).findFirst();
        if (!rid.isPresent()) {
            return;
        }

        final ResourceIdentifier openApiResource = rid.get();
        final Optional<String> maybeOpenApiResourceId = openApiResource.getId();
        if (!maybeOpenApiResourceId.isPresent()) {
            return;
        }

        final String openApiResourceId = maybeOpenApiResourceId.get();
        Optional<OpenApi> res = resourceManager.loadOpenApiDefinition(openApiResourceId);
        if (!res.isPresent()) {
            return;
        }

        final StringBuilder code = new StringBuilder();
        final byte[] openApiBytes = res.get().getDocument();
        final Swagger swagger = OpenApiHelper.parse(new String(openApiBytes, StandardCharsets.UTF_8));

        RestDslGenerator.toAppendable(ProjectGeneratorHelper.normalizePaths(swagger))
            .withClassName("RestRoute")
            .withPackageName("io.syndesis.example")
            .withoutSourceCodeTimestamps()
            .generate(code);

        addTarEntry(tos, "src/main/java/io/syndesis/example/RestRoute.java", code.toString().getBytes(StandardCharsets.UTF_8));
        addTarEntry(tos, "src/main/java/io/syndesis/example/RestRouteConfiguration.java", ProjectGeneratorHelper.generate(integration, restRoutesMustache));
        addTarEntry(tos, "src/main/resources/openapi.json", openApiBytes);
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
