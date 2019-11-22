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
package io.syndesis.server.api.generator.openapi.v3;

import java.util.Optional;

import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.ConnectorSettings;
import io.syndesis.server.api.generator.openapi.OpenApiModelInfo;
import io.syndesis.server.api.generator.openapi.PropertyGenerator;

@SuppressWarnings("PMD.GodClass")
public enum Oas30PropertyGenerators {

    accessToken {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return (info, template, settings) -> Optional.empty();
        }
    },
    accessTokenExpiresAt {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return (info, template, settings) -> Optional.empty();
        }
    },
    authenticationParameterName {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return (info, template, settings) -> Optional.empty();
        }
    },
    authenticationParameterPlacement {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return (info, template, settings) -> Optional.empty();
        }
    },
    authenticationParameterValue {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return (info, template, settings) -> Optional.empty();
        }
    },
    authenticationType {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return (info, template, settings) -> Optional.empty();
        }
    },
    authorizationEndpoint {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return (info, template, settings) -> Optional.empty();
        }
    },
    authorizeUsingParameters {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return (info, template, settings) -> Optional.empty();
        }
    },
    basePath {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return (info, template, settings) -> Optional.empty();
        }
    },
    clientId {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return (info, template, settings) -> Optional.empty();
        }
    },
    clientSecret {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return (info, template, settings) -> Optional.empty();
        }
    },
    host {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return (info, template, settings) -> Optional.empty();
        }
    },
    oauthScopes {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return (info, template, settings) -> Optional.empty();
        }
    },
    password {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return (info, template, settings) -> Optional.empty();
        }
    },
    refreshToken {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return (info, template, settings) -> Optional.empty();
        }
    },
    refreshTokenRetryStatuses {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return (info, template, settings) -> Optional.empty();
        }
    },
    specification {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return (info, template, settings) -> Optional.empty();
        }
    },
    tokenEndpoint {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return (info, template, settings) -> Optional.empty();
        }
    },
    tokenStrategy {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return (info, template, settings) -> Optional.empty();
        }
    },
    username {
        @Override
        protected PropertyGenerator propertyGenerator() {
            return (info, template, settings) -> Optional.empty();
        }
    };

    protected abstract PropertyGenerator propertyGenerator();

    public static Optional<ConfigurationProperty> createProperty(final String propertyName, final OpenApiModelInfo info,
                                                                 final ConfigurationProperty template, final ConnectorSettings connectorSettings) {
        final Oas30PropertyGenerators propertyGenerator = Oas30PropertyGenerators.valueOf(propertyName);

        return propertyGenerator.propertyGenerator().generate(info, template, connectorSettings);
    }
}
