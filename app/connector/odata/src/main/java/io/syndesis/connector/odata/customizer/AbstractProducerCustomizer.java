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
import org.apache.olingo.commons.api.http.HttpStatusCode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;

public abstract class AbstractProducerCustomizer extends AbstractODataCustomizer {

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        component.setBeforeProducer(this::beforeProducer);
        component.setAfterProducer(this::afterProducer);
    }

    protected abstract void beforeProducer(Exchange exchange) throws IOException;

    protected void afterProducer(Exchange exchange) throws IOException {
        Message in = exchange.getIn();

        JsonNodeFactory factory = JsonNodeFactory.instance;
        ObjectNode statusNode = factory.objectNode();

        int response = 400;
        String info = "No Information";
        HttpStatusCode code = in.getBody(HttpStatusCode.class);
        if (code != null) {
            response = code.getStatusCode();
            info = code.getInfo();
        }

        statusNode.put("Response", response);
        statusNode.put("Information", info);

        String json = OBJECT_MAPPER.writeValueAsString(statusNode);
        in.setBody(json);
    }

}
