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
package io.syndesis.connector.sql;

import java.util.List;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Consumer;
import org.apache.camel.EndpointConfiguration;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Expression;
import org.apache.camel.PollingConsumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.builder.ExpressionBuilder;
import org.apache.camel.component.connector.ConnectorComponent;
import org.apache.camel.component.connector.DataType;
import org.apache.camel.component.connector.DefaultConnectorEndpoint;
import org.apache.camel.processor.Pipeline;
import org.apache.camel.processor.Splitter;

@SuppressWarnings("deprecation")
public class RecordSplitterEndpoint extends DefaultConnectorEndpoint{

    private final DefaultConnectorEndpoint endpoint; 

    public RecordSplitterEndpoint(String endpointUri, ConnectorComponent component, DefaultConnectorEndpoint endpoint, DataType inputDataType,
            DataType outputDataType) {
        super(endpointUri, component, endpoint, inputDataType, outputDataType);
        this.endpoint = endpoint;
    }

    @Override
    public void configureProperties(final Map<String, Object> options) {
        endpoint.configureProperties(options);
    }

    /**
     * Creates a consumer endpoint that splits up the List of Maps into exchanges of single
     * Maps, and within each exchange it converts each Map to JSON.
     */
    @Override
    public Consumer createConsumer(final Processor processor) throws Exception {
        final ToJSONProcessor toJsonProcessor = new ToJSONProcessor();
        Processor pipeline = Pipeline.newInstance(getCamelContext(), toJsonProcessor, processor);
        final Expression expression = ExpressionBuilder.bodyExpression(List.class);
        final Splitter splitter = new Splitter(getCamelContext(), expression, pipeline, null);
        return endpoint.createConsumer(splitter);
    }

    @Override
    public Exchange createExchange() {
        return endpoint.createExchange();
    }

    @Override
    public Exchange createExchange(final Exchange exchange) {
        return endpoint.createExchange(exchange);
    }

    @Override
    public Exchange createExchange(final ExchangePattern pattern) {
        return endpoint.createExchange(pattern);
    }

    @Override
    public PollingConsumer createPollingConsumer() throws Exception {
        return endpoint.createPollingConsumer();
    }

    @Override
    public Producer createProducer() throws Exception {
        return endpoint.createProducer();
    }

    @Override
    public CamelContext getCamelContext() {
        return endpoint.getCamelContext();
    }

    @Override
    public EndpointConfiguration getEndpointConfiguration() {
        return endpoint.getEndpointConfiguration();
    }

    @Override
    public String getEndpointKey() {
        return endpoint.getEndpointKey();
    }

    @Override
    public String getEndpointUri() {
        return endpoint.getEndpointUri();
    }

    @Override
    public boolean isLenientProperties() {
        return endpoint.isLenientProperties();
    }

    @Override
    public boolean isSingleton() {
        return endpoint.isSingleton();
    }

    @Override
    public void setCamelContext(final CamelContext context) {
        endpoint.setCamelContext(context);
    }

    @Override
    public void start() throws Exception {
        endpoint.start();
    }

    @Override
    public void stop() throws Exception {
        endpoint.stop();
    }

}
