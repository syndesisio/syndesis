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
package io.syndesis.integration.runtime;

import io.syndesis.core.KeyGenerator;
import io.syndesis.integration.runtime.util.DefaultRoutePolicy;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Route;
import org.apache.camel.processor.DefaultExchangeFormatter;
import org.apache.camel.spi.InterceptStrategy;
import org.apache.camel.spi.LogListener;
import org.apache.camel.spi.RoutePolicyFactory;
import org.apache.camel.spi.UuidGenerator;

import java.io.PrintWriter;
import java.io.StringWriter;

import static io.syndesis.integration.runtime.util.JsonSupport.toJsonObject;

/**
 * Created by chirino on 1/10/18.
 */
public class SyndesisLoggingSupport {

    private SyndesisLoggingSupport() {
    }

    private static final DefaultExchangeFormatter FORMATTER = new DefaultExchangeFormatter();
    static {
        FORMATTER.setShowOut(true);
    }

    public static void install(CamelContext context) {
        context.setUuidGenerator(createUuidGenerator());
        context.addLogListener(createLogListener());
        context.addRoutePolicyFactory(createRoutePolicyFactory());
        context.addInterceptStrategy(createInterceptStrategy());
    }


    public static LogListener createLogListener() {
        return (exchange, camelLogger, message) -> {
            System.out.println(toJsonObject(
                    "exchange", exchange.getExchangeId(),
                    "step", camelLogger.getMarker().getName(),
                    "id", KeyGenerator.createKey(),
                    "message", message));
            return message;
        };
    }

    /**
     * Lets generates always incrementing lexically sortable unique uuids. These uuids are
     * also more compact than the camel default and contain an embedded timestamp.
     *
     * @return
     */
    public static UuidGenerator createUuidGenerator() {
        return () -> KeyGenerator.createKey();
    }

    /**
     * This lets us use a RoutePolicy to trap the onExchangeDone() event so we can log
     * the results of processing the exchange.
     */
    public static RoutePolicyFactory createRoutePolicyFactory() {
        return (camelContext, routeId, route) -> new DefaultRoutePolicy() {
            @Override
            public void onExchangeBegin(Route route, Exchange exchange) {
                System.out.println(toJsonObject(
                        "exchange", exchange.getExchangeId(),
                        "status", "begin"));
            }
            @Override
            public void onExchangeDone(Route route, Exchange exchange) {
                System.out.println(toJsonObject(
                        "exchange", exchange.getExchangeId(),
                        "status", "done",
                        "failed", exchange.isFailed()));
            }
        };
    }

    private static InterceptStrategy createInterceptStrategy() {
        return (context, definition, target, nextTarget) -> {

            if (!definition.hasCustomIdAssigned()) {
                // skip over processors with a generated id
                return target;
            }
            return exchange -> {
                String id = KeyGenerator.createKey();
                long startedAt = System.nanoTime();
                try {
                    target.process(exchange);
                } catch (RuntimeException e) {
                    exchange.setException(e);
                } finally {
                    // currentTimeMillis is not monotonic, nanoTime likely is
                    long duration = System.nanoTime() - startedAt;
                    System.out.println(toJsonObject(
                            "exchange", exchange.getExchangeId(),
                            "step", definition.getId(),
                            "id", id,
                            "duration", duration,
                            "failure", failure(exchange)));
                }
            };
        };
    }

    private static String failure(Exchange exchange) {
        if( exchange.isFailed() ) {
            if( exchange.getException()!=null ) {
                return toString(exchange.getException());
            }
            return FORMATTER.format(exchange);
        }
        return null;
    }

    public static String toString(Throwable exception) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        pw.close();
        return sw.toString();
    }
}
