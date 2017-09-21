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

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.camel.Endpoint;
import org.apache.camel.Message;
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
        super("salesforce-upsert-sobject", SalesforceUpsertSObjectComponent.class.getName());

        // set sObjectId header
        setBeforeProducer(exchange -> {

            // parse input json and extract Id field
            final ObjectMapper mapper = JsonUtils.createObjectMapper();
            final ObjectNode node = (ObjectNode) mapper.readTree(exchange.getIn().getBody(String.class));

            final JsonNode sObjectExternalId = node
                .remove(String.valueOf(getOptions().get(SalesforceEndpointConfig.SOBJECT_EXT_ID_NAME)));
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
                final CreateSObjectResult result = mapper.readValue(out.getBody(String.class),
                    CreateSObjectResult.class);
                out.setBody(result);
            }
        });
    }

    @Override
    protected Endpoint createEndpoint(final String uri, final String remaining, final Map<String, Object> parameters)
        throws Exception {
        parameters.forEach(this::addOption);
        return super.createEndpoint(uri, remaining, parameters);
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