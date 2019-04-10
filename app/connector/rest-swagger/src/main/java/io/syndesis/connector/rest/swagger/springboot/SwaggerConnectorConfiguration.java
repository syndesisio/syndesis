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
package io.syndesis.connector.rest.swagger.springboot;

import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.Producer;
import org.apache.camel.component.http4.HttpComponent;
import org.apache.camel.component.http4.HttpEndpoint;
import org.apache.camel.component.http4.HttpProducer;
import org.apache.camel.impl.DefaultHeaderFilterStrategy;
import org.apache.camel.spi.HeaderFilterStrategy;
import org.apache.camel.spi.RestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConnectorConfiguration {

    public static final class WithSyndesisHeaderFilterStrategy extends HttpComponent {
        final String inFilterPattern;
        final String outFilterPattern;

        public WithSyndesisHeaderFilterStrategy(final DefaultHeaderFilterStrategy global) {
            inFilterPattern = global.getInFilterPattern();
            outFilterPattern = global.getOutFilterPattern();
        }

        @Override
        @SuppressWarnings({"PMD.ExcessiveParameterList", "PMD.UseObjectForClearerAPI"})
        public Producer createProducer(final CamelContext camelContext, final String host, final String verb, final String basePath, final String uriTemplate,
            final String queryParameters, final String consumes, final String produces, final RestConfiguration configuration,
            final Map<String, Object> parameters) throws Exception {

            final HttpProducer producer = (HttpProducer) super.createProducer(camelContext, host, verb, basePath, uriTemplate, queryParameters, consumes,
                produces, configuration, parameters);
            final HttpEndpoint endpoint = producer.getEndpoint();
            final DefaultHeaderFilterStrategy used = (DefaultHeaderFilterStrategy) endpoint.getHeaderFilterStrategy();
            used.setInFilterPattern(inFilterPattern);
            used.setOutFilterPattern(outFilterPattern);

            return producer;
        }
    }

    @Bean("connector-rest-swagger-http4")
    public Component http4(final HeaderFilterStrategy strategy) {
        final DefaultHeaderFilterStrategy global = (DefaultHeaderFilterStrategy) strategy;

        // HttpComponent::createProducer will ignore any
        // HttpHeaderFilterStrategy set on the component/endpoint and set it's
        // own HttpRestHeaderFilterStrategy which cannot be influenced in any
        // way, here we're getting the reference to the endpoint that's created
        // in the createProducer and modifying the configured
        // DefaultHeaderFilterStrategy with the configuration from our global
        // HeaderFilterStrategy. This way we can filter out any HTTP headers we
        // do not wish to receive or send via HTTP (such as Syndesis.*)
        return new WithSyndesisHeaderFilterStrategy(global);
    }

}
