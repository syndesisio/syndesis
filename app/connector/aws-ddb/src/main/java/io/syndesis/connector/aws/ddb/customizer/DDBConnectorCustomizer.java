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
package io.syndesis.connector.aws.ddb.customizer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.syndesis.connector.aws.ddb.util.Util;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.aws.ddb.DdbConstants;
import org.apache.camel.component.aws.ddb.DdbOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic class to customize DDB operations. Common utilities.
 */
public abstract class DDBConnectorCustomizer implements ComponentProxyCustomizer {

    protected static final Logger LOG = LoggerFactory.getLogger(DDBConnectorCustomizer.class);
    //Store options to customize the connector
    private Map<String, Object> options;

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        this.options = options;

        component.setBeforeProducer(this::doBeforeProducer);
        component.setAfterProducer(this::doAfterProducer);
    }

    /**
     * Extract results and place them on the body.
     */
    @SuppressWarnings("unchecked")
    protected void doAfterProducer(Exchange exchange) {
        final Message in = exchange.getIn();
        Map<String, AttributeValue> attributes = (Map<String, AttributeValue>) in.getHeader(DdbConstants.ATTRIBUTES);
        String op = in.getHeader(DdbConstants.OPERATION).toString();

        if (op.equals(DdbOperations.PutItem.name())) {
            //Use input. If we are here, we know it went well (or so DDB says)
            //But attributes may be empty due to caching issues (DDB side)
            Map<String, AttributeValue> items =
                (Map<String, AttributeValue>) exchange.getIn().getHeader(DdbConstants.ITEM);
            in.setBody(mapToJSON(items));
        } else if (attributes != null) {
            in.setBody(mapToJSON(attributes));
        } else  {
            //Something went wrong, we always return something
            throw new IllegalArgumentException("DynamoDB operation failed: " + in.getHeaders());
        }
    }

    private String mapToJSON(Map<String, AttributeValue> item) {
        Set<Map.Entry<String, AttributeValue>> elements = item.entrySet();
        Map<String, Object> output = new HashMap<>();

        for (Map.Entry<String, AttributeValue> element : elements) {
            output.put(element.getKey(), Util.getValue(element.getValue()).toString());
        }

        String json;

        try {
            //Convert Map to JSON
            json = mapper.writeValueAsString(output);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error parsing json output.", e);
        }

        LOG.trace("Executing: " + this.getClass() + " with output body: " + json);

        return json;
    }


    /**
     * Setup the common headers for all operations.
     */
    protected void doBeforeProducer(Exchange exchange) {
        exchange.getIn().setHeader(DdbConstants.CONSISTENT_READ, "true");
        exchange.getIn().setHeader(DdbConstants.RETURN_VALUES, "ALL_OLD");

        if(this.options == null) {
            this.options = new HashMap<>();
        }

        String element = (String) this.options.get("element");
        String attributes = (String) this.options.get("attributes");

        LOG.trace("pre this.options: " + this.options);

        //Do we have variables from atlas?
        Object body = exchange.getIn().getBody();
        if (body != null) {
            Map<String, Object> map = parseMapFromBody(body);

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (entry.getKey().startsWith("#") && entry.getValue() != null) {
                    final String searchKey = ":" + entry.getKey();
                    final String replacement = entry.getValue().toString();

                    element = element.replace(searchKey, replacement);
                    if(attributes != null) {
                        attributes = attributes.replace(searchKey, replacement);
                    }
                }

            }
        }

        Map<String, Object> options = new HashMap<>();
        options.put("element", element);
        options.put("attributes", attributes);

        LOG.trace("post this.options: " + options);

        customize(exchange, options);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseMapFromBody(Object body) {

        if (body instanceof Map) {
            return (Map<String, Object>) body;
        } else {
            try {
                return (Map<String, Object>) mapper.readValue(body.toString(), Map.class);
            } catch (Exception e) {
                LOG.error("Couldn't parse parameters." + e);
            }
        }
        return new HashMap<>();
    }


    /**
     * Customizations for each operation.
     */
    abstract void customize(Exchange exchange, Map<String,
        Object> options);


}
