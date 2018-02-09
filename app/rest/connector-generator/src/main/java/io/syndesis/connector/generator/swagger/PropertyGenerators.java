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
package io.syndesis.connector.generator.swagger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import io.swagger.models.auth.AbstractSecuritySchemeDefinition;
import io.swagger.models.auth.BasicAuthDefinition;
import io.swagger.models.auth.OAuth2Definition;
import io.swagger.models.auth.SecuritySchemeDefinition;
import io.syndesis.model.connection.ConfigurationProperty;
import io.syndesis.model.connection.ConfigurationProperty.PropertyValue;

import org.apache.commons.lang3.StringUtils;

enum PropertyGenerators {

    accessToken {
        @Override
        protected BiFunction<Swagger, ConfigurationProperty, Optional<ConfigurationProperty>> propertyValueExtractor() {
            return PropertyGenerators::ifHasOAuthSecurityDefinition;
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
    authorizationEndpoint {
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
    clientId {
        @Override
        protected BiFunction<Swagger, ConfigurationProperty, Optional<ConfigurationProperty>> propertyValueExtractor() {
            return PropertyGenerators::ifHasOAuthSecurityDefinition;
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
            return withDefaultValue(PropertyGenerators::determineHost);
        }
    },
    oauthScopes {
        @Override
        protected BiFunction<Swagger, ConfigurationProperty, Optional<ConfigurationProperty>> propertyValueExtractor() {
            return (swagger, template) -> oauthProperty(swagger, template,
                d -> d.getScopes().keySet().stream().collect(Collectors.joining(" ")));
        }
    },
    password {
        @Override
        protected BiFunction<Swagger, ConfigurationProperty, Optional<ConfigurationProperty>> propertyValueExtractor() {
            return PropertyGenerators::ifHasBasicSecurityDefinition;
        }
    },
    specification {
        @Override
        protected BiFunction<Swagger, ConfigurationProperty, Optional<ConfigurationProperty>> propertyValueExtractor() {
            return PropertyGenerators::fromTemplate;
        }
    },
    tokenEndpoint {
        @Override
        protected BiFunction<Swagger, ConfigurationProperty, Optional<ConfigurationProperty>> propertyValueExtractor() {
            return (swagger, template) -> oauthProperty(swagger, template, OAuth2Definition::getTokenUrl);
        }
    },
    username {
        @Override
        protected BiFunction<Swagger, ConfigurationProperty, Optional<ConfigurationProperty>> propertyValueExtractor() {
            return PropertyGenerators::ifHasBasicSecurityDefinition;
        }
    };

    private static final ConfigurationProperty.PropertyValue NO_SECURITY = new ConfigurationProperty.PropertyValue.Builder().value("none")
        .label("No Security").build();

    protected abstract BiFunction<Swagger, ConfigurationProperty, Optional<ConfigurationProperty>> propertyValueExtractor();

    static String createHostUri(final String scheme, final String host) {
        try {
            return new URI(scheme, host, null, null).toString();
        } catch (final URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    static Optional<ConfigurationProperty> createProperty(final String propertyName, final Swagger swagger,
        final ConfigurationProperty template) {
        final PropertyGenerators propertyGenerator = PropertyGenerators.valueOf(propertyName);

        return propertyGenerator.propertyValueExtractor().apply(swagger, template);
    }

    static String determineHost(final Swagger swagger) {
        final Map<String, Object> vendorExtensions = Optional.ofNullable(swagger.getVendorExtensions()).orElse(Collections.emptyMap());
        final URI specificationUrl = (URI) vendorExtensions.get(BaseSwaggerConnectorGenerator.URL_EXTENSION);

        final List<Scheme> schemes = swagger.getSchemes();
        final String schemeToUse;
        if (schemes == null || schemes.isEmpty()) {
            schemeToUse = requireNonNull(specificationUrl,
                "Swagger specification does not provide a `schemes` definition "
                    + "and the Swagger specification was uploaded so the originating URL is lost to determine the scheme to use")
                        .getScheme();
        } else if (schemes.size() == 1) {
            final Scheme scheme = schemes.get(0);
            schemeToUse = scheme.toValue();
        } else if (schemes.contains(Scheme.HTTPS)) {
            schemeToUse = "https";
        } else {
            schemeToUse = schemes.stream()//
                .filter(s -> s.toValue().startsWith("http"))//
                .findFirst()//
                .orElseThrow(() -> new IllegalArgumentException(
                    "Unable to find a supported scheme within the schemes given in the Swagger specification: " + schemes))//
                .toValue();
        }

        final String host = swagger.getHost();
        String hostToUse;
        if (StringUtils.isEmpty(host)) {
            hostToUse = requireNonNull(specificationUrl, "Swagger specification does not provide a `host` definition "
                + "and the Swagger specification was uploaded so it is impossible to determine the originating URL").getHost();
        } else {
            hostToUse = swagger.getHost();
        }

        return createHostUri(schemeToUse, hostToUse);
    }

    private static Optional<ConfigurationProperty> fromTemplate(@SuppressWarnings("unused") final Swagger swagger,
        final ConfigurationProperty template) {
        return Optional.of(template);
    }

    private static Optional<ConfigurationProperty> ifHasBasicSecurityDefinition(final Swagger swagger,
        final ConfigurationProperty template) {
        return ifHasSecurityDefinition(swagger, template, BasicAuthDefinition.class);
    }

    private static Optional<ConfigurationProperty> ifHasOAuthSecurityDefinition(final Swagger swagger,
        final ConfigurationProperty template) {
        return ifHasSecurityDefinition(swagger, template, OAuth2Definition.class);
    }

    private static Optional<ConfigurationProperty> ifHasSecurityDefinition(final Swagger swagger, final ConfigurationProperty template,
        final Class<? extends AbstractSecuritySchemeDefinition> type) {
        if (securityDefinition(swagger, type).isPresent()) {
            return Optional.of(template);
        }

        return Optional.empty();
    }

    private static Optional<ConfigurationProperty> oauthProperty(final Swagger swagger, final ConfigurationProperty template,
        final Function<OAuth2Definition, String> defaultValueExtractor) {
        return securityDefinition(swagger, OAuth2Definition.class).map(definition -> new ConfigurationProperty.Builder()
            .createFrom(template).defaultValue(defaultValueExtractor.apply(definition)).build());
    }

    private static <T extends AbstractSecuritySchemeDefinition> Optional<T> securityDefinition(final Swagger swagger, final Class<T> type) {
        final Map<String, SecuritySchemeDefinition> securityDefinitions = swagger.getSecurityDefinitions();

        if (securityDefinitions == null) {
            return Optional.empty();
        }

        return securityDefinitions.values().stream().filter(type::isInstance).map(type::cast).findFirst();
    }

    private static BiFunction<Swagger, ConfigurationProperty, Optional<ConfigurationProperty>>
        withDefaultValue(final Function<Swagger, String> defaultValueExtractor) {
        return (swagger, template) -> Optional
            .of(new ConfigurationProperty.Builder().createFrom(template).defaultValue(defaultValueExtractor.apply(swagger)).build());
    }
}
