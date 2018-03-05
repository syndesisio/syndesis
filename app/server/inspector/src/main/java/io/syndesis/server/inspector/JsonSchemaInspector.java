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
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JsonSchemaInspector implements Inspector {

    private static final Logger LOG = LoggerFactory.getLogger(JsonSchemaInspector.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public List<String> getPaths(final String kind, final String type, final String specification,
        final Optional<byte[]> exemplar) {
        final ObjectSchema schema;
        try {
            schema = MAPPER.readerFor(ObjectSchema.class).readValue(specification);
        } catch (final IOException e) {
            LOG.warn(
                "Unable to parse the given JSON schema, increase log level to DEBUG to see the schema being parsed");
            LOG.debug(specification);

            return Collections.emptyList();
        }

        final Map<String, JsonSchema> properties = schema.getProperties();

        final List<String> paths = new ArrayList<>();
        fetchPaths(null, paths, properties);

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
                fetchPaths(path, paths, ((ObjectSchema) subschema).getProperties());
            }
        }
    }

}
