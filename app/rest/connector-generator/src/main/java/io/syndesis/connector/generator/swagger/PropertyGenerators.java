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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import io.swagger.models.auth.OAuth2Definition;
import io.swagger.models.auth.SecuritySchemeDefinition;
import io.syndesis.model.connection.ConfigurationProperty;
import io.syndesis.model.connection.ConfigurationProperty.PropertyValue;

import org.apache.commons.lang3.StringUtils;

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
            return withDefaultValue(PropertyGenerators::determineHost);
        }
    };

    private static final Set<String> KNOWN_PROPERTIES = Arrays.stream(values()).map(PropertyGenerators::name).collect(Collectors.toSet());

    private static final ConfigurationProperty.PropertyValue NO_SECURITY = new ConfigurationProperty.PropertyValue.Builder().value("none")
        .label("No Security").build();

    protected abstract BiFunction<Swagger, ConfigurationProperty, Optional<ConfigurationProperty>> propertyValueExtractor();

    /* default */ static String createHostUri(final String scheme, final String host) {
        try {
            return new URI(scheme, host, null, null).toString();
        } catch (final URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /* default */ static Optional<ConfigurationProperty> createProperty(final String propertyName, final Swagger swagger,
        final ConfigurationProperty template) {
        if (!KNOWN_PROPERTIES.contains(propertyName)) {
            return Optional.ofNullable(template);
        }

        final PropertyGenerators defaultPropertyValue = PropertyGenerators.valueOf(propertyName);

        return defaultPropertyValue.propertyValueExtractor().apply(swagger, template);
    }

    /* default */ static String determineHost(final Swagger swagger) {
        final Map<String, Object> vendorExtensions = Optional.ofNullable(swagger.getVendorExtensions()).orElse(Collections.emptyMap());
        final URI specificationUrl = (URI) vendorExtensions.get(SwaggerConnectorGenerator.URL_EXTENSION);

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
