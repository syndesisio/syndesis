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

package io.syndesis.server.api.generator.openapi;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.openapi.models.OasDocument;
import io.apicurio.datamodels.openapi.models.OasOperation;
import io.apicurio.datamodels.openapi.models.OasSchema;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.DataShapeMetaData;
import io.syndesis.server.api.generator.openapi.util.JsonSchemaHelper;
import io.syndesis.server.api.generator.openapi.util.OasModelHelper;

public abstract class UnifiedJsonDataShapeSupport<T extends OasDocument, O extends OasOperation> implements DataShapeGenerator<T, O> {

    private static final List<String> PROPERTIES_TO_REMOVE_ON_MERGE = Arrays.asList("$schema", "title");

    protected ObjectNode createJsonSchemaForBodyOf(final ObjectNode json, final T openApiDoc, final O operation) {
        final Optional<NameAndSchema> maybeRequestBody = findBodySchema(openApiDoc, operation);

        if (!maybeRequestBody.isPresent()) {
            return null;
        }

        final OasSchema requestSchema = maybeRequestBody.get().schema;
        final String name = maybeRequestBody.get().name;

        return createSchemaFromModel(json, name, requestSchema);
    }

    protected static ObjectNode createSchemaFromModel(final ObjectNode json, final String name, final OasSchema schema) {
        if (OasModelHelper.isArrayType(schema)) {
            final OasSchema items = (OasSchema) schema.items;

            final ObjectNode itemSchema = createSchemaFromProperty(json, name, items);
            itemSchema.remove(Arrays.asList("$schema", "title"));

            final ObjectNode jsonSchema = JsonNodeFactory.instance.objectNode();
            jsonSchema.put("type", "array");
            jsonSchema.set("items", itemSchema);

            return jsonSchema;
        } else if (OasModelHelper.isReferenceType(schema)) {
            final String title = determineTitleOf(name, schema);
            return JsonSchemaHelper.resolveSchemaForReference(json, title, schema.$ref);
        }

        return createSchemaFromModelImpl(name, schema);
    }

    private static ObjectNode createSchemaFromProperty(final ObjectNode json, final String name, final OasSchema schema) {
        if ("object".equals(schema.type)) {
            try {
                return (ObjectNode) Library.writeNode(schema);
            } catch (final Exception e) {
                throw new IllegalStateException("Unable to serialize/read given JSON specification in response schema: " + schema, e);
            }
        } else if (OasModelHelper.isReferenceType(schema) || OasModelHelper.isArrayType(schema)) {
            final String reference = JsonSchemaHelper.determineSchemaReference(schema)
                .orElseThrow(() -> new IllegalArgumentException("Only references to schemas are supported"));
            final String title = determineTitleOf(name, schema);
            return JsonSchemaHelper.resolveSchemaForReference(json, title, reference);
        }

        return createPropertySchema(name, schema);
    }

    private static String determineTitleOf(final String name, final OasSchema schema) {
        final String title = schema.title;
        if (title != null) {
            return title;
        }

        final String description = schema.description;
        if (description != null) {
            return description;
        }

        final Optional<String> reference = JsonSchemaHelper.determineSchemaReference(schema);
        return reference.map(OasModelHelper::getReferenceName)
            .orElse(name);
    }

    private static ObjectNode createSchemaFromModelImpl(final String name, final OasSchema model) {
        final ObjectNode schema = (ObjectNode) Library.writeNode(model);
        JsonSchemaHelper.sanitize(schema);

        final String title = determineTitleOf(name, model);
        return JsonSchemaHelper.createJsonSchema(title, schema);
    }

    private static ObjectNode createPropertySchema(final String name, final OasSchema schema) {
        final ObjectNode jsonSchema = JsonNodeFactory.instance.objectNode();
        final String format = schema.format;
        if (JsonSchemaHelper.isKnownFormat(format)) {
            jsonSchema.put("format", format);
        }
        final String type = schema.type;
        if (type != null) {
            jsonSchema.put("type", type);
        }
        final String title = schema.title;
        if (title != null) {
            jsonSchema.put("title", title);
        } else {
            jsonSchema.put("title", name);
        }
        final String description = schema.description;
        if (description != null) {
            jsonSchema.put("description", description);
        }

        return jsonSchema;
    }

    protected static DataShape unifiedJsonSchema(final String name, final String description, final ObjectNode bodySchema, final ObjectNode parametersSchema) {
        if (bodySchema == null && parametersSchema == null) {
            return DATA_SHAPE_NONE;
        }

        final ObjectNode unifiedSchema = JsonSchemaHelper.newJsonObjectSchema();
        unifiedSchema.put("$id", "io:syndesis:wrapped");
        final ObjectNode properties = unifiedSchema.putObject("properties");

        if (parametersSchema != null) {
            properties.remove(PROPERTIES_TO_REMOVE_ON_MERGE);
            properties.set("parameters", parametersSchema.get("properties").get("parameters"));
        }

        if (bodySchema != null) {
            bodySchema.remove(PROPERTIES_TO_REMOVE_ON_MERGE);
            properties.set("body", bodySchema);
        }

        return new DataShape.Builder()//
            .name(name)//
            .description(description)//
            .kind(DataShapeKinds.JSON_SCHEMA)//
            .specification(JsonSchemaHelper.serializeJson(unifiedSchema))//
            .putMetadata(DataShapeMetaData.UNIFIED, "true")
            .build();
    }
}
