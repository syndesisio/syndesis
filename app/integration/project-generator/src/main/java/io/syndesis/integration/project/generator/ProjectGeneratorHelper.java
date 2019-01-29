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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.camel.util.ObjectHelper;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Scheduler;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.step.template.TemplateStepLanguage;
import io.syndesis.integration.api.IntegrationResourceManager;
import io.syndesis.integration.project.generator.mvn.MavenGav;

@SuppressWarnings("PMD.CyclomaticComplexity")
public final class ProjectGeneratorHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectGeneratorHelper.class);

    private ProjectGeneratorHelper() {
    }

    public static void addResource(TarArchiveOutputStream tos, String destination, String resource) throws IOException {
        final URL url = ProjectGeneratorHelper.class.getResource(resource);
        final byte[] bytes = IOUtils.toByteArray(url);

        addTarEntry(tos, destination, bytes);
    }

    public static void addTarEntry(TarArchiveOutputStream tos, String path, byte[] content) throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry(path);
        entry.setSize(content.length);
        tos.putArchiveEntry(entry);
        tos.write(content);
        tos.closeArchiveEntry();
    }

    public static byte[] generate(Object scope, Mustache template) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        template.execute(new OutputStreamWriter(bos, StandardCharsets.UTF_8), scope).flush();
        return bos.toByteArray();
    }

    public static boolean filterDefaultDependencies(MavenGav gav) {
        boolean answer = true;

        if ("org.springframework.boot".equals(gav.getGroupId())) {
            if ("spring-boot-starter-web".equals(gav.getArtifactId())) {
                answer = false;
            } if ("spring-boot-starter-undertow".equals(gav.getArtifactId())) {
                answer = false;
            } else if ("spring-boot-starter-actuator".equals(gav.getArtifactId())) {
                answer = false;
            }
        }

        if ("org.apache.camel".equals(gav.getGroupId()) && "camel-spring-boot-starter".equals(gav.getArtifactId())) {
            answer = false;
        }

        if ("io.syndesis".equals(gav.getGroupId()) && "integration-runtime".equals(gav.getArtifactId())) {
            answer = false;
        }

        if (!answer) {
            LOGGER.debug("Dependency: {} filtered", gav);
        }

        return answer;
    }

    public static Mustache compile(MustacheFactory mustacheFactory, ProjectGeneratorConfiguration generatorProperties, String template, String name) throws IOException {
        String overridePath = generatorProperties.getTemplates().getOverridePath();
        URL resource = null;

        if (!StringUtils.isEmpty(overridePath)) {
            resource = ProjectGeneratorHelper.class.getResource("templates/" + overridePath + "/" + template);
        }
        if (resource == null) {
            resource = ProjectGeneratorHelper.class.getResource("templates/" + template);
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
            return mustacheFactory.compile(new InputStreamReader(stream, StandardCharsets.UTF_8), name);
        }
    }

    public static String mandatoryDecrypt(IntegrationResourceManager manager, String propertyKey, String propertyVal) {
        String answer = propertyKey;

        if (propertyVal != null) {
            answer = manager.decrypt(propertyVal);
            if (answer == null) {
                throw new IllegalArgumentException("Unable to decrypt key:" + propertyKey);
            }
        }

        return answer;
    }

    public static Integration sanitize(Integration integration, IntegrationResourceManager resourceManager) {
        if (integration.getFlows().isEmpty()) {
            return integration;
        }

        final List<Flow> replacementFlows = new ArrayList<>(integration.getFlows());
        final ListIterator<Flow> flows = replacementFlows.listIterator();
        while (flows.hasNext()) {
            final Flow flow = flows.next();
            if (flow.getSteps().isEmpty()) {
                continue;
            }

            final List<Step> replacementSteps = new ArrayList<>(flow.getSteps());
            final ListIterator<Step> steps = replacementSteps.listIterator();

            while (steps.hasNext()) {
                final Step source = steps.next();

                Step replacement = source;
                if (source.getConnection().isPresent()) {
                    final Connection connection = source.getConnection().get();

                    // If connector is not set, fetch it from data source and update connection
                    if (!connection.getConnector().isPresent()) {
                        Connector connector = resourceManager.loadConnector(connection.getConnectorId()).orElseThrow(
                            () -> new IllegalArgumentException("Unable to fetch connector: " + connection.getConnectorId())
                        );

                        // Add missing connector to connection.
                        Connection newConnection = new Connection.Builder()
                            .createFrom(connection)
                            .connector(connector)
                            .build();

                        // Replace with the new 'sanitized' step
                        replacement =
                            new Step.Builder()
                                .createFrom(source)
                                .connection(newConnection)
                                .build();
                    }
                }

                //
                // If a template step then update it to ensure it
                // has the correct dependencies
                //
                steps.set(TemplateStepLanguage.updateStep(replacement));
            }

            final Flow.Builder replacementFlowBuilder = flow.builder().createFrom(flow).steps(replacementSteps);
            flows.set(replacementFlowBuilder.build());

            // Temporary implementation until https://github.com/syndesisio/syndesis/issues/736
            // is fully implemented and schedule options are set on integration.
            if (!flow.getScheduler().isPresent()) {
                final Step firstStep = replacementSteps.get(0);
                final Map<String, String> properties = new HashMap<>(firstStep.getConfiguredProperties());
                String type = properties.remove("schedulerType");
                final String expr = properties.remove("schedulerExpression");

                if (StringUtils.isNotEmpty(expr)) {
                    if (StringUtils.isEmpty(type)) {
                        type = "timer";
                    }

                    final Scheduler scheduler = new Scheduler.Builder().type(Scheduler.Type.valueOf(type)).expression(expr).build();
                    final Flow replacementFlow = replacementFlowBuilder.scheduler(scheduler).build();

                    flows.set(replacementFlow);
                }

                // Replace first step so underlying connector won't fail uri param
                // validation if schedule options were set.
                steps.set(
                    new Step.Builder()
                        .createFrom(firstStep)
                        .configuredProperties(properties)
                        .build()
                );
            }
        }

        return new Integration.Builder().createFrom(integration)
            .flows(replacementFlows)
            .build();
    }

    /**
     * The Swager to REST DSL generator in Camel doesn't properly handle
     * basePaths, so here we set the basePath to null and prefix all
     * paths with the basePath. See CAMEL-12893.
     */
    public static Swagger normalizePaths(final Swagger swagger) {
        final String basePath = swagger.getBasePath();
        if (ObjectHelper.isEmpty(basePath)) {
            return swagger;
        }

        swagger.basePath(null);

        final Map<String, Path> normalized = new LinkedHashMap<>();
        for (Entry<String, Path> given : swagger.getPaths().entrySet()) {
            normalized.put(basePath + given.getKey(), given.getValue());
        }
        swagger.paths(normalized);

        return swagger;
    }
}
