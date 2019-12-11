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

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.apicurio.datamodels.core.models.common.SecurityScheme;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.ConfigurationProperty.PropertyValue;
import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("ImmutableEnumChecker")
public enum SupportedAuthenticationTypes {
    apiKey("API Key"),
    basic("HTTP Basic Authentication", "http"),
    oauth2("OAuth 2.0", SupportedAuthenticationTypes::oauthType, SupportedAuthenticationTypes::oauthFlow);

    private static final Set<String> SUPPORTED = Stream.concat(
        Arrays.stream(SupportedAuthenticationTypes.values()).map(SupportedAuthenticationTypes::name),
        Arrays.stream(SupportedAuthenticationTypes.values()).flatMap(type -> Stream.of(type.aliases)))
        .collect(Collectors.toSet());

    final transient ConfigurationProperty.PropertyValue propertyValue;

    private final Predicate<String> authTypeFilter;
    private final Predicate<String> authFlowFilter;
    private final String aliases;

    SupportedAuthenticationTypes(final String label, final String... aliases) {
        this(label, SupportedAuthenticationTypes::any, SupportedAuthenticationTypes::any, aliases);
    }

    SupportedAuthenticationTypes(final String label, final Predicate<String> authTypeFilter, final Predicate<String> authFlowFilter, String... aliases) {
        this.authTypeFilter = authTypeFilter;
        this.authFlowFilter = authFlowFilter;
        propertyValue = new ConfigurationProperty.PropertyValue.Builder().value(name()).label(label).build();
        this.aliases = String.join(",", aliases);
    }

    public static SupportedAuthenticationTypes fromConfiguredPropertyValue(final String value) {
        final int idx = Objects.requireNonNull(value, "value").indexOf(':');

        return fromSecurityDefinition(idx > 0 ? value.substring(0, idx) : value);
    }

    public static SupportedAuthenticationTypes fromSecurityDefinition(final String value) {
        for (SupportedAuthenticationTypes type : values()) {
            if (Arrays.asList(type.aliases.split(",")).contains(value)) {
                return type;
            }
        }

        return valueOf(value);
    }

    public static ConfigurationProperty.PropertyValue asPropertyValue(final String name, final SecurityScheme scheme) {
        final PropertyValue template = fromSecurityDefinition(scheme.type).propertyValue;

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

    public static boolean supports(final String schemeType, final String flow) {
        SupportedAuthenticationTypes supported = fromSecurityDefinition(schemeType);
        return SUPPORTED.contains(schemeType) &&
            supported.authTypeFilter.test(schemeType) &&
            supported.authFlowFilter.test(flow);
    }

    private static boolean any(@SuppressWarnings("unused") final String typeOrScheme) {
        return true;
    }

    private static boolean oauthType(final String type) {
        return "oauth2".equals(type);
    }

    private static boolean oauthFlow(final String scheme) {
        return "accessCode".equals(scheme) ||  //OpenAPI 2.x
            "authorizationCode".equals(scheme);  //OpenAPI 3.x
    }
}
