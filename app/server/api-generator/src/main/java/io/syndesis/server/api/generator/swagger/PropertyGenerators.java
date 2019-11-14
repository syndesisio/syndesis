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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.apicurio.datamodels.core.models.Extension;
import io.apicurio.datamodels.core.models.common.SecurityScheme;
import io.apicurio.datamodels.openapi.v2.models.Oas20Document;
import io.apicurio.datamodels.openapi.v2.models.Oas20Scopes;
import io.apicurio.datamodels.openapi.v2.models.Oas20SecurityDefinitions;
import io.apicurio.datamodels.openapi.v2.models.Oas20SecurityScheme;
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
            return (openApiDoc, template, settings) -> apiKeyProperty(openApiDoc, template, settings, SecurityScheme::getSchemeName);
        }
    },
    authenticationParameterPlacement {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return (openApiDoc, template, settings) -> securityDefinition(openApiDoc, settings, SchemeType.API_KEY)
                .map(definition -> new ConfigurationProperty.Builder()
                    .createFrom(template)
                    .getEnum(Collections.emptyList())
                    .defaultValue(definition.in)
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
            return (openApiDoc, template, settings) -> {
                final List<Oas20SecurityScheme> securityDefinitions = ofNullable(openApiDoc.securityDefinitions)
                                                                        .map(Oas20SecurityDefinitions::getSecuritySchemes)
                                                                        .orElse(Collections.emptyList());
                if (securityDefinitions.isEmpty()) {
                    return Optional.of(NO_SECURITY.apply(template));
                }

                final ConfigurationProperty.PropertyValue[] enums = securityDefinitions.stream()
                    .filter(SupportedAuthenticationTypes::supports)
                    .map(e -> SupportedAuthenticationTypes.asPropertyValue(e.getSchemeName(), e))
                    .toArray(ConfigurationProperty.PropertyValue[]::new);

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
            return (openApiDoc, template, settings) -> oauthProperty(openApiDoc, template, settings, scheme -> scheme.authorizationUrl);
        }
    },
    authorizeUsingParameters {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return (openApiDoc, template, settings) -> oauthVendorProperty(openApiDoc, template, settings, "x-authorize-using-parameters");
        }
    },
    basePath {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return withDefaultValue(doc -> doc.basePath);
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
            return (openApiDoc, template, settings) -> oauthProperty(openApiDoc, template, settings,
                d -> ofNullable(d.scopes).map(Oas20Scopes::getScopeNames).map(scopes -> scopes.stream().sorted().collect(Collectors.joining(" "))).orElse(null));
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
            return (openApiDoc, template, settings) -> oauthVendorProperty(openApiDoc, template, settings, "x-refresh-token-retry-statuses");
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
            return (openApiDoc, template, settings) -> oauthProperty(openApiDoc, template, settings, scheme -> scheme.tokenUrl);
        }
    },
    tokenStrategy {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return (openApiDoc, template, settings) -> oauthVendorProperty(openApiDoc, template, settings, "x-token-strategy");
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
        Optional<ConfigurationProperty> generate(Oas20Document openApiDoc, ConfigurationProperty template, ConnectorSettings connectorSettings);
    }

    /**
     * Supported security schemes.
     */
    enum SchemeType {
        OAUTH2("oauth2"),
        BASIC("basic"),
        API_KEY("apiKey");

        private final String name;
        SchemeType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
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

    static Optional<ConfigurationProperty> createProperty(final String propertyName, final Oas20Document openApiDoc,
        final ConfigurationProperty template, final ConnectorSettings connectorSettings) {
        final PropertyGenerators propertyGenerator = PropertyGenerators.valueOf(propertyName);

        return propertyGenerator.propertyGenerator().generate(openApiDoc, template, connectorSettings);
    }

    static String determineHost(final Oas20Document openApiDoc) {
        final Collection<Extension> vendorExtensions = ofNullable(openApiDoc.getExtensions()).orElse(Collections.emptyList());
        final URI specificationUrl = vendorExtensions.stream()
                                                     .filter(extension -> BaseOpenApiConnectorGenerator.URL_EXTENSION.equals(extension.name))
                                                     .findFirst()
                                                     .map(extension -> (URI) extension.value)
                                                     .orElse(null);

        final String schemeToUse = determineSchemeToUse(openApiDoc, specificationUrl);
        if (schemeToUse == null) {
            return null;
        }

        final String specificationHost = openApiDoc.host;
        final boolean specificationWithoutHost = StringUtils.isEmpty(specificationHost);
        if (specificationWithoutHost && specificationUrl == null) {
            return null;
        }

        String hostToUse;
        if (specificationWithoutHost) {
            hostToUse = specificationUrl.getHost();
        } else {
            hostToUse = openApiDoc.host;
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

    static Optional<Oas20SecurityScheme> securityDefinition(final Oas20Document openApiDoc,
                                                            final ConnectorSettings connectorSettings, final SchemeType type) {
        final List<Oas20SecurityScheme> securitySchemes = ofNullable(openApiDoc.securityDefinitions)
                                                              .map(Oas20SecurityDefinitions::getSecuritySchemes)
                                                              .orElse(Collections.emptyList());
        if (securitySchemes.isEmpty()) {
            return empty();
        }

        final List<Oas20SecurityScheme> supportedSecuritySchemes = securitySchemes.stream()
            .filter(scheme -> type.getName().equals(scheme.type))
            .filter(SupportedAuthenticationTypes::supports)
            .collect(Collectors.toList());

        if (supportedSecuritySchemes.isEmpty()) {
            // no supported security definitions of selected type defined
            return empty();
        }

        final Map<String, String> configuredProperties = connectorSettings.getConfiguredProperties();
        final String configuredAuthenticationType = configuredProperties.get(authenticationType.name());

        if (supportedSecuritySchemes.size() == 1 && configuredAuthenticationType == null) {
            // we have only one, so we provide that one as the user hasn't
            // expressed any preference
            final Oas20SecurityScheme onlySecuritySchemePresent = supportedSecuritySchemes.get(0);

            return Optional.of(onlySecuritySchemePresent);
        }

        if (configuredAuthenticationType == null) {
            // we don't have a way to choose, no preference was given and there
            // are zero or more than one security definitions present
            return empty();
        }

        for (final Oas20SecurityScheme securityScheme : supportedSecuritySchemes) {
            // we have more than one supported security definition and the
            // configured authentication type matches that definition
            final int idx = configuredAuthenticationType.indexOf(':');

            if (idx > 0 && securityScheme.getSchemeName().equals(configuredAuthenticationType.substring(idx + 1))) {
                return Optional.of(securityScheme);
            }
        }

        // more than one security definition of the requested type is present
        // and the configured authentication type doesn't match either of those
        return empty();
    }

    private static Optional<ConfigurationProperty> apiKeyProperty(final Oas20Document openApiDoc, final ConfigurationProperty template,
        final ConnectorSettings connectorSettings,
        final Function<Oas20SecurityScheme, String> defaultValueExtractor) {
        return securityDefinition(openApiDoc, connectorSettings, SchemeType.API_KEY)
            .map(definition -> new ConfigurationProperty.Builder()
                .createFrom(template)
                .defaultValue(defaultValueExtractor.apply(definition))
                .build());
    }

    private static String determineSchemeToUse(final Oas20Document openApiDoc, final URI specificationUrl) {
        final List<String> schemes = openApiDoc.schemes;
        final boolean noSchemes = schemes == null || schemes.isEmpty();
        if (noSchemes && specificationUrl == null) {
            return null;
        }

        final String schemeToUse;
        if (noSchemes) {
            schemeToUse = specificationUrl.getScheme();
        } else if (schemes.contains("https")) {
            schemeToUse = "https";
        } else {
            schemeToUse = schemes.stream()//
                .filter(s -> s.startsWith("http"))//
                .findFirst()//
                .orElse(null);
        }
        return schemeToUse;
    }

    private static Optional<ConfigurationProperty> fromTemplate(@SuppressWarnings("unused") final Oas20Document openApiDoc,
        final ConfigurationProperty template, @SuppressWarnings("unused") final ConnectorSettings connectorSettings) {
        return Optional.of(template);
    }

    private static Optional<ConfigurationProperty> ifHasApiKeysSecurityDefinition(final Oas20Document openApiDoc,
        final ConfigurationProperty template, final ConnectorSettings connectorSettings) {
        return ifHasSecurityDefinition(openApiDoc, template, connectorSettings, SchemeType.API_KEY);
    }

    private static Optional<ConfigurationProperty> ifHasBasicSecurityDefinition(final Oas20Document openApiDoc,
        final ConfigurationProperty template, final ConnectorSettings connectorSettings) {
        return ifHasSecurityDefinition(openApiDoc, template, connectorSettings, SchemeType.BASIC);
    }

    private static Optional<ConfigurationProperty> ifHasOAuthSecurityDefinition(final Oas20Document openApiDoc,
        final ConfigurationProperty template, final ConnectorSettings connectorSettings) {
        return ifHasSecurityDefinition(openApiDoc, template, connectorSettings, SchemeType.OAUTH2);
    }

    private static Optional<ConfigurationProperty> ifHasSecurityDefinition(final Oas20Document openApiDoc, final ConfigurationProperty template,
        final ConnectorSettings connectorSettings, final SchemeType type) {
        final Optional<Oas20SecurityScheme> securityDefinition = securityDefinition(openApiDoc, connectorSettings, type);

        if (securityDefinition.isPresent()) {
            return Optional.of(template);
        }

        return empty();
    }

    private static Optional<ConfigurationProperty> oauthProperty(final Oas20Document openApiDoc, final ConfigurationProperty template,
        final ConnectorSettings connectorSettings,
        final Function<Oas20SecurityScheme, String> defaultValueExtractor) {
        return securityDefinition(openApiDoc, connectorSettings, SchemeType.OAUTH2).map(definition -> new ConfigurationProperty.Builder()
            .createFrom(template).defaultValue(defaultValueExtractor.apply(definition)).build());
    }

    private static Optional<ConfigurationProperty> oauthVendorProperty(final Oas20Document openApiDoc, final ConfigurationProperty template,
        final ConnectorSettings connectorSettings,
        final String name) {
        return securityDefinition(openApiDoc, connectorSettings, SchemeType.OAUTH2).flatMap(definition -> vendorExtension(definition, template, name));
    }

    private static Optional<ConfigurationProperty> vendorExtension(final Oas20SecurityScheme definition,
        final ConfigurationProperty template, final String name) {
        final Collection<Extension> vendorExtensions = definition.getExtensions();
        if (vendorExtensions == null) {
            return empty();
        }

        final Optional<Extension> maybeExtension = vendorExtensions.stream()
                                                                .filter(extension -> name.equals(extension.name))
                                                                .findFirst();
        if (!maybeExtension.isPresent()) {
            return empty();
        }

        final ConfigurationProperty property = new ConfigurationProperty.Builder().createFrom(template).defaultValue(String.valueOf(maybeExtension.get().value))
            .build();

        return Optional.of(property);
    }

    private static PropertyGenerator
        withDefaultValue(final Function<Oas20Document, String> defaultValueExtractor) {
        return (openApiDoc, template, settings) -> Optional
            .of(new ConfigurationProperty.Builder().createFrom(template).defaultValue(defaultValueExtractor.apply(openApiDoc)).build());
    }
}
