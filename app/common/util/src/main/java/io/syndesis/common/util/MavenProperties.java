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
package io.syndesis.common.util;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

public class MavenProperties {
    private final Map<String, String> repositories = new ConcurrentSkipListMap<>();

    public MavenProperties() {
        // allow instantiation, not 100% sure it's needed (or the setter below)
    }

    public MavenProperties(final Map<String, String> repositories) {
        this.repositories.putAll(repositories);
    }

    public Map<String, String> getRepositories() {
        return repositories;
    }

    public void setRepositories(final Map<String, String> repositories) {
        this.repositories.clear();
        this.repositories.putAll(repositories);
    }


    public MavenProperties addRepository(String id, String url) {
        this.repositories.put(id, url);
        return this;
    }
}

