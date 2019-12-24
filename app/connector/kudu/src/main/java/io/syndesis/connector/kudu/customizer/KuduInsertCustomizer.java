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

package io.syndesis.connector.kudu.customizer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import io.syndesis.common.util.json.JsonUtils;
import io.syndesis.connector.kudu.common.KuduDbOperations;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.util.ObjectHelper;

public class KuduInsertCustomizer implements ComponentProxyCustomizer {

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        setOptions(options);
        component.setBeforeProducer(KuduInsertCustomizer::beforeProducer);
        component.setAfterProducer(KuduInsertCustomizer::afterProducer);
    }

    private static void setOptions(Map<String, Object> options) {
        options.put("operation", KuduDbOperations.INSERT);
        options.put("type", KuduDbOperations.INSERT);
    }

    private static void beforeProducer(Exchange exchange) throws IOException {
        final Message in = exchange.getIn();
        final String body = in.getBody(String.class);

        if (ObjectHelper.isNotEmpty(body)) {
            in.setBody(JsonUtils.reader().forType(Map.class).readValue(body));
        } else {
            in.setBody(new HashMap<>());
        }
    }

    private static void afterProducer(Exchange exchange) {
        final Message in = exchange.getIn();
        final String body = in.getBody(String.class);

        if (ObjectHelper.isNotEmpty(body)) {
            in.setBody(body);
        } else {
            in.setBody("{}");
        }
    }
}
