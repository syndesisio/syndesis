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
package io.syndesis.server.inspector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.syndesis.common.util.Json;
import io.syndesis.common.util.SyndesisServerException;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;

abstract class DataMapperBaseInspector<T> implements Inspector {

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

    static class Context<T> {
        private final T state;

        private final List<String> visited = new ArrayList<>();

        public Context(final T initial) {
            this.state = initial;
        }

        public T getState() {
            return state;
        }

        void addVisited(final String fullyQualifiedName) {
            visited.add(fullyQualifiedName);
        }

        boolean hasVisited(final String fullyQualifiedName) {
            return visited.contains(fullyQualifiedName);
        }
    }

    DataMapperBaseInspector(final boolean strict) {
        this.strict = strict;
    }

    @Override
    public List<String> getPaths(final String kind, final String type, final String specification, final Optional<byte[]> exemplar) {
        return getPathsForJavaClassName("", type, specification, createContext(kind, type, specification, exemplar));
    }

    @Override
    public final boolean supports(final String kind, final String type, final String specification, final Optional<byte[]> exemplar) {
        return "java".equals(kind) && !StringUtils.isEmpty(type) && internalSupports(kind, type, specification, exemplar);
    }

    protected Context<T> createContext(final String kind, final String type, final String specification, final Optional<byte[]> exemplar) {
        return new Context<>(null);
    }

    protected abstract String fetchJsonFor(String fullyQualifiedName, Context<T> context) throws IOException;

    protected final List<String> getPathsForJavaClassName(final String prefix, final String fullyQualifiedName, final String specification,
        final Context<T> context) {
        if (context.hasVisited(fullyQualifiedName)) {
            return Collections.emptyList();
        }

        context.addVisited(fullyQualifiedName);

        String json;
        try {
            json = fetchJsonFor(fullyQualifiedName, context);
        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") final Exception e) {
            if (strict) {
                throw SyndesisServerException.launderThrowable(e);
            }
            return Collections.emptyList();
        }

        return getPathsFromJavaClassJson(prefix, json, context);
    }

    @SuppressWarnings("PMD.CyclomaticComplexity")
    protected final List<String> getPathsFromJavaClassJson(final String prefix, final String specification, final Context<T> context) {
        try {
            final JsonNode node = Json.reader().readTree(specification);
            if (node == null) {
                return Collections.emptyList();
            }

            final JsonNode javaClass = node.get(JAVA_CLASS);
            if (javaClass == null) {
                return Collections.emptyList();
            }

            final JsonNode fields = javaClass.get(JAVA_FIELDS);
            if (fields == null) {
                return Collections.emptyList();
            }

            final JsonNode field = fields.get(JAVA_FIELD);
            if (field != null && field.isArray()) {
                final List<String> paths = new ArrayList<>();

                for (final JsonNode f : field) {
                    final String name = f.get(NAME).asText();
                    final String fieldClassName = f.get(CLASSNAME).asText();
                    final Boolean isPrimitive = f.get(PRIMITIVE).asBoolean();
                    if (isPrimitive || isTerminal(fieldClassName)) {
                        paths.add(prependPrefix(prefix, name));
                        continue;
                    }

                    paths.addAll(getPathsForJavaClassName(prependPrefix(prefix, name), fieldClassName, specification, context));
                }

                return paths;
            }

            return Collections.emptyList();
        } catch (final IOException e) {
            throw SyndesisServerException.launderThrowable(e);
        }
    }

    protected abstract boolean internalSupports(final String kind, final String type, final String specification,
        final Optional<byte[]> exemplar);

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
