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

package io.syndesis.server.api.generator.openapi.v3;

import io.syndesis.server.api.generator.openapi.OpenApiSchemaValidator;

import com.github.fge.jsonschema.main.JsonSchema;

public class Oas30SchemaValidator implements OpenApiSchemaValidator {

    private static final JsonSchema OPENAPI_3_0_SCHEMA = OpenApiSchemaValidator.loadSchema("schema/openapi-3.0-schema.json", "https://spec.openapis.org/oas/3.0/schema/2019-04-02");

    @Override
    public JsonSchema getSchema() {
        return OPENAPI_3_0_SCHEMA;
    }
}
