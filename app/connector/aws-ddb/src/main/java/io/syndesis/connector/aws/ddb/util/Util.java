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

package io.syndesis.connector.aws.ddb.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.syndesis.connector.support.util.ConnectorOptions;
import org.apache.camel.CamelContext;
import org.apache.camel.TypeConverter;
import org.apache.camel.util.EndpointHelper;
import org.apache.camel.util.IntrospectionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for common procedures.
 */
public class Util {
    public static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Logger LOG = LoggerFactory.getLogger(Util.class);

    /**
     * Extract a map from a JSON in a header as AttributeValue. Useful for the headers.
     *
     * @param parameterName
     * @param options
     * @return
     */
    public static Map<String, AttributeValue> getAttributeValueMap(
    final String parameterName, Map<String, Object> options) {
        final Map<String, AttributeValue> attributeMap = new HashMap<String, AttributeValue>();


        String element = ConnectorOptions.extractOption(options, parameterName, "");

        if (!element.isEmpty()) {
            try {

                @SuppressWarnings("unchecked")
                Map<String, Object> attributes = MAPPER.readValue(element, Map.class);

                if (attributes.isEmpty() && LOG.isWarnEnabled()) {
                    LOG.warn("The parameter {} is an empty map.", parameterName);
                }

                for (Map.Entry<String, Object> attribute : attributes.entrySet()) {
                    attributeMap.put(attribute.getKey(),
                    new AttributeValue(attribute.getValue().toString()));
                    LOG.trace("Parameter adding '" + attribute.getKey() + "'->'" + attribute.getValue() + "'");

                }
            } catch (IOException e) {
                LOG.warn("Error trying to parse the json: {}", element, e);
            }
        } else {
            LOG.warn("The parameter {} is empty.", parameterName);
        }

        return attributeMap;
    }

    public static Object getValue(AttributeValue value) {
        if(value.getB() != null) {
            return value.getB();
        } else if (value.getS() != null){
            return value.getS();
        } else if (value.getBOOL() != null){
            return value.getBOOL();
        } else if (value.getBS() != null){
            return value.getBS();
        } else if (value.getL() != null){
            return value.getL();
        } else if (value.getM() != null){
            return value.getM();
        } else if (value.getN() != null){
            return value.getN();
        } else if (value.getNS() != null){
            return value.getNS();
        } else if (value.getSS() != null){
            return value.getSS();
        }
        return null;
    }



    public static <T> T setProperties(T instance, Map<String, Object> properties,
                                  CamelContext camelContext) {
        if (camelContext == null) {
            throw new IllegalStateException("Camel context is null");
        }

        try {
            if (!properties.isEmpty()) {
                final TypeConverter converter = camelContext.getTypeConverter();

                IntrospectionSupport.setProperties(converter, instance, properties);

                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    if (entry.getValue() instanceof String) {
                        String value = (String) entry.getValue();
                        if (EndpointHelper.isReferenceParameter(value)) {
                            IntrospectionSupport.setProperty(camelContext, converter, instance, entry.getKey(), null, value, true);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(
            "Get information about tables failed.", e);
        }

        return instance;
    }
}
