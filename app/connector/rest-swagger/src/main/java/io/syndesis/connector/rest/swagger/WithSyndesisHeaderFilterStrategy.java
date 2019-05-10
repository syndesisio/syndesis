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
package io.syndesis.connector.rest.swagger;

import java.util.function.Consumer;

import io.syndesis.integration.runtime.util.SyndesisHeaderStrategy;

import org.apache.camel.component.http4.HttpEndpoint;
import org.apache.camel.component.http4.HttpProducer;

public final class WithSyndesisHeaderFilterStrategy implements Consumer<HttpProducer> {

    @Override
    public void accept(final HttpProducer producer) {
        // HttpComponent::createProducer will ignore any
        // HttpHeaderFilterStrategy set on the component/endpoint and set
        // it's own HttpRestHeaderFilterStrategy which cannot be influenced
        // in any way, here we're getting the reference to the endpoint
        // that's created in the createProducer and modifying the configured
        // DefaultHeaderFilterStrategy with the configuration from our
        // global HeaderFilterStrategy. This way we can filter out any HTTP
        // headers we do not wish to receive or send via HTTP (such as
        // Syndesis.*)
        final HttpEndpoint endpoint = producer.getEndpoint();

        endpoint.setHeaderFilterStrategy(SyndesisHeaderStrategy.INSTANCE);
    }
}
