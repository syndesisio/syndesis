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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.syndesis.connector.kudu.common.KuduSupport;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultExchange;
import org.apache.kudu.ColumnSchema;
import org.apache.kudu.client.KuduClient;
import org.apache.kudu.client.KuduException;
import org.apache.kudu.client.KuduScanner;
import org.apache.kudu.client.KuduTable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;

public class KuduScanCustomizerTest extends AbstractKuduCustomizerTestSupport {
    private static final String HOST = "quickstart.cloudera";
    private static final String PORT = "7051";
    private KuduClient connection;
    private KuduScanCustomizer customizer;

    @BeforeEach
    public void setupCustomizer() {
        customizer = new KuduScanCustomizer();
        final Map<String, Object> options = new HashMap<>();
        options.put("host", HOST);
        options.put("port", PORT);

        connection = KuduSupport.createConnection(options);
    }

    @AfterEach
    public void shutDown() throws KuduException {
        connection.shutdown();
    }

    @Disabled
    public void testBeforeConsumer() throws Exception {
        final Map<String, Object> options = new HashMap<>();

        customizer.customize(getComponent(), options);

        final KuduTable table = connection.openTable("impala::default.syndesis_todo");

        final List<String> projectColumns = new ArrayList<>(1);
        final Iterator<ColumnSchema> columns = table.getSchema().getColumns().iterator();

        while (columns.hasNext()) {
            projectColumns.add(columns.next().getName());
        }

        final KuduScanner scanner = connection.newScannerBuilder(table)
            .setProjectedColumnNames(projectColumns)
            .build();

        final Exchange inbound = new DefaultExchange(createCamelContext());
        inbound.getIn().setBody(scanner);
        getComponent().getBeforeConsumer().process(inbound);
    }
}
