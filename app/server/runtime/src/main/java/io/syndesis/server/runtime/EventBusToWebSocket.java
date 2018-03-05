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

import java.io.IOException;

import io.syndesis.common.util.EventBus;
import io.syndesis.common.model.EventMessage;
import io.syndesis.server.endpoint.v1.handler.events.EventReservationsHandler;
import io.undertow.Handlers;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.sse.ServerSentEventConnection;
import io.undertow.server.handlers.sse.ServerSentEventConnectionCallback;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.WebSocketProtocolHandshakeHandler;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import io.undertow.websockets.spi.WebSocketHttpExchange;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Connects the the EventBus to an Undertow WebSocket Event handler
 * at the "/api/v1/events.ws/{:subscription}" path.
 */
@Component
public class EventBusToWebSocket extends EventBusToServerSentEvents {

    private static final Logger LOG = LoggerFactory.getLogger(EventBusToWebSocket.class);

    public static final String DEFAULT_PATH = "/api/v1/event/streams.ws";


    @Autowired
    public EventBusToWebSocket(SyndesisCorsConfiguration cors, EventBus bus, EventReservationsHandler eventReservationsHandler) {
        super(cors, bus, eventReservationsHandler);
        path = DEFAULT_PATH;
    }

    @Override
    public void customize(DeploymentInfo deploymentInfo) {
        deploymentInfo.addInitialHandlerChainWrapper(handler -> {
                return Handlers.path()
                    .addPrefixPath("/", handler)
                    .addPrefixPath(path, new WebSocketProtocolHandshakeHandler(new WSHandler()) {
                        @Override
                        @SuppressWarnings("PMD.SignatureDeclareThrowsException")
                        public void handleRequest(HttpServerExchange exchange) throws Exception {
                            if (reservationCheck(exchange)) {
                                super.handleRequest(exchange);
                            }
                        }
                    });
            }
        );
    }

    public class SSEHandler implements ServerSentEventConnectionCallback {

        @Override
        public void connected(ServerSentEventConnection connection, String lastEventId) {
            String uri = connection.getRequestURI();
            final String subscriptionId = uri.substring(path.length() + 1);
            EventReservationsHandler.Reservation reservation = eventReservationsHandler.claimReservation(subscriptionId);
            if (reservation == null) {
                connection.send("Invalid subscription: not reserved", "error", null, null);
                connection.shutdown();
                return;
            }
            LOG.debug("Principal is: {}", reservation.getPrincipal());
            connection.send("connected", "message", null, null);
            connection.setKeepAliveTime(25*1000);
            bus.subscribe(subscriptionId, (type, data) -> {
                if (connection.isOpen()) {
                    connection.send(data, type, null, null);
                } else {
                    bus.unsubscribe(subscriptionId);
                }
            });
        }

    }

    public class WSHandler implements WebSocketConnectionCallback {

        @Override
        public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel) {

            String uri = exchange.getRequestURI();
            final String subscriptionId = uri.substring(path.length() + 1);
            EventReservationsHandler.Reservation reservation = eventReservationsHandler.claimReservation(subscriptionId);
            if (reservation == null) {
                send(channel, "error", "Invalid subscription: not reserved");
                safeClose(channel);
                return;
            }
            LOG.debug("Principal is: {}", reservation.getPrincipal());
            send(channel, "message", "connected");
            bus.subscribe(subscriptionId, (type, data) -> {
                if (channel.isOpen()) {
                    send(channel, type, data);
                } else {
                    bus.unsubscribe(subscriptionId);
                }
            });
        }

        private void safeClose(WebSocketChannel channel) {
            try {
                channel.close();
            } catch (IOException e) {
                LOG.debug("IO Error at channel close, ignoring", e);
            }
        }

        private void send(WebSocketChannel channel, String type, String data) {
            WebSockets.sendText(EventMessage.of(type, data).toJson(), channel, null);
        }
    }

}
