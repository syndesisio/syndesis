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
package io.syndesis.server.api.generator.openapi.v2;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.apicurio.datamodels.openapi.models.OasOperation;
import io.apicurio.datamodels.openapi.models.OasParameter;
import io.apicurio.datamodels.openapi.models.OasPathItem;
import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.v2.models.Oas20Document;
import io.apicurio.datamodels.openapi.v2.models.Oas20Items;
import io.apicurio.datamodels.openapi.v2.models.Oas20Operation;
import io.apicurio.datamodels.openapi.v2.models.Oas20Parameter;
import io.apicurio.datamodels.openapi.v2.models.Oas20SchemaDefinition;
import io.syndesis.server.api.generator.openapi.util.JsonSchemaHelper;
import io.syndesis.server.api.generator.openapi.util.OasModelHelper;

final class Oas20ModelHelper {

    private Oas20ModelHelper() {
        // utility class
    }

    /**
     * Iterate through list of generic path parameters on the given operation and collect those of given type.
     * @param operation given path item.
     * @return typed list of path parameters.
     */
    static List<Oas20Parameter> getParameters(OasOperation operation) {
        return OasModelHelper.getParameters(operation)
            .stream()
            .filter(Oas20Parameter.class::isInstance)
            .map(Oas20Parameter.class::cast)
            .collect(Collectors.toList());
    }

    /**
     * Iterate through list of generic path parameters on the given path item and collect those of given type.
     * @param pathItem given path item.
     * @return typed list of path parameters.
     */
    static List<Oas20Parameter> getParameters(OasPathItem pathItem) {
        return OasModelHelper.getParameters(pathItem)
            .stream()
            .filter(Oas20Parameter.class::isInstance)
            .map(Oas20Parameter.class::cast)
            .collect(Collectors.toList());
    }

    static Oas20SchemaDefinition dereference(final OasSchema model, final Oas20Document openApiDoc) {
        String reference = OasModelHelper.getReferenceName(model.$ref);
        return openApiDoc.definitions.getDefinition(reference);
    }

    /**
     * Determines if given parameter is of type array.
     * @param parameter to check
     * @return true if given parameter is an array.
     */
    private static boolean isArrayType(Oas20Parameter parameter) {
        return "array".equals(parameter.type);
    }

    static String javaTypeFor(final Oas20Parameter parameter) {
        if (isArrayType(parameter)) {
            final Oas20Items items = parameter.items;
            final String elementType = items.type;
            final String elementFormat = items.format;

            return JsonSchemaHelper.javaTypeFor(elementType, elementFormat) + "[]";
        }

        final String format = parameter.format;
        return JsonSchemaHelper.javaTypeFor(parameter.type, format);
    }

    /**
     * Delegates to common model helper with OpenAPI 2.x model type parameter.
     * @param pathItem holding the operations.
     * @return typed map of OpenAPI 2.x operations where the key is the Http method of the operation.
     */
    static Map<String, Oas20Operation> getOperationMap(OasPathItem pathItem) {
        return OasModelHelper.getOperationMap(pathItem, Oas20Operation.class);
    }

    /**
     * Find parameter that is defined to live in body.
     */
    static Optional<OasParameter> findBodyParameter(Oas20Operation operation) {
        if (operation.parameters == null) {
            return Optional.empty();
        }

        final List<OasParameter> operationParameters = operation.parameters;

        return operationParameters.stream()
            .filter(p -> "body".equals(p.in) && p.schema != null)
            .findFirst();
    }
}
