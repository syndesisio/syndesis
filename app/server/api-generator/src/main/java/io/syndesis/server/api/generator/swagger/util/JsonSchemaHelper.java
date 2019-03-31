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
package io.syndesis.server.api.generator.swagger.util;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.models.parameters.SerializableParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.syndesis.common.util.Json;
import me.andrz.jackson.JsonContext;
import me.andrz.jackson.JsonReferenceException;
import me.andrz.jackson.JsonReferenceProcessor;
import org.apache.commons.lang3.StringUtils;

public final class JsonSchemaHelper {

    private static final String JSON_SCHEMA_URI = "http://json-schema.org/schema#";

    private JsonSchemaHelper() {
        // utility class
    }

    public static ObjectNode createJsonSchema(final String title, final ObjectNode schema) {
        final ObjectNode schemaNode = newJsonObjectSchema();
        if (title != null) {
            schemaNode.put("title", title);
        }

        schemaNode.setAll(schema);

        return schemaNode;
    }

    public static String determineSchemaReference(final Property schema) {
        if (schema instanceof RefProperty) {
            return ((RefProperty) schema).get$ref();
        } else if (schema instanceof ArrayProperty) {
            final Property property = ((ArrayProperty) schema).getItems();

            return determineSchemaReference(property);
        }

        throw new IllegalArgumentException("Only references to schemas are supported");
    }

    public static String javaTypeFor(final SerializableParameter parameter) {
        final String type = parameter.getType();

        if ("array".equals(type)) {
            final Property items = parameter.getItems();
            final String elementType = items.getType();
            final String elementFormat = items.getFormat();

            return javaTypeFor(elementType, elementFormat) + "[]";
        }

        final String format = parameter.getFormat();
        return javaTypeFor(type, format);
    }

    public static ObjectNode newJsonObjectSchema() {
        final ObjectNode schema = JsonNodeFactory.instance.objectNode();
        schema.put("$schema", JSON_SCHEMA_URI);
        schema.put("type", "object");

        return schema;
    }

    public static ObjectNode parseJsonSchema(final String schema) {
        try {
            return (ObjectNode) Json.reader().readTree(schema);
        } catch (final IOException e) {
            throw new IllegalStateException("Unable to parse given JSON schema: " + StringUtils.abbreviate(schema, 100), e);
        }
    }

    public static ObjectNode resolvableNodeForSpecification(final ObjectNode json) {
        final ObjectNode resolved;
        try {
            final JsonReferenceProcessor referenceProcessor = new JsonReferenceProcessor();
            resolved = (ObjectNode) referenceProcessor.process(new JsonContext(DummyStreamHandler.DUMMY_URL) {
                @Override
                public JsonNode getDocument() throws IOException {
                    return json;
                }
            }, json);

            sanitize(resolved);
        } catch (JsonReferenceException | IOException e) {
            throw new IllegalStateException("Unable to process JSON references", e);
        }
        return resolved;
    }

    public static ObjectNode resolveSchemaForReference(final ObjectNode json, final String title, final String reference) {
        final ObjectNode dereferenced = (ObjectNode) json.at(reference.substring(1));

        return createJsonSchema(title, dereferenced);
    }

    public static String serializeJson(final JsonNode schemaNode) {
        try {
            return Json.writer().writeValueAsString(schemaNode);
        } catch (final JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize JSON schema", e);
        }
    }

    public static JsonNode sanitize(final JsonNode node) {
        if (node == null) {
            return null;
        }

        if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            Iterator<JsonNode> elements = arrayNode.elements();
            elements.forEachRemaining(JsonSchemaHelper::sanitize);
        } else if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;

            JsonNode formatNode = node.get("format");
            if (formatNode != null &&
                    formatNode.isTextual() &&
                    !isKnownFormat(formatNode.asText())) {
                objectNode.remove("format");
            }

            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
            fields.forEachRemaining(field -> sanitize(field.getValue()));
        }

        return node;
    }

    public static boolean isKnownFormat(String format) {
        return format != null && Stream.of(JsonValueFormat.values())
                .map(Objects::toString)
                .anyMatch(jsonSchemaFormat -> jsonSchemaFormat.equals(format));
    }

    static String javaTypeFor(final String type, final String format) {
        switch (type) {
        case "string":
            return String.class.getName();
        case "number":
            if ("float".equals(format)) {
                return Float.class.getName();
            }

            return Double.class.getName();
        case "integer":
            if ("int64".equals(format)) {
                return Long.class.getName();
            }

            return Integer.class.getName();
        case "boolean":
            return Boolean.class.getName();
        case "file":
            return File.class.getName();
        default:
            throw new IllegalArgumentException("Given parameter is of unknown type/format: " + type + "/" + format);
        }
    }

}
