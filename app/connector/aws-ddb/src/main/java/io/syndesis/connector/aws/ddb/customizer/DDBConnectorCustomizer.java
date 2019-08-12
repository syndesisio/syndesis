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

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.aws.ddb.DdbConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Generic class to customize DDB operations. Common utilities.
 */
public abstract class DDBConnectorCustomizer implements ComponentProxyCustomizer {

    //Store options to customize the connector
    private Map<String, Object> options;

    private static final Logger LOG = LoggerFactory.getLogger(DDBConnectorCustomizer.class);

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {

        this.options = options;
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
    }


    /**
     * Setup the common headers for all operations.
     *
     * @param exchange
     */
    protected void doBeforeProducer(Exchange exchange) {
        exchange.getIn().setHeader(DdbConstants.CONSISTENT_READ, "true");
        exchange.getIn().setHeader(DdbConstants.RETURN_VALUES, "ALL_OLD");

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


    /**
     * Extract a map from a JSON in a header as AttributeValue. Useful for the headers.
     *
     * @param parameterName
     * @param options
     * @return
     */
    protected Map<String, AttributeValue> getAttributeValueMap(
            final String parameterName, Map<String, Object> options) {
        final Map<String, AttributeValue> attributeMap = new HashMap<String, AttributeValue>();

        if (options.containsKey(parameterName)) {

            String element = options.get(parameterName).toString();

            if (!element.isEmpty()) {
                try {
                    ObjectMapper mapper = new ObjectMapper();

                    @SuppressWarnings("unchecked")
                    Map<String, Object> attributes = mapper.readValue(element, Map.class);

                    if (attributes.isEmpty() && LOG.isWarnEnabled()) {
                        LOG.warn("The parameter " + parameterName + " is an empty map.");
                    }

                    for (Map.Entry<String, Object> attribute : attributes.entrySet()) {
                        attributeMap.put(attribute.getKey(),
                                new AttributeValue(attribute.getValue().toString()));
                        if(LOG.isTraceEnabled()) {
                            LOG.trace("Parameter adding '" + attribute.getKey() + "'->'" + attribute.getValue() + "'");
                        }
                    }
                } catch (IOException e) {
                    LOG.warn("Error trying to parse the json: " + element, e);
                }
            } else {
                LOG.warn("The parameter " + parameterName + " is empty.");
            }
        } else {
            LOG.warn("The parameter " + parameterName + " does not exist.");
        }
        return attributeMap;
    }

}
