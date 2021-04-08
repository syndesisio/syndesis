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

import io.syndesis.common.model.WithName;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;

public final class Auditing {

    public static final String CHANGE = "change";

    public static final String CONFIGURED_PROPERTIES_PREFIX = "configuredProperties.";

    private final Supplier<Long> time;

    private final Supplier<String> username;

    public Auditing(final Supplier<Long> time, final Supplier<String> username) {
        this.time = time;
        this.username = username;
    }

    public Optional<AuditRecord> create(final Connection old, final Connection current) {
        final List<AuditEvent> events = computeEvents(old, current);

        if (events.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new AuditRecord("Connection", current.getName(), time.get(), username.get(), events));
    }

    private static void addBaseChangesTo(final List<AuditEvent> changes, final WithName old, final WithName current) {
        if (!Objects.equals(old.getName(), current.getName())) {
            changes.add(new AuditEvent(CHANGE, "name", old.getName(),
                current.getName()));
        }
    }

    private static void addConfiguredPropertiesChanges(final List<AuditEvent> changes, final Connection old, final Connection current) {
        final Map<String, String> oldConfiguredProperties = old.getConfiguredProperties();
        final Map<String, String> currentConfiguredProperties = current.getConfiguredProperties();

        final Set<String> propertyNames = new HashSet<>(oldConfiguredProperties.keySet());
        propertyNames.addAll(currentConfiguredProperties.keySet());

        for (final String propertyName : propertyNames) {
            final boolean isSecret = connectorFor(old).map(c -> c.isSecret(propertyName)).orElse(false);
            final String oldConfiguredValue = oldConfiguredProperties.get(propertyName);
            final String currentConfiguredValue = currentConfiguredProperties.get(propertyName);

            // Objects.equals when comparing two null values will return true
            if (!Objects.equals(oldConfiguredValue, currentConfiguredValue)) {
                changes.add(new AuditEvent(CHANGE, CONFIGURED_PROPERTIES_PREFIX + propertyName,
                    isSecret ? null : oldConfiguredValue,
                    isSecret ? null : currentConfiguredValue));
            }
        }
    }

    private static List<AuditEvent> computeEvents(final Connection old, final Connection current) {
        final List<AuditEvent> changes = new ArrayList<>();
        addBaseChangesTo(changes, old, current);
        addConfiguredPropertiesChanges(changes, old, current);
        return changes;
    }

    private static Optional<Connector> connectorFor(final Connection connection) {
        return connection.getConnector();
    }
}
