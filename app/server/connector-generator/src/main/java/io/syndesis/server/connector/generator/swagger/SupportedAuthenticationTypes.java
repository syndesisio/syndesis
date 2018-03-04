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
package io.syndesis.server.connector.generator.swagger;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import io.syndesis.common.model.connection.ConfigurationProperty;

@SuppressWarnings("ImmutableEnumChecker")
enum SupportedAuthenticationTypes {
    basic("HTTP Basic Authentication"), oauth2("OAuth 2.0");

    static final Set<String> SUPPORTED = Arrays.stream(SupportedAuthenticationTypes.values()).map(SupportedAuthenticationTypes::name)
        .collect(Collectors.toSet());

    final String label;

    private final transient ConfigurationProperty.PropertyValue propertyValue;

    SupportedAuthenticationTypes(final String label) {
        this.label = label;
        propertyValue = new ConfigurationProperty.PropertyValue.Builder().value(name()).label(label).build();
    }

    ConfigurationProperty.PropertyValue asPropertyValue() {
        return propertyValue;
    }
}
