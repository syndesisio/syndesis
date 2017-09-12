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
package io.syndesis.example;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class PullPushRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("periodic-timer")
            .log("Timer is triggered")
            .to("http-get-connector:hello")
            .log("GET: ${body}")
            .transform().constant("{ \"message\": \"Syndesis was here\" }")
            .to("http-post-connector:bye");
    }
}
