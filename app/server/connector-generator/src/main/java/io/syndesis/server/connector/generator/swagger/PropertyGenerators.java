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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import io.swagger.models.auth.AbstractSecuritySchemeDefinition;
import io.swagger.models.auth.BasicAuthDefinition;
import io.swagger.models.auth.OAuth2Definition;
import io.swagger.models.auth.SecuritySchemeDefinition;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.ConfigurationProperty.PropertyValue;

import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("PMD.GodClass")
enum PropertyGenerators {

    accessToken {
        @Override
        protected BiFunction<Swagger, ConfigurationProperty, Optional<ConfigurationProperty>> propertyValueExtractor() {
            return PropertyGenerators::ifHasOAuthSecurityDefinition;
        }
    },
    accessTokenExpiresAt {
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
    authorizeUsingParameters {
        @Override
        protected BiFunction<Swagger, ConfigurationProperty, Optional<ConfigurationProperty>> propertyValueExtractor() {
            return (swagger, template) -> oauthVendorProperty(swagger, template, "x-authorize-using-parameters");
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
    refreshToken {
        @Override
        protected BiFunction<Swagger, ConfigurationProperty, Optional<ConfigurationProperty>> propertyValueExtractor() {
            return PropertyGenerators::ifHasOAuthSecurityDefinition;
        }
    },
    refreshTokenRetryStatuses {
        @Override
        protected BiFunction<Swagger, ConfigurationProperty, Optional<ConfigurationProperty>> propertyValueExtractor() {
            return (swagger, template) -> oauthVendorProperty(swagger, template, "x-refresh-token-retry-statuses");
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
    tokenStrategy {
        @Override
        protected BiFunction<Swagger, ConfigurationProperty, Optional<ConfigurationProperty>> propertyValueExtractor() {
            return (swagger, template) -> oauthVendorProperty(swagger, template, "x-token-strategy");
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

    static String createHostUri(final String scheme, final String host, final int port) {
        try {
            if (port == -1) {
                return new URI(scheme, host, null, null).toString();
            }

            return new URI(scheme, null, host, port, null, null, null).toString();
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
        final Map<String, Object> vendorExtensions = ofNullable(swagger.getVendorExtensions()).orElse(Collections.emptyMap());
        final URI specificationUrl = (URI) vendorExtensions.get(BaseSwaggerConnectorGenerator.URL_EXTENSION);

        final String schemeToUse = determineSchemeToUse(swagger, specificationUrl);
        if (schemeToUse == null) {
            return null;
        }

        final String specificationHost = swagger.getHost();
        final boolean specificationWithoutHost = StringUtils.isEmpty(specificationHost);
        if (specificationWithoutHost && specificationUrl == null) {
            return null;
        }

        String hostToUse;
        if (specificationWithoutHost && specificationUrl != null) {
            hostToUse = specificationUrl.getHost();
        } else {
            hostToUse = swagger.getHost();
        }

        final int portToUse;
        final int colonIdx = hostToUse.indexOf(':');
        if (colonIdx == -1) {
            portToUse = -1;
        } else {
            portToUse = Integer.parseUnsignedInt(hostToUse.substring(colonIdx + 1));
            hostToUse = hostToUse.substring(0, colonIdx);
        }

        return createHostUri(schemeToUse, hostToUse, portToUse);
    }

    private static String determineSchemeToUse(final Swagger swagger, final URI specificationUrl) {
        final List<Scheme> schemes = swagger.getSchemes();
        final boolean noSchemes = schemes == null || schemes.isEmpty();
        if (noSchemes && specificationUrl == null) {
            return null;
        }

        final String schemeToUse;
        if (noSchemes && specificationUrl != null) {
            schemeToUse = specificationUrl.getScheme();
        } else if (schemes.contains(Scheme.HTTPS)) {
            schemeToUse = "https";
        } else {
            schemeToUse = schemes.stream()//
                .filter(s -> s.toValue().startsWith("http"))//
                .map(s -> s.toValue())//
                .findFirst()//
                .orElse(null);
        }
        return schemeToUse;
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

        return empty();
    }

    private static Optional<ConfigurationProperty> oauthProperty(final Swagger swagger, final ConfigurationProperty template,
        final Function<OAuth2Definition, String> defaultValueExtractor) {
        return securityDefinition(swagger, OAuth2Definition.class).map(definition -> new ConfigurationProperty.Builder()
            .createFrom(template).defaultValue(defaultValueExtractor.apply(definition)).build());
    }

    private static Optional<ConfigurationProperty> oauthVendorProperty(final Swagger swagger, final ConfigurationProperty template,
        final String name) {
        return securityDefinition(swagger, OAuth2Definition.class).map(definition -> vendorExtension(definition, template, name))
            .orElse(empty());
    }

    private static <T extends AbstractSecuritySchemeDefinition> Optional<T> securityDefinition(final Swagger swagger, final Class<T> type) {
        final Map<String, SecuritySchemeDefinition> securityDefinitions = swagger.getSecurityDefinitions();

        if (securityDefinitions == null) {
            return empty();
        }

        return securityDefinitions.values().stream().filter(type::isInstance).map(type::cast).findFirst();
    }

    private static Optional<ConfigurationProperty> vendorExtension(final SecuritySchemeDefinition definition,
        final ConfigurationProperty template, final String name) {
        final Map<String, Object> vendorExtensions = definition.getVendorExtensions();
        if (vendorExtensions == null) {
            return empty();
        }

        final Object value = vendorExtensions.get(name);
        if (value == null) {
            return empty();
        }

        final ConfigurationProperty property = new ConfigurationProperty.Builder().createFrom(template).defaultValue(String.valueOf(value))
            .build();

        return Optional.of(property);
    }

    private static BiFunction<Swagger, ConfigurationProperty, Optional<ConfigurationProperty>>
        withDefaultValue(final Function<Swagger, String> defaultValueExtractor) {
        return (swagger, template) -> Optional
            .of(new ConfigurationProperty.Builder().createFrom(template).defaultValue(defaultValueExtractor.apply(swagger)).build());
    }
}
