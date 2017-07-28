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
import io.syndesis.connector.catalog.ConnectorCatalog;
import io.syndesis.integration.model.Flow;
import io.syndesis.integration.model.SyndesisModel;
import io.syndesis.integration.model.steps.Endpoint;
import io.syndesis.integration.support.YamlHelper;
import io.syndesis.model.integration.Integration;
import io.syndesis.model.integration.Step;
import io.syndesis.project.converter.visitor.GeneratorContext;
import io.syndesis.project.converter.visitor.StepVisitor;
import io.syndesis.project.converter.visitor.StepVisitorContext;
import io.syndesis.project.converter.visitor.StepVisitorFactory;
import io.syndesis.project.converter.visitor.StepVisitorFactoryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

public class DefaultProjectGenerator implements ProjectGenerator {

    private static final ObjectMapper YAML_OBJECT_MAPPER = YamlHelper.createYamlMapper();

    private MustacheFactory mf = new DefaultMustacheFactory();

    private Mustache readmeMustache = mf.compile(
        new InputStreamReader(getClass().getResourceAsStream("templates/README.md.mustache"), UTF_8),
        "README.md"
    );
    private Mustache applicationJavaMustache = mf.compile(
        new InputStreamReader(getClass().getResourceAsStream("templates/Application.java.mustache"), UTF_8),
        "Application.java"
    );

    private Mustache applicationYmlMustache = mf.compile(
        new InputStreamReader(getClass().getResourceAsStream("templates/application.properties.mustache"), UTF_8),
        "application.properties"
    );

    private Mustache pomMustache = mf.compile(
        new InputStreamReader(getClass().getResourceAsStream("templates/pom.xml.mustache"), UTF_8),
        "pom.xml"
    );

    /**
     * Not required for the moment, needed for local forking of a maven process
    private Mustache connectorPomMustache = mf.compile(
        new InputStreamReader(getClass().getResourceAsStream("templates/connector/pom.xml.mustache")),
        "pom.xml"
    );
    */
    private final ConnectorCatalog connectorCatalog;
    private final ProjectGeneratorProperties generatorProperties;
    private final StepVisitorFactoryRegistry registry;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public DefaultProjectGenerator(ConnectorCatalog connectorCatalog, ProjectGeneratorProperties generatorProperties, StepVisitorFactoryRegistry registry) {
        this.connectorCatalog = connectorCatalog;
        this.generatorProperties = generatorProperties;
        this.registry = registry;
    }

    @Override
    public Map<String, byte[]> generate(GenerateProjectRequest request) throws IOException {
        request.getIntegration().getSteps().ifPresent(steps -> {
            for (Step step : steps) {
                log.info("Integration {} : Adding step {} ",
                         request.getIntegration().getId().orElse("[none]"),
                         step.getId().orElse(""));
                step.getAction().ifPresent(action -> connectorCatalog.addConnector(action.getCamelConnectorGAV()));
            }
        });

        Map<String, byte[]> contents = new HashMap<>();
        contents.put("README.md", generateFromRequest(request, readmeMustache));
        contents.put("src/main/java/io/syndesis/example/Application.java", generateFromRequest(request, applicationJavaMustache));
        contents.put("src/main/resources/application.properties", generateFromRequest(request, applicationYmlMustache));
        contents.put("src/main/resources/syndesis.yml", generateFlowYaml(contents, request));
        contents.put("pom.xml", generatePom(request.getIntegration()));

        return contents;
    }

    public byte[] generatePom(Integration integration) throws IOException {
        Set<MavenGav> connectors = new LinkedHashSet<>();
        integration.getSteps().ifPresent(steps -> {
            for (Step step : steps) {
                if (step.getStepKind().equals(Endpoint.KIND)) {
                    step.getAction().ifPresent(action -> {
                        String[] splitGav = action.getCamelConnectorGAV().split(":");
                        if (splitGav.length == 3) {
                            connectors.add(new MavenGav(splitGav[0], splitGav[1], splitGav[2]));
                        }
                    });
                }
            }
        });
        return generateFromPomContext(new PomContext(integration.getId().orElse(""), integration.getName(), integration.getDescription().orElse(null), connectors), pomMustache);
    }


    /*
    Required for a local verifier, but does not work because the connector does not carry any GAV (but a
    reference to a 'default' action. Or the mapping happens in the external syndesis-verifier service:

    public byte[] generatePom(Connector connector) throws IOException {
        Set<MavenGav> connectors = new LinkedHashSet<>();
        String[] splitGav = connector.getCamelConnectorGAV().get().split(":");
        if (splitGav.length == 3) {
            connectors.add(new MavenGav(splitGav[0], splitGav[1], splitGav[2]));
        }
        return generate(
            new IntegrationForPom(connector.getId().get(), connector.getName(), null, connectors),
            connectorPomMustache
        );
    }
    */

    private byte[] generateFlowYaml(Map<String, byte[]> contents, GenerateProjectRequest request) throws JsonProcessingException {
        Flow flow = new Flow();
        request.getIntegration().getSteps().ifPresent(steps -> {
            if (steps.isEmpty()) {
                return;
            }

            Queue<Step> remaining = new LinkedList<>(steps);
            Step first = remaining.remove();
            if (first != null) {
                GeneratorContext generatorContext = new GeneratorContext.Builder()
                    .connectorCatalog(connectorCatalog)
                    .generatorProperties(generatorProperties)
                    .request(request)
                    .contents(contents)
                    .flow(flow)
                    .visitorFactoryRegistry(registry)
                    .build();

                StepVisitorContext stepContext = new StepVisitorContext.Builder()
                    .index(1)
                    .step(first)
                    .remaining(remaining)
                    .build();

                visitStep(generatorContext, stepContext);
            }
        });
        SyndesisModel syndesisModel = new SyndesisModel();
        syndesisModel.addFlow(flow);
        return YAML_OBJECT_MAPPER.writeValueAsBytes(syndesisModel);
    }

    private void visitStep(GeneratorContext generatorContext, StepVisitorContext stepContext) {
        StepVisitorFactory factory = registry.get(stepContext.getStep().getStepKind());

        StepVisitor visitor = factory.create(generatorContext);
        generatorContext.getFlow().addStep(visitor.visit(stepContext));
        if (stepContext.hasNext()) {
             visitStep(generatorContext, stepContext.next());
        }
    }

    private byte[] generateFromRequest(GenerateProjectRequest request, Mustache template) throws IOException {
        return generate(request, template);
    }

    private byte[] generateFromPomContext(PomContext integration, Mustache template) throws IOException {
        return generate(integration, template);
    }

    private byte[] generate(Object obj, Mustache template) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        template.execute(new OutputStreamWriter(bos, UTF_8), obj).flush();
        return bos.toByteArray();
    }



    private static class MavenGav {
        private final String groupId;

        private final String artifactId;

        private final String version;

        private MavenGav(String groupId, String artifactId, String version) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
        }

        public String getGroupId() {
            return groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public String getVersion() {
            return version;
        }
    }

    private static class PomContext {

        private final String id;

        private final String name;

        private final String description;

        private final Set<MavenGav> connectors;

        private PomContext(String id, String name, String description, Set<MavenGav> connectors) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.connectors = connectors;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public Set<MavenGav> getConnectors() {
            return connectors;
        }
    }
}
