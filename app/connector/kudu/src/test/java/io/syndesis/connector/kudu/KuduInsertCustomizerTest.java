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

package io.syndesis.connector.kudu;

import io.syndesis.common.util.json.JsonUtils;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultExchange;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class KuduInsertCustomizerTest extends AbstractKuduCustomizerTestSupport {
    private KuduInsertCustomizer customizer;

    @BeforeEach
    public void setupCustomizer() {
        customizer = new KuduInsertCustomizer();
    }

    @Test
    public void testBeforeProducerFromModel() throws Exception {
        customizer.customize(getComponent(), new HashMap<>());

        String model = "{" +
                "\"id\": " + 5 + "," +
                "\"title\": \"Mr.\"," +
                "\"name\": \"Samuel\"," +
                "\"lastname\": \"Smith\"," +
                "\"address\": \"4359  Plainfield Avenue\"" +
                "}";

        Exchange inbound = new DefaultExchange(createCamelContext());
        inbound.getIn().setBody(model);
        getComponent().getBeforeProducer().process(inbound);

        Map<?, ?> body = inbound.getIn().getMandatoryBody(Map.class);

        Assertions.assertEquals(5, body.size(), "The row has the expected elements");
        Assertions.assertEquals(5, body.get("id"), "First element of the row is an integer");
        Assertions.assertEquals("Mr.", body.get("title"), "Second element of the row is the title");
    }

    @Test
    public void testAfterProducerFromModel() throws Exception {
        customizer.customize(getComponent(), new HashMap<>());

        String json = "{" +
                "\"id\": " + 5 + "," +
                "\"title\": \"Mr.\"," +
                "\"name\": \"Samuel\"," +
                "\"lastname\": \"Smith\"," +
                "\"address\": \"4359  Plainfield Avenue\"" +
                "}";

        Exchange inbound = new DefaultExchange(createCamelContext());
        inbound.getIn().setBody(json);
        getComponent().getAfterProducer().process(inbound);

        String model = (String) inbound.getIn().getBody();

        Assertions.assertNotNull(model, "Model is not null");

        Map<String, Object> modelMap = JsonUtils.reader().forType(Map.class).readValue(model);

        Assertions.assertEquals(5, modelMap.size(), "Model has all elements");

        Assertions.assertEquals(5, modelMap.get("id"), "First element is the id");
        Assertions.assertEquals("Samuel", modelMap.get("name"), "Third element is the name");
    }
}
