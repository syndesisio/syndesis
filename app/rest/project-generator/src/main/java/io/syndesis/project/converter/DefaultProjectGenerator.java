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
package io.syndesis.project.converter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import io.syndesis.core.Names;
import io.syndesis.dao.extension.ExtensionDataManager;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.integration.model.Flow;
import io.syndesis.integration.model.SyndesisModel;
import io.syndesis.integration.model.YamlHelpers;
import io.syndesis.integration.support.Strings;
import io.syndesis.model.Dependency;
import io.syndesis.model.WithDependencies;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.extension.Extension;
import io.syndesis.model.integration.IntegrationDeployment;
import io.syndesis.model.integration.IntegrationDeploymentSpec;
import io.syndesis.model.integration.Step;
import io.syndesis.project.converter.ProjectGeneratorProperties.Templates;
import io.syndesis.project.converter.mvn.MavenGav;
import io.syndesis.project.converter.mvn.PomContext;
import io.syndesis.project.converter.visitor.GeneratorContext;
import io.syndesis.project.converter.visitor.StepVisitor;
import io.syndesis.project.converter.visitor.StepVisitorContext;
import io.syndesis.project.converter.visitor.StepVisitorFactory;
import io.syndesis.project.converter.visitor.StepVisitorFactoryRegistry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultProjectGenerator implements ProjectGenerator {
    private static final ObjectMapper YAML_OBJECT_MAPPER = YamlHelpers.createObjectMapper();

    private final MustacheFactory mf = new DefaultMustacheFactory();
    private final ProjectGeneratorProperties generatorProperties;
    private final StepVisitorFactoryRegistry registry;
    private final DataManager dataManager;
    private final ExtensionDataManager extensionDataManager;
    private final Mustache applicationJavaMustache;
    private final Mustache applicationPropertiesMustache;
    private final Mustache pomMustache;

    private static final Logger LOG = LoggerFactory.getLogger(DefaultProjectGenerator.class);

    public DefaultProjectGenerator(
            ProjectGeneratorProperties generatorProperties,
            StepVisitorFactoryRegistry registry,
            DataManager dataManager,
            ExtensionDataManager extensionDataManager) throws IOException {

        this.generatorProperties = generatorProperties;
        this.registry = registry;
        this.dataManager = dataManager;
        this.extensionDataManager = extensionDataManager;
        this.applicationJavaMustache = compile(generatorProperties, "Application.java.mustache", "Application.java");
        this.applicationPropertiesMustache = compile(generatorProperties, "application.properties.mustache", "application.properties");
        this.pomMustache = compile(generatorProperties, "pom.xml.mustache", "pom.xml");
    }

    private Mustache compile(ProjectGeneratorProperties generatorProperties, String template, String name) throws IOException {
        String overridePath = generatorProperties.getTemplates().getOverridePath();
        URL resource = null;

        if (!Strings.isEmpty(overridePath)) {
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

    @SuppressWarnings("resource")
    @Override
    public InputStream generate(IntegrationDeployment integrationDeployment) throws IOException {
        final PipedInputStream is = new PipedInputStream();
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final PipedOutputStream os = new PipedOutputStream(is);

        executor.execute(generateAddProjectTarEntries(integrationDeployment, os));

        return is;
    }

    private void addAdditionalResources(TarArchiveOutputStream tos) throws IOException {
        for (Templates.Resource additionalResource : generatorProperties.getTemplates().getAdditionalResources()) {
            String overridePath = generatorProperties.getTemplates().getOverridePath();
            URL resource = null;

            if (!Strings.isEmpty(overridePath)) {
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
    private Runnable generateAddProjectTarEntries(IntegrationDeployment integrationDeployment, OutputStream os) {
        return () -> {
            try (
                TarArchiveOutputStream tos = new TarArchiveOutputStream(os)) {
                tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);

                addTarEntry(tos, "src/main/java/io/syndesis/example/Application.java", generate(integrationDeployment, applicationJavaMustache));
                addTarEntry(tos, "src/main/resources/application.properties", generate(integrationDeployment, applicationPropertiesMustache));
                addTarEntry(tos, "src/main/resources/syndesis.yml", generateFlow(tos, integrationDeployment.getSpec()));
                addTarEntry(tos, "pom.xml", generatePom(integrationDeployment));
                addResource(tos, ".s2i/bin/assemble", "s2i/assemble");
                addExtensions(tos, integrationDeployment);
                addAdditionalResources(tos);
                LOG.info("Integration [{}]: Project files written to output stream",Names.sanitize(integrationDeployment.getSpec().getName()));
            } catch (IOException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(String.format("Exception while creating runtime build tar for integration %s : %s",
                        integrationDeployment.getSpec().getName(), e.toString()), e);
                }
            }
        };
    }

    private void addTarEntry(TarArchiveOutputStream tos, String path, byte[] content) throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry(path);
        entry.setSize(content.length);
        tos.putArchiveEntry(entry);
        tos.write(content);
        tos.closeArchiveEntry();
    }

    private void addResource(TarArchiveOutputStream tos, String destination, String resource) throws IOException {
        final URL url = getClass().getResource(resource);
        final byte[] bytes = IOUtils.toByteArray(url);

        addTarEntry(tos, destination, bytes);
    }

    private void addExtensions(TarArchiveOutputStream tos, IntegrationDeployment integrationDeployment) throws IOException {
        final Set<String> extensions = collectDependencies(integrationDeployment.getSpec()).stream()
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
                        extensionDataManager.getExtensionBinaryFile(extensionId)
                    )
                );
            }
        }
    }

    @Override
    public byte[] generatePom( IntegrationDeployment integrationDeployment) throws IOException {
        final Set<MavenGav> dependencies = collectDependencies(integrationDeployment.getSpec()).stream()
            .filter(Dependency::isMaven)
            .map(Dependency::getId)
            .map(MavenGav::new)
            .collect(Collectors.toCollection(TreeSet::new));

        return generate(
            new PomContext(
                integrationDeployment.getIntegrationId().orElse(""),
                integrationDeployment.getSpec().getName(),
                integrationDeployment.getSpec().getDescription().orElse(null),
                dependencies,
                generatorProperties.getMavenProperties()),
            pomMustache
        );
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

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private byte[] generateFlow(TarArchiveOutputStream tos, IntegrationDeploymentSpec spec) throws JsonProcessingException {
        final List<? extends Step> steps =  spec.getSteps();
        final Flow flow = new Flow();

        if (!steps.isEmpty()) {
            Queue<Step> remaining = new ArrayDeque<>(steps);
            Step first = remaining.remove();
            if (first != null) {
                StepVisitorContext stepContext = new StepVisitorContext.Builder()
                    .generatorContext(new GeneratorContext.Builder()
                        .generatorProperties(generatorProperties)
                        //TODO: Check if we really need that.
                        //.integration(integration)
                        .tarArchiveOutputStream(tos)
                        .flow(flow)
                        .visitorFactoryRegistry(registry)
                        .dataManager(dataManager)
                        .extensionDataManager(extensionDataManager)
                        .build())
                    .index(1)
                    .step(first)
                    .remaining(remaining)
                    .build();

                visitStep(stepContext);
            }
        }

        SyndesisModel syndesisModel = new SyndesisModel();
        syndesisModel.addFlow(flow);

        return YAML_OBJECT_MAPPER.writeValueAsBytes(syndesisModel);
    }

    private void visitStep(StepVisitorContext stepContext) {
        StepVisitorFactory<?> factory = registry.get(stepContext.getStep().getStepKind());
        StepVisitor visitor = factory.create();
        Collection<io.syndesis.integration.model.steps.Step> steps = visitor.visit(stepContext);

        if (steps != null) {
            steps.forEach(stepContext.getGeneratorContext().getFlow()::addStep);
        }
        if (stepContext.hasNext()) {
             visitStep(stepContext.next());
        }
    }

    private byte[] generate(Object scope, Mustache template) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        template.execute(new OutputStreamWriter(bos, StandardCharsets.UTF_8), scope).flush();
        return bos.toByteArray();
    }


    private Collection<Dependency> collectDependencies(IntegrationDeploymentSpec spec) {
        return collectDependencies(dataManager, spec);
    }

    public static Collection<Dependency> collectDependencies(DataManager dataManager, Collection<IntegrationDeployment> deployments) {
        if (deployments == null) {
            return Collections.emptyList();
        }

        return deployments.stream().
            flatMap(d ->  collectDependencies(dataManager, d.getSpec()).stream())
            .collect(Collectors.toList());
    }

    public static Collection<Dependency> collectDependencies(DataManager dataManager, IntegrationDeploymentSpec spec) {
        List<Dependency> dependencies = new ArrayList<>();

        for (Step step : spec.getSteps()) {
            step.getAction()
                .filter(WithDependencies.class::isInstance)
                .map(WithDependencies.class::cast)
                .map(WithDependencies::getDependencies)
                .ifPresent(dependencies::addAll);

            step.getConnection()
                .filter(c -> c.getConnector().isPresent())
                .map(c -> c.getConnector().get())
                .map(WithDependencies::getDependencies)
                .ifPresent(dependencies::addAll);

            step.getConnection()
                .filter(c -> Objects.nonNull(dataManager))
                .filter(c -> !c.getConnector().isPresent())
                .filter(c -> c.getConnectorId().isPresent())
                .map(c -> c.getConnectorId().get())
                .map(c -> dataManager.fetch(Connector.class, c))
                .filter(Objects::nonNull)
                .map(WithDependencies::getDependencies)
                .ifPresent(dependencies::addAll);

            step.getExtension()
                .map(WithDependencies::getDependencies)
                .ifPresent(dependencies::addAll);

            step.getExtension()
                .map(Extension::getExtensionId)
                .map(Dependency::extension)
                .ifPresent(dependencies::add);
        }

        return dependencies;
    }
}
