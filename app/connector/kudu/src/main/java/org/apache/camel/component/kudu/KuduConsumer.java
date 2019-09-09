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

package org.apache.camel.component.kudu;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.ScheduledPollConsumer;
import org.apache.kudu.ColumnSchema;
import org.apache.kudu.client.KuduClient;
import org.apache.kudu.client.KuduException;
import org.apache.kudu.client.KuduScanner;
import org.apache.kudu.client.KuduTable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The Kudu consumer.
 */
public class KuduConsumer extends ScheduledPollConsumer {
    private final KuduEndpoint endpoint;
    private final KuduClient connection;

    public KuduConsumer(KuduEndpoint endpoint, Processor processor) {
        super(endpoint, processor);
        this.endpoint = endpoint;
        this.connection = endpoint.getKuduClient();
    }

    @Override
    protected int poll() throws Exception {
        Exchange exchange = endpoint.createExchange();

        // create a message body
        KuduScanner scanner = doScan(endpoint.getTableName());

        exchange.getIn().setBody(scanner);

        try {
            // send message to next processor in the route
            getProcessor().process(exchange);
            return 1; // number of messages polled
        } finally {
            // log exception if an exception occurred and was not handled
            if (exchange.getException() != null) {
                getExceptionHandler().handleException("Error processing exchange", exchange, exchange.getException());
            }
        }
    }

    private KuduScanner doScan(String tableName) throws KuduException {
        KuduTable table = connection.openTable(tableName);

        List<String> projectColumns = new ArrayList<>(1);
        Iterator<ColumnSchema> columns = table.getSchema().getColumns().iterator();

        while (columns.hasNext()) {
            projectColumns.add(columns.next().getName());
        }

        return connection.newScannerBuilder(table)
                .setProjectedColumnNames(projectColumns)
                .build();
    }
}
