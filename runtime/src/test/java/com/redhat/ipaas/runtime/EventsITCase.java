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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.List;
import java.util.UUID;
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
    public void sseEventsWithToken() throws Exception {
        ResponseEntity<EventMessage> r1 = post("/api/v1/event/reservations", null, EventMessage.class);
        assertThat(r1.getBody().getEvent().get()).as("event").isEqualTo("uuid");
        String uuid = (String) r1.getBody().getData().get();
        assertThat(uuid).as("data").isNotNull();

        String uriTemplate = "/api/v1/event/streams/" + uuid;
        URI uri = resolveURI(uriTemplate);

        // lets setup an event handler that we can inspect events on..
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
        assertThat(((MessageEvent) invocations.get(1).getArgs()[1]).getData()).isEqualTo("connected");

        /////////////////////////////////////////////////////
        // Test that we get notified of created entities
        /////////////////////////////////////////////////////
        invocations.clear();
        countDownLatch = resetRecorderLatch(handler, 1);

        Integration integration = new Integration.Builder().id("1001").name("test").build();
        post("/api/v1/integrations", integration, Integration.class);

        assertThat(countDownLatch.await(1000, TimeUnit.SECONDS)).isTrue();
        assertThat(invocations.get(0).getArgs()[0]).isEqualTo("change-event");
        assertThat(((MessageEvent) invocations.get(0).getArgs()[1]).getData())
            .isEqualTo(ChangeEvent.of("created", "integration", "1001").toJson());

        eventSource.close();
    }

    private URI resolveURI(String uriTemplate) {
        return restTemplate().getRestTemplate().getUriTemplateHandler().expand(uriTemplate);
    }

    @Test
    public void sseEventsWithoutToken() throws Exception {

        // We should not be able to get a reservation uuid to connect to the event stream
        ResponseEntity<EventMessage> response = restTemplate().postForEntity("/api/v1/event/reservations", null, EventMessage.class);
        assertThat(response.getStatusCode()).as("reservations post status code").isEqualTo(HttpStatus.UNAUTHORIZED);

        // lets setup an event handler that we can inspect events on..
        EventHandler handler = recorder(mock(EventHandler.class), EventHandler.class);
        List<Recordings.Invocation> invocations = recordedInvocations(handler);
        CountDownLatch countDownLatch = resetRecorderLatch(handler, 1);

        // Using a random uuid should not work either.
        String uuid = UUID.randomUUID().toString();
        EventSource eventSource = new EventSource.Builder(handler, resolveURI("/api/v1/event/streams/" + uuid)).build();
        eventSource.start();

        assertThat(countDownLatch.await(1000, TimeUnit.SECONDS)).isTrue();
        assertThat(invocations.get(0).getMethod().getName()).isEqualTo("onError");
        assertThat(invocations.get(0).getArgs()[0].toString())
            .isEqualTo("com.launchdarkly.eventsource.UnsuccessfulResponseException: Unsuccessful response code received from stream: 404");

        eventSource.close();

    }

    @Test
    public void wsEventsWithToken() throws Exception {
        OkHttpClient client = new OkHttpClient();

        String url = resolveURI("/wsevents").toString().replaceFirst("^http", "ws");
        Request request = new Request.Builder()
            .url(url)
            .addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + tokenRule.validToken())
            .build();

        // lets setup an event handler that we can inspect events on..
        WebSocketListener listener = recorder(mock(WebSocketListener.class), WebSocketListener.class);
        List<Recordings.Invocation> invocations = recordedInvocations(listener);
        CountDownLatch countDownLatch = resetRecorderLatch(listener, 2);

        WebSocket ws = client.newWebSocket(request, listener);

        // We auto get a message letting us know we connected.
        assertThat(countDownLatch.await(1000, TimeUnit.SECONDS)).isTrue();
        assertThat(invocations.get(0).getMethod().getName()).isEqualTo("onOpen");
        assertThat(invocations.get(1).getMethod().getName()).isEqualTo("onMessage");
        assertThat(invocations.get(1).getArgs()[1]).isEqualTo(EventMessage.of("message", "connected").toJson());

        /////////////////////////////////////////////////////
        // Test that we get notified of created entities
        /////////////////////////////////////////////////////
        invocations.clear();
        countDownLatch = resetRecorderLatch(listener, 1);

        Integration integration = new Integration.Builder().id("1002").name("test").build();
        post("/api/v1/integrations", integration, Integration.class);

        assertThat(countDownLatch.await(1000, TimeUnit.SECONDS)).isTrue();
        assertThat(invocations.get(0).getMethod().getName()).isEqualTo("onMessage");
        assertThat(invocations.get(0).getArgs()[1]).isEqualTo(EventMessage.of("change-event", ChangeEvent.of("created", "integration", "1002").toJson()).toJson());

        ws.close(1000, "closing");
    }

    @Ignore("Not yet working.. don't think security is being applied to the /wsevents endpoints.")
    public void wsEventsWithoutToken() throws Exception {

        OkHttpClient client = new OkHttpClient();

        String url = resolveURI("/wsevents").toString().replaceFirst("^http", "ws");
        Request request = new Request.Builder().url(url).build();

        // lets setup an event handler that we can inspect events on..
        WebSocketListener listener = recorder(mock(WebSocketListener.class), WebSocketListener.class);
        List<Recordings.Invocation> invocations = recordedInvocations(listener);
        CountDownLatch countDownLatch = resetRecorderLatch(listener, 2);

        WebSocket ws = client.newWebSocket(request, listener);

        // We auto get a message letting us know we connected.
        assertThat(countDownLatch.await(1000, TimeUnit.SECONDS)).isTrue();
        assertThat(invocations.get(0).getMethod().getName()).isEqualTo("onFailure");
        assertThat(invocations.get(0).getArgs()[0].toString())
            .isEqualTo("com.launchdarkly.eventsource.UnsuccessfulResponseException: Unsuccessful response code received from stream: 404");

    }
}
