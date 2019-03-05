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

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.InputDataShapeAware;
import io.syndesis.common.model.OutputDataShapeAware;
import io.syndesis.connector.support.processor.SyndesisHeaderStrategy;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import io.syndesis.integration.component.proxy.Processors;

import org.apache.camel.CamelContext;

public final class RequestCustomizer implements ComponentProxyCustomizer, InputDataShapeAware, OutputDataShapeAware {

    private static final String DELEGATE_COMPONENT_NAME = "connector-rest-swagger-http4";

    private DataShape inputDataShape;

    private DataShape outputDataShape;

    @Override
    public void customize(final ComponentProxyComponent component, final Map<String, Object> options) {
        final CamelContext camelContext = component.getCamelContext();

        if (!camelContext.getComponentNames().contains(DELEGATE_COMPONENT_NAME)) {
            // HttpComponent::createProducer will ignore any
            // HttpHeaderFilterStrategy set on the component/endpoint and set
            // it's own HttpRestHeaderFilterStrategy which cannot be influenced
            // in any way, here we're getting the reference to the endpoint
            // that's created in the createProducer and modifying the configured
            // DefaultHeaderFilterStrategy with the configuration from our
            // global HeaderFilterStrategy. This way we can filter out any HTTP
            // headers we do not wish to receive or send via HTTP (such as
            // Syndesis.*)
            final WithSyndesisHeaderFilterStrategy delegate = new WithSyndesisHeaderFilterStrategy(new SyndesisHeaderStrategy());
            camelContext.addComponent(DELEGATE_COMPONENT_NAME, delegate);
        }

        Processors.addBeforeProducer(component, new RequestHeaderSetter(options, inputDataShape, outputDataShape));
        Processors.addBeforeProducer(component, new RequestPayloadConverter(inputDataShape));
    }

    @Override
    public DataShape getInputDataShape() {
        return inputDataShape;
    }

    @Override
    public DataShape getOutputDataShape() {
        return outputDataShape;
    }

    @Override
    public void setInputDataShape(final DataShape dataShape) {
        inputDataShape = dataShape;
    }

    @Override
    public void setOutputDataShape(final DataShape dataShape) {
        outputDataShape = dataShape;
    }

}
