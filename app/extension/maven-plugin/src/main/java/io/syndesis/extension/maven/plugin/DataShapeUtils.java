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
package io.syndesis.extension.maven.plugin;

import java.util.Optional;
import java.util.function.Consumer;

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;

import com.fasterxml.jackson.databind.JsonNode;

import static io.syndesis.extension.maven.plugin.Utils.isNotEmpty;

final class DataShapeUtils {

    private DataShapeUtils() {
        // utility class
    }

    static Optional<DataShape> buildDataShape(final JsonNode root) {
        if (root == null) {
            return Optional.empty();
        }

        final DataShape.Builder builder = new DataShape.Builder();

        addKindAndTypeTo(builder, root);

        addValueTo(root, "name", builder::name);

        addValueTo(root, "description", builder::description);

        addValueTo(root, "specification", builder::specification);

        addMetadataTo(builder, root);

        addVariantsTo(builder, root);

        return Optional.of(builder.build());
    }

    private static void addKindAndTypeTo(final DataShape.Builder builder, final JsonNode root) {
        String kind = valueFromTextNodeOr(root, "kind", DataShapeKinds.NONE.toString());

        String type = valueFromTextNodeOr(root, "type", "");

        if (isNotEmpty(type)) {
            final int separator = type.indexOf(':');

            if (separator != -1) {
                kind = type.substring(0, separator);
                type = type.substring(separator + 1);
            }
        }

        if (isNotEmpty(kind)) {
            builder.kind(DataShapeKinds.fromString(kind));
        }

        if (isNotEmpty(type)) {
            builder.type(type);
        }
    }

    private static void addMetadataTo(final DataShape.Builder builder, final JsonNode root) {
        final JsonNode meta = root.get("metadata");
        if (meta != null) {
            for (final JsonNode node : meta) {
                final JsonNode key = node.get("key");
                final JsonNode val = node.get("value");

                if (key != null && val != null) {
                    builder.putMetadata(key.asText(), val.asText());
                }
            }
        }
    }

    private static void addValueTo(final JsonNode node, final String property, final Consumer<String> consumer) {
        final JsonNode jsonKind = node.get(property);

        if (jsonKind == null) {
            return;
        }

        final String value = jsonKind.asText();

        consumer.accept(value);
    }

    private static void addVariantsTo(final DataShape.Builder builder, final JsonNode root) {
        final JsonNode variants = root.get("variants");
        if (variants != null) {
            for (final JsonNode node : variants) {
                buildDataShape(node).ifPresent(builder::addVariant);
            }
        }
    }

    private static String valueFromTextNodeOr(final JsonNode node, final String property, final String alternative) {
        final JsonNode jsonKind = node.get(property);

        if (jsonKind == null) {
            return alternative;
        }

        return jsonKind.asText();
    }
}
