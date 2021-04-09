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

import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AuditingTest {

    static Long testTime = 123456789L;

    static String username = "username";

    Auditing auditing = new Auditing(() -> testTime, () -> username);

    @Test
    public void shouldAuditConnectionNameChanges() {
        final Connection old = new Connection.Builder()
            .name("Old name")
            .build();

        final Connection changed = new Connection.Builder()
            .id("id")
            .name("New name")
            .build();

        final AuditRecord record = auditing.create(old, changed).get();

        assertThat(record.id()).isEqualTo("id");
        assertThat(record.type()).isEqualTo("Connection");
        assertThat(record.name()).isEqualTo("New name");
        assertThat(record.timestamp()).isEqualTo(testTime);
        assertThat(record.user()).isEqualTo(username);

        assertThat(record.events()).containsOnly(AuditEvent.propertyChanged("name", "Old name", "New name"));
    }

    @Test
    public void shouldAuditConnectionPropertyAdditionChanges() {
        final Connection old = new Connection.Builder()
            .build();

        final Connection changed = new Connection.Builder()
            .putConfiguredProperty("accessKey", "OEIUFLKJHFLKJH")
            .build();

        final AuditRecord record = auditing.create(old, changed).get();

        assertThat(record.events()).containsOnly(AuditEvent.propertyChanged("configuredProperties.accessKey", null, "OEIUFLKJHFLKJH"));
    }

    @Test
    public void shouldAuditConnectionPropertyChanges() {
        final Connection old = new Connection.Builder()
            .putConfiguredProperty("accessKey", "AKJFNWEUGALASJ")
            .build();

        final Connection changed = new Connection.Builder()
            .putConfiguredProperty("accessKey", "OEIUFLKJHFLKJH")
            .build();

        final AuditRecord record = auditing.create(old, changed).get();

        assertThat(record.type()).isEqualTo("Connection");
        assertThat(record.timestamp()).isEqualTo(testTime);
        assertThat(record.user()).isEqualTo(username);

        assertThat(record.events()).containsOnly(AuditEvent.propertyChanged("configuredProperties.accessKey", "AKJFNWEUGALASJ", "OEIUFLKJHFLKJH"));
    }

    @Test
    public void shouldAuditConnectionPropertyRemovalChanges() {
        final Connection old = new Connection.Builder()
            .putConfiguredProperty("accessKey", "OEIUFLKJHFLKJH")
            .build();

        final Connection changed = new Connection.Builder()
            .build();

        final AuditRecord record = auditing.create(old, changed).get();

        assertThat(record.events()).containsOnly(AuditEvent.propertyChanged("configuredProperties.accessKey", "OEIUFLKJHFLKJH", null));
    }

    @Test
    public void shouldAuditSecretConnectionPropertyChanges() {
        final String secretKey = "secretKey";
        final Connection old = new Connection.Builder()
            .connector(new Connector.Builder()
                .putProperty(secretKey,
                    new ConfigurationProperty.Builder()
                        .secret(true)
                        .build())
                .build())
            .putConfiguredProperty(secretKey, "12345")
            .build();

        final Connection changed = new Connection.Builder()
            .putConfiguredProperty(secretKey, "98765")
            .build();

        final AuditRecord record = auditing.create(old, changed).get();
        assertThat(record.events()).containsOnly(AuditEvent.propertyChanged("configuredProperties." + secretKey, "**********", "**********"));
    }

    @Test
    public void shouldNotGenerateRecordsForSameConnections() {
        final Connection same = new Connection.Builder()
            .name("Same name")
            .build();

        assertThat(auditing.create(same, same)).isEmpty();
    }
}
