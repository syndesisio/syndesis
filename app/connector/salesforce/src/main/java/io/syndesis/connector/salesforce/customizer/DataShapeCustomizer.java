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
package io.syndesis.connector.salesforce.customizer;

import java.io.IOException;
import java.util.Map;

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeAware;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.util.Json;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.processor.Pipeline;
import org.springframework.cglib.core.internal.Function;

public class DataShapeCustomizer implements ComponentProxyCustomizer, CamelContextAware, DataShapeAware {
    private CamelContext camelContext;
    private DataShape inputDataShape;
    private DataShape outputDataShape;

    @Override
    public CamelContext getCamelContext() {
        return camelContext;
    }

    @Override
    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    @Override
    public DataShape getInputDataShape() {
        return inputDataShape;
    }

    @Override
    public void setInputDataShape(DataShape inputDataShape) {
        this.inputDataShape = inputDataShape;
    }

    @Override
    public DataShape getOutputDataShape() {
        return outputDataShape;
    }

    @Override
    public void setOutputDataShape(DataShape outputDataShape) {
        this.outputDataShape = outputDataShape;
    }

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        if (inputDataShape != null) {
            Processor processor = new UnmarshallProcessor(inputDataShape, Exchange::getIn);
            Processor pipeline = Pipeline.newInstance(this.camelContext, processor, component.getBeforeProducer());

            component.setBeforeProducer(pipeline);
        }
        if (outputDataShape != null) {
            Processor processor = new UnmarshallProcessor(outputDataShape, Exchange::getOut);
            Processor pipeline = Pipeline.newInstance(this.camelContext, processor, component.getAfterProducer());

            component.setAfterProducer(pipeline);
        }
    }

    public final class UnmarshallProcessor implements Processor {
        private final Class<?> type;
        private final Function<Exchange, Message> messageFunction;

        public UnmarshallProcessor(final DataShape dataShape, Function<Exchange, Message> messageFunction) {
            this.messageFunction = messageFunction;

            if (dataShape.getKind() == DataShapeKinds.JAVA) {
                type = camelContext.getClassResolver().resolveClass(dataShape.getType());
                if (type == null) {
                    throw new IllegalArgumentException("The specified class for shape `" + dataShape + "` cannot be found");
                }
            } else {
                type = null;
            }
        }

        @Override
        public final void process(final Exchange exchange) {
            if (exchange.isFailed()) {
                return;
            }

            if (type == null) {
                return;
            }

            final Message message = messageFunction.apply(exchange);
            final String bodyAsString = message.getBody(String.class);

            if (bodyAsString == null) {
                return;
            }

            try {
                final Object output = Json.reader().forType(type).readValue(bodyAsString);
                message.setBody(output);
            } catch (final IOException e) {
                exchange.setException(e);
            }
        }
    }
}
