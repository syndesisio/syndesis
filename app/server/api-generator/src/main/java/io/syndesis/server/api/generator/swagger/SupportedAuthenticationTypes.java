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
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import io.swagger.models.auth.OAuth2Definition;
import io.swagger.models.auth.SecuritySchemeDefinition;
import io.syndesis.common.model.connection.ConfigurationProperty;

@SuppressWarnings("ImmutableEnumChecker")
enum SupportedAuthenticationTypes {
    apiKey("API Key"),
    basic("HTTP Basic Authentication"),
    oauth2("OAuth 2.0", SupportedAuthenticationTypes::authorizationFlow);

    private static final Set<String> SUPPORTED = Arrays.stream(SupportedAuthenticationTypes.values()).map(SupportedAuthenticationTypes::name)
        .collect(Collectors.toSet());

    final String label;

    private Predicate<SecuritySchemeDefinition> filter;

    private final transient ConfigurationProperty.PropertyValue propertyValue;

    SupportedAuthenticationTypes(final String label) {
        this(label, SupportedAuthenticationTypes::any);
    }

    SupportedAuthenticationTypes(final String label, final Predicate<SecuritySchemeDefinition> filter) {
        this.label = label;
        this.filter = filter;
        propertyValue = new ConfigurationProperty.PropertyValue.Builder().value(name()).label(label).build();
    }

    static ConfigurationProperty.PropertyValue asPropertyValue(final SecuritySchemeDefinition def) {
        return valueOf(def.getType()).propertyValue;
    }

    static boolean supports(final SecuritySchemeDefinition def) {
        final String type = def.getType();

        return SUPPORTED.contains(type) && valueOf(type).filter.test(def);
    }

    private static boolean any(@SuppressWarnings("unused") final SecuritySchemeDefinition def) {
        return true;
    }

    private static boolean authorizationFlow(final SecuritySchemeDefinition def) {
        if (!(def instanceof OAuth2Definition)) {
            return false;
        }

        final OAuth2Definition oauth = (OAuth2Definition) def;

        return "accessCode".equals(oauth.getFlow());
    }
}
