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

import io.syndesis.connector.odata.ODataResource;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class TimerODataRoute extends RouteBuilder {

    private static final String USERNAME_KEY = "'tgeisel'";

    @Override
    public void configure() throws Exception {
        
        String jsonBody = "{\"UserName\":\"tgeisel\",\"FirstName\":\"Theodor\",\"LastName\":\"Geisel\"}";

        from("timer://myTimer?period=10000")
            // create entity
            .setBody().constant(jsonBody)
            .to("odata-create-entity")
            .log("Create Entity result: ${body}")

            // retrieve entity, note the single quotes
            .setBody(constant(new ODataResource(USERNAME_KEY)))
            .to("odata-retrieve-entity")
            .log("Retrieve Entity result: ${body}")

/*
            // update entity
            .process(exchange -> {
                final Message in = exchange.getIn();
                final String body = in.getBody(String.class);
                in.setBody(body.replace("\"MiddleName\":null", "\"MiddleName\":\"Seuss\""));
            })
            .log("Update Entity request: ${body}")
            .to("odata-update-entity")
            .log("Update Entity result: ${body}")

*/
            // delete entity
            .setBody(constant(new ODataResource(USERNAME_KEY)))
            .to("odata-delete-entity")
            .log("Delete Entity result: ${body}");
    }
}
