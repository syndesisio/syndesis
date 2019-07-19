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
package io.syndesis.connector.salesforce.customizer;

import java.io.IOException;
import java.util.Map;

import io.syndesis.common.util.Json;
import io.syndesis.connector.support.util.ConnectorOptions;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.salesforce.SalesforceEndpointConfig;
import org.apache.camel.component.salesforce.api.SalesforceException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ForUpdateCustomizer implements ComponentProxyCustomizer {

    private String idPropertyName;

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        idPropertyName = ConnectorOptions.extractOption(options, SalesforceEndpointConfig.SOBJECT_EXT_ID_NAME, "Id");

        component.setBeforeProducer(this::beforeProducer);
    }

    public void beforeProducer(final Exchange exchange) throws IOException {
        // parse input json and extract Id field
        final Message in = exchange.getIn();
        final String body = in.getBody(String.class);

        if (body == null) {
            return;
        }

        final ObjectNode node = (ObjectNode) Json.reader().readTree(body);

        final JsonNode idProperty = node.remove(idPropertyName);
        if (idProperty == null) {
            exchange.setException(
                new SalesforceException("Missing option value for Id or " + SalesforceEndpointConfig.SOBJECT_EXT_ID_NAME, 404));

            return;
        }

        final String idValue = idProperty.textValue();
        if ("Id".equals(idPropertyName)) {
            in.setHeader(SalesforceEndpointConfig.SOBJECT_ID, idValue);
        } else {
            in.setHeader(SalesforceEndpointConfig.SOBJECT_EXT_ID_VALUE, idValue);
        }

        // base fields are not allowed to be updated
        clearBaseFields(node);

        // update input json
        in.setBody(Json.writer().writeValueAsString(node));
    }

    private static void clearBaseFields(final ObjectNode node) {
        node.remove("attributes");
        node.remove("Id");
        node.remove("IsDeleted");
        node.remove("CreatedDate");
        node.remove("CreatedById");
        node.remove("LastModifiedDate");
        node.remove("LastModifiedById");
        node.remove("SystemModstamp");
        node.remove("LastActivityDate");
    }
}
