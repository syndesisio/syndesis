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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ArraySchema;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JsonSchemaInspector implements Inspector {

    private static final Logger LOG = LoggerFactory.getLogger(JsonSchemaInspector.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String ARRAY_CONTEXT = "[]";
    static final List<String> COLLECTION_PATHS = Collections.singletonList("size()");

    @Override
    public List<String> getPaths(final String kind, final String type, final String specification,
        final Optional<byte[]> exemplar) {
        final JsonSchema schema;
        try {
            schema = MAPPER.readerFor(JsonSchema.class).readValue(specification);
        } catch (final IOException e) {
            LOG.warn(
                "Unable to parse the given JSON schema, increase log level to DEBUG to see the schema being parsed");
            LOG.debug(specification);

            return Collections.emptyList();
        }

        String context = null;
        final List<String> paths = new ArrayList<>();
        ObjectSchema objectSchema;
        if (schema.isObjectSchema()) {
            objectSchema = schema.asObjectSchema();
        } else if (schema.isArraySchema()) {
            objectSchema = getItemSchema(schema.asArraySchema());
            // add collection specific paths
            paths.addAll(COLLECTION_PATHS);
            context = ARRAY_CONTEXT;
        } else {
            throw new IllegalStateException(String.format("Unexpected schema type %s - expected object or array schema", schema.getType()));
        }

        if (objectSchema != null) {
            final Map<String, JsonSchema> properties = objectSchema.getProperties();
            fetchPaths(context, paths, properties);
        }

        return paths;
    }

    @Override
    public boolean supports(final String kind, final String type, final String specification,
        final Optional<byte[]> exemplar) {
        return "json-schema".equals(kind) && !StringUtils.isEmpty(specification);
    }

    static void fetchPaths(final String context, final List<String> paths, final Map<String, JsonSchema> properties) {
        for (final Entry<String, JsonSchema> entry : properties.entrySet()) {
            final JsonSchema subschema = entry.getValue();

            String path;
            final String key = entry.getKey();
            if (context == null) {
                path = key;
            } else {
                path = context + "." + key;
            }

            if (subschema.isValueTypeSchema()) {
                paths.add(path);
            } else if (subschema.isObjectSchema()) {
                fetchPaths(path, paths, subschema.asObjectSchema().getProperties());
            } else if (subschema.isArraySchema()) {
                COLLECTION_PATHS.stream().map(p -> path + "." + p).forEach(paths::add);
                fetchPaths(path + ARRAY_CONTEXT, paths, getItemSchema(subschema.asArraySchema()).getProperties());
            }
        }
    }

    /**
     * Extract item schema from array schema. Only supports single item array schema.
     * @param arraySchema schema as array schema
     * @return the nested item schema
     */
    private static ObjectSchema getItemSchema(ArraySchema arraySchema) {
        if (arraySchema.getItems().isSingleItems()) {
            return arraySchema.getItems().asSingleItems().getSchema().asObjectSchema();
        } else {
            throw new IllegalStateException("Unexpected array schema type - expected single item schema");
        }
    }
}
