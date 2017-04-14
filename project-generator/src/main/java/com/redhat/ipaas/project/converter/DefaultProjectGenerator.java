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
package com.redhat.ipaas.project.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.redhat.ipaas.connector.catalog.ConnectorCatalog;
import com.redhat.ipaas.core.Json;
import com.redhat.ipaas.model.connection.Connector;
import com.redhat.ipaas.model.integration.Integration;
import com.redhat.ipaas.model.integration.Step;
import io.fabric8.funktion.model.Flow;
import io.fabric8.funktion.model.Funktion;
import io.fabric8.funktion.model.StepKinds;
import io.fabric8.funktion.model.steps.Endpoint;
import io.fabric8.funktion.support.YamlHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultProjectGenerator implements ProjectGenerator {

    private final static ObjectMapper YAML_OBJECT_MAPPER = YamlHelper.createYamlMapper();
    private final static String PLACEHOLDER_FORMAT = "{{%s}}";

    private MustacheFactory mf = new DefaultMustacheFactory();

    private Mustache readmeMustache = mf.compile(
        new InputStreamReader(getClass().getResourceAsStream("templates/README.md.mustache")),
        "README.md"
    );
    private Mustache applicationJavaMustache = mf.compile(
        new InputStreamReader(getClass().getResourceAsStream("templates/Application.java.mustache")),
        "Application.java"
    );

    private Mustache applicationYmlMustache = mf.compile(
        new InputStreamReader(getClass().getResourceAsStream("templates/application.yml.mustache")),
        "application.yml"
    );

    private Mustache pomMustache = mf.compile(
        new InputStreamReader(getClass().getResourceAsStream("templates/pom.xml.mustache")),
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

    public DefaultProjectGenerator(ConnectorCatalog connectorCatalog, ProjectGeneratorProperties generatorProperties) {
        this.connectorCatalog = connectorCatalog;
        this.generatorProperties = generatorProperties;
    }

    @Override
    public Map<String, byte[]> generate(GenerateProjectRequest request) throws IOException {
        request.getIntegration().getSteps().ifPresent(steps -> {
            for (Step step : steps) {
                step.getAction().ifPresent(action -> connectorCatalog.addConnector(action.getCamelConnectorGAV()));
            }
        });

        Map<String, byte[]> contents = new HashMap<>();
        contents.put("README.md", generateFromRequest(request, readmeMustache));
        contents.put("src/main/java/com/redhat/ipaas/example/Application.java", generateFromRequest(request, applicationJavaMustache));
        contents.put("src/main/resources/application.yml", generateFromRequest(request, applicationYmlMustache));
        contents.put("src/main/resources/funktion.yml", generateFlowYaml(request));
        contents.put("pom.xml", generatePom(request.getIntegration()));

        return contents;
    }

    public byte[] generatePom(Integration integration) throws IOException {
        Set<MavenGav> connectors = new LinkedHashSet<>();
        integration.getSteps().ifPresent(steps -> {
            for (Step step : steps) {
                if (step.getStepKind().equals(StepKinds.ENDPOINT)) {
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
    reference to a 'default' action. Or the mapping happens in the external ipaas-verifier service:

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

    private byte[] generateFlowYaml(GenerateProjectRequest request) throws JsonProcessingException {
        Flow flow = new Flow();
        request.getIntegration().getSteps().ifPresent(steps -> {
            for (Step step : steps) {
                if (step.getStepKind().equals(StepKinds.ENDPOINT)) {
                    step.getAction().ifPresent(action -> {
                        step.getConnection().ifPresent(connection -> {
                            try {
                                String connectorId = step.getConnection().get().getConnectorId().orElse(action.getConnectorId());
                                if (!request.getConnectors().containsKey(connectorId)) {
                                    throw new IllegalStateException("Connector:["+connectorId+"] not found.");
                                }

                                Connector connector = request.getConnectors().get(connectorId);
                                flow.addStep(createEndpointStep(connector, action.getCamelConnectorPrefix(), 
                                        connection.getConfiguredProperties(), step.getConfiguredProperties().orElse(new HashMap<String,String>())));
                            } catch (IOException | URISyntaxException e) {
                                e.printStackTrace();
                            }
                        });
                    });
                    continue;
                }

                try {
                    HashMap<String, Object> stepJSON = new HashMap<>(step.getConfiguredProperties().orElse(new HashMap<String,String>()));
                    stepJSON.put("kind", step.getStepKind());
                    String json = Json.mapper().writeValueAsString(stepJSON);
                    flow.addStep(Json.mapper().readValue(json, io.fabric8.funktion.model.steps.Step.class));
                    continue;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Funktion funktion = new Funktion();
        funktion.addFlow(flow);
        return YAML_OBJECT_MAPPER.writeValueAsBytes(funktion);
    }

    private io.fabric8.funktion.model.steps.Step createEndpointStep(Connector connector, String camelConnectorPrefix, Map<String, String> connectionConfiguredProperties, Map<String, String> stepConfiguredProperties) throws IOException, URISyntaxException {
        Map<String, String> properties = aggregate(connectionConfiguredProperties, stepConfiguredProperties);
        Map<String, String> secrets = connector.filterSecrets(properties, e -> String.format(PLACEHOLDER_FORMAT, e.getKey()));

        // TODO Remove this hack... when we can read endpointValues from connector schema then we should use those as initial properties.
        if ("periodic-timer".equals(camelConnectorPrefix)) {
            properties.put("timerName", "every");
        }

        Map<String, String> maskedProperties = generatorProperties.isSecretMaskingEnabled() ? aggregate(properties, secrets) : properties;
        String endpointUri = connectorCatalog.buildEndpointUri(camelConnectorPrefix, maskedProperties);
        return new Endpoint(endpointUri);
    }

    private static Map<String, String> aggregate(Map<String, String> ... maps) throws IOException {
        return Stream.of(maps).flatMap(map -> map.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> newValue));
    }

    private byte[] generateFromRequest(GenerateProjectRequest request, Mustache template) throws IOException {
        return generate(request, template);
    }

    private byte[] generateFromPomContext(PomContext integration, Mustache template) throws IOException {
        return generate(integration, template);
    }

    private byte[] generate(Object obj, Mustache template) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        template.execute(new PrintWriter(bos), obj).flush();
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
