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
import org.apache.camel.Processor;
import org.apache.camel.processor.Pipeline;

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
            Processor processor = new UnmarshallProcessor(inputDataShape);
            Processor pipeline = Pipeline.newInstance(this.camelContext, processor, component.getBeforeProducer());

            component.setBeforeProducer(pipeline);
        }
        if (outputDataShape != null) {
            Processor processor = new UnmarshallProcessor(outputDataShape);
            Processor pipeline = Pipeline.newInstance(this.camelContext, processor, component.getAfterProducer());

            component.setAfterProducer(pipeline);
        }
    }

    public final class UnmarshallProcessor implements Processor {
        private final Class<?> type;

        public UnmarshallProcessor(final DataShape dataShape) {
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
        public void process(final Exchange exchange) {
            if (exchange.isFailed()) {
                return;
            }

            if (type == null) {
                return;
            }

            final Object body = exchange.getIn().getBody(type);
            if (body != null) {
                return;
            }

            try {
                final String bodyAsString = exchange.getIn().getBody(String.class);
                final Object output = Json.reader().forType(type).readValue(bodyAsString);
                exchange.getIn().setBody(output);
            } catch (final IOException e) {
                exchange.setException(e);
            }
        }
    }
}
