/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.connector;

import java.util.Properties;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.connector.DefaultConnectorComponent;

/**
 * Camel SqlStoredConnector connector
 */
public class SqlStoredConnectorComponent extends DefaultConnectorComponent {
    
    
    
    public SqlStoredConnectorComponent() {
        super("sqlStoredConnector", "io.syndesis.connector.SqlStoredConnectorComponent");
        registerExtension(SqlStoredConnectorVerifierExtension::new);
        registerExtension(SqlStoredConnectorMetaDataExtension::new);
    }

    @Override
    public Processor getBeforeProducer() {

        Processor processor = new Processor() {
            public void process(Exchange exchange)
                    throws Exception {
                System.out.println(exchange.getIn()
                        .getBody().getClass());
                String body = (String) exchange.getIn().getBody();
                Properties properties = JSONBeanUtil.parsePropertiesFromJSONBean(body);
                exchange.getIn().setBody(properties);
            }
        };
        return processor;
    }


}
