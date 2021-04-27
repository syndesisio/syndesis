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

package io.syndesis.server.api.generator.openapi;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.apicurio.datamodels.openapi.models.OasDocument;
import io.apicurio.datamodels.openapi.models.OasParameter;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.server.api.generator.openapi.util.OasModelHelper;

import static org.apache.commons.lang3.StringUtils.trimToNull;

/**
 * Generator creates configuration properties from OpenAPI document parameters. Created configuration properties are added
 * to the connector builder. This is the abstract  base class for specific 2.x and 3.x OpenAPI implementations.
 */
public abstract class OpenApiParameterGenerator<T extends OasDocument> {

    /**
     * Extract global parameters from OpenAPI document and generate configuration properties on given connector builder.
     * @param builder the connector builder to receive generated configuration properties.
     * @param openApiDoc the OpenAPI document.
     */
    public void addGlobalParameters(Connector.Builder builder, T openApiDoc) {
        createConfigurationProperties(openApiDoc)
            .forEach(builder::putProperty);
    }

    /**
     * Create configuration properties from global parameters on given OpenAPI document.
     * @param openApiDoc the OpenAPI document.
     * @return map of generated configuration properties where the key is the property name.
     */
    protected abstract Map<String, ConfigurationProperty> createConfigurationProperties(T openApiDoc);

    /**
     * Checks if we should generate a configuration property for given global parameter.
     * @param parameter the parameter to check.
     * @return true if parameter should generate a configuration property.
     */
    protected boolean shouldCreateProperty(OasParameter parameter) {
        if (OasModelHelper.isReferenceType(parameter) || OasModelHelper.isBody(parameter)) {
            // Reference parameters are not supported, body parameters are
            // handled in createShape* methods
            return false;
        }

        return OasModelHelper.isSerializable(parameter);
    }

    /**
     * Creates a configuration property from given parameter. OpenAPI version specific information like type, default values and enum
     * list are extracted by version specific subclass implementations.
     * @param parameter the global parameter on the specification.
     * @param type the parameter type (array, object, etc.)
     * @param javaType the corresponding java type.
     * @param defaultValue optional default value.
     * @param enums optional list of allowed values as enumeration.
     * @return generated configuration property that gets added to the connector builder.
     */
    public static ConfigurationProperty createPropertyFromParameter(final OasParameter parameter,
                                                                      final String type, final String javaType,
                                                                      final Object defaultValue, List<String> enums) {
        final String name = trimToNull(parameter.name);
        final String description = trimToNull(parameter.description);
        final boolean required = Boolean.TRUE.equals(parameter.required);

        final ConfigurationProperty.Builder propertyBuilder = new ConfigurationProperty.Builder()
            .kind("property")
            .displayName(name)
            .description(description)
            .group("producer")
            .required(required)
            .componentProperty(false)
            .deprecated(false)
            .secret(false);

        if (defaultValue != null) {
            propertyBuilder.defaultValue(String.valueOf(defaultValue));
        }

        propertyBuilder.type(type).javaType(javaType);

        if (enums != null) {
            propertyBuilder.addAllEnum(createEnums(enums));
        }

        return propertyBuilder.build();
    }

    private static List<ConfigurationProperty.PropertyValue> createEnums(final List<String> enums) {
        return enums.stream().map(OpenApiParameterGenerator::createPropertyValue).collect(Collectors.toList());
    }

    private static ConfigurationProperty.PropertyValue createPropertyValue(final String value) {
        return new ConfigurationProperty.PropertyValue.Builder().label(value).value(value).build();
    }
}
