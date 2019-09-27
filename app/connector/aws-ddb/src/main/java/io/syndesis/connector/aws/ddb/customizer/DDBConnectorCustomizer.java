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
import io.syndesis.connector.support.util.ConnectorOptions;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.aws.ddb.DdbConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic class to customize DDB operations. Common utilities.
 */
public abstract class DDBConnectorCustomizer implements ComponentProxyCustomizer {

    private static final Logger LOG = LoggerFactory.getLogger(DDBConnectorCustomizer.class);
    //Store options to customize the connector
    private Map<String, Object> options;

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {

        this.options = options;

        options.put("element", ConnectorOptions.popOption(options, "element"));
        options.put("attributes", ConnectorOptions.popOption(options, "attributes"));

        component.setBeforeProducer(this::doBeforeProducer);
        component.setAfterProducer(this::doAfterProducer);
    }

    /**
     * Extract results and place them on the body.
     *
     * @param exchange
     */
    protected void doAfterProducer(Exchange exchange) {

        final Message in = exchange.getIn();
        final Message out = exchange.getOut();
        final Object item = in.getHeader(DdbConstants.ATTRIBUTES);

        if (item != null) {
            out.setBody(item);
        } else if (in.getHeader(DdbConstants.ITEM) != null) {
            out.setBody(in.getHeader(DdbConstants.ITEM));
        } else {
            out.setBody(in.getHeader(DdbConstants.ITEMS));
        }

        if (out.getBody() instanceof Map) {
            @SuppressWarnings("unchecked")
            Set<Map.Entry<String, AttributeValue>> elements = ((Map<String, AttributeValue>) out.getBody()).entrySet();
            Map<String, Object> output = new HashMap<String, Object>();

            for (Map.Entry<String, AttributeValue> element : elements) {
                output.put(element.getKey(), Util.getValue(element.getValue()).toString());
            }

            try {
                //Convert Map to JSON
                String json = mapper.writeValueAsString(output);

                out.setBody(json);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

    }


    /**
     * Setup the common headers for all operations.
     *
     * @param exchange
     */
    @SuppressWarnings("unchecked")
    protected void doBeforeProducer(Exchange exchange) {
        exchange.getIn().setHeader(DdbConstants.CONSISTENT_READ, "true");
        exchange.getIn().setHeader(DdbConstants.RETURN_VALUES, "ALL_OLD");


        LOG.trace("pre this.options: " + this.options);

        //Do we have variables from atlas?
        if (exchange.getIn().getBody() != null) {
            Object body = exchange.getIn().getBody();
            Map<String, Object> map = null;

            if (body instanceof Map) {
                map = (Map<String, Object>) body;
            } else {
                try {
                    map = (Map<String, Object>) mapper.readValue(body.toString(), Map.class);
                } catch (Exception e) {
                    LOG.trace("Couldn't parse parameters." + e);
                }
            }

            if (map != null && !map.isEmpty()) {
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    if (entry.getKey().startsWith("#")) {
                        final String searchKey = ":" + entry.getKey();
                        final String replacement = entry.getValue().toString();
                        for (Map.Entry<String, Object> option : options.entrySet()) {
                            if (option.getValue() != null &&
                                option.getValue() instanceof String) {
                                final String oldValue = option.getValue().toString();
                                final String key = option.getKey();
                                final String newValue = oldValue.replace(searchKey, replacement);

                                LOG.trace("this.option: " + key + ":" + oldValue + "->" + newValue);
                                this.options.put(key, newValue);
                            }
                        }
                    }
                }
            }
        }

        LOG.trace("post this.options: " + this.options);

        customize(exchange, this.options);
    }


    /**
     * Customizations for each operation.
     *
     * @param exchange
     * @param options
     */
    abstract void customize(Exchange exchange, Map<String,
        Object> options);


}
