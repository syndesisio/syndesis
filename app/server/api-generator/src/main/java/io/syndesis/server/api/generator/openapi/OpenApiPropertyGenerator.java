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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.apicurio.datamodels.core.models.Extension;
import io.apicurio.datamodels.core.models.common.SecurityScheme;
import io.apicurio.datamodels.openapi.models.OasDocument;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.ConnectorSettings;
import io.syndesis.server.api.generator.openapi.util.OasModelHelper;
import org.apache.commons.lang3.StringUtils;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

/**
 * Set of property generators that may apply to the give OpenAPI specification. When applicable property is added as configuration
 * property. Subclasses provide 2.x and 3.x OpenAPI specific implementations.
 */
@SuppressWarnings("PMD.GodClass")
public abstract class OpenApiPropertyGenerator<T extends OasDocument, S extends SecurityScheme> {

    private final Map<String, PropertyGenerator> propertyGenerators = new HashMap<>();

    private static final Function<ConfigurationProperty, ConfigurationProperty> NO_SECURITY = template -> new ConfigurationProperty.Builder()
        .createFrom(template)
        .defaultValue("none")
        .addEnum(ConfigurationProperty.PropertyValue.Builder.of("none", "No Security"))
        .build();

    protected OpenApiPropertyGenerator() {
        propertyGenerators.put("accessToken", this::ifHasOAuthSecurityDefinition);
        propertyGenerators.put("accessTokenExpiresAt", this::ifHasOAuthSecurityDefinition);
        propertyGenerators.put("authenticationParameterName", (info, template, settings) -> apiKeyProperty(info, template, settings, SecurityScheme::getSchemeName));
        propertyGenerators.put("authenticationParameterPlacement", this::apiKeyPropertyPlacement);
        propertyGenerators.put("authenticationParameterValue", this::ifHasApiKeysSecurityDefinition);
        propertyGenerators.put("authenticationType", (info, template, settings) -> authenticationTypeProperty(info, template));
        propertyGenerators.put("authorizationEndpoint", (info, template, settings) -> oauthProperty(info, template, settings, authorizationUrl()));
        propertyGenerators.put("authorizeUsingParameters", (info, template, settings) -> oauthVendorProperty(info, template, settings, "x-authorize-using-parameters"));
        propertyGenerators.put("basePath", withDefaultValue(this::basePath));
        propertyGenerators.put("clientId", this::ifHasOAuthSecurityDefinition);
        propertyGenerators.put("clientSecret", this::ifHasOAuthSecurityDefinition);
        propertyGenerators.put("host", withDefaultValue(this::determineHost));
        propertyGenerators.put("oauthScopes", (info, template, settings) -> oauthProperty(info, template, settings, scopes()));
        propertyGenerators.put("password", this::ifHasBasicSecurityDefinition);
        propertyGenerators.put("refreshToken", this::ifHasOAuthSecurityDefinition);
        propertyGenerators.put("refreshTokenRetryStatuses", (info, template, settings) -> oauthVendorProperty(info, template, settings, "x-refresh-token-retry-statuses"));
        propertyGenerators.put("specification", OpenApiPropertyGenerator::fromTemplate);
        propertyGenerators.put("tokenEndpoint", (info, template, settings) -> oauthProperty(info, template, settings, tokenUrl()));
        propertyGenerators.put("tokenStrategy", (info, template, settings) -> oauthVendorProperty(info, template, settings, "x-token-strategy"));
        propertyGenerators.put("username", this::ifHasBasicSecurityDefinition);
    }

    public Optional<ConfigurationProperty> createProperty(final String propertyName, final OpenApiModelInfo info,
                                                                 final ConfigurationProperty template, final ConnectorSettings connectorSettings) {
        final PropertyGenerator propertyGenerator = propertyGenerators.get(propertyName);

        return propertyGenerator.generate(info, template, connectorSettings);
    }

    protected abstract String basePath(OpenApiModelInfo info);

    protected abstract Function<S, String> authorizationUrl();

    protected abstract Function<S, String> tokenUrl();

    protected abstract Function<S, String> scopes();

    protected abstract Collection<S> getSecuritySchemes(OpenApiModelInfo info);

    protected abstract String getFlow(S scheme);

    protected abstract String getHost(OpenApiModelInfo info);

    protected abstract List<String> getSchemes(OpenApiModelInfo info);

    public PropertyGenerator forProperty(String propertyName) {
        return propertyGenerators.get(propertyName);
    }

