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
package io.syndesis.connector.sql;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.camel.Processor;
import org.apache.camel.component.connector.DefaultConnectorComponent;

import io.syndesis.connector.sql.stored.JSONBeanUtil;

/**
 * Camel SqlConnector connector
 */
public class SqlConnectorComponent extends DefaultConnectorComponent {

    final static String COMPONENT_NAME  ="sql-connector";
    final static String COMPONENT_SCHEME="sql-connector";

    public SqlConnectorComponent() {
        super(COMPONENT_NAME, SqlConnectorComponent.class.getName());
        registerExtension(new SqlConnectorVerifierExtension(COMPONENT_SCHEME));
        registerExtension(SqlConnectorMetaDataExtension::new);
    }

    public SqlConnectorComponent(String componentScheme) {
        super(COMPONENT_NAME, SqlConnectorComponent.class.getName());
    }

    @Override
    public Processor getBeforeProducer() {

        final Processor processor = exchange -> {
            final String body = (String) exchange.getIn().getBody();
            if (body!=null) {
                final Properties properties = JSONBeanUtil.parsePropertiesFromJSONBean(body);
                exchange.getIn().setBody(properties);
            }
        };
        return processor;
    }

    @Override
    public Processor getAfterProducer() {
        @SuppressWarnings("unchecked")
        final Processor processor = exchange -> {
            String jsonBean = "";
            if (exchange.getIn().getBody(List.class) != null) {
                jsonBean = JSONBeanUtil.toJSONBean(exchange.getIn().getBody(List.class));
            } else {
                jsonBean = JSONBeanUtil.toJSONBean(exchange.getIn().getBody(Map.class));
            }
            exchange.getIn().setBody(jsonBean);
        };
        return processor;
    }

}
