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
import org.apache.kudu.Schema;
import org.apache.kudu.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * The Kudu producer.
 */
public class KuduProducer extends DefaultProducer {

    private static final Logger LOG = LoggerFactory.getLogger(KuduProducer.class);

    private KuduEndpoint endpoint;
    private KuduClient connection;

    public KuduProducer(KuduEndpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;

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
            case KuduDbOperations.QUERY:
                doQuery(exchange, table);
                break;
            case KuduDbOperations.CREATE_TABLE:
                doCreateTable(exchange, table);
                break;
            default:
                throw new IllegalArgumentException("The operation " + endpoint.getOperation() + " is not supported");
        }
        System.out.println(exchange.getIn().getBody());
    }

    private void doInsert(Exchange exchange, String tableName) throws Exception {
        KuduTable table = connection.openTable(tableName);
        KuduSession session = connection.newSession();

        Insert insert = table.newInsert();
        PartialRow row = insert.getRow();

        Object[] rows = exchange.getIn().getMandatoryBody(Object[].class);
        LOG.debug("Writing row {}", Arrays.toString(rows));

        for (int i = 0; i < rows.length; i++) {
            Object value = rows[i];
            String a = value.getClass().toString();
            switch (value.getClass().toString()) {
                case "class java.lang.String":
                    row.addString(i, (String) value);
                    break;
                case "class java.lang.Integer":
                    row.addInt(i, (int) value);
                    break;
                default:
                    throw new IllegalArgumentException("The type " + value.getClass().toString() + " is not supported");
            }
        }

        session.apply(insert);
    }

    private KuduTable doCreateTable(Exchange exchange, String tableName) throws Exception {
        LOG.debug("Creating table {}", tableName);

        Schema schema = (Schema) exchange.getIn().getHeader("Schema");
        CreateTableOptions builder = (CreateTableOptions) exchange.getIn().getHeader("TableOptions");
        return connection.createTable(tableName, schema, builder);
    }

    private void doQuery(Exchange exchange, String table) {

    }

}
