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
package io.syndesis.server.dao.audit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import io.syndesis.common.model.WithConfiguredProperties;
import io.syndesis.common.model.WithId;
import io.syndesis.common.model.WithName;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;

public final class Auditing {

    private final Supplier<Long> time;

    private final Supplier<String> username;

    public Auditing(final Supplier<Long> time, final Supplier<String> username) {
        this.time = time;
        this.username = username;
    }

    public Auditing(final Supplier<String> username) {
        this(System::currentTimeMillis, username);
    }

    public <T extends WithId<T>> Optional<AuditRecord> create(final T created) {
        final String id = created.getId().orElse("*");
        final String name = determineName(created);

        final List<AuditEvent> events = computeEvents(created);

        return Optional.of(new AuditRecord(id, "Connection", name, time.get(), username.get(), events));
    }

    public <T extends WithId<T>> Optional<AuditRecord> create(final T previous, final T current) {
        final List<AuditEvent> events = computeEvents(previous, current);

        if (events.isEmpty()) {
            return Optional.empty();
        }

        final String id = current.getId().orElse("*");
        final String name = determineName(current);

        return Optional.of(new AuditRecord(id, "Connection", name, time.get(), username.get(), events));
    }

    private static <T extends WithId<T>> void addBaseChangesTo(final List<AuditEvent> changes, final T created) {
        if (created instanceof WithName) {
            changes.add(AuditEvent.propertySet("name", ((WithName) created).getName()));
        }
    }

    private static <T extends WithId<T>> void addBaseChangesTo(final List<AuditEvent> changes, final T previous, final T current) {
        if (previous instanceof WithName) {
            final String previousName = ((WithName) previous).getName();
            final String currentName = ((WithName) current).getName();

            if (!Objects.equals(previousName, currentName)) {
                changes.add(AuditEvent.propertyChanged("name", previousName, currentName));
            }
        }
    }

    private static void addConfiguredPropertiesChanges(final List<AuditEvent> changes, final WithConfiguredProperties created) {
        final Map<String, String> configuredProperties = created.getConfiguredProperties();

        for (final Map.Entry<String, String> configuredProperty : configuredProperties.entrySet()) {
            final String propertyName = configuredProperty.getKey();
            final String value = configuredProperty.getValue();

            changes.add(AuditEvent.propertySet("configuredProperties." + propertyName, isSecret(created, propertyName) ? "**********" : value));
        }
    }

    private static void addConfiguredPropertiesChanges(final List<AuditEvent> changes, final WithConfiguredProperties previous,
        final WithConfiguredProperties current) {
        final Map<String, String> previousConfiguredProperties = previous.getConfiguredProperties();
        final Map<String, String> currentConfiguredProperties = current.getConfiguredProperties();

        final Set<String> propertyNames = new HashSet<>(previousConfiguredProperties.keySet());
        propertyNames.addAll(currentConfiguredProperties.keySet());

        for (final String propertyName : propertyNames) {
            final String previousConfiguredValue = previousConfiguredProperties.get(propertyName);
            final String currentConfiguredValue = currentConfiguredProperties.get(propertyName);

            // Objects.equals when comparing two null values will return true
            if (!Objects.equals(previousConfiguredValue, currentConfiguredValue)) {
                final boolean secret = isSecret(current, propertyName) || isSecret(previous, propertyName);

                changes.add(
                    AuditEvent.propertyChanged("configuredProperties." + propertyName, secret ? "**********" : previousConfiguredValue,
                        secret ? "**********" : currentConfiguredValue));
            }
        }
    }

    private static <T extends WithId<T>> List<AuditEvent> computeEvents(final T created) {
        final List<AuditEvent> changes = new ArrayList<>();
        addBaseChangesTo(changes, created);

        if (created instanceof WithConfiguredProperties) {
            addConfiguredPropertiesChanges(changes, (WithConfiguredProperties) created);
        }

        return changes;
    }

    private static <T extends WithId<T>> List<AuditEvent> computeEvents(final T previous, final T current) {
        final List<AuditEvent> changes = new ArrayList<>();
        addBaseChangesTo(changes, previous, current);

        if (current instanceof WithConfiguredProperties) {
            addConfiguredPropertiesChanges(changes, (WithConfiguredProperties) previous, (WithConfiguredProperties) current);
        }

        return changes;
    }

    private static Optional<Connector> connectorFor(final Connection connection) {
        return connection.getConnector();
    }

    private static <T extends WithId<T>> String determineName(final T created) {
        if (created instanceof WithName) {
            return ((WithName) created).getName();
        }

        return "*";
    }

    private static boolean isSecret(final WithConfiguredProperties withConfiguredProperties, final String propertyName) {
        if (withConfiguredProperties instanceof Connection) {
            return connectorFor((Connection) withConfiguredProperties).map(c -> c.isSecret(propertyName)).orElse(false);
        }

        return false;
    }
}
