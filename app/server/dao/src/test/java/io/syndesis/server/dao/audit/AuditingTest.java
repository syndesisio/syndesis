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

import java.util.Collections;
import java.util.Optional;

import io.syndesis.common.model.Audited;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.WithId;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.server.dao.audit.AuditRecord.RecordType;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AuditingTest {

    static Long testTime = 123456789L;

    static String username = "username";

    Auditing auditing = new Auditing(() -> testTime, () -> username);

    private static class Base implements WithId<Base> {
        @Override
        public Optional<String> getId() {
            return Optional.of("id");
        }

        @Override
        public Kind getKind() {
            return Kind.Action; // needs to be typesafe
        }

        @Override
        public Base withId(final String id) {
            return null;
        }

    }

    @Audited
    private static class Marked extends Base {
        // example test case class
    }

    private static class Unmarked extends Base {
        // example test case class
    }

    @Test
    public void shouldAuditConnectionDeletion() {
        final Connection deleted = new Connection.Builder()
            .id("id")
            .name("Name")
            .build();

        final AuditRecord record = auditing.onDelete(deleted).get();

        assertThat(record.id()).isEqualTo("id");
        assertThat(record.type()).isEqualTo("connection");
        assertThat(record.name()).isEqualTo("Name");
        assertThat(record.timestamp()).isEqualTo(testTime);
        assertThat(record.user()).isEqualTo(username);
        assertThat(record.recordType()).isEqualTo(RecordType.deleted);
        assertThat(record.events()).isEmpty();
    }

    @Test
    public void shouldAuditConnectionNameChanges() {
        final Connection old = new Connection.Builder()
            .name("Old name")
            .build();

        final Connection changed = new Connection.Builder()
            .id("id")
            .name("New name")
            .build();

        final AuditRecord record = auditing.onUpdate(old, changed).get();

        assertThat(record.id()).isEqualTo("id");
        assertThat(record.type()).isEqualTo("connection");
        assertThat(record.name()).isEqualTo("New name");
        assertThat(record.timestamp()).isEqualTo(testTime);
        assertThat(record.user()).isEqualTo(username);
        assertThat(record.recordType()).isEqualTo(RecordType.updated);
        assertThat(record.events()).containsOnly(AuditEvent.propertyChanged("name", "Old name", "New name"));
    }

    @Test
    public void shouldAuditConnectionPropertyAdditionChanges() {
        final Connection old = new Connection.Builder()
            .build();

        final Connection changed = new Connection.Builder()
            .putConfiguredProperty("accessKey", "OEIUFLKJHFLKJH")
            .build();

        final AuditRecord record = auditing.onUpdate(old, changed).get();

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

        final AuditRecord record = auditing.onUpdate(old, changed).get();

        assertThat(record.type()).isEqualTo("connection");
        assertThat(record.timestamp()).isEqualTo(testTime);
        assertThat(record.user()).isEqualTo(username);
        assertThat(record.recordType()).isEqualTo(RecordType.updated);
        assertThat(record.events()).containsOnly(AuditEvent.propertyChanged("configuredProperties.accessKey", "AKJFNWEUGALASJ", "OEIUFLKJHFLKJH"));
    }

    @Test
    public void shouldAuditConnectionPropertyRemovalChanges() {
        final Connection old = new Connection.Builder()
            .putConfiguredProperty("accessKey", "OEIUFLKJHFLKJH")
            .build();

        final Connection changed = new Connection.Builder()
            .build();

        final AuditRecord record = auditing.onUpdate(old, changed).get();
        assertThat(record.recordType()).isEqualTo(RecordType.updated);
        assertThat(record.events()).containsOnly(AuditEvent.propertyChanged("configuredProperties.accessKey", "OEIUFLKJHFLKJH", null));
    }

    @Test
    public void shouldAuditOnlyMarkedTypes() {
        assertThat(auditing.onCreate(new Unmarked())).isEmpty();
        assertThat(auditing.onCreate(new Marked()))
            .contains(new AuditRecord("id", "action", "*", testTime, username, RecordType.created, Collections.emptyList()));
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

        final AuditRecord record = auditing.onUpdate(old, changed).get();
        assertThat(record.events()).containsOnly(AuditEvent.propertyChanged("configuredProperties." + secretKey, "**********", "**********"));
    }

    @Test
    public void shouldNotGenerateRecordsForSameConnections() {
        final Connection same = new Connection.Builder()
            .name("Same name")
            .build();

        assertThat(auditing.onUpdate(same, same)).isEmpty();
    }
}
