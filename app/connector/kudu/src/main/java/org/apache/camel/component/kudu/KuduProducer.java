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
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.util.ObjectHelper;
import org.apache.kudu.ColumnSchema;
import org.apache.kudu.Schema;
import org.apache.kudu.client.CreateTableOptions;
import org.apache.kudu.client.KuduScanner;
import org.apache.kudu.client.PartialRow;
import org.apache.kudu.client.Insert;
import org.apache.kudu.client.KuduClient;
import org.apache.kudu.client.KuduException;
import org.apache.kudu.client.KuduTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The Kudu producer.
 */
public class KuduProducer extends DefaultProducer {

    private static final Logger LOG = LoggerFactory.getLogger(KuduProducer.class);

    private final KuduEndpoint endpoint;
    private final KuduClient connection;

    public KuduProducer(KuduEndpoint endpoint) {
        super(endpoint);

        if (ObjectHelper.isEmpty(endpoint.getKuduClient())) {
            throw new IllegalArgumentException("Can't create a producer when the database connection is null");
        }

        this.connection = endpoint.getKuduClient();
        this.endpoint = endpoint;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        String table = endpoint.getTableName();
        switch (endpoint.getOperation()) {
            case KuduDbOperations.INSERT:
                doInsert(exchange, table);
                break;
            case KuduDbOperations.CREATE_TABLE:
                doCreateTable(exchange, table);
                break;
            case KuduDbOperations.SCAN:
                doScan(exchange, table);
                break;
            default:
                throw new IllegalArgumentException("The operation " + endpoint.getOperation() + " is not supported");
        }
    }

    private void doInsert(Exchange exchange, String tableName) throws KuduException {
        KuduTable table = connection.openTable(tableName);

        Insert insert = table.newInsert();
        PartialRow row = insert.getRow();

        Map<?, ?> rows = exchange.getIn().getBody(Map.class);

        for (Map.Entry<?, ?> entry : rows.entrySet()) {
            String key = (String) entry.getKey();
            Object value = entry.getValue();
            switch (value.getClass().toString()) {
                case "class java.lang.String":
                    row.addString(key, (String) value);
                    break;
                case "class java.lang.Long":
                    row.addLong(key, (long) value);
                    break;
                case "class java.lang.Integer":
                    row.addInt(key, (int) value);
                    break;
                case "class java.lang.Double":
                    row.addDouble(key, (double) value);
                    break;
                case "class java.lang.Float":
                    row.addFloat(key, (float) value);
                    break;
                default:
                    throw new IllegalArgumentException("The type " + value.getClass().toString() + " is not supported");
            }
        }

        connection.newSession().apply(insert);
    }

    private KuduTable doCreateTable(Exchange exchange, String tableName) throws KuduException {
        LOG.debug("Creating table {}", tableName);

        Schema schema = (Schema) exchange.getIn().getHeader("Schema");
        CreateTableOptions builder = (CreateTableOptions) exchange.getIn().getHeader("TableOptions");
        return connection.createTable(tableName, schema, builder);
    }

    private KuduScanner doScan(Exchange exchange, String tableName) throws KuduException {
        KuduTable table = connection.openTable(tableName);

        List<String> projectColumns = new ArrayList<>(1);
        Iterator<ColumnSchema> columns = table.getSchema().getColumns().iterator();

        while (columns.hasNext()) {
            projectColumns.add(columns.next().getName());
        }

        KuduScanner scanner = connection.newScannerBuilder(table)
                .setProjectedColumnNames(projectColumns)
                .build();

        exchange.getIn().setBody(scanner);
        return scanner;
    }
}
