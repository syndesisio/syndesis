/*
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.integration.runtime.components.json;

import java.io.InputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import io.syndesis.integration.support.Strings;
import org.apache.camel.Component;
import org.apache.camel.Consumer;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.util.ServiceHelper;

public class JsonEndpoint extends DefaultEndpoint {

    public static final String JSON_CONTENT_TYPE = "application/json";
    private static final Set<Class<?>> stringableClasses = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
        String.class, byte[].class, ByteBuffer.class
    )));
    private static final Set<Class<?>> stringableInterfaces = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
        InputStream.class, Reader.class
    )));

    private Producer jsonMarshalProducer;
    private Endpoint jsonMarshalEndpoint;

    public JsonEndpoint() {
    }

    public JsonEndpoint(String endpointUri, Component component) {
        super(endpointUri, component);
    }

    @Override
    public Producer createProducer() throws Exception {
        return new JsonProducer(this);
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        return new JsonConsumer(this, processor);
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    protected void doStart() throws Exception {
        jsonMarshalEndpoint = getCamelContext().getEndpoint("dataformat:json-jackson:marshal");
        Objects.requireNonNull(jsonMarshalEndpoint, "jsonMarshalEndpoint");
        jsonMarshalProducer = jsonMarshalEndpoint.createProducer();
        Objects.requireNonNull(jsonMarshalProducer, "jsonMarshalProducer");
        ServiceHelper.startServices(jsonMarshalEndpoint, jsonMarshalProducer);
        super.doStart();
    }

    @Override
    protected void doStop() throws Exception {
        ServiceHelper.stopServices(jsonMarshalProducer, jsonMarshalEndpoint);
        super.doStop();
    }

    /**
     * Lets marshal the body to JSON using Jackson if we require it.
     * <br>
     * The current rules are to only marshal to JSON if we don't have a {@link Exchange#CONTENT_TYPE} header.
     * If we can convert the body to a String then we test if its already JSON and if not we marshal it using the JSON
     * data format with the Jackson library
     */
    public void jsonMarshalIfRequired(Exchange exchange) throws Exception {
        Message in = exchange.getIn();
        if (in == null) {
            return;
        }
        String contentType = in.getHeader(Exchange.CONTENT_TYPE, String.class);
        if (!Strings.isEmpty(contentType)) {
            // lets preserve existing content types as it could be XML, YAML or something else
            return;
        }
        Object body = in.getBody();
        if (body != null) {
            if (isPossibleJsonClass(exchange, body.getClass(), body)) {
                try {
                    String text = in.getBody(String.class);
                    if (text != null) {
                        if (isJsonLookingString(text.trim())) {
                            in.setHeader(Exchange.CONTENT_TYPE, JSON_CONTENT_TYPE);
                        }
                        in.setBody(text);
                        return;
                    }
                } catch (Exception e) {
                    // ignore
                }
            }

            in.setHeader(Exchange.CONTENT_TYPE, JSON_CONTENT_TYPE);

            jsonMarshalProducer.process(exchange);
        }
    }

    private boolean isJsonLookingString(String possibleJson) {
        return (possibleJson.startsWith("{") && possibleJson.endsWith("}")) ||
                (possibleJson.startsWith("[") && possibleJson.endsWith("]")) ||
                (possibleJson.startsWith("\"") && possibleJson.endsWith("\"")) ||
                (possibleJson.startsWith("'") && possibleJson.endsWith("'"));
    }

    /**
     * Returns true if the body class is a java type which may be converted to a String so we can test for
     * it being JSON already
     */
    private boolean isPossibleJsonClass(Exchange exchange, Class<?> clazz, Object body) {
        if (stringableClasses.contains(clazz)) {
            return true;
        }
        for (Class<?> stringableInterface : stringableInterfaces) {
            if (stringableInterface.isInstance(body)) {
                return true;
            }
        }
        return false;
    }
}
