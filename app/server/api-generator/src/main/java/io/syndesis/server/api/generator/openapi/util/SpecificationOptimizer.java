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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.StreamSupport;

import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.openapi.models.OasDocument;
import io.syndesis.common.util.json.JsonUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class SpecificationOptimizer {

    private static final List<String> COMPONENTS_PROPERTIES_TO_REMOVE = Arrays.asList("schemas", "responses", "requestBodies", "examples", "headers", "links",
        "callbacks", "parameters");

    private static final List<String> GLOBAL_PROPERTIES_TO_REMOVE = Arrays.asList("info", "tags", "definitions", "responses", "externalDocs");

    private static final List<String> OPERATION_PROPERTIES_TO_REMOVE = Arrays.asList("tags", "summary", "description", "externalDocs", "callbacks", "servers",
        "requestBody", "responses");

    private static final List<String> PARAMETER_PROPERTIES_TO_REMOVE = Arrays.asList("description", "type", "required", "format", "$ref");

    private SpecificationOptimizer() {
        // utility class
    }

    /**
     * Removes all properties from the given Swagger document that are not used
     * by the REST Swagger Camel component in order to minimize the amount of
     * data stored in the configured properties.
     */
    public static String minimizeForComponent(final OasDocument openApiDoc) {
        final ObjectNode json = (ObjectNode) Library.writeNode(openApiDoc);

        final JsonNode paths = json.get("paths");

        minimizePaths(json, paths);

        // after resolving and minimizing the parameters we can minimize bits in
        // the components...
        final JsonNode components = json.get("components");
        if (components != null) {
            ((ObjectNode) components).remove(COMPONENTS_PROPERTIES_TO_REMOVE);
        }

        // ...and definitions
        json.remove(GLOBAL_PROPERTIES_TO_REMOVE);

        try {
            return JsonUtils.writer().writeValueAsString(json);
        } catch (final JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize minified OpenAPI document", e);
        }
    }

    private static void addAllTo(final ArrayNode source, final ArrayNode target) {
        if (source == null) {
            return;
        }

        target.addAll(source);
    }

    private static ArrayNode concat(final ArrayNode pathParameters, final ArrayNode operationParameters) {
        int capacity = 0;
        if (pathParameters != null) {
            capacity = pathParameters.size();
        }
        if (operationParameters != null) {
            capacity += operationParameters.size();
        }

        final ArrayNode parameters = new ArrayNode(JsonNodeFactory.instance, capacity);

        addAllTo(pathParameters, parameters);
        addAllTo(operationParameters, parameters);

        return parameters;
    }

    private static boolean isHeaderParameter(final JsonNode parameter) {
        return "header".equals(parameterPlacement(parameter));
    }

    private static boolean isQueryOrPathParameter(final JsonNode parameter) {
        final String parameterPlacement = parameterPlacement(parameter);

        return "query".equals(parameterPlacement) || "path".equals(parameterPlacement);
    }

    private static boolean isQueryPathOrHeaderParameter(final JsonNode parameter) {
        return isHeaderParameter(parameter) || isQueryOrPathParameter(parameter);
    }

    private static void keepHeaderParameters(final ArrayNode parameters) {
        if (parameters == null) {
            return;
        }

        for (final Iterator<JsonNode> i = parameters.iterator(); i.hasNext();) {
            final ObjectNode parameter = (ObjectNode) i.next();
            if (!isHeaderParameter(parameter)) {
                i.remove();
            }
        }
    }

    /**
     * The Camel component doesn't resolve references -- "$ref" properties,
     * pointing to a definition (OpenAPI 2) or components/parameters (OpenAPI
     * 3), so this removes properites that are not needed by the connector but
     * also needs to resolve parameters pointed to by references.
     */
    private static void minimizeAndResolveParameters(final JsonNode root, final ArrayNode parameters) {
        if (parameters == null) {
            return;
        }

        for (final JsonNode parameter : parameters) {
            if ("body".equals(parameterPlacement(parameter))) {
                // we don't care about body parameters, the data shape holds
                // that information already, and the Camel component and the
                // connector implementation don't require it. The body
                // parameters will be filtered out anyhow.
                continue;
            }

            final ObjectNode param = resolve(root, (ObjectNode) parameter);
            param.remove(PARAMETER_PROPERTIES_TO_REMOVE);
        }
    }

    private static void minimizePaths(final JsonNode root, final JsonNode paths) {
        if (paths == null) {
            return;
        }

        paths.forEach(path -> {
            final ArrayNode pathParameters = (ArrayNode) ((ObjectNode) path).get("parameters");
            // we include only header parameters on the path, this resolves and
            // minimizes but (currently) keeps query and path parameters, below
            // only headers will be kept
            minimizeAndResolveParameters(root, pathParameters);

            StreamSupport.stream(path.spliterator(), false)
                .filter(JsonNode::isObject)
                .forEach(operation -> {
                    final ObjectNode operationNode = (ObjectNode) operation;
                    operationNode.remove(OPERATION_PROPERTIES_TO_REMOVE);
                    final ArrayNode parameters = (ArrayNode) operation.get("parameters");
                    minimizeAndResolveParameters(root, parameters);

                    // operation needs to hold query and path parameters, the
                    // Camel component doesn't resolve and doesn't consult path
                    // parameters, so query and path parameters from both the
                    // path and the operation need to be kept, the connector's
                    // header customizer consults both path and operation
                    // parameters so we can keep header parameters from the
                    // original operation
                    operationNode.replace("parameters", concat(queryAndPathParametersFrom(pathParameters), queryPathAndHeaderParametersFrom(parameters)));
                });

            // we have copied path query and path parameters to the operation,
            // now we filter out anything but header parameters; Camel component
            // cares about query and path parameters, but doesn't resolve and
            // considers only operation parameters, on the other hand
            // connector's customizers consult both operation and path
            // parameters but need only header parameters so we can keep those
            // here
            keepHeaderParameters(pathParameters);
        });
    }

    private static String parameterPlacement(final JsonNode parameter) {
        final JsonNode in = parameter.get("in");
        if (in == null) {
            return null;
        }

        return in.textValue();
    }

    private static ArrayNode queryAndPathParametersFrom(final ArrayNode parameters) {
        if (parameters == null) {
            return null;
        }

        final ArrayNode ret = JsonNodeFactory.instance.arrayNode(parameters.size());
        for (final JsonNode parameter : parameters) {
            if (isQueryOrPathParameter(parameter)) {
                ret.add(parameter);
            }
        }

        return ret;
    }

    private static ArrayNode queryPathAndHeaderParametersFrom(final ArrayNode parameters) {
        if (parameters == null) {
            return null;
        }

        final ArrayNode ret = JsonNodeFactory.instance.arrayNode(parameters.size());
        for (final JsonNode parameter : parameters) {
            if (isQueryPathOrHeaderParameter(parameter)) {
                ret.add(parameter);
            }
        }

        return ret;
    }

    private static ObjectNode resolve(final JsonNode root, final ObjectNode param) {
        final JsonNode ref = param.get("$ref");
        if (ref == null) {
            return param;
        }

        final String pointer = ref.textValue();
        if (pointer == null) {
            return param;
        }

        String pointerToUse = pointer;
        if (pointer.charAt(0) == '#') {
            pointerToUse = pointerToUse.substring(1);
        }

        final ObjectNode resolved = (ObjectNode) root.at(pointerToUse);
        param.setAll(resolved);

        return param;
    }
}
