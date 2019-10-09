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
package io.syndesis.connector.mongo;

import java.util.Arrays;
import java.util.Map;

import io.syndesis.connector.support.util.ConnectorOptions;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.Exchange;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoProducerCustomizer implements ComponentProxyCustomizer {
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoProducerCustomizer.class);

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        String operation = ConnectorOptions.extractOption(options, "operation");
        if (operation != null) {
            if (!MongoProducerOperation.isValid(operation)) {
                throw new IllegalArgumentException(String.format("Operation %s is not supported. " +
                    "Supported operations are %s", operation, Arrays.toString(MongoProducerOperation.values())));
            }
            // Input format conversion to text
            if (MongoProducerOperation.findById.name().equals(operation)){
                component.setBeforeProducer(this::inputToObjectId);
            } else if (MongoProducerOperation.update.name().equals(operation)){
                component.setBeforeProducer(exchange -> {
                    exchange.getIn().getHeaders().put("CamelMongoDbMultiUpdate","true");
                    this.inputToText(exchange);
                });
            } else {
                component.setBeforeProducer(this::inputToText);
            }
        } else {
            throw new IllegalArgumentException(String.format("You must provide a text `operation` option. " +
                "Supported operations are %s", Arrays.toString(MongoProducerOperation.values())));
        }
    }

    private void inputToObjectId(Exchange exchange) {
        String body = exchange.getIn().getBody(String.class);
        if (body != null) {
            // input stream new line cleaning
            body = body.replaceAll("\n", "");
        }
        if (ObjectId.isValid(body)) {
            LOGGER.debug("Converting text to ObjectId type with value `{}`", body);
            exchange.getIn().setBody(new ObjectId(body));
        } else {
            // fallback as text value
            LOGGER.debug("Setting text input as `{}`", body);
            exchange.getIn().setBody(body);
        }
    }

    private void inputToText(Exchange exchange) {
        String body = exchange.getIn().getBody(String.class);
        // Always set body to text (actual expected input format)
        LOGGER.debug("Setting text input as `{}`", body);
        exchange.getIn().setBody(body);
    }
}

