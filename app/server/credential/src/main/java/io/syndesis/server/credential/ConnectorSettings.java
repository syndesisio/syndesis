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
package io.syndesis.server.credential;

import java.util.Optional;

import io.syndesis.common.model.connection.Connector;

import org.springframework.boot.autoconfigure.social.SocialProperties;

@SuppressWarnings("PMD.UseUtilityClass")
class ConnectorSettings extends SocialProperties {

    ConnectorSettings(final Connector connector) {
        setAppId(requiredProperty(connector, Credentials.CLIENT_ID_TAG));
        setAppSecret(requiredProperty(connector, Credentials.CLIENT_SECRET_TAG));
    }

    static Optional<String> optionalProperty(final Connector connector, final String tag) {
        return connector.propertyTaggedWith(tag);
    }

    static String requiredProperty(final Connector connector, final String tag) {
        return optionalProperty(connector, tag).orElseThrow(
            () -> new IllegalArgumentException("No property tagged with `" + tag + "` on connector: " + connector));
    }
}
