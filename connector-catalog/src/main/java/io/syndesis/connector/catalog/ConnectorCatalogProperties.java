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
package io.syndesis.connector.catalog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("camel.connector.catalog")
public class ConnectorCatalogProperties {

    private Map<String, String> mavenRepos = new ConcurrentHashMap<>(1);

    private List<String> connectorGAVs = new ArrayList<>(5);

    public ConnectorCatalogProperties() {
        this.mavenRepos.put("maven.central", "https://repo.maven.apache.org/maven2/");
    }

    public Map<String, String> getMavenRepos() {
        return mavenRepos;
    }

    public void setMavenRepos(Map<String, String> mavenRepos) {
        this.mavenRepos = mavenRepos;
    }

    public List<String> getConnectorGAVs() {
        return connectorGAVs;
    }

    public void setConnectorGAVs(List<String> connectorGAVs) {
        this.connectorGAVs = connectorGAVs;
    }

}
