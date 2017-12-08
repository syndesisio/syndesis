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
package io.syndesis.connector.daytrade;

import org.apache.camel.Exchange;
import org.apache.camel.component.connector.DefaultConnectorComponent;

import static org.apache.camel.Exchange.CONTENT_TYPE;

public class DayTradePlaceComponent extends DefaultConnectorComponent {

    public DayTradePlaceComponent() {
        this(null);
    }

    public DayTradePlaceComponent(String componentSchema) {
        super("day-trade-place", componentSchema, DayTradePlaceComponent.class.getName());

        // remove all the headers as we should not propagate any of them
        // and set the content type as json which is what this connector uses
        setBeforeProducer(exchange -> {
            exchange.getIn().removeHeaders("*");
            exchange.getIn().setHeader(CONTENT_TYPE, "application/json");
        });
    }

}
