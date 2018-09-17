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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeAware;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.util.Json;
import io.syndesis.connector.support.processor.HttpRequestWrapperProcessor;
import io.syndesis.connector.support.processor.util.SimpleJsonSchemaInspector;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.RuntimeCamelException;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ApiProviderStartEndpointCustomizer implements ComponentProxyCustomizer, CamelContextAware, DataShapeAware {

    private static final ObjectReader READER = Json.reader().forType(JsonNode.class);

    private CamelContext context;

    private DataShape inputDataShape;

    private DataShape outputDataShape;

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        if (outputDataShape != null && outputDataShape.getKind() == DataShapeKinds.JSON_SCHEMA && outputDataShape.getSpecification() != null) {
            try {
                final JsonNode schema = READER.readTree(outputDataShape.getSpecification());
                Set<String> properties = SimpleJsonSchemaInspector.getProperties(schema);
                Set<String> extraneousProperties = new HashSet<>(properties);
                extraneousProperties.removeAll(Arrays.asList("parameters", "body"));

                if (!properties.isEmpty() && extraneousProperties.isEmpty()) {
                    component.setBeforeConsumer(new HttpRequestWrapperProcessor(schema));
                }
            } catch (IOException e) {
                throw new RuntimeCamelException(e);
            }
        }
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
