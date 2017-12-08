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
package io.syndesis.example;

import java.util.Date;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class PublishSubscribeRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // subscribe route
        from("activemq-subscribe")
            .log("Received body ${body}");

        // publish route
        from("periodic-timer-connector")
            .log("Timer is triggered")
            .setBody(constant("Hello at " + new Date()))
            .log("Publishing message ${body}")
            .to("activemq-publish");
    }
}
