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

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.WithNull;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import nl.jqno.equalsverifier.EqualsVerifier;

public class AuditEventTest {
    @Property
    public void shouldAbbreviateValues(@ForAll @WithNull final String value) {
        assertThat(AuditEvent.abbreviate(value)).satisfiesAnyOf(
            s -> {
                assertThat(value).isNull();
                assertThat(s).isNull();
            },
            s -> {
                assertThat(s).hasSizeLessThanOrEqualTo(AuditEvent.MAX_LENGTH);
            });
    }

    @Test
    public void shouldAbbreviateWithThreeDots() {
        assertThat(AuditEvent.abbreviate("really long value here put for the purposes of the test")).hasSize(AuditEvent.MAX_LENGTH).endsWith("...");
    }

    @Test
    public void shouldAllowChangingPropertyNames() {
        assertPropertyChangeFor(AuditEvent.propertySet("x", "value"));
        assertPropertyChangeFor(AuditEvent.propertyChanged("x", "previous", "current"));
    }

    @Test
    public void shouldUpholdEqualsHashCodeContract() {
        EqualsVerifier.forClass(AuditEvent.class).withCachedHashCode("hashCode", "calculateHashCode", AuditEvent.propertySet("property", "value"))
            .verify();
    }

    private static void assertPropertyChangeFor(final AuditEvent event) {
        assertThat(event.withProperty("y"))
            .isEqualToIgnoringGivenFields(event, "property", "hashCode")
            .extracting(AuditEvent::property).isEqualTo("y");
    }
}
