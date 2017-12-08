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


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;

import io.syndesis.core.Json;
import io.syndesis.core.SyndesisServerException;

import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Component
public class DataMapperClassInspector implements Inspector {

    private static final String INSPECTOR_URL_FORMAT = "http://%s/%s?%s=%s";

    private static final String JAVA_CLASS = "JavaClass";
    private static final String JAVA_FIELDS = "javaFields";
    private static final String JAVA_FIELD = "javaField";

    private static final String NAME = "name";
    private static final String CLASSNAME = "className";
    private static final String PRIMITIVE = "primitive";


    private static final String DEFAULT_SEPARATOR = ".";

    private static final String JAVA_LANG = "java.lang";
    private static final String JAVA_UTIL = "java.util";

    private static final String CACHE_NAME = Inspector.class.getName();

    private final CacheContainer caches;
    private final RestTemplate restTemplate;
    private final ClassInspectorConfigurationProperties config;


    protected DataMapperClassInspector(CacheContainer caches, RestTemplate restTemplate, ClassInspectorConfigurationProperties config) {
        this.caches = caches;
        this.restTemplate = restTemplate;
        this.config = config;
    }

    @Override
    public List<String> getPaths(String kind, String type, String specification, Optional<byte[]> exemplar) {
        Cache<String, List<String>> cache = caches.getCache(CACHE_NAME);
        if (cache.containsKey(type)) {
            return cache.get(type);
        }
        List<String> paths = getPathsForJavaClassName("", type, new ArrayList<>());
        cache.put(type, paths);
        return paths;
    }

    @Override
    public boolean supports(String kind, String type, String specification, Optional<byte[]> exemplar) {
        return "java".equals(kind) && !StringUtils.isEmpty(type);
    }

    protected List<String> getPathsForJavaClassName(String prefix, String fullyQualifiedName, List<String> visited) {
        if (visited.contains(fullyQualifiedName)) {
            return Collections.emptyList();
        }

        visited.add(fullyQualifiedName);

        ResponseEntity<String> response = null;
        try {
            response = restTemplate.getForEntity(getClassInspectionUrl(config, fullyQualifiedName), String.class);

        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception e) {
            if (config.isStrict()) {
                throw SyndesisServerException.launderThrowable(e);
            }
            return Collections.emptyList();
        }
        String json = response.getBody();
        return getPathsFromJavaClassJson(prefix, json, visited);
    }

    @SuppressWarnings("PMD.CyclomaticComplexity")
    protected List<String> getPathsFromJavaClassJson(String prefix, String json, List<String> visited) {
        List<String> paths = new ArrayList<>();
        try {
            JsonNode node = Json.mapper().readTree(json);
            if (node != null) {
                JsonNode javaClass = node.get(JAVA_CLASS);
                if (javaClass != null) {
                    JsonNode fields = javaClass.get(JAVA_FIELDS);
                    if (fields != null) {
                        JsonNode field = fields.get(JAVA_FIELD);
                        if (field != null && field.isArray()) {
                            for (JsonNode f : field) {
                                String name = f.get(NAME).asText();
                                String fieldClassName = f.get(CLASSNAME).asText();
                                Boolean isPrimitive = f.get(PRIMITIVE).asBoolean();
                                if (isPrimitive || isTerminal(fieldClassName)) {
                                    paths.add(prependPrefix(prefix,name));
                                    continue;
                                }

                                paths.addAll(
                                    getPathsForJavaClassName(
                                        prependPrefix(prefix,name), fieldClassName, visited));
                            }
                        }
                    }
                }
            }

        } catch (IOException e) {
            throw SyndesisServerException.launderThrowable(e);
        }
        return paths;
    }

    private String prependPrefix(String prefix, String name) {
        return StringUtils.hasLength(prefix) ? prefix + DEFAULT_SEPARATOR + name : name;
    }

    protected static String getClassName(String fullyQualifiedName) {
        int index = fullyQualifiedName.lastIndexOf('.');
        if (index > 0) {
            return fullyQualifiedName.substring(index + 1);
        }

        return fullyQualifiedName;
    }

    /**
     * Checks if the the specified class name is terminal (we can't further expand the path).
     * Examples of terminals are primitive, or java.lang classes.
     *
     * @param fullyQualifiedName The specified class name.
     * @return True if terminal, false otherwise.
     */
    protected static boolean isTerminal(String fullyQualifiedName) {
        return fullyQualifiedName == null || fullyQualifiedName.startsWith(JAVA_LANG) || fullyQualifiedName.startsWith(JAVA_UTIL);
    }

    protected static String getClassInspectionUrl(ClassInspectorConfigurationProperties config, String className) {
        return String.format(INSPECTOR_URL_FORMAT,
            config.getHost() + ":" + config.getPort(),
            config.getPath(),
            config.getClassNameParameter(),
            className);
    }
}
