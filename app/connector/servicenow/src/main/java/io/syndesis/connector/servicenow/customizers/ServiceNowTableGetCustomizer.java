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
package io.syndesis.connector.servicenow.customizers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.syndesis.common.util.Json;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.servicenow.ServiceNowConstants;
import org.apache.camel.component.servicenow.ServiceNowParams;

public class ServiceNowTableGetCustomizer implements ComponentProxyCustomizer {
    private Integer limit;
    private String query;

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> properties) {
        component.setBeforeProducer(this::beforeProducer);
        component.setAfterProducer(this::afterProducer);
    }

    //
    // we need to set headers with the support of a customizer as there's
    // not yet a way to mark a property as header value.
    //
    // https://github.com/syndesisio/syndesis/issues/2819
    //
    private void beforeProducer(final Exchange exchange) {
        exchange.getIn().setHeader(ServiceNowConstants.RESOURCE, ServiceNowConstants.RESOURCE_TABLE);
        exchange.getIn().setHeader(ServiceNowConstants.ACTION, ServiceNowConstants.ACTION_RETRIEVE);
        exchange.getIn().setHeader(ServiceNowConstants.RESPONSE_MODEL, JsonNode.class);

        // set the maximum number of item that can be include in a page
        //
        // TODO: we need to discuss how to handle pagination.
        if (limit != null) {
            exchange.getIn().setHeader(ServiceNowParams.SYSPARM_LIMIT.getHeader(), limit);
        }

        // set the query used to filter out record sets, the query is
        // expected to be encoded.
        if (query != null) {
            try {
                final String key = ServiceNowParams.SYSPARM_QUERY.getHeader();
                final String val = URLEncoder.encode(query, StandardCharsets.UTF_8.name());

                exchange.getIn().setHeader(key, val);
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private void afterProducer(final Exchange exchange) {
        final Message message = exchange.getIn();
        final Object body = message.getBody();

        if (body == null) {
            message.setBody(Collections.emptyList());
        }

        if  (body instanceof List) {
            final ObjectWriter writer = Json.writer();
            final List<?> elements = List.class.cast(body);
            final List<String> answer = new ArrayList<>(elements.size());

            for (int i = 0; i < elements.size(); i++) {
                try {
                    answer.add(writer.writeValueAsString(elements.get(i)));
                } catch (JsonProcessingException e) {
                    throw new IllegalStateException(e);
                }
            }

            message.setBody(answer);
        } else {
            try {
                final ObjectWriter writer = Json.writer();
                final String answer = writer.writeValueAsString(body);

                message.setBody(Collections.singletonList(answer));
            } catch (JsonProcessingException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
