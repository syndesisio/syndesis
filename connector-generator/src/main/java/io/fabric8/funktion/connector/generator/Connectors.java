/*
 * Copyright 2016 Red Hat, Inc.
 * <p>
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */
package io.fabric8.funktion.connector.generator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.funktion.DataKeys;
import io.fabric8.funktion.Labels;
import io.fabric8.funktion.support.YamlHelper;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.api.model.extensions.DeploymentBuilder;
import io.fabric8.utils.IOHelpers;
import io.fabric8.utils.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static io.fabric8.funktion.Labels.Kind.CONNECTOR;
import static io.fabric8.funktion.Labels.Kind.SUBSCRIPTION;

/**
 */
public class Connectors {
    private static final transient Logger LOG = LoggerFactory.getLogger(Connectors.class);

    public static ConfigMap createConnector(ComponentModel component, String jSonSchema, String asciiDoc, String image, File applicationPropertiesFile) {
        String componentName = component.getScheme().toLowerCase();
        Map<String, String> annotations = new LinkedHashMap<>();
        Map<String, String> labels = new LinkedHashMap<>();
        labels.put(Labels.KIND, CONNECTOR);

        Map<String, String> data = new LinkedHashMap<>();

        Map<String, String> subscriptionLabels = new HashMap<>();
        subscriptionLabels.put(Labels.KIND, SUBSCRIPTION);
        subscriptionLabels.put(Labels.CONNECTOR, componentName);

        Deployment deployment = new DeploymentBuilder().
                withNewMetadata().withLabels(subscriptionLabels).endMetadata().
                withNewSpec().withReplicas(1).
                withNewTemplate().withNewMetadata().withLabels(subscriptionLabels).endMetadata().
                withNewSpec().addNewContainer().withName("connector").withImage(image).endContainer().
                endSpec().endTemplate().endSpec().build();
        try {
            String deploymentYaml = YamlHelper.createYamlMapper().writeValueAsString(deployment);
            data.put(DataKeys.Connector.DEPLOYMENT_YAML, deploymentYaml);
        } catch (JsonProcessingException e) {
            LOG.error("Failed to marshal Deployment " + deployment + ". " + e, e);
        }
        String schemaYaml = convertToYaml(jSonSchema);
        if (Strings.isNotBlank(schemaYaml)) {
            data.put(DataKeys.Connector.SCHEMA_YAML, schemaYaml);
        }
        if (Strings.isNotBlank(asciiDoc)) {
            data.put(DataKeys.Connector.ASCIIDOC, asciiDoc);
        }
        if (applicationPropertiesFile.isFile() && applicationPropertiesFile.exists()) {
            try {
                String applicationPropertiesText = IOHelpers.readFully(applicationPropertiesFile);
                if (Strings.isNotBlank(applicationPropertiesText)) {
                    data.put(DataKeys.Connector.APPLICATION_PROPERTIES, applicationPropertiesText);
                }
            } catch (IOException e) {
                LOG.error("Failed to load " + applicationPropertiesFile + ". " + e, e);
            }
        }

        return new ConfigMapBuilder().
                withNewMetadata().withName(componentName).withAnnotations(annotations).withLabels(labels).endMetadata().
                withData(data).build();
    }

    private static String convertToYaml(String jSonSchema) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Object value = mapper.readerFor(Map.class).readValue(jSonSchema);
            if (value != null) {
                return YamlHelper.createYamlMapper().writeValueAsString(value);
            }
        } catch (IOException e) {
            LOG.info("Failed to convert JSON " + jSonSchema + " to YAML: " + e, e);
        }
        return null;
    }


}
