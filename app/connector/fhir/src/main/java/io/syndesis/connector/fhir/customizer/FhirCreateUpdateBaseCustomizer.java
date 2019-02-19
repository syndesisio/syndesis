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
package io.syndesis.connector.fhir.customizer;

import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.util.component.ApiMethod;

import java.util.Map;

public abstract class FhirCreateUpdateBaseCustomizer implements ComponentProxyCustomizer {

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        options.put("apiName", FhirCustomizerHelper.getFhirApiName(getApiMethodClass()));
        options.put("methodName", "resource");

        component.setBeforeProducer(this::beforeProducer);
    }

    public abstract Class<? extends ApiMethod> getApiMethodClass();

    public void beforeProducer(Exchange exchange) {
        final Message in = exchange.getIn();
        String resource = in.getBody(String.class);
        in.setHeader("CamelFhir.resourceAsString", resource);
    }
}
