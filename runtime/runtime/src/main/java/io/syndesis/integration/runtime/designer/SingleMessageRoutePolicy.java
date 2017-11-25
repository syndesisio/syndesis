/*
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
package io.syndesis.integration.runtime.designer;

import org.apache.camel.Exchange;
import org.apache.camel.Route;
import org.apache.camel.RuntimeExchangeException;
import org.apache.camel.support.RoutePolicySupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class SingleMessageRoutePolicy extends RoutePolicySupport {
    private static final transient Logger LOG = LoggerFactory.getLogger(SingleMessageRoutePolicy.class);

    @Override
    public void onExchangeBegin(Route route, Exchange exchange) {
        super.onExchangeBegin(route, exchange);

        LOG.info("Exchange Begin for route " + route.getId() +
                " exchange: " + exchange.getExchangeId());
    }

    @Override
    public void onExchangeDone(Route route, Exchange exchange) {
        super.onExchangeDone(route, exchange);

        LOG.info("Exchange Done for route " + route.getId() +
                " exchange: " + exchange.getExchangeId() + " in: " + exchange.getIn().getBody(String.class));
        try {
            stopRoute(route);
        } catch (Exception e) {
            throw new RuntimeExchangeException(e.getMessage(), exchange, e);
        }
    }
}
