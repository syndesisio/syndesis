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
package io.syndesis.connector.odata.customizer;

import java.io.IOException;
import java.util.Map;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.util.ObjectHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.syndesis.connector.odata.ODataUtil;
import io.syndesis.connector.support.util.ConnectorOptions;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;

public class ODataPatchCustomizer extends AbstractProducerCustomizer {

    private String resourcePath;

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        resourcePath = ConnectorOptions.extractOption(options, RESOURCE_PATH);

        component.setBeforeProducer(this::beforeProducer);
        component.setAfterProducer(this::afterProducer);
    }

    @Override
    protected void beforeProducer(Exchange exchange) throws IOException {
        Message in = exchange.getIn();

        //
        // Expect a json object containing both the keyPredicate and the entity properties
        // to be updated.
        // The keyPredicate has been added to the entity schema in ODataMetaDataRetrieval
        // as a means of specifying which entity to be modified.
        //
        // Upon each message, a new keyPredicate can be provided and will be processed accordingly.
        //
        // Need to find the keyPredicate and add it as a header so the component can find it.
        // Then must remove it from the properties since its not a data property.
        //
        String body = in.getBody(String.class);
        JsonNode node = OBJECT_MAPPER.readTree(body);
        JsonNode keyPredicateNode = node.get(KEY_PREDICATE);

        if (! ObjectHelper.isEmpty(keyPredicateNode)) {
            String keyPredicate = keyPredicateNode.asText();
            //
            // Change the resource path instead as there is a bug in using the
            // keyPredicate header (adds brackets around regardless of a subpredicate
            // being present). When that's fixed we can revert back to using keyPredicate
            // header instead.
            //
            in.setHeader(OLINGO4_PROPERTY_PREFIX + RESOURCE_PATH,
                         resourcePath + ODataUtil.formatKeyPredicate(keyPredicate, true));

            // Remove the key predicate from the body
            ObjectNode objNode = ((ObjectNode) node);
            objNode.remove(KEY_PREDICATE);
            body = OBJECT_MAPPER.writeValueAsString(objNode);
        } else {
            // No key predicate found ... this means trouble!
            throw new CamelExecutionException("No Key Predicate available for OData Patching", exchange);
        }

        if (! ObjectHelper.isEmpty(body)) {
            in.setHeader(OLINGO4_PROPERTY_PREFIX + DATA, body);
        }

        in.setBody(body);
    }
}
