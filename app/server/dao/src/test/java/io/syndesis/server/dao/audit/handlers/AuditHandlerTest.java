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

import java.util.Optional;

import io.syndesis.server.dao.audit.AuditEvent;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class AuditHandlerTest {

    @Test
    public void shouldDetermineKnownHandlers() {
        assertThat(AuditHandler.handlers()).isNotEmpty();
    }

    @Test
    public void shouldDeterminePropertyDifference() {
        assertThat(AuditHandler.propertyDifference("propertyName", "current", "previous"))
            .contains(AuditEvent.propertyChanged("propertyName", "previous", "current"));
    }

    @Test
    public void shouldDeterminePropertyEquivalence() {
        assertThat(AuditHandler.propertyDifference("propertyName", "same", "same"))
            .isEmpty();
    }

    @Test
    public void shouldHandleAbsentPreviousValues() {
        @SuppressWarnings("unchecked")
        final AuditHandler<Object> handler = spy(AuditHandler.class);

        final Object current = new Object();
        handler.handle(current, Optional.empty());

        verify(handler).definition(current);
    }

    @Test
    public void shouldHandlePresnetPreviousValues() {
        @SuppressWarnings("unchecked")
        final AuditHandler<Object> handler = spy(AuditHandler.class);

        final Object current = new Object();
        final Object previous = new Object();
        handler.handle(current, Optional.of(previous));

        verify(handler).difference(current, previous);
    }
}
