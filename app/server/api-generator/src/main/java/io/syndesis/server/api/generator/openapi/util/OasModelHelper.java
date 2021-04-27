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

package io.syndesis.server.api.generator.openapi.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.apicurio.datamodels.core.models.Extension;
import io.apicurio.datamodels.openapi.models.IOasPropertySchema;
import io.apicurio.datamodels.openapi.models.OasDocument;
import io.apicurio.datamodels.openapi.models.OasOperation;
import io.apicurio.datamodels.openapi.models.OasParameter;
import io.apicurio.datamodels.openapi.models.OasPathItem;
import io.apicurio.datamodels.openapi.models.OasPaths;
import io.apicurio.datamodels.openapi.models.OasSchema;
import org.apache.commons.lang3.StringUtils;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.trimToNull;

public final class OasModelHelper {

    public static final String URL_EXTENSION = "x-syndesis-swagger-url";
    private static final Pattern JSONDB_DISALLOWED_KEY_CHARS = Pattern.compile("[^ -\"&-\\-0-Z\\^-\u007E\u0080-\u10FFFF]");

    private OasModelHelper() {
        // utility class
    }

    public static OperationDescription operationDescriptionOf(final OasDocument openApiDoc, final OasOperation operation,
                                                              final BiFunction<String, String, String> consumer) {
        final List<OasPathItem> pathItems = getPathItems(openApiDoc.paths);
        for (OasPathItem pathEntry : pathItems) {
            final String path = pathEntry.getPath();
            final Map<String, OasOperation> operations = getOperationMap(pathEntry);
            for (Map.Entry<String, OasOperation> operationEntry : operations.entrySet()) {
                if (operationEntry.getValue().equals(operation)) {
                    final String method = operationEntry.getKey().toUpperCase(Locale.US);

                    final String specifiedSummary = trimToNull(operation.summary);
                    final String specifiedDescription = trimToNull(operation.description);

                    final String name = ofNullable(toLiteralNull(specifiedSummary)).orElseGet(() -> method + " " + path);
                    final String description = ofNullable(toLiteralNull(specifiedDescription))
                        .orElseGet(() -> consumer.apply(method, path));

                    return new OperationDescription(name, description);
                }
            }
        }

        throw new IllegalArgumentException(String.format("Unable to find operation '%s' in given paths in OpenAPI document", operation.operationId));
    }

    /**
     * Iterate through list of generic path items and collect path items of given type.
     * @param paths given path items.
     * @return typed list of path items.
     */
    public static List<OasPathItem> getPathItems(OasPaths paths) {
        if (paths == null) {
            return Collections.emptyList();
        }

        return paths.getItems();
    }

    public static List<OasParameter> getParameters(final OasOperation operation) {
        if (operation == null || operation.getParameters() == null) {
            return new ArrayList<>();
        }

        return operation.getParameters();
    }

    public static List<OasParameter> getParameters(final OasPathItem pathItem) {
        if (pathItem == null || pathItem.getParameters() == null) {
            return new ArrayList<>();
        }

        return pathItem.getParameters();
    }

    /**
     * Makes sure that the tag used as a key in a JSON object is a valid key
     * determined by
     * io.syndesis.server.jsondb.impl.JsonRecordSupport::validateKey.
     */
    public static String sanitizeTag(final String tag) {
        if (StringUtils.isEmpty(tag)) {
            return null;
        }

        final String sanitized = JSONDB_DISALLOWED_KEY_CHARS.matcher(tag).replaceAll("").trim();
        // 768 is maximum length for keys JSONDB supports
        if (sanitized.length() > 768) {
            return sanitized.substring(0, Math.min(tag.length(), 768));
        }

        return sanitized;
    }

    public static Stream<String> sanitizeTags(final List<String> list) {
        if (list == null || list.isEmpty()) {
            return Stream.empty();
        }

        return list.stream().map(OasModelHelper::sanitizeTag).filter(Objects::nonNull).distinct();
    }

    private static String toLiteralNull(final String given) {
        if (given == null) {
            return null;
        }

        // Swagger parser sometimes interprets empty strings as literal `"null"`
        // strings
        if ("null".equals(given)) {
            return null;
        }

        return given;
    }

