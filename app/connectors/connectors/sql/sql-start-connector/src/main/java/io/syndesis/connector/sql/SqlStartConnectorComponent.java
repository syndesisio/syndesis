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

import io.syndesis.connector.sql.stored.JSONBeanUtil;
import org.apache.camel.NoTypeConversionAvailableException;
import org.apache.camel.Processor;
import org.apache.camel.TypeConverter;
import org.apache.camel.component.connector.DefaultConnectorComponent;
import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * Camel SqlConnector connector
 */
public class SqlStartConnectorComponent extends DefaultConnectorComponent {
    private final static Logger LOGGER = LoggerFactory.getLogger(SqlStartConnectorComponent.class);

    final static String COMPONENT_NAME  ="sql-start-connector";
    final static String COMPONENT_SCHEME="sql-start-connector";

    public SqlStartConnectorComponent() {
        super(COMPONENT_NAME, SqlStartConnectorComponent.class.getName());
        registerExtension(new SqlConnectorVerifierExtension(COMPONENT_SCHEME));
        registerExtension(SqlConnectorMetaDataExtension::new);
    }

    public SqlStartConnectorComponent(String componentScheme) {
        super(COMPONENT_NAME, SqlStartConnectorComponent.class.getName());
    }


    @Override
    public Processor getBeforeProducer() {

        final Processor processor = exchange -> {
            final String body = exchange.getIn().getBody(String.class);
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

    @Override
    protected void doStart() throws Exception {
        final Map<String, Object> options = getOptions();

        if (!options.containsKey("dataSource")) {
            if (options.containsKey("user") && options.containsKey("password") && options.containsKey("url")) {
                BasicDataSource ds = new BasicDataSource();

                consumeOption("user", String.class, ds::setUsername);
                consumeOption("password", String.class, ds::setPassword);
                consumeOption("url", String.class, ds::setUrl);

                addOption("dataSource", ds);
            } else {
                LOGGER.debug("Not enough information provided to set-up the DataSource");
            }
        }

        super.doStart();
    }

    private <T> void consumeOption(String name, Class<T> type, Consumer<T> consumer) throws NoTypeConversionAvailableException {
        final TypeConverter converter = getCamelContext().getTypeConverter();
        final Object val = getOptions().get(name);
        final T result = converter.mandatoryConvertTo(type, val);

        consumer.accept(result);

        LOGGER.debug("Consume option {}", name);
        getOptions().remove(name);
    }

}
