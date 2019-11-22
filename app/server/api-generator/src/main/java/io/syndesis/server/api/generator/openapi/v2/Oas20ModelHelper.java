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

import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.v2.models.Oas20Document;
import io.apicurio.datamodels.openapi.v2.models.Oas20Parameter;
import io.apicurio.datamodels.openapi.v2.models.Oas20SchemaDefinition;
import io.syndesis.server.api.generator.openapi.util.OasModelHelper;

public final class Oas20ModelHelper {

    private Oas20ModelHelper() {
        // utility class
    }

    public static Oas20SchemaDefinition dereference(final OasSchema model, final Oas20Document openApiDoc) {
        String reference = OasModelHelper.getReferenceName(model.$ref);
        return openApiDoc.definitions.getDefinition(reference);
    }

    /**
     * Determines if given parameter is of type array.
     * @param parameter to check
     * @return true if given parameter is an array.
     */
    public static boolean isArrayType(Oas20Parameter parameter) {
        return "array".equals(parameter.type);
    }
}
