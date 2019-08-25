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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeAware;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.util.Json;
import io.syndesis.connector.support.processor.HttpRequestUnwrapperProcessor;
import io.syndesis.connector.support.processor.util.SimpleJsonSchemaInspector;
import io.syndesis.connector.support.util.ConnectorOptions;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.RuntimeCamelException;

public class ApiProviderReturnPathCustomizer implements ComponentProxyCustomizer, CamelContextAware, DataShapeAware {

    private static final ObjectReader READER = Json.reader().forType(JsonNode.class);

    private static final String HTTP_RESPONSE_CODE_PROPERTY        = "httpResponseCode";
    private static final String HTTP_ERROR_RESPONSE_CODES_PROPERTY = "errorResponseCodes";
    private static final String ERROR_RESPONSE_BODY                = "returnBody";

    private CamelContext context;

    private DataShape inputDataShape;

    private DataShape outputDataShape;

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        if (inputDataShape != null && inputDataShape.getKind() == DataShapeKinds.JSON_SCHEMA && inputDataShape.getSpecification() != null) {
            try {
                final JsonNode schema = READER.readTree(inputDataShape.getSpecification());
                Set<String> properties = SimpleJsonSchemaInspector.getProperties(schema);
                Set<String> extraneousProperties = new HashSet<>(properties);
                extraneousProperties.removeAll(Arrays.asList("parameters", "body"));

                if (!properties.isEmpty() && extraneousProperties.isEmpty()) {
                    component.setBeforeProducer(new HttpRequestUnwrapperProcessor(schema));
                }
            } catch (IOException e) {
                throw new RuntimeCamelException(e);
            }
        }

        Map<String, String> errorResponseCodeMappings = ErrorMapper.jsonToMap(
                ConnectorOptions.extractOptionAndMap(options, HTTP_ERROR_RESPONSE_CODES_PROPERTY, String::valueOf, ""));
        Boolean isReturnBody =
                ConnectorOptions.extractOptionAndMap(options, ERROR_RESPONSE_BODY, Boolean::valueOf, false);
        Integer httpResponseStatus =
                ConnectorOptions.extractOptionAndMap(options, HTTP_RESPONSE_CODE_PROPERTY, Integer::valueOf, 200);

        component.setAfterProducer(statusCodeUpdater(httpResponseStatus, errorResponseCodeMappings, isReturnBody));
    }

    private Processor statusCodeUpdater(Integer responseCode, Map<String, String> errorResponseCodeMappings,
            Boolean isReturnBody) {
        return exchange -> {
            if (exchange.getException() != null) {
                ErrorStatusInfo statusInfo =
                        ErrorMapper.mapError(exchange.getException(), errorResponseCodeMappings, responseCode);
                exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, statusInfo.getResponseCode());
                if (isReturnBody) {
                    exchange.getIn().setBody(statusInfo.toJson());
                } else {
                    //making sure we don't return anything
                    exchange.getIn().setBody("");
                }
            } else if (responseCode != null) {
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
