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
package io.syndesis.server.runtime;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.syndesis.common.util.EventBus;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Used to test the SimpleEventBus
 */
public class SimpleEventBusTest {

    private SimpleEventBus createEventBus() {
        return new SimpleEventBus();
    }

    @Test
    public void testSend() throws InterruptedException {
        CountDownLatch done = new CountDownLatch(1);
        String sub1[] = new String[2];
        EventBus eventBus = createEventBus();

        // Add a subscriber named "a"
        eventBus.subscribe("a", (event, data) -> {
            sub1[0] = event;
            sub1[1] = data;
            done.countDown();
        });

        // Now send an event to that subscriber.
        eventBus.send("a", "text", "data");

        // the send could be done async, so we wait for it to complete.
        done.await(5, TimeUnit.SECONDS);
        assertEquals("text", sub1[0]);
        assertEquals("data", sub1[1]);
    }

    @Test
    public void testBroadcast() throws InterruptedException {
        CountDownLatch done = new CountDownLatch(2);
        String sub1[] = new String[2];
        String sub2[] = new String[2];
        EventBus eventBus = createEventBus();

        // Add a subscriber named "a"
        eventBus.subscribe("a", (event, data) -> {
            sub1[0] = event;
            sub1[1] = data;
            done.countDown();
        });
        eventBus.subscribe("b", (event, data) -> {
            sub2[0] = event;
            sub2[1] = data;
            done.countDown();
        });

        // Now send an event to that subscriber.
        eventBus.broadcast("text", "data");

        // the send could be done async, so we wait for it to complete.
        done.await(5, TimeUnit.SECONDS);
        assertEquals("text", sub1[0]);
        assertEquals("data", sub1[1]);
        assertEquals("text", sub2[0]);
        assertEquals("data", sub2[1]);
    }

}
