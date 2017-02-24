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

import com.redhat.ipaas.rest.EventBus;
import com.redhat.ipaas.rest.v1.model.EventMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;

/**
 * Connects a WebSocket endpoint to the EventBus
 */
@Configuration
@EnableWebSocket
@EnableScheduling
public class EventBusToWebSocket implements WebSocketConfigurer {

    private final IPaaSCorsConfiguration cors;
    private final EventBus bus;

    @Autowired
    public EventBusToWebSocket(IPaaSCorsConfiguration cors, EventBus bus) {
        this.cors = cors;
        this.bus = bus;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        List<String> allowedOrigins = cors.getAllowedOrigins();
        String[] origins = allowedOrigins.toArray(new String[allowedOrigins.size()]);

        registry.addHandler(new TextWebSocketHandler() {

            WebSocketSession session;

            @Override
            public void afterConnectionEstablished(WebSocketSession session) {
                this.session = session;

                String subscriptionId = session.getId();
                if( subscriptionId.isEmpty() ) {
                    send(session, "error", "Invalid subscription id");
                    try {
                        session.close();
                    } catch (IOException e) {
                    }
                    return;
                }

                send(session, "message", "connected");
                bus.subscribe(subscriptionId, (type, data)->{
                    send(session, type, data);
                });

            }

            @Override
            public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
                bus.unsubscribe(session.getId());
                super.afterConnectionClosed(session, status);
            }

            private void send(WebSocketSession session, String type, String data) {
                try {
                    session.sendMessage(new TextMessage(EventMessage.of(type, data).toJson()));
                } catch (IOException e) {
                }
            }

        }, "/wsevents").setAllowedOrigins(origins);
    }

}
