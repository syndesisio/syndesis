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
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.aws.ddb.DdbConstants;

/**
 * Generic class to customize DDB operations. Common utilities.
 */
public abstract class DDBConnectorCustomizer implements ComponentProxyCustomizer {

    //Store options to customize the connector
    private Map<String, Object> options;

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

        if(out.getBody() instanceof Map) {
            @SuppressWarnings("unchecked")
            Set<Map.Entry<String, AttributeValue>> elements = ((Map<String, AttributeValue>) out.getBody()).entrySet();
            Map<String, Object> output = new HashMap<String, Object>();

            for(Map.Entry<String, AttributeValue> element : elements) {
                AttributeValue value = element.getValue();
                if(value.getB() != null) {
                    output.put(element.getKey(), value.getB());
                } else if (value.getS() != null){
                    output.put(element.getKey(), value.getS());
                } else if (value.getBOOL() != null){
                    output.put(element.getKey(), value.getBOOL());
                } else if (value.getBS() != null){
                    output.put(element.getKey(), value.getBS());
                } else if (value.getL() != null){
                    output.put(element.getKey(), value.getL());
                } else if (value.getM() != null){
                    output.put(element.getKey(), value.getM());
                } else if (value.getN() != null){
                    output.put(element.getKey(), value.getN());
                } else if (value.getNS() != null){
                    output.put(element.getKey(), value.getNS());
                } else if (value.getSS() != null){
                    output.put(element.getKey(), value.getSS());
                } else if (value.getNULL() != null){
                    output.put(element.getKey(), null);
                }
            }

            ObjectMapper mapper = new ObjectMapper();
            try
            {
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


}
