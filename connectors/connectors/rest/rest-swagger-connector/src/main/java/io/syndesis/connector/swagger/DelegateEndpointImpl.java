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
package io.syndesis.connector.swagger;

import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Consumer;
import org.apache.camel.DelegateEndpoint;
import org.apache.camel.Endpoint;
import org.apache.camel.EndpointConfiguration;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.PollingConsumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.component.connector.DefaultConnectorEndpoint;

@SuppressWarnings("deprecation")
final class DelegateEndpointImpl implements DelegateEndpoint {

    private final DefaultConnectorEndpoint endpoint;

    private final Map<String, Object> headers;

    private static final class DelegateProducerImpl implements Producer {
        private final Producer delegate;

        private final Map<String, Object> headers;

        private DelegateProducerImpl(final Producer delegate, final Map<String, Object> headers) {
            this.delegate = delegate;
            this.headers = headers;
        }

        @Override
        public Exchange createExchange() {
            return delegate.createExchange();
        }

        @Override
        public Exchange createExchange(final Exchange exchange) {
            return delegate.createExchange(exchange);
        }

        @Override
        public Exchange createExchange(final ExchangePattern pattern) {
            return delegate.createExchange(pattern);
        }

        @Override
        public Endpoint getEndpoint() {
            return delegate.getEndpoint();
        }

        @Override
        public boolean isSingleton() {
            return delegate.isSingleton();
        }

        @Override
        public void process(final Exchange exchange) throws Exception {
            exchange.getIn().setHeaders(headers);
            delegate.process(exchange);
        }

        @Override
        public void start() throws Exception {
            delegate.start();
        }

        @Override
        public void stop() throws Exception {
            delegate.stop();
        }
    }

    DelegateEndpointImpl(final DefaultConnectorEndpoint endpoint, final Map<String, Object> headers) {
        this.endpoint = endpoint;
        this.headers = headers;
    }

    @Override
    public void configureProperties(final Map<String, Object> options) {
        endpoint.configureProperties(options);
    }

    @Override
    public Consumer createConsumer(final Processor processor) throws Exception {
        return endpoint.createConsumer(processor);
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
        final Producer delegate = endpoint.createProducer();

        return new DelegateProducerImpl(delegate, headers);
    }

    @Override
    public CamelContext getCamelContext() {
        return endpoint.getCamelContext();
    }

    @Override
    public Endpoint getEndpoint() {
        return endpoint.getEndpoint();
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