    public static String createHostUri(final String scheme, final String host, final int port) {
        try {
            if (port == -1) {
                return new URI(scheme, host, null, null).toString();
            }

            return new URI(scheme, null, host, port, null, null, null).toString();
        } catch (final URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public String determineHost(final OpenApiModelInfo info) {
        final Collection<Extension> vendorExtensions = ofNullable(info.getModel().getExtensions()).orElse(Collections.emptyList());
        final URI specificationUrl = vendorExtensions.stream()
            .filter(extension -> OasModelHelper.URL_EXTENSION.equals(extension.name))
            .findFirst()
            .map(extension -> (URI) extension.value)
            .orElse(null);

        final String schemeToUse = determineSchemeToUse(info, specificationUrl);
        if (schemeToUse == null) {
            return null;
        }

        final String specificationHost = getHost(info);

        String hostToUse;
        if (StringUtils.isNotEmpty(specificationHost)) {
            hostToUse = specificationHost;
        } else if (specificationUrl != null) {
            hostToUse = specificationUrl.getHost();
        } else {
            return null;
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

    public Optional<S> securityDefinition(final OpenApiModelInfo info,
                                           final ConnectorSettings connectorSettings, final OpenApiSecurityScheme type) {
        final Collection<S> securitySchemes = getSecuritySchemes(info);
        if (securitySchemes.isEmpty()) {
            return empty();
        }

        final List<S> supportedSecuritySchemes = securitySchemes.stream()
            .filter(scheme -> type.equalTo(scheme.type))
            .filter(scheme -> SupportedAuthenticationTypes.supports(scheme.type, getFlow(scheme)))
            .collect(Collectors.toList());

        if (supportedSecuritySchemes.isEmpty()) {
            // no supported security definitions of selected type defined
            return empty();
        }

        final Map<String, String> configuredProperties = connectorSettings.getConfiguredProperties();
        final String configuredAuthenticationType = configuredProperties.get("authenticationType");

        if (supportedSecuritySchemes.size() == 1 && configuredAuthenticationType == null) {
            // we have only one, so we provide that one as the user hasn't
            // expressed any preference
            final S onlySecuritySchemePresent = supportedSecuritySchemes.get(0);

            return Optional.of(onlySecuritySchemePresent);
        }

        if (configuredAuthenticationType == null) {
            // we don't have a way to choose, no preference was given and there
            // are zero or more than one security definitions present
            return empty();
        }

        for (final S securityScheme : supportedSecuritySchemes) {
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

    private Optional<ConfigurationProperty> authenticationTypeProperty(OpenApiModelInfo info, ConfigurationProperty template) {
        final Collection<S> securityDefinitions = getSecuritySchemes(info);
        if (securityDefinitions.isEmpty()) {
            return Optional.of(NO_SECURITY.apply(template));
        }

        final ConfigurationProperty.PropertyValue[] enums = securityDefinitions.stream()
            .filter(scheme -> SupportedAuthenticationTypes.supports(scheme.type, getFlow(scheme)))
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
    }

    private Optional<ConfigurationProperty> apiKeyProperty(final OpenApiModelInfo info, final ConfigurationProperty template,
                                                                  final ConnectorSettings connectorSettings,
                                                                  final Function<SecurityScheme, String> defaultValueExtractor) {
        return securityDefinition(info, connectorSettings, OpenApiSecurityScheme.API_KEY)
            .map(definition -> new ConfigurationProperty.Builder()
                .createFrom(template)
                .defaultValue(defaultValueExtractor.apply(definition))
                .build());
    }

    private Optional<ConfigurationProperty> apiKeyPropertyPlacement(final OpenApiModelInfo info, final ConfigurationProperty template,
                                                                  final ConnectorSettings connectorSettings) {
        return securityDefinition(info, connectorSettings, OpenApiSecurityScheme.API_KEY)
            .map(definition -> new ConfigurationProperty.Builder()
                .createFrom(template)
                .getEnum(Collections.emptyList())
                .defaultValue(definition.in)
                .build());
    }

    private String determineSchemeToUse(final OpenApiModelInfo info, final URI specificationUrl) {
        final List<String> schemes = getSchemes(info);
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

    private Optional<ConfigurationProperty> ifHasApiKeysSecurityDefinition(final OpenApiModelInfo info,
                                                                                  final ConfigurationProperty template, final ConnectorSettings connectorSettings) {
        return ifHasSecurityDefinition(info, template, connectorSettings, OpenApiSecurityScheme.API_KEY);
    }

    private Optional<ConfigurationProperty> ifHasBasicSecurityDefinition(final OpenApiModelInfo info,
                                                                                final ConfigurationProperty template, final ConnectorSettings connectorSettings) {
        return ifHasSecurityDefinition(info, template, connectorSettings, OpenApiSecurityScheme.BASIC);
    }

    private Optional<ConfigurationProperty> ifHasOAuthSecurityDefinition(final OpenApiModelInfo info,
                                                                                final ConfigurationProperty template, final ConnectorSettings connectorSettings) {
        return ifHasSecurityDefinition(info, template, connectorSettings, OpenApiSecurityScheme.OAUTH2);
    }

    private Optional<ConfigurationProperty> ifHasSecurityDefinition(final OpenApiModelInfo info, final ConfigurationProperty template,
                                                                    final ConnectorSettings connectorSettings, final OpenApiSecurityScheme type) {
        final Optional<S> securityDefinition = securityDefinition(info, connectorSettings, type);

        if (securityDefinition.isPresent()) {
            return Optional.of(template);
        }

        return empty();
    }

    private Optional<ConfigurationProperty> oauthProperty(final OpenApiModelInfo info, final ConfigurationProperty template,
                                                                 final ConnectorSettings connectorSettings,
                                                                 final Function<S, String> defaultValueExtractor) {
        return securityDefinition(info, connectorSettings, OpenApiSecurityScheme.OAUTH2).map(definition -> new ConfigurationProperty.Builder()
            .createFrom(template).defaultValue(defaultValueExtractor.apply(definition)).build());
    }

    private Optional<ConfigurationProperty> oauthVendorProperty(final OpenApiModelInfo info, final ConfigurationProperty template,
                                                                       final ConnectorSettings connectorSettings,
                                                                       final String name) {
        return securityDefinition(info, connectorSettings, OpenApiSecurityScheme.OAUTH2).flatMap(definition -> vendorExtension(definition, template, name));
    }

    private static Optional<ConfigurationProperty> vendorExtension(final SecurityScheme definition,
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
