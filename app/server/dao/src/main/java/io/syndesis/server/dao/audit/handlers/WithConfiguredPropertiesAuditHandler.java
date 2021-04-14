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
package io.syndesis.server.dao.audit.handlers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import io.syndesis.common.model.WithConfiguredProperties;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.server.dao.audit.AuditEvent;

final class WithConfiguredPropertiesAuditHandler extends AuditHandler<WithConfiguredProperties> {

    @Override
    public List<AuditEvent> definition(final WithConfiguredProperties current) {
        final List<AuditEvent> changes = new ArrayList<>();

        final Map<String, String> configuredProperties = current.getConfiguredProperties();

        for (final Map.Entry<String, String> configuredProperty : configuredProperties.entrySet()) {
            final String propertyName = configuredProperty.getKey();
            final String value = configuredProperty.getValue();

            changes.add(AuditEvent.propertySet("configuredProperties." + propertyName, isSecret(current, propertyName) ? "**********" : value));
        }

        return changes;
    }

    @Override
    public List<AuditEvent> difference(final WithConfiguredProperties current, final WithConfiguredProperties previous) {
        final List<AuditEvent> changes = new ArrayList<>();

        final Map<String, String> currentConfiguredProperties = current.getConfiguredProperties();
        final Map<String, String> previousConfiguredProperties = previous.getConfiguredProperties();

        final Set<String> propertyNames = new HashSet<>(previousConfiguredProperties.keySet());
        propertyNames.addAll(currentConfiguredProperties.keySet());

        for (final String propertyName : propertyNames) {
            final String currentConfiguredValue = currentConfiguredProperties.get(propertyName);
            final String previousConfiguredValue = previousConfiguredProperties.get(propertyName);

            // Objects.equals when comparing two null values will return true
            if (!Objects.equals(previousConfiguredValue, currentConfiguredValue)) {
                final boolean secret = isSecret(current, propertyName) || isSecret(previous, propertyName);

                changes.add(
                    AuditEvent.propertyChanged("configuredProperties." + propertyName, secret ? "**********" : previousConfiguredValue,
                        secret ? "**********" : currentConfiguredValue));
            }
        }

        return changes;
    }

    static boolean isSecret(final WithConfiguredProperties withConfiguredProperties, final String propertyName) {
        if (withConfiguredProperties instanceof Connection) {
            return connectorFor((Connection) withConfiguredProperties).map(c -> isSecret(propertyName, c)).orElse(false);
        }

        if (withConfiguredProperties instanceof Connector) {
            return isSecret(propertyName, (Connector) withConfiguredProperties);
        }

        return false;
    }

    private static Optional<Connector> connectorFor(final Connection connection) {
        return connection.getConnector();
    }

    private static boolean isSecret(final String propertyName, final Connector connector) {
        return connector.isSecret(propertyName);
    }
}
