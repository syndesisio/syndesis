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
import io.syndesis.common.model.integration.Integration;
import io.syndesis.server.dao.audit.AuditEvent;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IntegrationAuditHandlerTest {

    IntegrationAuditHandler handler = new IntegrationAuditHandler();

    @Test
    public void shouldComputeFlowDefinition() {
        final Integration integration = new Integration.Builder()
            .name("integration")
            .addFlow(new Flow.Builder()
                .name("flow1")
                .id("f1")
                .build())
            .addFlow(new Flow.Builder()
                .name("flow2")
                .id("f2")
                .build())
            .build();

        assertThat(handler.definition(integration)).containsOnly(
            AuditEvent.propertySet("name", "integration"),
            AuditEvent.propertySet("flows[f1].name", "flow1"),
            AuditEvent.propertySet("flows[f2].name", "flow2"));
    }

    @Test
    public void shouldComputePropertyDifference() {
        final Integration previous = new Integration.Builder()
            .name("previous")
            .addFlow(new Flow.Builder()
                .name("flow0")
                .id("f0")
                .build())
            .addFlow(new Flow.Builder()
                .name("flow1")
                .id("f1")
                .build())
            .addFlow(new Flow.Builder()
                .name("flow2")
                .id("f2")
                .build())
            .build();

        final Integration current = new Integration.Builder()
            .name("current")
            .addFlow(new Flow.Builder()
                .name("flow0")
                .id("f0")
                .build())
            .addFlow(new Flow.Builder()
                .name("flow1A")
                .id("f1")
                .build())
            .addFlow(new Flow.Builder()
                .name("flow3")
                .id("f3")
                .build())
            .build();

        assertThat(handler.difference(current, previous)).containsOnly(
            AuditEvent.propertyChanged("name", "previous", "current"),
            AuditEvent.propertyChanged("flows[f1].name", "flow1", "flow1A"),
            AuditEvent.propertyChanged("flows[f2].name", "flow2", null),
            AuditEvent.propertySet("flows[f3].name", "flow3"));
    }

    @Test
    public void shouldComputePropertyEquivalence() {
        final Integration same1 = new Integration.Builder()
            .name("integration")
            .addFlow(new Flow.Builder()
                .name("flow1")
                .id("f1")
                .build())
            .addFlow(new Flow.Builder()
                .name("flow2")
                .id("f2")
                .build())
            .build();
        final Integration same2 = new Integration.Builder()
            .name("integration")
            .addFlow(new Flow.Builder()
                .name("flow1")
                .id("f1")
                .build())
            .addFlow(new Flow.Builder()
                .name("flow2")
                .id("f2")
                .build())
            .build();

        assertThat(handler.difference(same1, same2)).isEmpty();
    }
}
