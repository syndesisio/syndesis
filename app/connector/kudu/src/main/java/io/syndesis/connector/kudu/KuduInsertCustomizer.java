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

package io.syndesis.connector.kudu;

import io.syndesis.common.util.Json;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.kudu.KuduDbOperations;
import org.apache.camel.util.ObjectHelper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class KuduInsertCustomizer implements ComponentProxyCustomizer {

    private Map<String, Object> row;

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        row = new HashMap<>();
        setOptions(options);
        component.setBeforeProducer(this::beforeProducer);
        component.setAfterProducer(this::afterProducer);
    }

    private void setOptions(Map<String, Object> options) {
        options.put("operation", KuduDbOperations.INSERT);
        options.put("type", KuduDbOperations.INSERT);
    }

    private void beforeProducer(Exchange exchange) throws IOException {
        final Message in = exchange.getIn();
        final String body = in.getBody(String.class);

        if (ObjectHelper.isNotEmpty(body)) {
            Map<String, Object> dataShape = Json.reader().forType(Map.class).readValue(body);
            row = dataShape;
        }

        in.setBody(row);
    }

    private void afterProducer(Exchange exchange) {
        final Message in = exchange.getIn();
        final String body = in.getBody(String.class);

        if (ObjectHelper.isNotEmpty(body)) {
            in.setBody(body);
        } else {
            in.setBody("{}");
        }
    }
}
