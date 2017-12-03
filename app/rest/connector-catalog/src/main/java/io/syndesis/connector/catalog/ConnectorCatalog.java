/**
 * Copyright (C) 2016 Red Hat, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.connector.catalog;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.camel.catalog.CamelCatalog;
import org.apache.camel.catalog.CollectionStringBuffer;
import org.apache.camel.catalog.DefaultCamelCatalog;
import org.apache.camel.catalog.connector.CamelConnectorCatalog;
import org.apache.camel.catalog.connector.DefaultCamelConnectorCatalog;
import org.apache.camel.catalog.maven.DefaultMavenArtifactProvider;
import org.apache.camel.catalog.maven.MavenArtifactProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectorCatalog {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectorCatalog.class);
    private static final String CONNECTOR_SCHEMAS = "/META-INF/camel/camel-meta.json";

    private final CamelConnectorCatalog connectorCatalog;
    private final CamelCatalog camelCatalog;
    private final MavenArtifactProvider maven;

    private final Set<String> prefetchedConnectors = new HashSet<>();

    public ConnectorCatalog(ConnectorCatalogProperties props) {

        connectorCatalog = new DefaultCamelConnectorCatalog();
        camelCatalog = new DefaultCamelCatalog(true);

        prefetchConnectors();

        maven = new DefaultMavenArtifactProvider();

        for (Map.Entry<String, String> repo : props.getMavenRepos().entrySet()) {
            maven.addMavenRepository(repo.getKey(), repo.getValue());
        }

        for (String gav : props.getConnectorGAVs()) {
            addConnector(gav);
        }
    }

    private void prefetchConnectors() {
        try (InputStream is = getClass().getResourceAsStream(CONNECTOR_SCHEMAS)) {
            if (is != null) {
                ArrayNode descriptors = (ArrayNode) new ObjectMapper().readTree(is);
                for (JsonNode descriptor : descriptors) {
                    JsonNode gav = descriptor.get("gav");
                    if (gav == null) {
                        continue;
                    }
                    String gavCoords[] = gav.textValue().split(":");
                    if (gavCoords.length < 3) {
                        continue;
                    }
                    LOG.info("Prefetched meta for {}:{}:{}", gavCoords[0], gavCoords[1], gavCoords[2]);
                    prefetchedConnectors.add(gav.textValue());

                    JsonNode connector = descriptor.get("connector");
                    ObjectNode component = (ObjectNode) descriptor.get("component");
                    if (connector != null) {
                        addConnector(gavCoords, connector, component);
                    }
                    if (component != null) {
                        addComponent(component);
                    }
                }
            }
        } catch (IOException exp) {
            LOG.error("Cannot initialize connectors from {} : {}", CONNECTOR_SCHEMAS, exp.getMessage(), exp);
        }
    }

    private void addComponent(ObjectNode component) {
        ObjectNode meta = (ObjectNode) component.get("meta");
        Iterator<Map.Entry<String, JsonNode>> it = meta.fields();
        while (it.hasNext()) {
            Map.Entry<String, JsonNode> entry = it.next();
            String json = entry.getValue().asText();
            String javaType = extractJavaType(json);
            if (javaType != null) {
                camelCatalog.addComponent(entry.getKey(), javaType, json);
            }
        }
    }

    private void addConnector(String[] gavCoords, JsonNode connector, ObjectNode component) {
        JsonNode meta = connector.get("meta");
        connectorCatalog.addConnector(
            gavCoords[0], gavCoords[1], gavCoords[2],
            meta.get("name").textValue(),
            meta.get("scheme").textValue(),
            meta.get("javaType").textValue(),
            meta.get("description").textValue(),
            extractLabels(meta),
            extractJson(connector, "meta"),
            extractJsonTextString(connector, "schema"),
            extractJsonTextString(component, "schema"));
    }

    private String extractJavaType(String json) {
        try {
            ObjectNode node = (ObjectNode) new ObjectMapper().readTree(json);
            JsonNode c = node.get("component");
            if (c != null) {
                JsonNode ret = c.get("javaType");
                if (ret != null) {
                    return ret.asText();
                }
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    private String extractJsonTextString(JsonNode descriptor, String key) {
        if (descriptor != null && descriptor.hasNonNull(key)) {
            // TODO: If this is a real JSON object, print it as JSON
            // I.e. when JsonSchemaHelper is 'fixed', and
            // ExtractConnectorDescriptorsMojo updated to store real json objects for
            // JSONNodes and not only text
            return descriptor.get(key).asText();
        } else {
            return null;
        }
    }

    private String extractJson(JsonNode descriptor, String key) {
        if (descriptor != null && descriptor.hasNonNull(key)) {
            return getJsonAsString(descriptor.get(key));
        } else {
            return null;
        }
    }

    private String getJsonAsString(JsonNode jsonNode) {
        try {
            return new ObjectMapper().writeValueAsString(jsonNode);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private String extractLabels(JsonNode tree) {
        Iterator<JsonNode> it = tree.withArray("labels").iterator();

        CollectionStringBuffer csb = new CollectionStringBuffer(",");
        while (it.hasNext()) {
            String text = it.next().textValue();
            csb.append(text);
        }
        return csb.toString();
    }

    public final void addConnector(String gav) {
        if (prefetchedConnectors.contains(gav)) {
            return;
        }

        String[] splitGAV = gav.split(":");
        if (splitGAV.length == 3) {
            String groupId = splitGAV[0];
            String artifactId = splitGAV[1];
            String version = splitGAV[2];

            LOG.info("Downloading Maven GAV: {}:{}:{}", groupId, artifactId, version);

            maven.addArtifactToCatalog(camelCatalog, connectorCatalog, groupId, artifactId, version);
        }
    }

    public String buildEndpointUri(String scheme, Map<String, String> options) throws URISyntaxException {
        String result = camelCatalog.asEndpointUri(scheme, options, false);
        // we need to strip off the colon bit.
        if (result.equals(scheme + ":")) {
            result = scheme;
        }
        if (result.startsWith(scheme + ":?")) {
            result = scheme + result.substring(scheme.length() + 1);
        }
        return result;
    }

}
