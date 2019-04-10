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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

public class ApiProviderStartEndpointCustomizer implements ComponentProxyCustomizer, CamelContextAware, DataShapeAware {

    private static final ObjectReader READER = Json.reader().forType(JsonNode.class);

    private CamelContext context;

    private DataShape inputDataShape;

    private DataShape outputDataShape;

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        final List<Processor> beforeConsumers = new ArrayList<>(2);
        if (outputDataShape != null && outputDataShape.getKind() == DataShapeKinds.JSON_SCHEMA && outputDataShape.getSpecification() != null) {
            try {
                final JsonNode schema = READER.readTree(outputDataShape.getSpecification());
                Set<String> properties = SimpleJsonSchemaInspector.getProperties(schema);
                Set<String> extraneousProperties = new HashSet<>(properties);
                extraneousProperties.removeAll(Arrays.asList("parameters", "body"));

                if (!properties.isEmpty() && extraneousProperties.isEmpty()) {
                    beforeConsumers.add(new HttpRequestWrapperProcessor(schema));
                }
            } catch (IOException e) {
                throw new RuntimeCamelException(e);
            }
        }

        beforeConsumers.add(new HttpMessageToDefaultMessageProcessor());

        // removes all non Syndesis.* headers, this is so the headers that might
        // influence HTTP components in the flow after this connector don't
        // interpret them, for instance the `Host` header is particularly
        // troublesome
        beforeConsumers.add((e) -> e.getIn().removeHeaders("*", Exchange.CONTENT_TYPE, "Syndesis.*"));

        component.setBeforeConsumer(Pipeline.newInstance(context, beforeConsumers));
    }

    @Override
    public void setCamelContext(CamelContext camelContext) {
        this.context = camelContext;
    }

    @Override
    public CamelContext getCamelContext() {
        return this.context;
    }

    @Override
    public void setInputDataShape(DataShape dataShape) {
        this.inputDataShape = dataShape;
    }

    @Override
    public DataShape getInputDataShape() {
        return this.inputDataShape;
    }

    @Override
    public void setOutputDataShape(DataShape dataShape) {
        this.outputDataShape = dataShape;
    }

    @Override
    public DataShape getOutputDataShape() {
        return this.outputDataShape;
    }
}
