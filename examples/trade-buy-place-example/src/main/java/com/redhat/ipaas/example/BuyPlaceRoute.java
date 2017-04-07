/**
 * Copyright (C) 2017 Red Hat, Inc.
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
package com.redhat.ipaas.example;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class BuyPlaceRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("periodic-timer?synchronous=true")
            .log("Triggering")
            .to("trade-insight-buy")
            .log("Transforming")
            .transform().method(TradeDataMapper.class)
            .log("Trading")
            .removeHeaders("*")
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .to("day-trade-place")
            .log("Done");
    }
}
