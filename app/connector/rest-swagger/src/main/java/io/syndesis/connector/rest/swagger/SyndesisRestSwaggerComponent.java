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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.camel.CamelContext;
import org.apache.camel.Producer;
import org.apache.camel.component.http4.HttpComponent;
import org.apache.camel.component.http4.HttpProducer;
import org.apache.camel.spi.RestConfiguration;

public final class SyndesisRestSwaggerComponent extends HttpComponent {
    public static final String COMPONENT_NAME = "connector-rest-swagger-http4";
    private static final List<Consumer<HttpProducer>> CUSTOMIZERS = new ArrayList<>(3);

    static {
        CUSTOMIZERS.add(new WithSyndesisHeaderFilterStrategy());
    }

    @Override
    @SuppressWarnings("PMD.ExcessiveParameterList") // overriden method
    public Producer createProducer(final CamelContext camelContext, final String host, final String verb, final String basePath, final String uriTemplate,
        final String queryParameters, final String consumes, final String produces, final RestConfiguration configuration, final Map<String, Object> parameters)
        throws Exception {

        final HttpProducer producer = (HttpProducer) super.createProducer(camelContext, host, verb, basePath, uriTemplate, queryParameters, consumes, produces,
            configuration, parameters);

        for (final Consumer<HttpProducer> customizer : CUSTOMIZERS) {
            customizer.accept(producer);
        }

        return producer;
    }
}