    /**
     * Construct map of all specified operations for given path item. Only non-null operations are added to the
     * map where the key is the http method name.
     * @param pathItem path holding operations.
     * @return map of operations on the given path where Http method name is the key.
     */
    public static Map<String, OasOperation> getOperationMap(OasPathItem pathItem) {
        Map<String, OasOperation> operations = new LinkedHashMap<>();

        if (pathItem.get != null) {
            operations.put("get", pathItem.get);
        }

        if (pathItem.put != null) {
            operations.put("put", pathItem.put);
        }

        if (pathItem.post != null) {
            operations.put("post", pathItem.post);
        }

        if (pathItem.delete != null) {
            operations.put("delete", pathItem.delete);
        }

        if (pathItem.options != null) {
            operations.put("options", pathItem.options);
        }

        if (pathItem.head != null) {
            operations.put("head", pathItem.head);
        }

        if (pathItem.patch != null) {
            operations.put("patch", pathItem.patch);
        }

        return operations;
    }

    /**
     * Construct map of all specified operations for given path item. Only non-null operations are added to the
     * map where the key is the http method name.
     * @param pathItem path holding operations.
     * @param type operation type for version 2.x or 3.x.
     * @return map of operations on the given path where Http method name is the key.
     */
    public static <T extends OasOperation> Map<String, T> getOperationMap(OasPathItem pathItem, Class<T> type) {
        return getOperationMap(pathItem)
            .entrySet()
            .stream()
            .filter(entry -> type.isInstance(entry.getValue()))
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> type.cast(entry.getValue()),
                (u, v) -> {
                    throw new IllegalStateException(String.format("Duplicate key %s", u));
                },
                LinkedHashMap::new));
    }

    /**
     * Get pure name from reference path. Usually reference definitions start with '#/definitions/' for OpenAPI 2.x and
     * '#/components/schemas/' for OpenAPI 3.x and this method removes the basic reference path part and just returns the
     * reference object name.
     * @param reference path expression.
     * @return the name of the reference object.
     */
    public static String getReferenceName(String reference) {
        if (reference != null) {
            return reference.replaceAll("^.*/", "");
        }

        return null;
    }

    /**
     * Determines if given schema is of type array.
     * @param schema to check
     * @return true if given schema is an array.
     */
    public static boolean isArrayType(OasSchema schema) {
        return "array".equals(schema.type);
    }

    /**
     * Determines if given schema has a reference to another schema object.
     * @param schema to check
     * @return true if given schema has a reference.
     */
    public static boolean isReferenceType(OasSchema schema) {
        return schema.$ref != null;
    }

    /**
     * Determines if given parameter has a reference to another schema object.
     * @param parameter to check
     * @return true if given parameter has a reference.
     */
    public static boolean isReferenceType(OasParameter parameter) {
        return parameter.$ref != null;
    }

    /**
     * Determines if given parameter lives in body.
     * @param parameter to check.
     * @return true if given parameter is a body parameter.
     */
    public static boolean isBody(OasParameter parameter) {
        return "body".equals(parameter.in);
    }

    /**
     * Applies property name getter on given schema object if applicable. Otherwise use null.
     * @param schema the provided schema
     * @return property name or null.
     */
    public static String getPropertyName(OasSchema schema) {
        return getPropertyName(schema, null);
    }

    /**
     * Applies property name getter on given schema object if applicable. Otherwise use given default name.
     * @param schema the provided schema.
     * @param defaultName the default name if property name is not applicable.
     * @return property name or default value.
     */
    public static String getPropertyName(OasSchema schema, String defaultName) {
        return ofNullable(schema)
            .filter(IOasPropertySchema.class::isInstance)
            .map(IOasPropertySchema.class::cast)
            .map(IOasPropertySchema::getPropertyName)
            .orElse(defaultName);
    }

    /**
     * Checks for serializable nature of given parameter. These are parameters that live in
     * form data, query, header, request path or cookies.
     * @param parameter to check.
     * @return true if parameter is serializable.
     */
    public static boolean isSerializable(OasParameter parameter) {
        return Arrays.asList("formData", "query", "header", "path", "cookie").contains(parameter.in);
    }

    public static boolean schemaIsNotSpecified(final OasSchema schema) {
        if (schema == null) {
            return true;
        }

        if (isArrayType(schema)) {
            return schema.items == null;
        }

        final Map<String, OasSchema> properties = schema.properties;
        final boolean noProperties = properties == null || properties.isEmpty();
        final boolean noReference = schema.$ref == null;
        return noProperties && noReference;
    }

    public static URI specificationUriFrom(final OasDocument openApiDoc) {
        final Collection<Extension> vendorExtensions = openApiDoc.getExtensions();

        if (vendorExtensions == null) {
            return null;
        }

        return vendorExtensions.stream().filter(extension -> URL_EXTENSION.equals(extension.name))
            .map(extension -> (URI) extension.value)
            .findFirst()
            .orElse(null);
    }
}
