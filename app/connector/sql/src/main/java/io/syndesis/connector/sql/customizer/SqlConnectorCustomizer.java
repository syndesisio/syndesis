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
package io.syndesis.connector.sql.customizer;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import io.syndesis.connector.sql.common.JSONBeanUtil;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.Exchange;

public final class SqlConnectorCustomizer implements ComponentProxyCustomizer {
    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        component.setBeforeProducer(this::doBeforeProducer);
        component.setAfterProducer(this::doAfterProducer);
    }

    private void doBeforeProducer(Exchange exchange) {
        final String body = exchange.getIn().getBody(String.class);
        if (body != null) {
            final Properties properties = JSONBeanUtil.parsePropertiesFromJSONBean(body);
            exchange.getIn().setBody(properties);
        }
    }

    private void doAfterProducer(Exchange exchange) {
        final String jsonBean;

        if (exchange.getIn().getBody(List.class) != null) {
            List<Map<String, Object>> maps = exchange.getIn().getBody(List.class);
            if (maps.isEmpty()) {
                throw new IllegalStateException("Got an empty collection");
            }

            //Only grabbing the first record (map) in the list
            jsonBean = JSONBeanUtil.toJSONBean(maps.get(0));
        } else {
            jsonBean = JSONBeanUtil.toJSONBean(exchange.getIn().getBody(Map.class));
        }

        exchange.getIn().setBody(jsonBean);
    }
}
