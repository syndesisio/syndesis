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
package io.syndesis.server.api.generator.swagger;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import io.swagger.models.ArrayModel;
import io.swagger.models.Model;
import io.swagger.models.RefModel;
import io.swagger.models.Swagger;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;

final class CyclicValidationCheck {

    private CyclicValidationCheck() {
        // utility class
    }

    static boolean hasCyclicReferences(final Swagger swagger) {
        if (swagger == null || swagger.getDefinitions() == null || swagger.getDefinitions().isEmpty()) {
            return false;
        }

        final Map<String, Set<String>> references = collectReferences(swagger);

        return isCyclic(references);
    }

    private static Map<String, Set<String>> collectReferences(final Swagger swagger) {
        final Map<String, Set<String>> references = new TreeMap<>();

        for (final Map.Entry<String, Model> definition : swagger.getDefinitions().entrySet()) {
            final String name = definition.getKey();

            references.putIfAbsent(name, new HashSet<>());

            final Model model = definition.getValue();
            if (model instanceof RefModel) {
                referenceFrom(references, name, (RefModel) model);
            } else if (model instanceof ArrayModel) {
                final Property property = ((ArrayModel) model).getItems();
                collectReferencesFromProperty(references, name, property);
            } else {
                collectReferencesFromProperties(references, name, model.getProperties());
            }
        }
        return references;
    }

    private static void collectReferencesFromProperties(final Map<String, Set<String>> allReferences, final String from,
        final Map<String, Property> properties) {
        if (properties == null) {
            return;
        }

        for (final Property nestedProperty : properties.values()) {
            collectReferencesFromProperty(allReferences, from, nestedProperty);
        }
    }

    private static void collectReferencesFromProperty(final Map<String, Set<String>> allReferences, final String from, final Property property) {
        if (property instanceof RefProperty) {
            referenceFrom(allReferences, from, (RefProperty) property);
        } else if (property instanceof ObjectProperty) {
            collectReferencesFromProperties(allReferences, from, ((ObjectProperty) property).getProperties());
        }
    }

    private static boolean isCyclic(final Map<String, Set<String>> references) {
        for (final Map.Entry<String, Set<String>> reference : references.entrySet()) {
            final String current = reference.getKey();
            final Set<String> referencing = reference.getValue();

            if (isCyclic(current, references, referencing)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isCyclic(final String starting, final Map<String, Set<String>> references, final Set<String> referencing) {
        if (referencing == null) {
            return false;
        }

        if (referencing.contains(starting)) {
            return true;
        }

        for (final String next : referencing) {
            if (isCyclic(starting, references, references.get(next))) {
                return true;
            }
        }

        return false;
    }

    private static void referenceFrom(final Map<String, Set<String>> allReferences, final String from, final RefModel to) {
        final String simpleRef = to.getSimpleRef();
        allReferences.putIfAbsent(from, new HashSet<>());
        allReferences.get(from).add(simpleRef);
    }

    private static void referenceFrom(final Map<String, Set<String>> allReferences, final String from, final RefProperty to) {
        final String simpleRef = to.getSimpleRef();
        allReferences.putIfAbsent(from, new HashSet<>());
        allReferences.get(from).add(simpleRef);
    }

}
