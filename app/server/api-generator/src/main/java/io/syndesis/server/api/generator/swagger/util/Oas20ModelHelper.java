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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.load.configuration.LoadingConfiguration;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.core.models.Document;
import io.apicurio.datamodels.openapi.models.IOasPropertySchema;
import io.apicurio.datamodels.openapi.models.OasOperation;
import io.apicurio.datamodels.openapi.models.OasParameter;
import io.apicurio.datamodels.openapi.models.OasPathItem;
import io.apicurio.datamodels.openapi.models.OasPaths;
import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.v2.models.Oas20Document;
import io.apicurio.datamodels.openapi.v2.models.Oas20Operation;
import io.apicurio.datamodels.openapi.v2.models.Oas20Parameter;
import io.apicurio.datamodels.openapi.v2.models.Oas20PathItem;
import io.apicurio.datamodels.openapi.v2.models.Oas20SchemaDefinition;
import io.syndesis.common.model.Violation;
import io.syndesis.common.util.IOStreams;
import io.syndesis.common.util.Resources;
import io.syndesis.common.util.json.JsonUtils;
import io.syndesis.server.api.generator.APIValidationContext;
import io.syndesis.server.api.generator.swagger.OpenApiModelInfo;
import io.syndesis.server.api.generator.swagger.OpenApiValidationRules;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.trimToNull;

public final class Oas20ModelHelper {

    private static final Pattern JSON_TEST = Pattern.compile("^\\s*\\{.*");

    private static final Pattern JSONDB_DISALLOWED_KEY_CHARS = Pattern.compile("[^ -\"&-\\-0-Z\\^-\u007E\u0080-\u10FFFF]");

    private static final Logger LOG = LoggerFactory.getLogger(Oas20ModelHelper.class);

    private static final JsonSchema SWAGGER_2_0_SCHEMA;

    private static final String SWAGGER_2_0_SCHEMA_FILE = "schema/swagger-2.0-schema.json";

    private static final String SWAGGER_IO_V2_SCHEMA_URI = "http://swagger.io/v2/schema.json#";

    private static final Yaml YAML_PARSER = new Yaml();

    static {
        try {
            final JsonNode swagger20Schema = JsonUtils.reader().readTree(Resources.getResourceAsText(SWAGGER_2_0_SCHEMA_FILE));
            final LoadingConfiguration loadingConfiguration = LoadingConfiguration.newBuilder()
                .preloadSchema(SWAGGER_IO_V2_SCHEMA_URI, swagger20Schema)
                .freeze();
            SWAGGER_2_0_SCHEMA = JsonSchemaFactory.newBuilder().setLoadingConfiguration(loadingConfiguration).freeze().getJsonSchema(SWAGGER_IO_V2_SCHEMA_URI);
        } catch (final ProcessingException | IOException ex) {
            throw new IllegalStateException("Unable to load the schema file embedded in the artifact", ex);
        }
    }

    private Oas20ModelHelper() {
        // utility class
    }

    public static Oas20SchemaDefinition dereference(final OasSchema model, final Oas20Document openApiDoc) {
        String reference = getReferenceName(model.$ref);
        return openApiDoc.definitions.getDefinition(reference);
    }

    /**
     * Removes all properties from the given Swagger document that are not used
     * by the REST Swagger Camel component in order to minimize the amount of
     * data stored in the configured properties.
     */
    public static String minimalOpenApiUsedByComponent(final Oas20Document openApiDoc) {
        final ObjectNode json = (ObjectNode) Library.writeNode(openApiDoc);
        json.remove(Arrays.asList("info", "tags", "definitions", "externalDocs"));

        final JsonNode paths = json.get("paths");

        if (paths != null) {
            paths.forEach(path -> {
                JsonNode globalParameters = ((ObjectNode)path).remove("parameters");
                final List<JsonNode> globalParametersList = new ArrayList<>();

                if (globalParameters != null) {
                    final List<JsonNode> parametersList = StreamSupport.stream(globalParameters.spliterator(), false).collect(Collectors.toList());
                    for (JsonNode jsonNode : parametersList) {
                        final ObjectNode param = (ObjectNode) jsonNode;
                        param.remove(Arrays.asList("description", "type", "required", "format"));

                        if (isPathOrQueryParameter(param)) {
                            globalParametersList.add(param);
                        }
                    }

                    ((ArrayNode) globalParameters).removeAll();
                    ((ArrayNode) globalParameters).addAll(globalParametersList);
                }

                StreamSupport.stream(path.spliterator(), false)
                    .filter(JsonNode::isObject)
                    .forEach(operation -> {
                    final ObjectNode operationNode = (ObjectNode) operation;
                    operationNode.remove(Arrays.asList("tags", "summary", "description"));
                    final ArrayNode parameters = (ArrayNode) operation.get("parameters");

                    if (parameters != null) {
                        final List<JsonNode> parametersList = StreamSupport.stream(parameters.spliterator(), false).collect(Collectors.toList());
                        for (final ListIterator<JsonNode> i = parametersList.listIterator(); i.hasNext();) {
                            final ObjectNode param = (ObjectNode) i.next();
                            param.remove(Arrays.asList("description", "type", "required", "format"));

                            if (!isPathOrQueryParameter(param)) {
                                i.remove();
                            }
                        }

                        if (parameters.size() == 0 && globalParametersList.isEmpty()) {
                            operationNode.remove("parameters");
                        }

                        if (parametersList.isEmpty() && globalParametersList.isEmpty()) {
                            operationNode.remove("parameters");
                        } else {
                            parameters.removeAll();
                            parameters.addAll(parametersList);
                            parameters.addAll(globalParametersList);
                        }
                    } else if (!globalParametersList.isEmpty()) {
                        operationNode.set("parameters", globalParameters);
                    }

                    operationNode.remove("responses");
                });
            });
        }

        try {
            return JsonUtils.writer().writeValueAsString(json);
        } catch (final JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize minified OpenAPI document", e);
        }
    }

