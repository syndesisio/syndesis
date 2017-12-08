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
package io.syndesis.connector.salesforce;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.camel.component.connector.DefaultConnectorComponent;
import org.apache.camel.component.salesforce.SalesforceEndpointConfig;
import org.apache.camel.component.salesforce.api.SalesforceException;
import org.apache.camel.component.salesforce.api.utils.JsonUtils;

/**
 * Camel salesforce-update-sobject connector
 */
public class SalesforceUpdateSObjectComponent extends DefaultConnectorComponent {

    public SalesforceUpdateSObjectComponent() {
        this(null);
    }

    public SalesforceUpdateSObjectComponent(String componentSchema) {
        super("salesforce-update-sobject", componentSchema, SalesforceUpdateSObjectComponent.class.getName());

        // set sObjectId header
        setBeforeProducer(exchange -> {

            // parse input json and extract Id field
            final ObjectMapper mapper = JsonUtils.createObjectMapper();
            final JsonNode node = mapper.readTree(exchange.getIn().getBody(String.class));

            final JsonNode sObjectId = node.get("Id");
            if (sObjectId == null) {
                exchange.setException(new SalesforceException("Missing field Id", 404));
            } else {
                exchange.getIn().setHeader(SalesforceEndpointConfig.SOBJECT_ID, sObjectId.asText());
            }

            clearBaseFields((ObjectNode) node);

            // update input json
            exchange.getIn().setBody(mapper.writeValueAsString(node));
        });
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
