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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.apicurio.datamodels.openapi.v3.models.Oas30Document;
import io.apicurio.datamodels.openapi.v3.models.Oas30Schema;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.server.api.generator.openapi.OpenApiParameterGenerator;
import io.syndesis.server.api.generator.openapi.util.JsonSchemaHelper;
import io.syndesis.server.api.generator.openapi.util.OasModelHelper;

public final class Oas30ParameterGenerator extends OpenApiParameterGenerator<Oas30Document> {

    @Override
    protected Map<String, ConfigurationProperty> createConfigurationProperties(Oas30Document openApiDoc) {
        if (openApiDoc.components == null || openApiDoc.components.parameters == null) {
            return new HashMap<>();
        }

        Map<String, ConfigurationProperty> properties = new HashMap<>();

        openApiDoc.components.parameters
            .entrySet()
            .stream()
            .filter(entry -> shouldCreateProperty(entry.getValue()))
            .forEach(entry -> {
                final Optional<Oas30Schema> schema = Oas30ModelHelper.getSchema(entry.getValue());
                schema.ifPresent(oas30Schema -> properties.put(entry.getKey(), createPropertyFromParameter(entry.getValue(), oas30Schema.type, javaTypeFor(oas30Schema), oas30Schema.default_, oas30Schema.enum_)));
        });

        return properties;
    }

    static String javaTypeFor(final Oas30Schema schema) {
        if (OasModelHelper.isArrayType(schema)) {
            final Oas30Schema items = (Oas30Schema) schema.items;
            final String elementType = items.type;
            final String elementFormat = items.format;

            return JsonSchemaHelper.javaTypeFor(elementType, elementFormat) + "[]";
        }

        final String format = schema.format;
        return JsonSchemaHelper.javaTypeFor(schema.type, format);
    }
}
