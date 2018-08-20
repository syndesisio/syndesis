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
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeAware;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.util.Json;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.Exchange;
import org.apache.camel.RuntimeCamelException;

public class WebhookConnectorCustomizer implements ComponentProxyCustomizer, CamelContextAware, DataShapeAware {
    public static final String SCHEMA_ID = "io:syndesis:webhook";

    static final ObjectReader READER = Json.reader().forType(ObjectSchema.class);

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
        if (outputDataShape != null && outputDataShape.getKind() == DataShapeKinds.JSON_SCHEMA && outputDataShape.getSpecification() != null) {
            try {
                final ObjectSchema schema = READER.readValue(outputDataShape.getSpecification());
                if (SCHEMA_ID.equals(schema.getId())) {
                    // check that the schema contains the right properties
                    if (!(schema.getProperties().get("parameters") instanceof ObjectSchema)) {
                        throw new IllegalArgumentException("JsonSchema does not define parameters property");
                    }

                    final JsonSchema bodySchema = schema.getProperties().get("body");
                    if (bodySchema != null && !(bodySchema instanceof ObjectSchema)) {
                        throw new IllegalArgumentException("JsonSchema does not define body property");
                    }

                    component.setBeforeConsumer(new WrapperProcessor(schema));
                }
            } catch (IOException e) {
                throw new RuntimeCamelException(e);
            }
        }

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

}
