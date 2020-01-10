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
public class Oas30SchemaValidator implements OpenApiSchemaValidator {

    private static final JsonSchema OPENAPI_3_0_SCHEMA;

    private static final String OPENAPI_3_0_SCHEMA_FILE = "schema/openapi-3.0-schema.json";

    private static final String OPENAPI_V3_SCHEMA_URI = "https://spec.openapis.org/oas/3.0/schema/2019-04-02";

    static {
        try {
            final JsonNode oas30Schema = JsonUtils.reader().readTree(Resources.getResourceAsText(OPENAPI_3_0_SCHEMA_FILE));
            final LoadingConfiguration loadingConfiguration = LoadingConfiguration.newBuilder()
                .preloadSchema(oas30Schema)
                .freeze();
            OPENAPI_3_0_SCHEMA = JsonSchemaFactory.newBuilder().setLoadingConfiguration(loadingConfiguration).freeze().getJsonSchema(OPENAPI_V3_SCHEMA_URI);
        } catch (final ProcessingException | IOException ex) {
            throw new IllegalStateException("Unable to load the schema file embedded in the artifact", ex);
        }
    }


    @Override
    public JsonSchema getSchema() {
        return OPENAPI_3_0_SCHEMA;
    }
}
