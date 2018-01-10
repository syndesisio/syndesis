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
package io.syndesis.inspector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.syndesis.core.cache.CacheManager;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class DataMapperClassInspector extends DataMapperBaseInspector {

    private static final String CACHE_NAME = Inspector.class.getName();

    private static final String INSPECTOR_URL_FORMAT = "http://%s/%s?%s=%s";

    private final CacheManager caches;
    private final ClassInspectorConfigurationProperties config;
    private final RestTemplate restTemplate;

    protected DataMapperClassInspector(final CacheManager caches, final RestTemplate restTemplate,
        final ClassInspectorConfigurationProperties config) {
        super(config.isStrict());
        this.caches = caches;
        this.restTemplate = restTemplate;
        this.config = config;
    }

    @Override
    public List<String> getPaths(final String kind, final String type, final String specification, final Optional<byte[]> exemplar) {
        final Map<String, List<String>> cache = caches.getCache(CACHE_NAME);
        if (cache.containsKey(type)) {
            return cache.get(type);
        }
        final List<String> paths = getPathsForJavaClassName("", type, new ArrayList<>());
        cache.put(type, paths);
        return paths;
    }

    @Override
    protected String fetchJsonFor(final String fullyQualifiedName) throws Exception {
        final ResponseEntity<String> response = restTemplate.getForEntity(getClassInspectionUrl(config, fullyQualifiedName), String.class);

        return response.getBody();
    }

    protected static String getClassInspectionUrl(final ClassInspectorConfigurationProperties config, final String className) {
        return String.format(INSPECTOR_URL_FORMAT, config.getHost() + ":" + config.getPort(), config.getPath(),
            config.getClassNameParameter(), className);
    }

    protected static String getClassName(final String fullyQualifiedName) {
        final int index = fullyQualifiedName.lastIndexOf('.');
        if (index > 0) {
            return fullyQualifiedName.substring(index + 1);
        }

        return fullyQualifiedName;
    }
}
