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
package io.syndesis.connector.catalog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.syndesis.core.MavenProperties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("camel.connector.catalog")
public class ConnectorCatalogProperties {

    private List<String> connectorGAVs = new ArrayList<>(5);

    private final MavenProperties mavenProperties;

    public ConnectorCatalogProperties(final MavenProperties mavenProperties) {
        this.mavenProperties = mavenProperties;
    }

    public List<String> getConnectorGAVs() {
        return connectorGAVs;
    }

    public Map<String, String> getMavenRepos() {
        return mavenProperties.getRepositories();
    }

    public void setConnectorGAVs(final List<String> connectorGAVs) {
        this.connectorGAVs = connectorGAVs;
    }

}
