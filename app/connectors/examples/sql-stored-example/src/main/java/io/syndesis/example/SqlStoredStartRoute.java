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

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class SqlStoredStartRoute extends RouteBuilder {
    
    @Override
    public void configure() throws Exception {

        from("sql-stored-start-connector:DEMO_OUT( "
                + "OUT INTEGER C )?schedulerPeriod=5000")
        .process(new Processor() {
            
            public void process(Exchange exchange)
                    throws Exception {
                System.out.println(exchange.getIn()
                        .getBody().getClass());
                System.out.println(exchange.getIn()
                        .getBody());
            }
        });
    }
}
