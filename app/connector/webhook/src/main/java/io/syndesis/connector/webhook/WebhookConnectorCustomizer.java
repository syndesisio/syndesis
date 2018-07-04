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
import java.io.InputStream;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import com.fasterxml.jackson.module.jsonSchema.types.StringSchema;
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
import org.apache.camel.RuntimeCamelException;

public class WebhookConnectorCustomizer implements ComponentProxyCustomizer, CamelContextAware, DataShapeAware {
    public static final String SCHEMA_ID = "io:syndesis:webhook";

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
                ObjectSchema schema = Json.reader().forType(ObjectSchema.class).readValue(outputDataShape.getSpecification());
                if (SCHEMA_ID.equals(schema.getId())) {
                    // check that the schema contains the right properties
                    if (!(schema.getProperties().get("parameters") instanceof ObjectSchema)) {
                        throw new IllegalArgumentException("JsonSchema does not define parameters property");
                    }
                    if (!(schema.getProperties().get("body") instanceof ObjectSchema)) {
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

    private static class WrapperProcessor implements Processor {
        private final ObjectSchema schema;

        public WrapperProcessor(ObjectSchema schema) {
            this.schema = schema;
        }

        @Override
        public void process(Exchange exchange) throws Exception {
            final Message message = exchange.getIn();
            final Object body = message.getBody();
            final Map<String, JsonSchema> properties = this.schema.getProperties();

            ObjectNode rootNode = Json.copyObjectMapperConfiguration().createObjectNode();
            ObjectNode parametersNode = rootNode.putObject("parameters");

            if (body instanceof String) {
                rootNode.putPOJO("body", Json.reader().forType(JsonNode.class).readValue((String)body));
            } else if (body instanceof InputStream) {
                rootNode.putPOJO("body", Json.reader().forType(JsonNode.class).readValue((InputStream)body));
            } else {
                rootNode.putPOJO("body", body);
            }

            if (properties.get("parameters") instanceof ObjectSchema) {
                ObjectSchema parameters = (ObjectSchema)properties.get("parameters");

                for (Map.Entry<String, JsonSchema> header : parameters.getProperties().entrySet()) {
                    if (header.getValue() instanceof StringSchema) {
                        parametersNode.put(
                            header.getKey(),
                            message.getHeader(header.getKey(), String.class)
                        );
                    }
                }
            }

            message.setBody(Json.toString(rootNode));
        }
    }
}
