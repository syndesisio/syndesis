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

import io.syndesis.connector.kudu.model.KuduTable;
import io.syndesis.connector.support.util.ConnectorOptions;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.kudu.KuduDbOperations;
import org.apache.camel.util.ObjectHelper;
import org.apache.kudu.ColumnSchema;
import org.apache.kudu.Schema;
import org.apache.kudu.Type;
import org.apache.kudu.client.CreateTableOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KuduCreateTableCustomizer implements ComponentProxyCustomizer {
    private KuduTable.Schema schema;

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        setOptions(options);
        component.setBeforeProducer(this::beforeProducer);
        component.setAfterProducer(this::afterProducer);
    }

    private void setOptions(Map<String, Object> options) {
        if (options == null) {
            return;
        }

        if (!options.isEmpty()) {
            String[] columns = ConnectorOptions.extractOptionAndMap(options, "columns",
                names -> names.split(";", -1), new String[]{});
            KuduTable.ColumnSchema[] columnSchemas = new KuduTable.ColumnSchema[columns.length];

            for (int i = 0; i < columns.length; i++) {
                String[] column = columns[i].split(",", 2);
                columnSchemas[i] = new KuduTable.ColumnSchema(
                        column[1],
                        column[0],
                        i == 0
                );
            }

            schema = new KuduTable.Schema();
            schema.setColumns(columnSchemas, true);
        }

        options.put("operation", KuduDbOperations.CREATE_TABLE);
        options.put("type", KuduDbOperations.CREATE_TABLE);
    }

    private void beforeProducer(Exchange exchange) {
        final Message in = exchange.getIn();
        final KuduTable model = exchange.getIn().getBody(KuduTable.class);

        if (model != null && ObjectHelper.isNotEmpty(model.getSchema())) {
            schema = model.getSchema();
        }

        KuduTable.ColumnSchema[] columnSchema = schema.getColumns();
        List<ColumnSchema> columns = new ArrayList<>(columnSchema.length);
        List<String> rangeKeys = new ArrayList<>();
        for (int i = 0; i < columnSchema.length; i++) {
            if (columnSchema[i].isKey()) {
                rangeKeys.add(columnSchema[i].getName());
            }

            columns.add(
                    new ColumnSchema.ColumnSchemaBuilder(columnSchema[i].getName(), convertType(columnSchema[i].getType()))
                            .key(columnSchema[i].isKey())
                            .build()
            );
        }

        in.setHeader("Schema", new Schema(columns));
        in.setHeader("TableOptions", new CreateTableOptions().setRangePartitionColumns(rangeKeys));
    }

    private void afterProducer(Exchange exchange) {
        final Message in = exchange.getIn();
        final KuduTable table = exchange.getIn().getBody(KuduTable.class);

        KuduTable model = new KuduTable();
        if (ObjectHelper.isNotEmpty(table)) {
            model.setName(table.getName());
            model.setSchema(table.getSchema());
            model.setBuilder(table.getBuilder());
        }

        in.setBody(model);
    }

    private Type convertType(String type) {
        switch (type) {
            case "String":
                return Type.STRING;
            case "Integer":
                return Type.INT32;
            default:
                throw new IllegalArgumentException("The type " + type + " is not supported");
        }
    }
}
