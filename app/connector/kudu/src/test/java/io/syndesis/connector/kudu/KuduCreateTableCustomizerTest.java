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

import org.apache.camel.Exchange;
import org.apache.camel.component.kudu.internal.KuduApiCollection;
import org.apache.camel.component.kudu.internal.KuduClientApiMethod;
import org.apache.camel.impl.DefaultExchange;
import org.apache.kudu.client.KuduTable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Luis Garcia Acosta
 */
public class KuduCreateTableCustomizerTest extends AbstractKuduCustomizerTestSupport {

    private KuduCreateTableCustomizer customizer;

    @Before
    public void setupCustomizer() {
        customizer = new KuduCreateTableCustomizer();
    }

    @Ignore
    public void testBeforeProducerFromOptions() throws Exception {
        Map<String, Object> options = new HashMap<>();

        options.put("name", "syndesis_test");
        options.put("columns", "key,java.lang.Integer:values,java.lang.String");
        options.put("table_options_key", "key");
        options.put("table_options_bucket", 8);

        customizer.customize(getComponent(), options);

        Exchange inbound = new DefaultExchange(createCamelContext());
        getComponent().getBeforeProducer().process(inbound);

        Assert.assertEquals(KuduApiCollection.getCollection().getApiName(KuduClientApiMethod.class).getName(), options.get("apiName"));
        Assert.assertEquals("create", options.get("methodName"));

        KuduTable table = (KuduTable) inbound.getIn().getBody();
        /*
        Assert.assertEquals("syndesis_test", table.getName());
        Assert.assertNotNull(table.getSchema().getColumn("key"));
        Assert.assertNotNull(table.getSchema().getColumn("value"));
        */
    }

    @Ignore
    public void testBeforeProducerFromModel() throws Exception {

    }
}