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
package io.syndesis.server.api.generator.openapi.v2;

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
import io.apicurio.datamodels.openapi.v2.models.Oas20Scopes;
import io.apicurio.datamodels.openapi.v2.models.Oas20SecurityDefinitions;
import io.apicurio.datamodels.openapi.v2.models.Oas20SecurityScheme;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.ConnectorSettings;
import io.syndesis.server.api.generator.openapi.OpenApiModelInfo;
import io.syndesis.server.api.generator.openapi.PropertyGenerator;
import io.syndesis.server.api.generator.openapi.SecurityScheme;
import io.syndesis.server.api.generator.openapi.SupportedAuthenticationTypes;
import io.syndesis.server.api.generator.openapi.util.OasModelHelper;
import org.apache.commons.lang3.StringUtils;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

@SuppressWarnings("PMD.GodClass")
public enum Oas20PropertyGenerators {

    accessToken {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return Oas20PropertyGenerators::ifHasOAuthSecurityDefinition;
        }
    },
    accessTokenExpiresAt {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return Oas20PropertyGenerators::ifHasOAuthSecurityDefinition;
        }
    },
    authenticationParameterName {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return (info, template, settings) -> apiKeyProperty(info, template, settings, io.apicurio.datamodels.core.models.common.SecurityScheme::getSchemeName);
        }
    },
    authenticationParameterPlacement {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return (info, template, settings) -> securityDefinition(info, settings, SecurityScheme.API_KEY)
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
            return Oas20PropertyGenerators::ifHasApiKeysSecurityDefinition;
        }
    },
    authenticationType {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return (info, template, settings) -> {
                final List<Oas20SecurityScheme> securityDefinitions = ofNullable(info.getV2Model().securityDefinitions)
                                                                        .map(Oas20SecurityDefinitions::getSecuritySchemes)
                                                                        .orElse(Collections.emptyList());
                if (securityDefinitions.isEmpty()) {
                    return Optional.of(NO_SECURITY.apply(template));
                }

                final ConfigurationProperty.PropertyValue[] enums = securityDefinitions.stream()
                    .filter(scheme -> SupportedAuthenticationTypes.supports(scheme.type, scheme.flow))
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
            return (info, template, settings) -> oauthProperty(info, template, settings, scheme -> scheme.authorizationUrl);
        }
    },
    authorizeUsingParameters {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return (info, template, settings) -> oauthVendorProperty(info, template, settings, "x-authorize-using-parameters");
        }
    },
    basePath {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return withDefaultValue(info -> info.getV2Model().basePath);
        }
    },
    clientId {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return Oas20PropertyGenerators::ifHasOAuthSecurityDefinition;
        }
    },
    clientSecret {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return Oas20PropertyGenerators::ifHasOAuthSecurityDefinition;
        }
    },
    host {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return withDefaultValue(Oas20PropertyGenerators::determineHost);
        }
    },
    oauthScopes {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return (info, template, settings) -> oauthProperty(info, template, settings,
                d -> ofNullable(d.scopes).map(Oas20Scopes::getScopeNames).map(scopes -> scopes.stream().sorted().collect(Collectors.joining(" "))).orElse(null));
        }
    },
    password {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return Oas20PropertyGenerators::ifHasBasicSecurityDefinition;
        }
    },
    refreshToken {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return Oas20PropertyGenerators::ifHasOAuthSecurityDefinition;
        }
    },
    refreshTokenRetryStatuses {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return (info, template, settings) -> oauthVendorProperty(info, template, settings, "x-refresh-token-retry-statuses");
        }
    },
    specification {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return Oas20PropertyGenerators::fromTemplate;
        }
    },
    tokenEndpoint {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return (info, template, settings) -> oauthProperty(info, template, settings, scheme -> scheme.tokenUrl);
        }
    },
    tokenStrategy {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return (info, template, settings) -> oauthVendorProperty(info, template, settings, "x-token-strategy");
        }
    },
    username {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return Oas20PropertyGenerators::ifHasBasicSecurityDefinition;
        }
    };

    private static final Function<ConfigurationProperty, ConfigurationProperty> NO_SECURITY = template -> new ConfigurationProperty.Builder()
        .createFrom(template)
        .defaultValue("none")
        .addEnum(ConfigurationProperty.PropertyValue.Builder.of("none", "No Security"))
        .build();

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

    public static Optional<ConfigurationProperty> createProperty(final String propertyName, final OpenApiModelInfo info,
        final ConfigurationProperty template, final ConnectorSettings connectorSettings) {
        final Oas20PropertyGenerators propertyGenerator = Oas20PropertyGenerators.valueOf(propertyName);

        return propertyGenerator.propertyGenerator().generate(info, template, connectorSettings);
    }

    static String determineHost(final OpenApiModelInfo info) {
        final Collection<Extension> vendorExtensions = ofNullable(info.getV2Model().getExtensions()).orElse(Collections.emptyList());
        final URI specificationUrl = vendorExtensions.stream()
                                                     .filter(extension -> OasModelHelper.URL_EXTENSION.equals(extension.name))
                                                     .findFirst()
                                                     .map(extension -> (URI) extension.value)
                                                     .orElse(null);

        final String schemeToUse = determineSchemeToUse(info, specificationUrl);
        if (schemeToUse == null) {
            return null;
        }

        final String specificationHost = info.getV2Model().host;
        final boolean specificationWithoutHost = StringUtils.isEmpty(specificationHost);
        if (specificationWithoutHost && specificationUrl == null) {
            return null;
        }

        String hostToUse;
        if (specificationWithoutHost) {
            hostToUse = specificationUrl.getHost();
        } else {
            hostToUse = info.getV2Model().host;
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

    static Optional<Oas20SecurityScheme> securityDefinition(final OpenApiModelInfo info,
                                                            final ConnectorSettings connectorSettings, final SecurityScheme type) {
        final List<Oas20SecurityScheme> securitySchemes = ofNullable(info.getV2Model().securityDefinitions)
                                                              .map(Oas20SecurityDefinitions::getSecuritySchemes)
                                                              .orElse(Collections.emptyList());
        if (securitySchemes.isEmpty()) {
            return empty();
        }

        final List<Oas20SecurityScheme> supportedSecuritySchemes = securitySchemes.stream()
            .filter(scheme -> type.getName().equals(scheme.type))
            .filter(scheme -> SupportedAuthenticationTypes.supports(scheme.type, scheme.flow))
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

    private static Optional<ConfigurationProperty> apiKeyProperty(final OpenApiModelInfo info, final ConfigurationProperty template,
        final ConnectorSettings connectorSettings,
        final Function<Oas20SecurityScheme, String> defaultValueExtractor) {
        return securityDefinition(info, connectorSettings, SecurityScheme.API_KEY)
            .map(definition -> new ConfigurationProperty.Builder()
                .createFrom(template)
                .defaultValue(defaultValueExtractor.apply(definition))
                .build());
    }

    private static String determineSchemeToUse(final OpenApiModelInfo info, final URI specificationUrl) {
        final List<String> schemes = info.getV2Model().schemes;
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

    private static Optional<ConfigurationProperty> fromTemplate(@SuppressWarnings("unused") final OpenApiModelInfo info,
        final ConfigurationProperty template, @SuppressWarnings("unused") final ConnectorSettings connectorSettings) {
        return Optional.of(template);
    }

    private static Optional<ConfigurationProperty> ifHasApiKeysSecurityDefinition(final OpenApiModelInfo info,
        final ConfigurationProperty template, final ConnectorSettings connectorSettings) {
        return ifHasSecurityDefinition(info, template, connectorSettings, SecurityScheme.API_KEY);
    }

    private static Optional<ConfigurationProperty> ifHasBasicSecurityDefinition(final OpenApiModelInfo info,
        final ConfigurationProperty template, final ConnectorSettings connectorSettings) {
        return ifHasSecurityDefinition(info, template, connectorSettings, SecurityScheme.BASIC);
    }

    private static Optional<ConfigurationProperty> ifHasOAuthSecurityDefinition(final OpenApiModelInfo info,
        final ConfigurationProperty template, final ConnectorSettings connectorSettings) {
        return ifHasSecurityDefinition(info, template, connectorSettings, SecurityScheme.OAUTH2);
    }

    private static Optional<ConfigurationProperty> ifHasSecurityDefinition(final OpenApiModelInfo info, final ConfigurationProperty template,
        final ConnectorSettings connectorSettings, final SecurityScheme type) {
        final Optional<Oas20SecurityScheme> securityDefinition = securityDefinition(info, connectorSettings, type);

        if (securityDefinition.isPresent()) {
            return Optional.of(template);
        }

        return empty();
    }

    private static Optional<ConfigurationProperty> oauthProperty(final OpenApiModelInfo info, final ConfigurationProperty template,
        final ConnectorSettings connectorSettings,
        final Function<Oas20SecurityScheme, String> defaultValueExtractor) {
        return securityDefinition(info, connectorSettings, SecurityScheme.OAUTH2).map(definition -> new ConfigurationProperty.Builder()
            .createFrom(template).defaultValue(defaultValueExtractor.apply(definition)).build());
    }

    private static Optional<ConfigurationProperty> oauthVendorProperty(final OpenApiModelInfo info, final ConfigurationProperty template,
        final ConnectorSettings connectorSettings,
        final String name) {
        return securityDefinition(info, connectorSettings, SecurityScheme.OAUTH2).flatMap(definition -> vendorExtension(definition, template, name));
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

    private static PropertyGenerator withDefaultValue(final Function<OpenApiModelInfo, String> defaultValueExtractor) {
        return (info, template, settings) -> Optional
            .of(new ConfigurationProperty.Builder().createFrom(template).defaultValue(defaultValueExtractor.apply(info)).build());
    }
}
