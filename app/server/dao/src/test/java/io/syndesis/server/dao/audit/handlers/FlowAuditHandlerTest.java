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

import io.syndesis.common.model.integration.Flow;
import io.syndesis.server.dao.audit.AuditEvent;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FlowAuditHandlerTest {

    FlowAuditHandler handler = new FlowAuditHandler();

    @Test
    public void shouldComputePropertyDefinition() {
        final Flow flow = new Flow.Builder()
            .id("id")
            .name("flow")
            .build();

        assertThat(handler.definition(flow)).containsOnly(AuditEvent.propertySet("flows[id].name", "flow"));
    }

    @Test
    public void shouldComputePropertyDifference() {
        final Flow current = new Flow.Builder()
            .id("id")
            .name("current")
            .build();
        final Flow previous = new Flow.Builder()
            .id("id")
            .name("previous")
            .build();

        assertThat(handler.difference(current, previous)).containsOnly(AuditEvent.propertyChanged("flows[id].name", "previous", "current"));
    }

    @Test
    public void shouldComputePropertyEquivalence() {
        final Flow same1 = new Flow.Builder()
            .name("same")
            .build();
        final Flow same2 = new Flow.Builder()
            .name("same")
            .build();

        assertThat(handler.difference(same1, same2)).isEmpty();
    }
}
