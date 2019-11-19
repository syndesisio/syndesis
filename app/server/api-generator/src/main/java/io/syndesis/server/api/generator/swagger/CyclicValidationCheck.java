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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.v2.models.Oas20Document;
import io.apicurio.datamodels.openapi.v2.models.Oas20SchemaDefinition;
import io.syndesis.server.api.generator.swagger.util.Oas20ModelHelper;

final class CyclicValidationCheck {

    private CyclicValidationCheck() {
        // utility class
    }

    static boolean hasCyclicReferences(final Oas20Document openApiDoc) {
        if (openApiDoc == null
            || openApiDoc.definitions == null
            || openApiDoc.definitions.getDefinitions().isEmpty()) {
            return false;
        }

        final Map<String, Set<String>> references = collectReferences(openApiDoc);

        return isCyclic(references);
    }

    private static Map<String, Set<String>> collectReferences(final Oas20Document openApiDoc) {
        final Map<String, Set<String>> references = new TreeMap<>();

        if (openApiDoc.definitions == null) {
            return references;
        }

        List<Oas20SchemaDefinition> schemaDefinitions = openApiDoc.definitions.getDefinitions();
        for (final Oas20SchemaDefinition definition : schemaDefinitions) {
            final String name = definition.getName();
            references.putIfAbsent(name, new HashSet<>());

            if (Oas20ModelHelper.isReferenceType(definition)) {
                referenceFrom(references, name, definition);
            } else if (Oas20ModelHelper.isArrayType(definition)) {
                final OasSchema property = (OasSchema) definition.items;
                collectReferencesFromProperty(references, name, property);
            } else {
                collectReferencesFromProperties(references, name, definition.properties);
            }
        }
        return references;
    }

    private static void collectReferencesFromProperties(final Map<String, Set<String>> allReferences, final String from,
        final Map<String, OasSchema> properties) {
        if (properties == null) {
            return;
        }

        for (final OasSchema nestedProperty : properties.values()) {
            collectReferencesFromProperty(allReferences, from, nestedProperty);
        }
    }

    private static void collectReferencesFromProperty(final Map<String, Set<String>> allReferences, final String from, final OasSchema property) {
        if (Oas20ModelHelper.isReferenceType(property)) {
            referenceFrom(allReferences, from, property);
        } else if ("object".equals(property.type)) {
            collectReferencesFromProperties(allReferences, from, property.properties);
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

    private static void referenceFrom(final Map<String, Set<String>> allReferences, final String from, final OasSchema to) {
        final String simpleRef = Oas20ModelHelper.getReferenceName(to.$ref);
        allReferences.putIfAbsent(from, new HashSet<>());
        allReferences.get(from).add(simpleRef);
    }

}
