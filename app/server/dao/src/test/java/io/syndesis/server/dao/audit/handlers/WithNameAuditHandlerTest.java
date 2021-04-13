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

import io.syndesis.common.model.WithName;
import io.syndesis.server.dao.audit.AuditEvent;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class WithNameAuditHandlerTest {
    final WithNameAuditHandler handler = new WithNameAuditHandler();

    private static final class Named implements WithName {

        private final String name;

        public Named(final String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    @Test
    public void shouldComputePropertyDefinition() {
        final WithName named = new Named("value");

        assertThat(handler.definition(named)).containsOnly(AuditEvent.propertySet("name", "value"));
    }

    @Test
    public void shouldComputePropertyDifference() {
        final WithName current = new Named("current");
        final WithName previous = new Named("previous");

        assertThat(handler.difference(current, previous)).containsOnly(AuditEvent.propertyChanged("name", "previous", "current"));
    }

    @Test
    public void shouldComputePropertyEquivalence() {
        final WithName same1 = new Named("same");
        final WithName same2 = new Named("same");

        assertThat(handler.difference(same1, same2)).isEmpty();
    }
}
