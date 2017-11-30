/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.connector.sql.stored;

import java.util.Map;
import java.util.Properties;

import org.apache.camel.Processor;
import org.apache.camel.component.connector.DefaultConnectorComponent;

import io.syndesis.connector.sql.SqlConnectorVerifierExtension;

/**
 * Camel SqlStoredConnector connector
 */
public class SqlStoredConnectorComponent extends DefaultConnectorComponent {

    final static String COMPONENT_NAME  ="sql-stored-connector";
    final static String COMPONENT_SCHEME="sql-stored-connector";

    public SqlStoredConnectorComponent() {
        this(null);
    }

    public SqlStoredConnectorComponent(String componentSchema) {
        super(COMPONENT_NAME, SqlStoredConnectorComponent.class.getName());
        registerExtension(new SqlConnectorVerifierExtension(COMPONENT_SCHEME));
        registerExtension(new SqlStoredConnectorMetaDataExtension());
    }

    @Override
    public Processor getBeforeProducer() {

        final Processor processor = exchange -> {
            final String body = (String) exchange.getIn().getBody();
            final Properties properties = JSONBeanUtil.parsePropertiesFromJSONBean(body);
            exchange.getIn().setBody(properties);
        };
        return processor;
    }

    @Override
    public Processor getAfterProducer() {
        final Processor processor = exchange -> {
            @SuppressWarnings("unchecked")
            Map<String,Object> map = (Map<String,Object>) exchange.getIn().getBody();
            String jsonBean = JSONBeanUtil.toJSONBean(map);
            exchange.getIn().setBody(jsonBean);
        };
        return processor;
    }
}
