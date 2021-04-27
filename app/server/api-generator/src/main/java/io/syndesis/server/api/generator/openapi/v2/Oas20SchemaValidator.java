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

import com.github.fge.jsonschema.main.JsonSchema;
import io.syndesis.server.api.generator.openapi.OpenApiSchemaValidator;

public final class Oas20SchemaValidator implements OpenApiSchemaValidator {

    private static final JsonSchema SWAGGER_2_0_SCHEMA = OpenApiSchemaValidator.loadSchema("schema/swagger-2.0-schema.json", "http://swagger.io/v2/schema.json#");

    @Override
    public JsonSchema getSchema() {
        return SWAGGER_2_0_SCHEMA;
    }
}
