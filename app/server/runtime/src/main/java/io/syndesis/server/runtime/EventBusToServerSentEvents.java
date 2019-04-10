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

import io.syndesis.common.util.EventBus;
import io.syndesis.server.endpoint.v1.handler.events.EventReservationsHandler;
import io.undertow.Handlers;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.sse.ServerSentEventConnection;
import io.undertow.server.handlers.sse.ServerSentEventConnectionCallback;
import io.undertow.server.handlers.sse.ServerSentEventHandler;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.util.HeaderMap;
import io.undertow.util.HttpString;
import org.jboss.resteasy.spi.CorsHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.undertow.UndertowDeploymentInfoCustomizer;
import org.springframework.stereotype.Component;

/**
 * Connects the the EventBus to an Undertow Sever Side Event handler
 * at the "/api/v1/events/{:subscription}" path.
 */
@Component
public class EventBusToServerSentEvents implements UndertowDeploymentInfoCustomizer {

    private static final Logger LOG = LoggerFactory.getLogger(EventBusToServerSentEvents.class);

    public static final String DEFAULT_PATH = "/api/v1/event/streams";
    protected final SyndesisCorsConfiguration cors;
    protected final EventBus bus;
    protected final EventReservationsHandler eventReservationsHandler;
    protected String path = DEFAULT_PATH;

    @Autowired
    public EventBusToServerSentEvents(SyndesisCorsConfiguration cors, EventBus bus, EventReservationsHandler eventReservationsHandler) {
        this.cors = cors;
        this.bus = bus;
        this.eventReservationsHandler = eventReservationsHandler;
    }

    public class EventBusHandler implements ServerSentEventConnectionCallback {

        @Override
        public void connected(ServerSentEventConnection connection, String lastEventId) {
            String uri = connection.getRequestURI();
            final String subscriptionId = uri.substring(path.length()+1);
            EventReservationsHandler.Reservation reservation = eventReservationsHandler.claimReservation(subscriptionId);
            if( reservation==null ) {
                connection.send("Invalid subscription: not reserved", "error", null, null);
                connection.shutdown();
                return;
            }
            LOG.debug("Principal is: {}", reservation.getPrincipal());
            connection.send("connected", "message", null, null);
            connection.setKeepAliveTime(25*1000);
            bus.subscribe(subscriptionId, (type, data)->{
                if( connection.isOpen() ) {
                    connection.send(data, type, null, null);
                } else {
                    bus.unsubscribe(subscriptionId);
                }
            });
        }

    }

    @Override
    public void customize(DeploymentInfo deploymentInfo) {
        deploymentInfo.addInitialHandlerChainWrapper(handler -> {
                return Handlers.path()
                    .addPrefixPath("/", handler)
                    .addPrefixPath(path, new ServerSentEventHandler(new EventBusHandler()){
                        @Override
                        @SuppressWarnings("PMD.SignatureDeclareThrowsException")
                        public void handleRequest(HttpServerExchange exchange) throws Exception {
                            if( reservationCheck(exchange) ) {
                                super.handleRequest(exchange);
                            }
                        }
                    });
            }
        );
    }

    protected boolean reservationCheck(HttpServerExchange exchange) {
        HeaderMap requestHeaders = exchange.getRequestHeaders();
        String origin = requestHeaders.getFirst(CorsHeaders.ORIGIN);
        if (cors.getAllowedOrigins().contains("*") || cors.getAllowedOrigins().contains(origin)) {
            HeaderMap responseHeaders = exchange.getResponseHeaders();
            responseHeaders.put(new HttpString(CorsHeaders.ACCESS_CONTROL_ALLOW_ORIGIN), origin);

            String value = requestHeaders.getFirst(CorsHeaders.ACCESS_CONTROL_REQUEST_HEADERS);
            if (value != null) {
                responseHeaders.put(new HttpString(CorsHeaders.ACCESS_CONTROL_ALLOW_HEADERS), value);
            }

            value = requestHeaders.getFirst(CorsHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS);
            if (value != null) {
                responseHeaders.put(new HttpString(CorsHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS), value);
            }

            value = requestHeaders.getFirst(CorsHeaders.ACCESS_CONTROL_REQUEST_METHOD);
            if (value != null) {
                responseHeaders.put(new HttpString(CorsHeaders.ACCESS_CONTROL_ALLOW_METHODS), value);
            }
        }

        String uri = exchange.getRequestURI();
        if (uri.indexOf(path + "/") != 0) {
            exchange.setStatusCode(404);
            return false;
        }

        final String subscriptionId = uri.substring(path.length() + 1);
        if (subscriptionId.isEmpty()) {
            exchange.setStatusCode(404);
            return false;
        }

        EventReservationsHandler.Reservation reservation = eventReservationsHandler.existsReservation(subscriptionId);
        if (reservation == null) {
            exchange.setStatusCode(404);
            return false;
        }
        return true;
    }
}
