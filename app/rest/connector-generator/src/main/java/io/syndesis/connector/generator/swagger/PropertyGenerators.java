/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.connector.generator.swagger;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.swagger.models.Swagger;
import io.swagger.models.auth.OAuth2Definition;
import io.swagger.models.auth.SecuritySchemeDefinition;
import io.syndesis.model.connection.ConfigurationProperty;
import io.syndesis.model.connection.ConfigurationProperty.PropertyValue;

/* default */ enum PropertyGenerators {

    accessToken {
        @Override
        protected BiFunction<Swagger, ConfigurationProperty, Optional<ConfigurationProperty>> propertyValueExtractor() {
            return PropertyGenerators::ifHasOAuthSecurityDefinition;
        }
    },
    accessTokenUrl {
        @Override
        protected BiFunction<Swagger, ConfigurationProperty, Optional<ConfigurationProperty>> propertyValueExtractor() {
            return (swagger, template) -> oauthProperty(swagger, template, OAuth2Definition::getTokenUrl);
        }
    },
    authenticationType {
        @Override
        protected BiFunction<Swagger, ConfigurationProperty, Optional<ConfigurationProperty>> propertyValueExtractor() {
            return (swagger, template) -> {
                final Map<String, SecuritySchemeDefinition> securityDefinitions = swagger.getSecurityDefinitions();
                if (securityDefinitions == null || securityDefinitions.isEmpty()) {
                    return Optional
                        .of(new ConfigurationProperty.Builder().createFrom(template).defaultValue("none").addEnum(NO_SECURITY).build());
                }

                final PropertyValue[] enums = securityDefinitions.values().stream()//
                    .map(SecuritySchemeDefinition::getType)//
                    .filter(SupportedAuthenticationTypes.SUPPORTED::contains)//
                    .map(SupportedAuthenticationTypes::valueOf)//
                    .map(SupportedAuthenticationTypes::asPropertyValue)//
                    .toArray(l -> new ConfigurationProperty.PropertyValue[l]);

                final ConfigurationProperty.Builder authenticationType = new ConfigurationProperty.Builder().createFrom(template)
                    .addEnum(enums);

                if (enums.length == 1) {
                    authenticationType.defaultValue(enums[0].getValue());
                }

                return Optional.of(authenticationType.build());
            };
        }
    },
    authorizationUrl {
        @Override
        protected BiFunction<Swagger, ConfigurationProperty, Optional<ConfigurationProperty>> propertyValueExtractor() {
            return (swagger, template) -> oauthProperty(swagger, template, OAuth2Definition::getAuthorizationUrl);
        }
    },
    basePath {
        @Override
        protected BiFunction<Swagger, ConfigurationProperty, Optional<ConfigurationProperty>> propertyValueExtractor() {
            return withDefaultValue(Swagger::getBasePath);
        }
    },
    clientSecret {
        @Override
        protected BiFunction<Swagger, ConfigurationProperty, Optional<ConfigurationProperty>> propertyValueExtractor() {
            return PropertyGenerators::ifHasOAuthSecurityDefinition;
        }
    },
    host {
        @Override
        protected BiFunction<Swagger, ConfigurationProperty, Optional<ConfigurationProperty>> propertyValueExtractor() {
            return withDefaultValue(Swagger::getHost);
        }
    };

    private static final Set<String> KNOWN_PROPERTIES = Arrays.stream(values()).map(PropertyGenerators::name).collect(Collectors.toSet());

    private static final ConfigurationProperty.PropertyValue NO_SECURITY = new ConfigurationProperty.PropertyValue.Builder().value("none")
        .label("No Security").build();

    protected abstract BiFunction<Swagger, ConfigurationProperty, Optional<ConfigurationProperty>> propertyValueExtractor();

    /* default */ static Optional<ConfigurationProperty> createProperty(final String propertyName, final Swagger swagger,
        final ConfigurationProperty template) {
        if (!KNOWN_PROPERTIES.contains(propertyName)) {
            return Optional.ofNullable(template);
        }

        final PropertyGenerators defaultPropertyValue = PropertyGenerators.valueOf(propertyName);

        return defaultPropertyValue.propertyValueExtractor().apply(swagger, template);
    }

    private static boolean hasOAuth2Definition(final Swagger swagger) {
        return oauth2Definition(swagger).isPresent();
    }

    private static Optional<ConfigurationProperty> ifHasOAuthSecurityDefinition(final Swagger swagger,
        final ConfigurationProperty template) {
        if (hasOAuth2Definition(swagger)) {
            return Optional.of(template);
        }

        return Optional.empty();
    }

    private static Optional<OAuth2Definition> oauth2Definition(final Swagger swagger) {
        final Map<String, SecuritySchemeDefinition> securityDefinitions = swagger.getSecurityDefinitions();

        if (securityDefinitions == null) {
            return Optional.empty();
        }

        return Optional.ofNullable((OAuth2Definition) securityDefinitions.get("oauth2"));
    }

    private static Optional<ConfigurationProperty> oauthProperty(final Swagger swagger, final ConfigurationProperty template,
        final Function<OAuth2Definition, String> defaultValueExtractor) {
        return oauth2Definition(swagger).map(definition -> new ConfigurationProperty.Builder().createFrom(template)
            .defaultValue(defaultValueExtractor.apply(definition)).build());
    }

    private static BiFunction<Swagger, ConfigurationProperty, Optional<ConfigurationProperty>>
        withDefaultValue(final Function<Swagger, String> defaultValueExtractor) {
        return (swagger, template) -> Optional
            .of(new ConfigurationProperty.Builder().createFrom(template).defaultValue(defaultValueExtractor.apply(swagger)).build());
    }
}
