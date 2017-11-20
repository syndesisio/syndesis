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
package io.syndesis.project.converter.mvn;

import java.util.Map;
import java.util.Set;

import io.syndesis.core.MavenProperties;

public final class PomContext {
    private final String id;
    private final String name;
    private final String description;
    private final Set<MavenGav> connectors;
    private final Set<MavenGav> extensions;
    private final MavenProperties mavenProperties;

    public PomContext(String id, String name, String description, Set<MavenGav> connectors, Set<MavenGav> extensions, MavenProperties mavenProperties) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.connectors = connectors;
        this.extensions = extensions;
        this.mavenProperties = mavenProperties;
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

    public Set<MavenGav> getExtensions() {
        return extensions;
    }

    public Set<Map.Entry<String, String>> getMavenRepositories() {
        return mavenProperties.getRepositories().entrySet();
    }
}
