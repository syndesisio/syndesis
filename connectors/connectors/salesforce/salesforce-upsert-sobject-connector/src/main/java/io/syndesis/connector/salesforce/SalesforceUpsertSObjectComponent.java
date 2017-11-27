/**
 * Copyright (C) 2017 Red Hat, Inc.
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
package io.syndesis.connector.salesforce;

import java.net.URI;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.catalog.URISupport;
import org.apache.camel.component.connector.DefaultConnectorComponent;
import org.apache.camel.component.salesforce.SalesforceEndpointConfig;
import org.apache.camel.component.salesforce.api.SalesforceException;
import org.apache.camel.component.salesforce.api.dto.CreateSObjectResult;
import org.apache.camel.component.salesforce.api.utils.JsonUtils;

/**
 * Camel salesforce-upsert-sobject connector
 */
public class SalesforceUpsertSObjectComponent extends DefaultConnectorComponent {

    public SalesforceUpsertSObjectComponent() {
        this(null);
    }

    public SalesforceUpsertSObjectComponent(String componentSchema) {
        super("salesforce-upsert-sobject", componentSchema, SalesforceUpsertSObjectComponent.class.getName());

        // set sObjectId header
        setBeforeProducer(exchange -> {
            // parse input json and extract Id field
            final ObjectMapper mapper = JsonUtils.createObjectMapper();
            final ObjectNode node = (ObjectNode) mapper.readTree(exchange.getIn().getBody(String.class));

            final URI toEndpointUri = exchange.getProperty(Exchange.TO_ENDPOINT, URI.class);
            final Map<String, Object> parameters = URISupport.parseParameters(toEndpointUri);
            final String externalIdPropertyName = String
                .valueOf(parameters.getOrDefault(SalesforceEndpointConfig.SOBJECT_EXT_ID_NAME,
                    getOptions().get(SalesforceEndpointConfig.SOBJECT_EXT_ID_NAME)));

            final JsonNode sObjectExternalId = node.remove(externalIdPropertyName);
            if (sObjectExternalId == null) {
                exchange.setException(
                    new SalesforceException("Missing option " + SalesforceEndpointConfig.SOBJECT_EXT_ID_NAME, 404));
            } else {
                exchange.getIn().setHeader(SalesforceEndpointConfig.SOBJECT_EXT_ID_VALUE,
                    sObjectExternalId.textValue());

                // base fields are not allowed to be updated
                clearBaseFields(node);

                // update input json
                exchange.getIn().setBody(mapper.writeValueAsString(node));
            }
        });

        setAfterProducer(exchange -> {

            // map json response back to CreateSObjectResult POJO
            final ObjectMapper mapper = JsonUtils.createObjectMapper();
            if (!exchange.isFailed()) {
                final Message out = exchange.getOut();
                final String body = out.getBody(String.class);
                if (body != null) {
                    final CreateSObjectResult result = mapper.readValue(body, CreateSObjectResult.class);
                    out.setBody(result);
                }
            }
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