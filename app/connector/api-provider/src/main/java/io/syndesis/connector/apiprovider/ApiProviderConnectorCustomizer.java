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
package io.syndesis.connector.apiprovider;

import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.util.Map;

public class ApiProviderConnectorCustomizer implements ComponentProxyCustomizer, CamelContextAware {

    private static final String HTTP_RESPONSE_CODE_PROPERTY = "httpResponseCode";

    private CamelContext context;

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        if (options.containsKey(HTTP_RESPONSE_CODE_PROPERTY)) {
            try {
                consumeOption(this.context, options, HTTP_RESPONSE_CODE_PROPERTY, Integer.class, code ->
                    component.setAfterProducer(statusCodeUpdater(code))
                );
            } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    private Processor statusCodeUpdater(Integer responseCode) {
        return exchange -> {
            if (responseCode != null && exchange.getException() == null) {
                // Let's not override the return code in case of exceptions in the route execution
                exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, responseCode);
            }
        };
    }

    @Override
    public void setCamelContext(CamelContext camelContext) {
        this.context = camelContext;
    }

    @Override
    public CamelContext getCamelContext() {
        return this.context;
    }
}
