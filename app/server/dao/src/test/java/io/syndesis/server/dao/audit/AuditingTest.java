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

import static org.assertj.core.api.Assertions.assertThat;

import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import org.junit.Test;

public class AuditingTest {

	Long testTime = 123456789l;
	String username = "username";
	Auditing auditing = new Auditing(() -> testTime, () -> username);

    @Test
    public void shouldAuditConnectionNameChanges() {
        Connection old = new Connection.Builder()
                             .name("Old name")
                             .build();

        Connection changed = new Connection.Builder()
                                 .name("New name")
                                 .build();

        AuditRecord record = auditing.create(old, changed).get();

        assertThat(record.type()).isEqualTo("Connection");
        assertThat(record.name()).isEqualTo("New name");
        assertThat(record.timestamp()).isEqualTo(testTime);
        assertThat(record.user()).isEqualTo(username);

        assertThat(record.events()).containsOnly(new AuditEvent("change", "name", "Old name", "New name"));
    }

    @Test
    public void shouldNotGenerateRecordsForSameConnections() {
        Connection same = new Connection.Builder()
                              .name("Same name")
                              .build();

        assertThat(auditing.create(same, same)).isEmpty();
    }


    @Test
    public void shouldAuditConnectionPropertyChanges() {
        Connection old = new Connection.Builder()
                             .putConfiguredProperty("accessKey", "AKJFNWEUGALASJ")
                             .build();

        Connection changed = new Connection.Builder()
                                 .putConfiguredProperty("accessKey", "OEIUFLKJHFLKJH")
                                 .build();

        AuditRecord record = auditing.create(old, changed).get();

        assertThat(record.type()).isEqualTo("Connection");
        assertThat(record.timestamp()).isEqualTo(testTime);
        assertThat(record.user()).isEqualTo(username);

        assertThat(record.events()).containsOnly(new AuditEvent("change", "configuredProperties.accessKey",
            "AKJFNWEUGALASJ", "OEIUFLKJHFLKJH"));
    }

    @Test
    public void shouldAuditSecretConnectionPropertyChanges() {
        final String secretKey = "secretKey";
        Connection old = new Connection.Builder()
                             .connector(new Connector.Builder()
                                            .putProperty(secretKey,
                                                new ConfigurationProperty.Builder()
                                                    .secret(true)
                                                    .build())
                                            .build()
                             )
                             .putConfiguredProperty(secretKey, "12345")
                             .build();

        Connection changed = new Connection.Builder()
                                 .putConfiguredProperty(secretKey, "98765")
                                 .build();

        AuditRecord record = auditing.create(old, changed).get();
        assertThat(record.events()).containsOnly(new AuditEvent("change", "configuredProperties." + secretKey, null,
            null));
    }

    @Test
    public void shouldAuditConnectionPropertyAdditionChanges() {
        Connection old = new Connection.Builder()
                             .build();

        Connection changed = new Connection.Builder()
                                 .putConfiguredProperty("accessKey", "OEIUFLKJHFLKJH")
                                 .build();

        AuditRecord record = auditing.create(old, changed).get();

        assertThat(record.events()).containsOnly(new AuditEvent("change", "configuredProperties.accessKey",
            null, "OEIUFLKJHFLKJH"));
    }

    @Test
    public void shouldAuditConnectionPropertyRemovalChanges() {
        Connection old = new Connection.Builder()
                                 .putConfiguredProperty("accessKey", "OEIUFLKJHFLKJH")
                                 .build();

        Connection changed = new Connection.Builder()
                                 .build();

        AuditRecord record = auditing.create(old, changed).get();

        assertThat(record.events()).containsOnly(new AuditEvent("change", "configuredProperties.accessKey",
            "OEIUFLKJHFLKJH", null));
    }
}
