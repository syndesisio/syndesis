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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import io.syndesis.connector.sql.common.JSONBeanUtil;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.Exchange;
import org.apache.camel.Message;

public final class SqlStartConnectorCustomizer implements ComponentProxyCustomizer {
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
        final List<String> answer = new ArrayList<>();

        final Message in = exchange.getIn();
        if (in.getBody(List.class) != null) {
            @SuppressWarnings("unchecked")
            final List<Map<String, Object>> maps = in.getBody(List.class);

            if (maps != null) {
                for (Map<String, Object> map: maps) {
                    final String bean = JSONBeanUtil.toJSONBean(map);
                    answer.add(bean);
                }
            }
        } else {
            @SuppressWarnings("unchecked")
            final Map<String, Object> singleMap = in.getBody(Map.class);

            if (singleMap != null) {
                final String bean = JSONBeanUtil.toJSONBean(singleMap);
                answer.add(bean);
            }
        }

        in.setBody(answer);
    }
}
