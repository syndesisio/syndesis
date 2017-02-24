/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.ipaas.runtime;

import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.EventSource;
import com.launchdarkly.eventsource.MessageEvent;
import com.redhat.ipaas.rest.v1.model.ChangeEvent;
import com.redhat.ipaas.rest.v1.model.EventMessage;
import com.redhat.ipaas.rest.v1.model.integration.Integration;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.redhat.ipaas.runtime.Recordings.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Used to test the SimpleEventBus
 */
public class EventsITCase extends BaseITCase {

    @Test
    public void connectorsListWithoutToken() throws Exception {
        ResponseEntity<EventMessage> r1 = post("/api/v1/event/reservations", null, EventMessage.class);
        assertThat(r1.getBody().getEvent().get()).as("event").isEqualTo("uuid");
        assertThat(r1.getBody().getData().get()).as("data").isNotNull();

        URI uri = restTemplate().getRestTemplate().getUriTemplateHandler().expand("/api/v1/event/streams/" + r1.getBody().getData().get());

        // lets setup an event handler that we can inpect events on..
        EventHandler handler = recorder(mock(EventHandler.class), EventHandler.class);
        List<Recordings.Invocation> invocations = recordedInvocations(handler);
        CountDownLatch countDownLatch = resetRecorderLatch(handler, 2);

        EventSource eventSource = new EventSource.Builder(handler, uri).build();
        eventSource.start();

        assertThat(countDownLatch.await(1000, TimeUnit.SECONDS)).isTrue();
        assertThat(invocations.get(0).getMethod().getName()).isEqualTo("onOpen");

        // We auto get a message letting us know we connected.
        assertThat(invocations.get(1).getMethod().getName()).isEqualTo("onMessage");
        assertThat(invocations.get(1).getArgs()[0]).isEqualTo("message");
        assertThat(((MessageEvent)invocations.get(1).getArgs()[1]).getData()).isEqualTo("connected");

        /////////////////////////////////////////////////////
        // Test that we get notified of created entities
        /////////////////////////////////////////////////////
        invocations.clear();
        countDownLatch = resetRecorderLatch(handler, 1);

        Integration integration = new Integration.Builder().id("1001").name("test").build();
        post("/api/v1/integrations", integration, Integration.class);

        assertThat(countDownLatch.await(1000, TimeUnit.SECONDS)).isTrue();
        assertThat(invocations.get(0).getArgs()[0]).isEqualTo("change-event");
        assertThat(((MessageEvent)invocations.get(0).getArgs()[1]).getData())
            .isEqualTo(ChangeEvent.of("created", "integration", "1001").toJson());

    }

}
