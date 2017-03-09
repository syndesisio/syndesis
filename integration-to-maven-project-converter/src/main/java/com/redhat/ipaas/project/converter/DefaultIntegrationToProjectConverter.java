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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.redhat.ipaas.connector.catalog.ConnectorCatalog;
import com.redhat.ipaas.model.integration.Integration;
import com.redhat.ipaas.model.integration.Step;
import io.fabric8.funktion.model.Flow;
import io.fabric8.funktion.model.Funktion;
import io.fabric8.funktion.model.StepKinds;
import io.fabric8.funktion.model.steps.Endpoint;
import io.fabric8.funktion.model.steps.Log;
import io.fabric8.funktion.support.YamlHelper;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

@Service
public class DefaultIntegrationToProjectConverter implements IntegrationToProjectConverter {

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final static ObjectMapper YAML_OBJECT_MAPPER = YamlHelper.createYamlMapper();
    public static final TypeReference<Map<String, String>> MAP_TYPE_REFERENCE = new TypeReference<Map<String, String>>() {
    };

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

    private final ConnectorCatalog connectorCatalog;

    public DefaultIntegrationToProjectConverter(ConnectorCatalog connectorCatalog) {
        this.connectorCatalog = connectorCatalog;
    }

    @Override
    public Map<String, byte[]> convert(Integration integration) throws IOException {
        Map<String, byte[]> contents = new HashMap<>();
        contents.put("README.md", generate(integration, readmeMustache));
        contents.put("src/main/java/com/redhat/ipaas/example/Application.java", generate(integration, applicationJavaMustache));
        contents.put("src/main/resources/application.yml", generate(integration, applicationYmlMustache));
        contents.put("src/main/resources/funktion.yml", generateFlowYaml(integration));

        return contents;
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
                    ObjectNode stepNode = (ObjectNode) OBJECT_MAPPER.readTree(step.getConfiguredProperties());
                    stepNode.set("kind", new TextNode(step.getStepKind()));
                    if (step.getStepKind().equals(StepKinds.LOG)) {
                        flow.addStep(OBJECT_MAPPER.readValue(stepNode.toString(), Log.class));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Funktion funktion = new Funktion();
        funktion.addFlow(flow);
        return YAML_OBJECT_MAPPER.writeValueAsBytes(funktion);
    }

    private io.fabric8.funktion.model.steps.Step createEndpointStep(String camelConnector, String connectionConfiguredProperties, String configuredProperties) throws IOException, URISyntaxException {
        Map<String, String> props = OBJECT_MAPPER.readValue(connectionConfiguredProperties, MAP_TYPE_REFERENCE);
        props.putAll(OBJECT_MAPPER.readValue(configuredProperties, MAP_TYPE_REFERENCE));
        String endpointUri = connectorCatalog.buildEndpointUri(camelConnector, props);
        return new Endpoint(endpointUri);
    }

    private byte[] generate(Integration integration, Mustache template) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        template.execute(new PrintWriter(bos), integration).flush();
        return bos.toByteArray();
    }
}
