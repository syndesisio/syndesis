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

import java.util.Map;

import io.syndesis.common.model.WithConfiguredProperties;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.util.CollectionsUtils;
import io.syndesis.server.dao.audit.AuditEvent;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class WithConfiguredPropertiesAuditHandlerTest {
    final WithConfiguredPropertiesAuditHandler handler = new WithConfiguredPropertiesAuditHandler();

    private static final class HasConfiguredProperties implements WithConfiguredProperties {

        private final Map<String, String> configured;

        public HasConfiguredProperties(final Map<String, String> configured) {
            this.configured = configured;
        }

        @Override
        public Map<String, String> getConfiguredProperties() {
            return configured;
        }
    }

    @Test
    public void shouldComputePropertyDefinition() {
        final HasConfiguredProperties created = new HasConfiguredProperties(CollectionsUtils.mapOf("p1", "v1", "p2", "v2"));

        assertThat(handler.definition(created)).containsOnly(
            AuditEvent.propertySet("configuredProperties.p1", "v1"),
            AuditEvent.propertySet("configuredProperties.p2", "v2"));
    }

    @Test
    public void shouldComputePropertyDifference() {
        final HasConfiguredProperties current = new HasConfiguredProperties(CollectionsUtils.mapOf("p1", "v1B", "p3", "v3"));
        final HasConfiguredProperties previous = new HasConfiguredProperties(CollectionsUtils.mapOf("p1", "v1", "p2", "v2"));

        assertThat(handler.difference(current, previous)).containsOnly(
            AuditEvent.propertyChanged("configuredProperties.p1", "v1", "v1B"),
            AuditEvent.propertyChanged("configuredProperties.p2", "v2", null),
            AuditEvent.propertyChanged("configuredProperties.p3", null, "v3"));
    }

    @Test
    public void shouldComputePropertyEquivalence() {
        final HasConfiguredProperties same1 = new HasConfiguredProperties(CollectionsUtils.mapOf("p1", "v1", "p2", "v2"));
        final HasConfiguredProperties same2 = new HasConfiguredProperties(CollectionsUtils.mapOf("p1", "v1", "p2", "v2"));

        assertThat(handler.difference(same1, same2)).isEmpty();
    }

    @Test
    public void shouldDetermineIfPropertyIsSecretForConnections() {
        final Connection connection = new Connection.Builder().connector(new Connector.Builder()
            .putProperty("superSecret", new ConfigurationProperty.Builder()
                .secret(true)
                .build())
            .build())
            .build();

        assertThat(WithConfiguredPropertiesAuditHandler.isSecret(connection, "notSecret")).isFalse();
        assertThat(WithConfiguredPropertiesAuditHandler.isSecret(connection, "superSecret")).isTrue();
        assertThat(WithConfiguredPropertiesAuditHandler.isSecret(new Connection.Builder().build(), "noConnector")).isFalse();
    }

    @Test
    public void shouldMaskKnownSecretPropertyValues() {
        final Connection connection = new Connection.Builder().connector(new Connector.Builder()
            .putProperty("superSecret", new ConfigurationProperty.Builder()
                .secret(true)
                .build())
            .build())
            .putConfiguredProperty("superSecret", "Mr. Mxyzptlk")
            .build();

        assertThat(handler.definition(connection)).contains(AuditEvent.propertySet("configuredProperties.superSecret", "**********"));

        assertThat(handler.difference(connection, connection.builder().putConfiguredProperty("superSecret", "dolphins").build()))
            .contains(AuditEvent.propertyChanged("configuredProperties.superSecret", "**********", "**********"));

    }
}
