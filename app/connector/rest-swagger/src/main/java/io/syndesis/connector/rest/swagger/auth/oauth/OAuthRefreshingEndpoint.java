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
package io.syndesis.connector.rest.swagger.auth.oauth;

import java.util.List;

import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyEndpoint;
import io.syndesis.integration.component.proxy.ComponentProxyProducer;

import org.apache.camel.Consumer;
import org.apache.camel.Endpoint;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.http.common.HttpOperationFailedException;
import org.apache.camel.processor.CatchProcessor;
import org.apache.camel.processor.Pipeline;
import org.apache.camel.processor.TryProcessor;

import static java.util.Collections.singletonList;

public class OAuthRefreshingEndpoint extends ComponentProxyEndpoint {
    private static final List<Class<? extends Throwable>> EXCEPTIONS_HANDLED = singletonList(HttpOperationFailedException.class);

    private final Endpoint delegate;

    private final Pipeline pipeline;

    public OAuthRefreshingEndpoint(final ComponentProxyComponent component, final Endpoint endpoint,
        final OAuthRefreshTokenOnFailProcessor retryProcessor) {
        super(endpoint.getEndpointUri(), component, endpoint);
        delegate = endpoint;

        final Producer producer;
        try {
            producer = endpoint.createProducer();
        } catch (final Exception e) {
            throw new ExceptionInInitializerError(e);
        }

        final Processor catchBody = retryProcessor;

        final Processor catchProcessor = new CatchProcessor(EXCEPTIONS_HANDLED, catchBody, null, null);

        final Processor tryProcessor = new TryProcessor(producer,
            singletonList(catchProcessor), null);

        pipeline = new Pipeline(delegate.getCamelContext(), singletonList(tryProcessor));
    }

    @Override
    public Consumer createConsumer(final Processor processor) throws Exception {
        return null;
    }

    @Override
    public Producer createProducer() throws Exception {
        return new ComponentProxyProducer(delegate, pipeline);
    }

    @Override
    public boolean isSingleton() {
        return delegate.isSingleton();
    }

    @Override
    protected String createEndpointUri() {
        return delegate.getEndpointUri();
    }
}
