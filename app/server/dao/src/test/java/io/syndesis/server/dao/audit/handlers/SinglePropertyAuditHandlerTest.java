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

import java.util.function.Function;

import io.syndesis.server.dao.audit.AuditEvent;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SinglePropertyAuditHandlerTest {

    final SinglePropertyAuditHandler<String> handler = new SinglePropertyAuditHandler<String>("name", Function.identity()) {
        // simple test handler
    };

    @Test
    public void shouldComputePropertyDefinition() {
        assertThat(handler.definition("value")).containsOnly(AuditEvent.propertySet("name", "value"));
    }

    @Test
    public void shouldComputePropertyDifference() {
        assertThat(handler.difference("current", "previous")).containsOnly(AuditEvent.propertyChanged("name", "previous", "current"));
    }

    @Test
    public void shouldComputePropertyEquivalence() {
        assertThat(handler.difference("same", "same")).isEmpty();
    }
}
