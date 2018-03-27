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
package io.syndesis.common.util;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TransactedEventBus implements EventBus {

    private final EventBus delegate;

    private final Queue<Event> events = new ConcurrentLinkedQueue<>();

    static class Event {
        final String data;

        final String event;

        final String subscriberId;

        Event(final String event, final String data, final String subscriberId) {
            this.event = event;
            this.data = data;
            this.subscriberId = subscriberId;
        }

    }

    public TransactedEventBus(final EventBus delegate) {
        this.delegate = delegate;
    }

    @Override
    public void broadcast(final String event, final String data) {
        events.add(new Event(event, data, null));
    }

    public void commit() {
        if (delegate == null) {
            events.clear();
            return;
        }

        Event event = events.poll();
        while (event != null) {
            if (event.subscriberId == null) {
                delegate.broadcast(event.event, event.data);
            } else {
                delegate.send(event.subscriberId, event.event, event.data);
            }

            event = events.poll();
        }
    }

    @Override
    public void send(final String subscriberId, final String event, final String data) {
        events.add(new Event(event, data, subscriberId));

    }

    @Override
    public Subscription subscribe(final String subscriberId, final Subscription handler) {
        return delegate.subscribe(subscriberId, handler);
    }

    @Override
    public Subscription unsubscribe(final String subscriberId) {
        return delegate.unsubscribe(subscriberId);
    }
}
