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
package io.syndesis.connector.odata2.customizer;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import io.syndesis.common.util.json.JsonUtils;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.olingo.odata2.api.edm.EdmException;

public class ODataCreateCustomizer extends AbstractProducerCustomizer {

    @Override
    protected void beforeProducer(Exchange exchange) throws IOException {
        Message in = exchange.getIn();

        String json = in.getBody(String.class);
        if (JsonUtils.isJson(json)) {
            JsonNode node = OBJECT_MAPPER.readTree(json);
            Map<String, Object> properties = OBJECT_MAPPER.convertValue(node, new TypeReference<Map<String, Object>>() {});
            in.setBody(properties);
        }
    }

    @Override
    protected void afterProducer(Exchange exchange) throws IOException, EdmException {
        //
        // Exchange should contain a single entity so
        // don't insert into list
        //
        setSplit(true);
        convertMessageToJson(exchange.getIn());
    }
}
