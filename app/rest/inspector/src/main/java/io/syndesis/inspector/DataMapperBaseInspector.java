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

import io.syndesis.core.Json;
import io.syndesis.core.SyndesisServerException;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;

abstract class DataMapperBaseInspector implements Inspector {

    private static final String CLASSNAME = "className";
    private static final String DEFAULT_SEPARATOR = ".";
    private static final String JAVA_CLASS = "JavaClass";

    private static final String JAVA_FIELD = "javaField";
    private static final String JAVA_FIELDS = "javaFields";
    private static final String JAVA_LANG = "java.lang";

    private static final String JAVA_UTIL = "java.util";

    private static final String NAME = "name";
    private static final String PRIMITIVE = "primitive";

    private final boolean strict;

    /* default */ DataMapperBaseInspector(final boolean strict) {
        this.strict = strict;
    }

    @Override
    public List<String> getPaths(final String kind, final String type, final String specification, final Optional<byte[]> exemplar) {
        return getPathsForJavaClassName("", type, new ArrayList<>());
    }

    @Override
    public final boolean supports(final String kind, final String type, final String specification, final Optional<byte[]> exemplar) {
        return "java".equals(kind) && !StringUtils.isEmpty(type);
    }

    protected abstract String fetchJsonFor(String fullyQualifiedName) throws Exception;

    protected final List<String> getPathsForJavaClassName(final String prefix, final String fullyQualifiedName,
        final List<String> visited) {
        if (visited.contains(fullyQualifiedName)) {
            return Collections.emptyList();
        }

        visited.add(fullyQualifiedName);

        String json;
        try {
            json = fetchJsonFor(fullyQualifiedName);
        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") final Exception e) {
            if (strict) {
                throw SyndesisServerException.launderThrowable(e);
            }
            return Collections.emptyList();
        }

        return getPathsFromJavaClassJson(prefix, json, visited);
    }

    @SuppressWarnings("PMD.CyclomaticComplexity")
    protected final List<String> getPathsFromJavaClassJson(final String prefix, final String json, final List<String> visited) {
        final List<String> paths = new ArrayList<>();
        try {
            final JsonNode node = Json.reader().readTree(json);
            if (node != null) {
                final JsonNode javaClass = node.get(JAVA_CLASS);
                if (javaClass != null) {
                    final JsonNode fields = javaClass.get(JAVA_FIELDS);
                    if (fields != null) {
                        final JsonNode field = fields.get(JAVA_FIELD);
                        if (field != null && field.isArray()) {
                            for (final JsonNode f : field) {
                                final String name = f.get(NAME).asText();
                                final String fieldClassName = f.get(CLASSNAME).asText();
                                final Boolean isPrimitive = f.get(PRIMITIVE).asBoolean();
                                if (isPrimitive || isTerminal(fieldClassName)) {
                                    paths.add(prependPrefix(prefix, name));
                                    continue;
                                }

                                paths.addAll(getPathsForJavaClassName(prependPrefix(prefix, name), fieldClassName, visited));
                            }
                        }
                    }
                }
            }

        } catch (final IOException e) {
            throw SyndesisServerException.launderThrowable(e);
        }
        return paths;
    }

    private static String prependPrefix(final String prefix, final String name) {
        return StringUtils.hasLength(prefix) ? prefix + DEFAULT_SEPARATOR + name : name;
    }

    /**
     * Checks if the the specified class name is terminal (we can't further
     * expand the path). Examples of terminals are primitive, or java.lang
     * classes.
     *
     * @param fullyQualifiedName The specified class name.
     * @return True if terminal, false otherwise.
     */
    protected static boolean isTerminal(final String fullyQualifiedName) {
        return fullyQualifiedName == null || fullyQualifiedName.startsWith(JAVA_LANG) || fullyQualifiedName.startsWith(JAVA_UTIL);
    }
}
