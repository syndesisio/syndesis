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

import io.syndesis.connector.kudu.common.KuduSupport;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultExchange;
import org.apache.kudu.ColumnSchema;
import org.apache.kudu.client.KuduClient;
import org.apache.kudu.client.KuduScanner;
import org.apache.kudu.client.KuduTable;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class KuduScanCustomizerTest extends AbstractKuduCustomizerTestSupport {
    private KuduScanCustomizer customizer;
    private KuduClient connection;
    private static final String TABLE = "impala::default.syndesis_test";
    private static final String HOST = "quickstart.cloudera";
    private static final String PORT = "7051";

    @Before
    public void setupCustomizer() {
        this.customizer = new KuduScanCustomizer();
        Map<String, Object> options = new HashMap<>();
        options.put("host", HOST);
        options.put("port", PORT);

        connection = KuduSupport.createConnection(options);
    }

    @After
    public void shutDown() {
        try {
            connection.shutdown();
        } catch (Exception e) {

        }
    }

    @Ignore
    public void testBeforeConsumer() throws Exception {
        Map<String, Object> options = new HashMap<>();

        customizer.customize(getComponent(), options);

        KuduTable table = connection.openTable("impala::default.syndesis_todo");

        List<String> projectColumns = new ArrayList<>(1);
        Iterator<ColumnSchema> columns = table.getSchema().getColumns().iterator();

        while (columns.hasNext()) {
            projectColumns.add(columns.next().getName());
        }

        KuduScanner scanner = connection.newScannerBuilder(table)
                .setProjectedColumnNames(projectColumns)
                .build();

        Exchange inbound = new DefaultExchange(createCamelContext());
        inbound.getIn().setBody(scanner);
        getComponent().getBeforeConsumer().process(inbound);
    }
}