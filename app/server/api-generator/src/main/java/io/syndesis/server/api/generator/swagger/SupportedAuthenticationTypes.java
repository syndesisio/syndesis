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
package io.syndesis.server.api.generator.swagger;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import io.apicurio.datamodels.openapi.v2.models.Oas20SecurityScheme;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.ConfigurationProperty.PropertyValue;
import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("ImmutableEnumChecker")
enum SupportedAuthenticationTypes {
    apiKey("API Key"),
    basic("HTTP Basic Authentication"),
    oauth2("OAuth 2.0", SupportedAuthenticationTypes::authorizationFlow);

    private static final Set<String> SUPPORTED = Arrays.stream(SupportedAuthenticationTypes.values())
        .map(SupportedAuthenticationTypes::name)
        .collect(Collectors.toSet());

    final String label;

    final transient ConfigurationProperty.PropertyValue propertyValue;

    private final Predicate<Oas20SecurityScheme> filter;

    SupportedAuthenticationTypes(final String label) {
        this(label, SupportedAuthenticationTypes::any);
    }

    SupportedAuthenticationTypes(final String label, final Predicate<Oas20SecurityScheme> filter) {
        this.label = label;
        this.filter = filter;
        propertyValue = new ConfigurationProperty.PropertyValue.Builder().value(name()).label(label).build();
    }

    public static SupportedAuthenticationTypes fromConfiguredPropertyValue(final String value) {
        final int idx = Objects.requireNonNull(value, "value").indexOf(':');

        return SupportedAuthenticationTypes.valueOf(idx > 0 ? value.substring(0, idx) : value);
    }

    public static SupportedAuthenticationTypes fromSecurityDefinition(final String value) {
        return valueOf(value);
    }

    static ConfigurationProperty.PropertyValue asPropertyValue(final String name, final Oas20SecurityScheme scheme) {
        final PropertyValue template = valueOf(scheme.type).propertyValue;

        final PropertyValue propertyValue = new ConfigurationProperty.PropertyValue.Builder()
            .createFrom(template)
            .label(template.getLabel() + " - " + name)
            .value(template.getValue() + ":" + name)
            .build();

        final String description = scheme.description;
        if (StringUtils.isEmpty(description)) {
            return propertyValue;
        }

        final String labelWithDescription = propertyValue.getLabel() + " (" + description + ")";
        return new ConfigurationProperty.PropertyValue.Builder()
            .createFrom(propertyValue)
            .label(labelWithDescription)
            .build();
    }

    static boolean supports(final Oas20SecurityScheme scheme) {
        final String type = scheme.type;
        return SUPPORTED.contains(type) && valueOf(type).filter.test(scheme);
    }

    private static boolean any(@SuppressWarnings("unused") final Oas20SecurityScheme scheme) {
        return true;
    }

    private static boolean authorizationFlow(final Oas20SecurityScheme scheme) {
        if (!"oauth2".equals(scheme.type)) {
            return false;
        }

        return "accessCode".equals(scheme.flow);
    }
}
