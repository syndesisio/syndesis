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
package io.syndesis.connector.generator.swagger.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import io.swagger.models.parameters.SerializableParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.syndesis.core.Json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import me.andrz.jackson.JsonReferenceException;
import me.andrz.jackson.JsonReferenceProcessor;

public final class JsonSchemaHelper {

    private JsonSchemaHelper() {
        // utility class
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

    public static String resolveSchemaForReference(final String specification, final String title, final String reference) {
        final JsonNode resolved;
        try {
            final URL inMemoryUrl = inMemory(specification);

            resolved = new JsonReferenceProcessor().process(inMemoryUrl);
        } catch (JsonReferenceException | IOException e) {
            throw new IllegalStateException("Unable to process JSON references", e);
        }

        final JsonNode node = resolved.at(reference.substring(1));
        final ObjectNode schemaNode = (ObjectNode) node;
        schemaNode.put("$schema", "http://json-schema.org/schema#");
        schemaNode.put("type", "object");
        schemaNode.put("title", title);

        return serializeJson(schemaNode);
    }

    public static String serializeJson(final ObjectNode schemaNode) {
        try {
            return Json.writer().writeValueAsString(schemaNode);
        } catch (final JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize JSON schema", e);
        }
    }

    /* default */ static URL inMemory(final String specification) throws MalformedURLException {
        return new URL("mem", null, 0, "specification", new InMemoryUrlStreamHandler(specification));
    }

    /* default */ static String javaTypeFor(final String type, final String format) {
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