    private static boolean isPathOrQueryParameter(ObjectNode param) {
        return param.get("in") != null && ("path".equals(param.get("in").textValue()) || "query".equals(param.get("in").textValue()));
    }

    public static OperationDescription operationDescriptionOf(final Oas20Document openApiDoc, final OasOperation operation,
        final BiFunction<String, String, String> consumer) {
        final List<Oas20PathItem> pathItems = getPathItems(openApiDoc.paths, Oas20PathItem.class);
        for (Oas20PathItem pathEntry : pathItems) {
            final String path = pathEntry.getPath();
            final Map<String, Oas20Operation> operations = Oas20ModelHelper.getOperationMap(pathEntry);
            for (Entry<String, Oas20Operation> operationEntry : operations.entrySet()) {
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
     * @param type the target path item type to collect.
     * @return typed list of path items.
     */
    public static <T extends OasPathItem> List<T> getPathItems(OasPaths paths, Class<T> type) {
        if (paths == null) {
            return Collections.emptyList();
        }

        return paths.getItems()
                .stream()
                .filter(type::isInstance)
                .map(type::cast)
                .collect(Collectors.toList());
    }

    /**
     * Iterate through list of generic path parameters on the given path item and collect those of given type.
     * @param pathItem given path item.
     * @param type the target path parameter type to collect.
     * @return typed list of path parameters.
     */
    public static <T extends OasParameter> List<T> getParameters(OasPathItem pathItem, Class<T> type) {
        if (pathItem == null || pathItem.getParameters() == null) {
            return new ArrayList<>();
        }

        return pathItem.getParameters()
                .stream()
                .filter(type::isInstance)
                .map(type::cast)
                .collect(Collectors.toList());
    }

    /**
     * Iterate through list of generic path parameters on the given operation and collect those of given type.
     * @param operation given path item.
     * @param type the target path parameter type to collect.
     * @return typed list of path parameters.
     */
    public static <T extends OasParameter> List<T> getParameters(OasOperation operation, Class<T> type) {
        if (operation == null || operation.getParameters() == null) {
            return new ArrayList<>();
        }

        return operation.getParameters()
                .stream()
                .filter(type::isInstance)
                .map(type::cast)
                .collect(Collectors.toList());
    }

    public static OpenApiModelInfo parse(final String specification, final APIValidationContext validationContext) {
        final OpenApiModelInfo.Builder resultBuilder = new OpenApiModelInfo.Builder();

        final String resolvedSpecification;
        try {
            resolvedSpecification = resolve(specification);
            resultBuilder.resolvedSpecification(resolvedSpecification);
        } catch (final Exception e) {
            LOG.debug("Unable to resolve OpenAPI document\n{}\n", specification, e);
            return resultBuilder
                .addError(new Violation.Builder().error("error").property("").message("Unable to resolve OpenAPI document from: "
                    + ofNullable(specification).map(s -> StringUtils.abbreviate(s, 100)).orElse("")).build())
                .build();
        }

        final JsonNode tree;
        try {
            tree = JsonUtils.reader().readTree(resolvedSpecification);
        } catch (final IOException e) {
            return new OpenApiModelInfo.Builder()
                .addError(new Violation.Builder()
                    .property("")
                    .error("ureadable-document")
                    .message("Unable to read OpenAPI document: " + e.getMessage())
                    .build())
                .build();
        }

        final JsonNode swaggerVersion = tree.get("swagger");
        if (swaggerVersion == null || swaggerVersion.isNull() || !"2.0".equals(swaggerVersion.textValue())) {
            return new OpenApiModelInfo.Builder()
                .addError(new Violation.Builder()
                    .property("")
                    .error("unsupported-version")
                    .message("This document cannot be uploaded. Provide an OpenAPI 2.0 document.")
                    .build())
                .build();
        }

        final Document parsed = Library.readDocumentFromJSONString(resolvedSpecification);
        if (!(parsed instanceof Oas20Document)) {
            LOG.debug("Unable to read OpenAPI document\n{}\n", specification);
            return resultBuilder
                .addError(new Violation.Builder().error("error").property("").message("Unable to read OpenAPI document from: '"
                    + StringUtils.abbreviate(specification, 100)).build())
                .build();
        }

        if (validationContext != APIValidationContext.NONE) {
            final OpenApiModelInfo swaggerModelInfo = validateJSonSchema(resolvedSpecification, (Oas20Document) parsed);
            return OpenApiValidationRules.get(validationContext).apply(swaggerModelInfo);
        }
        return resultBuilder.model((Oas20Document) parsed).build();
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

        return list.stream().map(Oas20ModelHelper::sanitizeTag).filter(Objects::nonNull).distinct();
    }

    static JsonNode convertToJson(final String specification) throws IOException, JsonProcessingException {
        final JsonNode specRoot;
        if (JSON_TEST.matcher(specification).matches()) {
            specRoot = JsonUtils.reader().readTree(specification);
        } else {
            specRoot = JsonUtils.convertValue(YAML_PARSER.load(specification), JsonNode.class);
        }
        return specRoot;
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    static String resolve(final String specification) throws Exception {
        final String specificationToUse;
        if (specification.toLowerCase(Locale.US).startsWith("http")) {
            specificationToUse = resolve(new URL(specification));
        } else {
            specificationToUse = specification;
        }

        final JsonNode node = convertToJson(specificationToUse);

        return JsonUtils.writer().writeValueAsString(node);
    }

    static String resolve(URL url) {
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            int status = con.getResponseCode();
            if (status > 299) {
                throw new IllegalStateException(String.format("Failed to retrieve Open API specification from %s", url),
                    new IOException(IOStreams.readText(con.getErrorStream())));
            } else {
                return IOStreams.readText(con.getInputStream());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to retrieve Open API specification: " + url.toString(), e);
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }

    private static boolean append(final List<Violation> violations, final ProcessingMessage message, final Optional<String> requiredLevel) {
        if (requiredLevel.isPresent()) {
            final Optional<String> level = ofNullable(message.asJson()).flatMap(node -> ofNullable(node.get("level")))
                .flatMap(node -> ofNullable(node.textValue()));

            if (!level.equals(requiredLevel)) {
                return false; // skip
            }
        }

        final Optional<String> property = ofNullable(message.asJson()).flatMap(node -> ofNullable(node.get("instance")))
            .flatMap(node -> ofNullable(node.get("pointer"))).flatMap(node -> ofNullable(node.textValue()));

        final Optional<String> error = ofNullable(message.asJson()).flatMap(node -> ofNullable(node.get("domain")))
            .flatMap(node -> ofNullable(node.textValue()));

        violations.add(new Violation.Builder().error(error.orElse("")).message(message.getMessage()).property(property.orElse("")).build());

        return true;
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

    private static OpenApiModelInfo validateJSonSchema(final String specification, final Oas20Document model) {
        try {
            final JsonNode specRoot = convertToJson(specification);
            final ProcessingReport report = SWAGGER_2_0_SCHEMA.validate(specRoot);
            final List<Violation> errors = new ArrayList<>();
            final List<Violation> warnings = new ArrayList<>();
            for (final ProcessingMessage message : report) {
                final boolean added = append(errors, message, Optional.of("error"));
                if (!added) {
                    append(warnings, message, Optional.empty());
                }
            }

            return new OpenApiModelInfo.Builder().errors(errors).warnings(warnings).model(model).resolvedSpecification(specification)
                .build();

        } catch (IOException | ProcessingException ex) {
            LOG.error("Unable to load the schema file embedded in the artifact", ex);
            return new OpenApiModelInfo.Builder().addError(new Violation.Builder().error("error").property("")
                .message("Unable to load the OpenAPI schema file embedded in the artifact").build()).build();
        }
    }

    /**
     * Construct map of all specified operations for given path item. Only non-null operations are added to the
     * map where the key is the http method name.
     * @param pathItem
     * @return
     */
    public static Map<String, Oas20Operation> getOperationMap(Oas20PathItem pathItem) {
        Map<String, Oas20Operation> operations = new LinkedHashMap<>();

        if (pathItem.get != null) {
            operations.put("get", (Oas20Operation) pathItem.get);
        }

        if (pathItem.put != null) {
            operations.put("put", (Oas20Operation) pathItem.put);
        }

        if (pathItem.post != null) {
            operations.put("post", (Oas20Operation) pathItem.post);
        }

        if (pathItem.delete != null) {
            operations.put("delete", (Oas20Operation) pathItem.delete);
        }

        if (pathItem.options != null) {
            operations.put("options", (Oas20Operation) pathItem.options);
        }

        if (pathItem.head != null) {
            operations.put("head", (Oas20Operation) pathItem.head);
        }

        if (pathItem.patch != null) {
            operations.put("patch", (Oas20Operation) pathItem.patch);
        }

        return operations;
    }

    /**
     * Get pure name from reference path. Usually reference definitions start with '#/definitions/'
     * and this method removes the basic reference path part and just returns the reference object name.
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
     * Determines if given parameter is of type array.
     * @param parameter to check
     * @return true if given parameter is an array.
     */
    public static boolean isArrayType(Oas20Parameter parameter) {
        return "array".equals(parameter.type);
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
}
