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
import com.redhat.ipaas.rest.v1.controller.handler.events.EventReservationsHandler;
import io.undertow.Handlers;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.sse.ServerSentEventConnection;
import io.undertow.server.handlers.sse.ServerSentEventConnectionCallback;
import io.undertow.server.handlers.sse.ServerSentEventHandler;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.util.HeaderMap;
import io.undertow.util.HttpString;
import org.jboss.resteasy.spi.CorsHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.undertow.UndertowDeploymentInfoCustomizer;
import org.springframework.stereotype.Component;

/**
 * Connects the the EventBus to an Undertow Sever Side Event handler at the "/api/v1/events/{:subscription}" path.
 */
@Component
public class EventBusToServerSentEvents implements UndertowDeploymentInfoCustomizer {

    private final IPaaSCorsConfiguration cors;
    private final EventBus bus;
    private final EventReservationsHandler eventReservationsHandler;

    private String path = "/api/v1/event/streams";

    @Autowired
    public EventBusToServerSentEvents(IPaaSCorsConfiguration cors, EventBus bus, EventReservationsHandler eventReservationsHandler) {
        this.cors = cors;
        this.bus = bus;
        this.eventReservationsHandler = eventReservationsHandler;
    }

    public class EventBusHandler implements ServerSentEventConnectionCallback {

        @Override
        public void connected(ServerSentEventConnection connection, String lastEventId) {
            String uri = connection.getRequestURI();
            if( uri.indexOf(path+"/") != 0 ) {
                connection.send("Invalid path", "error", null, null);
                connection.shutdown();
                return;
            }

            final String subscriptionId = uri.substring(path.length()+1);
            if( subscriptionId.isEmpty() ) {
                connection.send("Invalid subscription: not set", "error", null, null);
                connection.shutdown();
                return;
            }

            EventReservationsHandler.Reservation reservation = eventReservationsHandler.claimReservation(subscriptionId);
            if( reservation==null ) {
                connection.send("Invalid subscription: not reserved", "error", null, null);
                connection.shutdown();
                return;
            }
            System.out.println("Principal is: "+reservation.getPrincipal());
            connection.send("connected", "message", null, null);
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
        final ServerSentEventHandler sseHandler = new ServerSentEventHandler(new EventBusHandler()){
            @Override
            public void handleRequest(HttpServerExchange exchange) throws Exception {
                HeaderMap requestHeaders = exchange.getRequestHeaders();
                String origin = requestHeaders.getFirst(CorsHeaders.ORIGIN);
                if (cors.getAllowedOrigins().contains("*") || cors.getAllowedOrigins().contains(origin)) {
                    HeaderMap responseHeaders = exchange.getResponseHeaders();
                    responseHeaders.put(new HttpString(CorsHeaders.ACCESS_CONTROL_ALLOW_ORIGIN), origin);

                    String value = requestHeaders.getFirst(CorsHeaders.ACCESS_CONTROL_REQUEST_HEADERS);
                    if (value != null)
                        responseHeaders.put(new HttpString(CorsHeaders.ACCESS_CONTROL_ALLOW_HEADERS), value);

                    value = requestHeaders.getFirst(CorsHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS);
                    if (value != null)
                        responseHeaders.put(new HttpString(CorsHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS), value);

                    value = requestHeaders.getFirst(CorsHeaders.ACCESS_CONTROL_REQUEST_METHOD);
                    if (value != null)
                        responseHeaders.put(new HttpString(CorsHeaders.ACCESS_CONTROL_ALLOW_METHODS), value);
                }
                super.handleRequest(exchange);
            }
        };
        deploymentInfo.addInitialHandlerChainWrapper(handler -> {
                return Handlers.path()
                    .addPrefixPath("/", handler)
                    .addPrefixPath(path, sseHandler);
            }
        );
    }
}
