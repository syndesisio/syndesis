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
package io.syndesis.project.converter;

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
import io.syndesis.model.action.ConnectorAction;
import io.syndesis.model.extension.Extension;
import io.syndesis.model.integration.Integration;
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class DefaultProjectGenerator implements ProjectGenerator {
    private static final ObjectMapper YAML_OBJECT_MAPPER = YamlHelpers.createObjectMapper();

    private final MustacheFactory mf = new DefaultMustacheFactory();
    private final ProjectGeneratorProperties generatorProperties;
    private final StepVisitorFactoryRegistry registry;
    private final DataManager dataManager;
    private final Optional<ExtensionDataManager> extensionDataManager;
    private final Mustache applicationJavaMustache;
    private final Mustache applicationPropertiesMustache;
    private final Mustache pomMustache;

    private static final Logger LOG = LoggerFactory.getLogger(DefaultProjectGenerator.class);

    public DefaultProjectGenerator(
            ProjectGeneratorProperties generatorProperties,
            StepVisitorFactoryRegistry registry,
            DataManager dataManager,
            Optional<ExtensionDataManager> extensionDataManager) throws IOException {

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

    @Override
    public InputStream generate(Integration integration) throws IOException {
        final PipedInputStream is = new PipedInputStream();
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final PipedOutputStream os = new PipedOutputStream(is);

        executor.execute(generateAddProjectTarEntries(integration, os));

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
    private Runnable generateAddProjectTarEntries(Integration integration, OutputStream os) {
        return () -> {
            try (
                TarArchiveOutputStream tos = new TarArchiveOutputStream(os)) {
                tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);

                addTarEntry(tos, "src/main/java/io/syndesis/example/Application.java", generate(integration, applicationJavaMustache));
                addTarEntry(tos, "src/main/resources/application.properties", generate(integration, applicationPropertiesMustache));
                addTarEntry(tos, "src/main/resources/syndesis.yml", generateFlow(tos, integration));
                addTarEntry(tos, "pom.xml", generatePom(integration));
                addResource(tos, ".s2i/bin/assemble", "s2i/assemble");

                List<Extension> extensions = integration.getSteps().stream().map(Step::getExtension).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
                if (!extensions.isEmpty() && extensionDataManager.isPresent()) {
                    addTarEntry(tos, "src/main/resources/loader.properties", generateExtensionLoader(integration));

                    for (Extension extension: extensions) {
                        addTarEntry(
                            tos,
                            "extensions/" + Names.sanitize(extension.getExtensionId()) + ".jar",
                            IOUtils.toByteArray(
                                extensionDataManager.get().getExtensionBinaryFile(extension.getExtensionId())
                            )
                        );
                    }
                }

                addAdditionalResources(tos);
                LOG.info("Integration [{}]: Project files written to output stream",Names.sanitize(integration.getName()));
            } catch (IOException|URISyntaxException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(String.format("Exception while creating runtime build tar for integration %s : %s",
                        integration.getName(), e.toString()), e);
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

    private void addResource(TarArchiveOutputStream tos, String destination, String resource) throws IOException, URISyntaxException {
        final URL url = getClass().getResource(resource);
        final byte[] bytes = IOUtils.toByteArray(url);

        addTarEntry(tos, destination, bytes);
    }

    @Override
    public byte[] generatePom(Integration integration) throws IOException {
        final Set<MavenGav> connectors = new LinkedHashSet<>();
        final Set<MavenGav> extensions = new LinkedHashSet<>();

        for (Step step : integration.getSteps()) {
            if (step.getStepKind().equals(io.syndesis.integration.model.steps.Endpoint.KIND)) {
                step.getAction()
                    .filter(ConnectorAction.class::isInstance)
                    .map(ConnectorAction.class::cast)
                    .map(action -> action.getDescriptor().getCamelConnectorGAV())
                    .map(MavenGav::new)
                    .ifPresent(connectors::add);
            }
            if (step.getStepKind().equals(io.syndesis.integration.model.steps.Extension.KIND)) {
                step.getExtension()
                    .map(Extension::getDependencies)
                    .orElseGet(Collections::emptySortedSet)
                    .stream()
                        .map(MavenGav::new)
                        .forEach(extensions::add);
            }
        }

        return generate(
            new PomContext(
                integration.getId().orElse(""),
                integration.getName(),
                integration.getDescription().orElse(null),
                connectors,
                extensions,
                generatorProperties.getMavenProperties()),
            pomMustache
        );
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private byte[] generateExtensionLoader(Integration integration) {
        final Set<String> extensions = new LinkedHashSet<>();

        for (Step step : integration.getSteps()) {
            if (step.getStepKind().equals(io.syndesis.integration.model.steps.Extension.KIND)) {
                step.getExtension()
                    .map(Extension::getExtensionId)
                    .map(Names::sanitize)
                    .map(id -> generatorProperties.getSyndesisExtensionPath() + "/" + id + ".jar")
                    .ifPresent(extensions::add);
            }
        }

        if (!extensions.isEmpty()) {
            return new StringBuilder()
                .append("loader.path").append('=').append(String.join(",", extensions)).append('\n')
                .toString()
                    .getBytes(StandardCharsets.UTF_8);
        }

        return new byte[] {};
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private byte[] generateFlow(TarArchiveOutputStream tos, Integration integration) throws JsonProcessingException {
        final Map<Step, String> connectorIdMap = new HashMap<>();
        final List<? extends Step> steps =  integration.getSteps();
        final Flow flow = new Flow();

        if (!steps.isEmpty()) {
            // Determine connector prefix
            integration.getSteps().stream()
                .filter(s -> s.getStepKind().equals(io.syndesis.integration.model.steps.Endpoint.KIND))
                .filter(s -> s.getAction().filter(ConnectorAction.class::isInstance).isPresent())
                .filter(s -> s.getConnection().isPresent())
                .collect(Collectors.groupingBy(s -> s.getAction().map(ConnectorAction.class::cast).get().getDescriptor().getCamelConnectorPrefix()))
                .forEach(
                    (prefix, stepList) -> {
                        if (stepList.size() > 1) {
                            for (int i = 0; i < stepList.size(); i++) {
                                connectorIdMap.put(stepList.get(i), Integer.toString(i + 1));
                            }
                        }
                    }
                );

            Queue<Step> remaining = new ArrayDeque<>(steps);
            Step first = remaining.remove();
            if (first != null) {
                StepVisitorContext stepContext = new StepVisitorContext.Builder()
                    .generatorContext(new GeneratorContext.Builder()
                        .generatorProperties(generatorProperties)
                        .integration(integration)
                        .tarArchiveOutputStream(tos)
                        .flow(flow)
                        .visitorFactoryRegistry(registry)
                        .dataManager(dataManager)
                        .extensionDataManager(extensionDataManager)
                        .build())
                    .index(1)
                    .step(first)
                    .remaining(remaining)
                    .connectorIdSupplier(step -> Optional.ofNullable(connectorIdMap.get(step)))
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
}
