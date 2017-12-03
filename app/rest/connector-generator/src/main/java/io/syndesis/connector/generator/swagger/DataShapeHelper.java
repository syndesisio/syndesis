/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.connector.generator.swagger;

import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.swagger.models.ArrayModel;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Response;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import io.syndesis.core.Json;
import io.syndesis.model.DataShape;

final class DataShapeHelper {

    /* default */ static final DataShape DATA_SHAPE_NONE = new DataShape.Builder().kind("none").build();

    private DataShapeHelper() {
        // utility class
    }

    /* default */ static DataShape createShapeFromModel(final String specification, final Model schema) {
        if (schema instanceof ArrayModel) {
            final Property items = ((ArrayModel) schema).getItems();

            return createShapeFromProperty(specification, items);
        } else if (schema instanceof ModelImpl) {
            return createShapeFromModelImpl(schema);
        }

        final String title = Optional.ofNullable(schema.getTitle())
            .orElse(schema.getReference().replaceAll("^.*/", ""));

        return createShapeFromReference(specification, title, schema.getReference());
    }

    /* default */ static DataShape createShapeFromResponse(final String specification, final Response response) {
        final Property schema = response.getSchema();

        return createShapeFromProperty(specification, schema);
    }

    private static DataShape createShapeFromModelImpl(final Model schema) {
        try {
            final String schemaString = Json.mapper().writeValueAsString(schema);

            return new DataShape.Builder().kind("json-schema").specification(schemaString).build();
        } catch (final JsonProcessingException e) {
            throw new IllegalStateException(
                "Unable to serialize given JSON specification in response schema: " + schema, e);
        }
    }

    private static DataShape createShapeFromProperty(final String specification, final Property schema) {
        if (schema instanceof MapProperty) {
            try {
                final String schemaString = Json.mapper().writeValueAsString(schema);

                return new DataShape.Builder().kind("json-schema").specification(schemaString).build();
            } catch (final JsonProcessingException e) {
                throw new IllegalStateException(
                    "Unable to serialize given JSON specification in response schema: " + schema, e);
            }
        } else if (schema instanceof StringProperty) {
            return DATA_SHAPE_NONE;
        }

        final String reference = determineSchemaReference(schema);

        final String title = Optional.ofNullable(schema.getTitle()).orElse(reference.replaceAll("^.*/", ""));

        return createShapeFromReference(specification, title, reference);
    }

    private static DataShape createShapeFromReference(final String specification, final String title,
        final String reference) {
        final String jsonSchema = JsonSchemaHelper.resolveSchemaForReference(specification, title, reference);

        return new DataShape.Builder().kind("json-schema").specification(jsonSchema).build();
    }

    private static String determineSchemaReference(final Property schema) {
        if (schema instanceof RefProperty) {
            return ((RefProperty) schema).get$ref();
        } else if (schema instanceof ArrayProperty) {
            final Property property = ((ArrayProperty) schema).getItems();

            return determineSchemaReference(property);
        }

        throw new IllegalArgumentException("Only references to schemas are supported");
    }

}
