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
package io.syndesis.connector.webhook;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeAware;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.util.Json;
import io.syndesis.connector.support.processor.HttpMessageToDefaultMessageProcessor;
import io.syndesis.connector.support.processor.HttpRequestWrapperProcessor;
import io.syndesis.connector.support.processor.util.SimpleJsonSchemaInspector;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.processor.Pipeline;

public class WebhookConnectorCustomizer implements ComponentProxyCustomizer, CamelContextAware, DataShapeAware {
    public static final String SCHEMA_ID = "io:syndesis:webhook";

    static final ObjectReader READER = Json.reader();

    private CamelContext camelContext;
    private DataShape inputDataShape;
    private DataShape outputDataShape;

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        final List<Processor> beforeConsumers = new ArrayList<>();
        beforeConsumers.add(e -> e.getIn().removeHeader(Exchange.HTTP_URI));

        if (outputDataShape != null && outputDataShape.getKind() == DataShapeKinds.JSON_SCHEMA && outputDataShape.getSpecification() != null) {
            try {
                final JsonNode schema = READER.readTree(outputDataShape.getSpecification());
                if (Optional.of(SCHEMA_ID).equals(SimpleJsonSchemaInspector.getId(schema))) {
                    Set<String> properties = SimpleJsonSchemaInspector.getProperties(schema);
                    // check that the schema contains the right properties
                    if (!properties.contains("parameters")) {
                        throw new IllegalArgumentException("JsonSchema does not define parameters property");
                    }

                    if (!properties.contains("body")) {
                        throw new IllegalArgumentException("JsonSchema does not define body property");
                    }

                    beforeConsumers.add(new HttpRequestWrapperProcessor(schema));
                }
            } catch (IOException e) {
                throw new RuntimeCamelException(e);
            }
        }

        beforeConsumers.add(new HttpMessageToDefaultMessageProcessor());

        component.setBeforeConsumer(Pipeline.newInstance(camelContext, beforeConsumers));

        // Unconditionally we remove output in 7.1 release
        component.setAfterConsumer(this::removeOutput);
    }

    private void removeOutput(final Exchange exchange) {
        exchange.getOut().setBody("");
        exchange.getOut().removeHeaders("*");

        if (exchange.getException() == null) {
            // In case of exception, we leave the error code as is
            exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, 204);
            exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_TEXT, "No Content");
        }
    }

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
}
