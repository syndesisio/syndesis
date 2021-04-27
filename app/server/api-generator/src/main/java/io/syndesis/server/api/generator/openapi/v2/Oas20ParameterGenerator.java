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

import java.util.HashMap;
import java.util.Map;

import io.apicurio.datamodels.openapi.v2.models.Oas20Document;
import io.apicurio.datamodels.openapi.v2.models.Oas20ParameterDefinitions;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.server.api.generator.openapi.OpenApiParameterGenerator;

public final class Oas20ParameterGenerator extends OpenApiParameterGenerator<Oas20Document> {

    @Override
    public Map<String, ConfigurationProperty> createConfigurationProperties(Oas20Document openApiDoc) {
        final Oas20ParameterDefinitions globalParameters = openApiDoc.parameters;
        if (globalParameters == null) {
            return new HashMap<>();
        }

        Map<String, ConfigurationProperty> properties = new HashMap<>();

        globalParameters.getItems()
            .stream()
            .filter(this::shouldCreateProperty)
            .forEach(parameter -> properties.put(parameter.getName(),
                createPropertyFromParameter(parameter, parameter.type, Oas20ModelHelper.javaTypeFor(parameter), parameter.default_, parameter.enum_)));

        return properties;
    }
}
