/**
 * Copyright (C) 2017 Red Hat, Inc.
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
package io.syndesis.catalog;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.apache.camel.catalog.CamelCatalog;
import org.apache.camel.catalog.DefaultCamelCatalog;
import org.apache.camel.catalog.connector.CamelConnectorCatalog;
import org.apache.camel.catalog.connector.ConnectorDto;
import org.apache.camel.catalog.connector.DefaultCamelConnectorCatalog;
import org.apache.camel.catalog.maven.DefaultMavenArtifactProvider;
import org.apache.camel.catalog.maven.MavenArtifactProvider;

public class ConnectorCatalog {

    private CamelConnectorCatalog connectorCatalog;
    private CamelCatalog camelCatalog;
    private MavenArtifactProvider maven;

    public void init() throws Exception {
        connectorCatalog = new DefaultCamelConnectorCatalog();
        camelCatalog = new DefaultCamelCatalog(true);

        maven = new DefaultMavenArtifactProvider();

        // add 3rd party maven repos
        System.out.println("Adding bintray and jcenter as 3rd party Maven repositories");
        maven.addMavenRepository("jcenter", "https://jcenter.bintray.com");
    }

    public void addConnector(String groupId, String artifactId, String version) {
        System.out.println("Downloading Maven GAV: " + groupId + ":" + artifactId + ":" + version);

        maven.addArtifactToCatalog(camelCatalog, connectorCatalog, groupId, artifactId, version);
    }

    public List<ConnectorDto> listConnectors() {
        return connectorCatalog.findConnector(true);
    }

    public String buildEndpointUri(String scheme, Map<String, String> options) throws URISyntaxException {
        return camelCatalog.asEndpointUri(scheme, options, true);
    }
}
