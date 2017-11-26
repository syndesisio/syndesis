/**
 * Copyright (C) 2017 Red Hat, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.connector.tradeinsight;

import org.apache.camel.Exchange;
import org.apache.camel.component.connector.DefaultConnectorComponent;

import static org.apache.camel.Exchange.CONTENT_TYPE;

public class TradeInsightBuyComponent extends DefaultConnectorComponent {

    public TradeInsightBuyComponent() {
        this(null);
    }

    public TradeInsightBuyComponent(String componentSchema) {
        super("trade-insight-buy", componentSchema, TradeInsightBuyComponent.class.getName());

        // remove all the headers, as we should not propagate any of them
        // and set the content type as json which is what this connector uses
        setBeforeProducer(exchange -> {
            exchange.getIn().removeHeaders("*");
            exchange.getIn().setHeader(CONTENT_TYPE, "application/json");
        });
    }

}
