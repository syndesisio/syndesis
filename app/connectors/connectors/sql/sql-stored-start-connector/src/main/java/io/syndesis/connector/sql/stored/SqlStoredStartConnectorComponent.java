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

import io.syndesis.connector.sql.SqlConnectorVerifierExtension;
import org.apache.camel.NoTypeConversionAvailableException;
import org.apache.camel.Processor;
import org.apache.camel.TypeConverter;
import org.apache.camel.component.connector.DefaultConnectorComponent;
import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Camel SqlStoredStartConnector connector
 */
public class SqlStoredStartConnectorComponent extends DefaultConnectorComponent {
    private final static Logger LOGGER = LoggerFactory.getLogger(SqlStoredStartConnectorComponent.class);

    final static String COMPONENT_NAME  ="sql-stored-start-connector";
    final static String COMPONENT_SCHEME="sql-stored-start-connector";

    public SqlStoredStartConnectorComponent() {
        this(null);
    }

    public SqlStoredStartConnectorComponent(String componentSchema) {
        super(COMPONENT_NAME, componentSchema, SqlStoredStartConnectorComponent.class.getName());

        registerExtension(new SqlConnectorVerifierExtension(COMPONENT_SCHEME));
        registerExtension(SqlStoredConnectorMetaDataExtension::new);
    }

    @Override
    public Processor getAfterProducer() {
        final Processor processor = exchange -> {
            @SuppressWarnings("unchecked")
            String jsonBean = JSONBeanUtil.toJSONBean(exchange.getIn().getBody(Map.class));
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
