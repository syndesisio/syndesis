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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import io.swagger.models.auth.AbstractSecuritySchemeDefinition;
import io.swagger.models.auth.ApiKeyAuthDefinition;
import io.swagger.models.auth.BasicAuthDefinition;
import io.swagger.models.auth.OAuth2Definition;
import io.swagger.models.auth.SecuritySchemeDefinition;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.ConnectorSettings;

import org.apache.commons.lang3.StringUtils;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

@SuppressWarnings("PMD.GodClass")
enum PropertyGenerators {

    accessToken {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return PropertyGenerators::ifHasOAuthSecurityDefinition;
        }
    },
    accessTokenExpiresAt {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return PropertyGenerators::ifHasOAuthSecurityDefinition;
        }
    },
    authenticationParameterName {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return (swagger, template, settings) -> apiKeyProperty(swagger, template, settings, ApiKeyAuthDefinition::getName);
        }
    },
    authenticationParameterPlacement {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return (swagger, template, settings) -> securityDefinition(swagger, settings, ApiKeyAuthDefinition.class)
                .map(definition -> new ConfigurationProperty.Builder()
                    .createFrom(template)
                    .getEnum(Collections.emptyList())
                    .defaultValue(definition.getIn().toValue())
                    .build());
        }
    },
    authenticationParameterValue {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return PropertyGenerators::ifHasApiKeysSecurityDefinition;
        }
    },
    authenticationType {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return (swagger, template, settings) -> {
                final Map<String, SecuritySchemeDefinition> securityDefinitions = swagger.getSecurityDefinitions();
                if (securityDefinitions == null || securityDefinitions.isEmpty()) {
                    return Optional.of(NO_SECURITY.apply(template));
                }

                final ConfigurationProperty.PropertyValue[] enums = securityDefinitions.entrySet().stream()
                    .filter(e -> SupportedAuthenticationTypes.supports(e.getValue()))
                    .map(e -> SupportedAuthenticationTypes.asPropertyValue(e.getKey(), e.getValue()))
                    .toArray(l -> new ConfigurationProperty.PropertyValue[l]);

                if (enums.length == 0) {
                    return Optional.of(NO_SECURITY.apply(template));
                }

                final ConfigurationProperty.Builder authenticationType = new ConfigurationProperty.Builder()
                    .createFrom(template)
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
        protected PropertyGenerator propertyGenerator() {
            return (swagger, template, settings) -> oauthProperty(swagger, template, settings, OAuth2Definition::getAuthorizationUrl);
        }
    },
    authorizeUsingParameters {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return (swagger, template, settings) -> oauthVendorProperty(swagger, template, settings, "x-authorize-using-parameters");
        }
    },
    basePath {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return withDefaultValue(Swagger::getBasePath);
        }
    },
    clientId {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return PropertyGenerators::ifHasOAuthSecurityDefinition;
        }
    },
    clientSecret {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return PropertyGenerators::ifHasOAuthSecurityDefinition;
        }
    },
    host {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return withDefaultValue(PropertyGenerators::determineHost);
        }
    },
    oauthScopes {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return (swagger, template, settings) -> oauthProperty(swagger, template, settings,
                d -> ofNullable(d.getScopes()).map(scopes -> scopes.keySet().stream().sorted().collect(Collectors.joining(" "))).orElse(null));
        }
    },
    password {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return PropertyGenerators::ifHasBasicSecurityDefinition;
        }
    },
    refreshToken {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return PropertyGenerators::ifHasOAuthSecurityDefinition;
        }
    },
    refreshTokenRetryStatuses {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return (swagger, template, settings) -> oauthVendorProperty(swagger, template, settings, "x-refresh-token-retry-statuses");
        }
    },
    specification {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return PropertyGenerators::fromTemplate;
        }
    },
    tokenEndpoint {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return (swagger, template, settings) -> oauthProperty(swagger, template, settings, OAuth2Definition::getTokenUrl);
        }
    },
    tokenStrategy {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return (swagger, template, settings) -> oauthVendorProperty(swagger, template, settings, "x-token-strategy");
        }
    },
    username {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return PropertyGenerators::ifHasBasicSecurityDefinition;
        }
    };

    private static final Function<ConfigurationProperty, ConfigurationProperty> NO_SECURITY = template -> new ConfigurationProperty.Builder()
        .createFrom(template)
        .defaultValue("none")
        .addEnum(ConfigurationProperty.PropertyValue.Builder.of("none", "No Security"))
        .build();

    @FunctionalInterface
    interface PropertyGenerator {
        Optional<ConfigurationProperty> generate(Swagger swagger, ConfigurationProperty template, ConnectorSettings connectorSettings);
    }

    protected abstract PropertyGenerator propertyGenerator();

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
        final ConfigurationProperty template, final ConnectorSettings connectorSettings) {
        final PropertyGenerators propertyGenerator = PropertyGenerators.valueOf(propertyName);

        return propertyGenerator.propertyGenerator().generate(swagger, template, connectorSettings);
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

    static <T extends AbstractSecuritySchemeDefinition> Optional<T> securityDefinition(final Swagger swagger, final ConnectorSettings connectorSettings,
        final Class<T> type) {
        final Map<String, SecuritySchemeDefinition> securityDefinitions = swagger.getSecurityDefinitions();

        if (securityDefinitions == null || securityDefinitions.isEmpty()) {
            return empty();
        }

        final Map<String, SecuritySchemeDefinition> supportedSecurityDefinitions = securityDefinitions.entrySet().stream()
            .filter(e -> type.isInstance(e.getValue()))
            .filter(e -> SupportedAuthenticationTypes.supports(e.getValue()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (supportedSecurityDefinitions.isEmpty()) {
            // no supported security definitions of selected type defined
            return empty();
        }

        final Map<String, String> configuredProperties = connectorSettings.getConfiguredProperties();
        final String configuredAuthenticationType = configuredProperties.get(authenticationType.name());

        if (supportedSecurityDefinitions.size() == 1 && configuredAuthenticationType == null) {
            // we have only one, so we provide that one as the user hasn't
            // expressed any preference
            @SuppressWarnings("unchecked")
            final T onlySecurityDefinitionPresent = (T) supportedSecurityDefinitions.values().iterator().next();

            return Optional.of(onlySecurityDefinitionPresent);
        }

        if (configuredAuthenticationType == null) {
            // we don't have a way to choose, no preference was given and there
            // are zero or more than one security definitions present
            return empty();
        }

        for (final Map.Entry<String, SecuritySchemeDefinition> securityDefinition : supportedSecurityDefinitions.entrySet()) {
            // we have more than one supported security definition and the
            // configured authentication type matches that definition
            final int idx = configuredAuthenticationType.indexOf(':');

            if (idx > 0 && securityDefinition.getKey().equals(configuredAuthenticationType.substring(idx + 1))) {
                @SuppressWarnings("unchecked")
                final T choosenSecurityDefinition = (T) securityDefinition.getValue();

                return Optional.of(choosenSecurityDefinition);
            }
        }

        // more than one security definition of the requested type is present
        // and the configured authentication type doesn't match either of those
        return empty();
    }

    private static Optional<ConfigurationProperty> apiKeyProperty(final Swagger swagger, final ConfigurationProperty template,
        final ConnectorSettings connectorSettings,
        final Function<ApiKeyAuthDefinition, String> defaultValueExtractor) {
        return securityDefinition(swagger, connectorSettings, ApiKeyAuthDefinition.class)
            .map(definition -> new ConfigurationProperty.Builder()
                .createFrom(template)
                .defaultValue(defaultValueExtractor.apply(definition))
                .build());
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
        final ConfigurationProperty template, @SuppressWarnings("unused") final ConnectorSettings connectorSettings) {
        return Optional.of(template);
    }

    private static Optional<ConfigurationProperty> ifHasApiKeysSecurityDefinition(final Swagger swagger,
        final ConfigurationProperty template, final ConnectorSettings connectorSettings) {
        return ifHasSecurityDefinition(swagger, template, connectorSettings, ApiKeyAuthDefinition.class);
    }

    private static Optional<ConfigurationProperty> ifHasBasicSecurityDefinition(final Swagger swagger,
        final ConfigurationProperty template, final ConnectorSettings connectorSettings) {
        return ifHasSecurityDefinition(swagger, template, connectorSettings, BasicAuthDefinition.class);
    }

    private static Optional<ConfigurationProperty> ifHasOAuthSecurityDefinition(final Swagger swagger,
        final ConfigurationProperty template, final ConnectorSettings connectorSettings) {
        return ifHasSecurityDefinition(swagger, template, connectorSettings, OAuth2Definition.class);
    }

    private static Optional<ConfigurationProperty> ifHasSecurityDefinition(final Swagger swagger, final ConfigurationProperty template,
        final ConnectorSettings connectorSettings, final Class<? extends AbstractSecuritySchemeDefinition> type) {
        final Optional<? extends AbstractSecuritySchemeDefinition> securityDefinition = securityDefinition(swagger, connectorSettings, type);

        if (securityDefinition.isPresent()) {
            return Optional.of(template);
        }

        return empty();
    }

    private static Optional<ConfigurationProperty> oauthProperty(final Swagger swagger, final ConfigurationProperty template,
        final ConnectorSettings connectorSettings,
        final Function<OAuth2Definition, String> defaultValueExtractor) {
        return securityDefinition(swagger, connectorSettings, OAuth2Definition.class).map(definition -> new ConfigurationProperty.Builder()
            .createFrom(template).defaultValue(defaultValueExtractor.apply(definition)).build());
    }

    private static Optional<ConfigurationProperty> oauthVendorProperty(final Swagger swagger, final ConfigurationProperty template,
        final ConnectorSettings connectorSettings,
        final String name) {
        return securityDefinition(swagger, connectorSettings, OAuth2Definition.class).map(definition -> vendorExtension(definition, template, name))
            .orElse(empty());
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

    private static PropertyGenerator
        withDefaultValue(final Function<Swagger, String> defaultValueExtractor) {
        return (swagger, template, settings) -> Optional
            .of(new ConfigurationProperty.Builder().createFrom(template).defaultValue(defaultValueExtractor.apply(swagger)).build());
    }
}
