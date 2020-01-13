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

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.load.configuration.LoadingConfiguration;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import io.syndesis.common.util.Resources;
import io.syndesis.common.util.json.JsonUtils;
import io.syndesis.server.api.generator.openapi.OpenApiSchemaValidator;

/**
 * @author Christoph Deppisch
 */
public final class Oas20SchemaValidator implements OpenApiSchemaValidator {

    private static final JsonSchema SWAGGER_2_0_SCHEMA;

    private static final String SWAGGER_2_0_SCHEMA_FILE = "schema/swagger-2.0-schema.json";

    private static final String SWAGGER_IO_V2_SCHEMA_URI = "http://swagger.io/v2/schema.json#";

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


    @Override
    public JsonSchema getSchema() {
        return SWAGGER_2_0_SCHEMA;
    }
}
