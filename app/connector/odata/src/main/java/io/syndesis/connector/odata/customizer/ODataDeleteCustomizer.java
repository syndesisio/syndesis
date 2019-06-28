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
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.util.ObjectHelper;
import com.fasterxml.jackson.databind.JsonNode;
import io.syndesis.connector.odata.ODataUtil;
import io.syndesis.connector.support.util.ConnectorOptions;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;

public class ODataDeleteCustomizer extends AbstractProducerCustomizer {

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
        }

        in.setBody(EMPTY_STRING);
    }
}
