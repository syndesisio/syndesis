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
package com.redhat.ipaas.connector.catalog;

import org.apache.camel.catalog.CamelCatalog;
import org.apache.camel.catalog.DefaultCamelCatalog;
import org.apache.camel.catalog.connector.CamelConnectorCatalog;
import org.apache.camel.catalog.connector.DefaultCamelConnectorCatalog;
import org.apache.camel.catalog.maven.DefaultMavenArtifactProvider;
import org.apache.camel.catalog.maven.MavenArtifactProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.Map;

public class ConnectorCatalog {

    private final static Logger log = LoggerFactory.getLogger(ConnectorCatalog.class);

    private final CamelConnectorCatalog connectorCatalog;
    private final CamelCatalog camelCatalog;
    private final MavenArtifactProvider maven;

    public ConnectorCatalog(ConnectorCatalogProperties props) {

        connectorCatalog = new DefaultCamelConnectorCatalog();
        camelCatalog = new DefaultCamelCatalog(true);

        maven = new DefaultMavenArtifactProvider();

        for (Map.Entry<String, String> repo : props.getMavenRepos().entrySet()) {
            maven.addMavenRepository(repo.getKey(), repo.getValue());
        }

        for (String gav : props.getConnectorGAVs()) {
            addConnector(gav);
        }
    }

    public void addConnector(String gav) {
        String[] splitGAV = gav.split(":");
        if (splitGAV.length == 3) {
            String groupId = splitGAV[0];
            String artifactId = splitGAV[1];
            String version = splitGAV[2];

           log.info("Downloading Maven GAV: " + groupId + ":" + artifactId + ":" + version);

            maven.addArtifactToCatalog(camelCatalog, connectorCatalog, groupId, artifactId, version);
        }
    }

    public String buildEndpointUri(String scheme, Map<String, String> options) throws URISyntaxException {
        String result = camelCatalog.asEndpointUri(scheme, options, false);
        // we need to strip off the colon bit.
        if( result.equals(scheme+":")) {
            result = scheme;
        }
        if( result.startsWith(scheme+":?")) {
            result = scheme+result.substring(scheme.length()+1);
        }
        return result;
    }

}
