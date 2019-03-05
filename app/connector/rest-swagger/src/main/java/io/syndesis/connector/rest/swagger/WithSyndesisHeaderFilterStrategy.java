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

import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Producer;
import org.apache.camel.component.http4.HttpComponent;
import org.apache.camel.component.http4.HttpEndpoint;
import org.apache.camel.component.http4.HttpProducer;
import org.apache.camel.impl.DefaultHeaderFilterStrategy;
import org.apache.camel.spi.HeaderFilterStrategy;
import org.apache.camel.spi.RestConfiguration;

public final class WithSyndesisHeaderFilterStrategy extends HttpComponent {

    private final DefaultHeaderFilterStrategy globalFilter;

    static final class CombinedHeaderFilterStrategy implements HeaderFilterStrategy {

        private final DefaultHeaderFilterStrategy globalFilter;

        private final HeaderFilterStrategy restFilter;

        public CombinedHeaderFilterStrategy(final DefaultHeaderFilterStrategy globalFilter, final HeaderFilterStrategy restFilter) {
            this.globalFilter = globalFilter;
            this.restFilter = restFilter;
        }

        @Override
        public boolean applyFilterToCamelHeaders(final String headerName, final Object headerValue, final Exchange exchange) {
            final boolean globalWouldFilter = globalFilter.applyFilterToCamelHeaders(headerName, headerValue, exchange);
            final boolean restWouldFilter = restFilter.applyFilterToCamelHeaders(headerName, headerValue, exchange);

            // global | rest | outcome
            // false | false | false => both agree header should not be filtered
            // false | true | false => global has precedence over rest
            // true | false | true => global has precedence over rest
            // true | true | true => both agree header should be filtered out
            return globalWouldFilter || globalWouldFilter && !restWouldFilter;
        }

        @Override
        public boolean applyFilterToExternalHeaders(final String headerName, final Object headerValue, final Exchange exchange) {
            final boolean globalWouldFilter = globalFilter.applyFilterToExternalHeaders(headerName, headerValue, exchange);
            final boolean restWouldFilter = restFilter.applyFilterToExternalHeaders(headerName, headerValue, exchange);

            // global | rest | outcome
            // false | false | false => both agree header should not be filtered
            // false | true | false => global has precedence over rest
            // true | false | true => global has precedence over rest
            // true | true | true => both agree header should be filtered out
            return globalWouldFilter || globalWouldFilter && !restWouldFilter;
        }

    }

    public WithSyndesisHeaderFilterStrategy(final DefaultHeaderFilterStrategy global) {
        globalFilter = global;
    }

    @Override
    @SuppressWarnings({"PMD.ExcessiveParameterList", "PMD.UseObjectForClearerAPI"})
    public Producer createProducer(final CamelContext camelContext, final String host, final String verb, final String basePath, final String uriTemplate,
        final String queryParameters, final String consumes, final String produces, final RestConfiguration configuration,
        final Map<String, Object> parameters) throws Exception {

        final HttpProducer producer = (HttpProducer) super.createProducer(camelContext, host, verb, basePath, uriTemplate, queryParameters, consumes,
            produces, configuration, parameters);
        final HttpEndpoint endpoint = producer.getEndpoint();
        final HeaderFilterStrategy restFilter = endpoint.getHeaderFilterStrategy();

        endpoint.setHeaderFilterStrategy(new CombinedHeaderFilterStrategy(globalFilter, restFilter));

        return producer;
    }
}
