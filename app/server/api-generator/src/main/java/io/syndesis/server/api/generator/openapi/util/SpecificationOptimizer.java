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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.openapi.models.OasDocument;
import io.syndesis.common.util.json.JsonUtils;

public final class SpecificationOptimizer {

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
        json.remove(Arrays.asList("info", "tags", "definitions", "responses", "externalDocs"));

        final JsonNode components = json.get("components");
        if (components != null) {
            ((ObjectNode)components).remove(Arrays.asList("schemas", "responses", "requestBodies", "examples", "headers", "links", "callbacks"));
        }

        final JsonNode paths = json.get("paths");

        if (paths != null) {
            paths.forEach(path -> {
                JsonNode globalParameters = ((ObjectNode)path).remove("parameters");
                final List<JsonNode> globalParametersList = new ArrayList<>();

                if (globalParameters != null) {
                    collectPathOrQueryParameters(globalParameters, globalParametersList);
                    ((ArrayNode) globalParameters).removeAll();
                    ((ArrayNode) globalParameters).addAll(globalParametersList);
                }

                StreamSupport.stream(path.spliterator(), false)
                    .filter(JsonNode::isObject)
                    .forEach(operation -> {
                        final ObjectNode operationNode = (ObjectNode) operation;
                        operationNode.remove(Arrays.asList("tags", "summary", "description", "externalDocs", "callbacks", "servers"));
                        final ArrayNode parameters = (ArrayNode) operation.get("parameters");

                        if (parameters != null && parameters.size() > 0) {
                            final List<JsonNode> parametersList = StreamSupport.stream(parameters.spliterator(), false).collect(Collectors.toList());
                            for (final ListIterator<JsonNode> i = parametersList.listIterator(); i.hasNext();) {
                                final ObjectNode param = (ObjectNode) i.next();
                                param.remove(Arrays.asList("description", "type", "required", "format"));

                                if (!isPathOrQueryParameter(param)) {
                                    i.remove();
                                }
                            }

                            if (globalParametersList.isEmpty() && parametersList.isEmpty()) {
                                operationNode.remove("parameters");
                            } else {
                                parameters.removeAll();
                                parameters.addAll(parametersList);
                                parameters.addAll(globalParametersList);
                            }
                        } else if (!globalParametersList.isEmpty()) {
                            operationNode.set("parameters", globalParameters);
                        }

                        operationNode.remove(Arrays.asList("requestBody", "responses"));
                    });
            });
        }

        try {
            return JsonUtils.writer().writeValueAsString(json);
        } catch (final JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize minified OpenAPI document", e);
        }
    }

    private static void collectPathOrQueryParameters(JsonNode globalParameters, List<JsonNode> globalParametersList) {
        final List<JsonNode> parametersList = StreamSupport.stream(globalParameters.spliterator(), false).collect(Collectors.toList());
        for (JsonNode jsonNode : parametersList) {
            final ObjectNode param = (ObjectNode) jsonNode;
            param.remove(Arrays.asList("description", "type", "required", "format"));

            if (isPathOrQueryParameter(param)) {
                globalParametersList.add(param);
            }
        }
    }

    private static boolean isPathOrQueryParameter(ObjectNode param) {
        return param.get("in") != null && ("path".equals(param.get("in").textValue()) || "query".equals(param.get("in").textValue()));
    }
}
