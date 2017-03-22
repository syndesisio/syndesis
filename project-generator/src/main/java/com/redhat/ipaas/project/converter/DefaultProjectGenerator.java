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

public class DefaultProjectGenerator implements ProjectGenerator {

    private final static ObjectMapper YAML_OBJECT_MAPPER = YamlHelper.createYamlMapper();

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

    private Mustache connectorPomMustache = mf.compile(
        new InputStreamReader(getClass().getResourceAsStream("templates/connector/pom.xml.mustache")),
        "pom.xml"
    );

    private final ConnectorCatalog connectorCatalog;

    public DefaultProjectGenerator(ConnectorCatalog connectorCatalog) {
        this.connectorCatalog = connectorCatalog;
    }

    @Override
    public Map<String, byte[]> generate(Integration integration) throws IOException {
        integration.getSteps().ifPresent(steps -> {
            for (Step step : steps) {
                step.getAction().ifPresent(action -> connectorCatalog.addConnector(action.getCamelConnectorGAV()));
            }
        });

        Map<String, byte[]> contents = new HashMap<>();
        contents.put("README.md", generate(integration, readmeMustache));
        contents.put("src/main/java/com/redhat/ipaas/example/Application.java", generate(integration, applicationJavaMustache));
        contents.put("src/main/resources/application.yml", generate(integration, applicationYmlMustache));
        contents.put("src/main/resources/funktion.yml", generateFlowYaml(integration));
        contents.put("pom.xml", generatePom(integration));

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
        return generate(
            new IntegrationForPom(integration.getId().orElse(""), integration.getName(), integration.getDescription().orElse(null), connectors),
            pomMustache
        );
    }


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


    private byte[] generateFlowYaml(Integration integration) throws JsonProcessingException {
        Flow flow = new Flow();
        integration.getSteps().ifPresent(steps -> {
            for (Step step : steps) {
                if (step.getStepKind().equals(StepKinds.ENDPOINT)) {
                    step.getAction().ifPresent(action -> {
                        step.getConnection().ifPresent(connection -> {
                            try {
                                flow.addStep(createEndpointStep(action.getCamelConnectorPrefix(), connection.getConfiguredProperties(), step.getConfiguredProperties()));
                            } catch (IOException | URISyntaxException e) {
                                e.printStackTrace();
                            }
                        });
                    });
                    continue;
                }

                try {
                    HashMap<String, Object> stepJSON = new HashMap<>(step.getConfiguredProperties());
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

    private io.fabric8.funktion.model.steps.Step createEndpointStep(String camelConnector, Map<String, String> connectionConfiguredProperties, Map<String, String> configuredProperties) throws IOException, URISyntaxException {
        Map<String, String> props = readConfiguredProperties(connectionConfiguredProperties, configuredProperties);

        // TODO Remove this hack... when we can read endpointValues from connector schema then we should use those as initial properties.
        if ("periodic-timer".equals(camelConnector)) {
            props.put("timerName", "every");
        }

        String endpointUri = connectorCatalog.buildEndpointUri(camelConnector, props);
        return new Endpoint(endpointUri);
    }

    private Map<String, String> readConfiguredProperties(Map<String, String> ... configuredProperties) throws IOException {
        Map<String, String> configuredProps = new HashMap<>();
        for (Map<String, String> props : configuredProperties) {
            configuredProps.putAll(props);
        }
        return configuredProps;
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

    private static class IntegrationForPom {

        private final String id;

        private final String name;

        private final String description;

        private final Set<MavenGav> connectors;

        private IntegrationForPom(String id, String name, String description, Set<MavenGav> connectors) {
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
