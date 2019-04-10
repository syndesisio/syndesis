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
package io.syndesis.server.update.controller;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.syndesis.common.model.ChangeEvent;
import io.syndesis.common.util.EventBus;
import io.syndesis.common.util.Json;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ResourceUpdateControllerTest {

    ResourceUpdateController controller;

    final ChangeEvent event = ChangeEvent.of("action", "kind", "id");

    EventBus eventBus = mock(EventBus.class);

    ResourceUpdateHandler[] handlers = {mock(ResourceUpdateHandler.class), mock(ResourceUpdateHandler.class)};

    public ResourceUpdateControllerTest() {
        final ResourceUpdateConfiguration configuration = new ResourceUpdateConfiguration();

        controller = new ResourceUpdateController(configuration, eventBus, Arrays.asList(handlers));
    }

    @Test
    public void shouldProcessEvents() throws InterruptedException, ExecutionException, TimeoutException {
        reset(handlers); // ResourceUpdateController constructor invokes methods
                         // on handlers, this makes the mocks forget about that

        for (final ResourceUpdateHandler handler : handlers) {
            when(handler.canHandle(event)).thenReturn(true);
        }

        final CompletableFuture<Void> processed = controller.onEventInternal(EventBus.Type.CHANGE_EVENT, Json.toString(event));

        processed.get(1, TimeUnit.SECONDS);

        for (final ResourceUpdateHandler handler : handlers) {
            verify(handler).process(event);
        }
    }

    @Before
    public void startController() {
        controller.start();

        verify(eventBus).subscribe(eq(ResourceUpdateController.class.getName()), same(controller.handler));
        assertThat(controller.scheduler.isShutdown()).isFalse();
        assertThat(controller.running).isTrue();
    }

    @After
    public void stopController() {
        controller.stop();

        verify(eventBus).unsubscribe(eq(ResourceUpdateController.class.getName()));
        assertThat(controller.scheduler.isShutdown()).isTrue();
        assertThat(controller.running).isFalse();
    }
}
