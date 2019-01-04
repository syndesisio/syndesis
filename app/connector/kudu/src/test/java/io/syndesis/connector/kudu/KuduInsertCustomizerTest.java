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

import io.syndesis.connector.kudu.model.KuduInsert;
import org.apache.camel.Exchange;
import org.apache.camel.component.kudu.KuduDbOperations;
import org.apache.camel.impl.DefaultExchange;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class KuduInsertCustomizerTest extends AbstractKuduCustomizerTestSupport {
    private KuduInsertCustomizer customizer;

    @Before
    public void setupCustomizer() {
        customizer = new KuduInsertCustomizer();
    }

    @Test
    public void testBeforeProducerFromOptions() throws Exception {
        Map<String, Object> options = new HashMap<>();
        options.put("row", "Integer,6;String,Mr.;String,Samuel;String,Smith;String,4359  Plainfield Avenue");

        customizer.customize(getComponent(), options);

        Exchange inbound = new DefaultExchange(createCamelContext());
        getComponent().getBeforeProducer().process(inbound);

        Object[] row = (Object[]) inbound.getIn().getMandatoryBody();

        Assert.assertEquals(KuduDbOperations.INSERT, options.get("operation"));
        Assert.assertEquals("The row has the expected elements", 5, row.length);
        Assert.assertEquals("First element of the row is an integer", 6, row[0]);
        Assert.assertEquals("Second element of the row is the title", "Mr.", row[1]);
    }

    @Test
    public void testBeforeProducerFromModel() throws Exception {
        customizer.customize(getComponent(), new HashMap<>());

        KuduInsert model = new KuduInsert();
        Object[] insert =
                {
                        5,
                        "Mr.", "Samuel", "Smith", "4359  Plainfield Avenue"
                };
        model.setRow(insert, true);

        Exchange inbound = new DefaultExchange(createCamelContext());
        inbound.getIn().setBody(model);
        getComponent().getBeforeProducer().process(inbound);

        Object[] row = (Object[]) inbound.getIn().getMandatoryBody();

        Assert.assertEquals("The row has the expected elements", 5, row.length);
        Assert.assertEquals("First element of the row is an integer", 5, row[0]);
        Assert.assertEquals("Second element of the row is the title", "Mr.", row[1]);
    }
}
