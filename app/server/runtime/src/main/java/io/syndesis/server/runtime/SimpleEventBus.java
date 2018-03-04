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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.syndesis.common.util.EventBus;
import org.springframework.stereotype.Component;

/**
 * A simple event bus to abstract registering/sending Server Sent Events to browser clients
 * which have a subscribed to events.  This could potentially be implemented using a messaging broker.
 */
@Component
public class SimpleEventBus implements EventBus {

    private final ConcurrentHashMap<String, Subscription> subscriptions = new ConcurrentHashMap<>();

    @Override
    public Subscription subscribe(String subscriberId, Subscription handler) {
        return subscriptions.put(subscriberId, handler);
    }
    @Override
    public Subscription unsubscribe(String subscriberId) {
        return subscriptions.remove(subscriberId);
    }

    @Override
    public void broadcast(String event, String data) {
        for (Map.Entry<String, Subscription> entry : subscriptions.entrySet()) {
            entry.getValue().onEvent(event, data);
        }
    }

    @Override
    public void send(String subscriberId, String event, String data) {
        Subscription sub = subscriptions.get(subscriberId);
        if( sub!=null ) {
            sub.onEvent(event, data);
        }
    }


}
