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

import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.kudu.internal.KuduApiCollection;
import org.apache.camel.component.kudu.internal.KuduClientApiMethod;
import org.apache.camel.util.ObjectHelper;
import org.apache.kudu.ColumnSchema;
import org.apache.kudu.Schema;
import org.apache.kudu.Type;
import org.apache.kudu.client.CreateTableOptions;
import org.apache.kudu.client.KuduTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KuduCreateTableCustomizer  implements ComponentProxyCustomizer {
    private String name;
    private Schema schema;
    private CreateTableOptions cto;

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        setApiMethod(options);
        component.setBeforeProducer(this::beforeProducer);
        component.setAfterProducer(this::afterProducer);
    }

    private void setApiMethod(Map<String, Object> options) {
        name = (String) options.get("name");
        schema = toSchema((String) options.get("columns"));
        cto = toCreateTableOptions((String) options.get("table_options_key"),
                (int) options.get("table_options_bucket"));

        options.put("apiName",
                KuduApiCollection.getCollection().getApiName(KuduClientApiMethod.class).getName());
        options.put("methodName", "create");
    }

    private void beforeProducer(Exchange exchange) {
        final Message in = exchange.getIn();
        final KuduTable model = exchange.getIn().getBody(KuduTable.class);

        if (model != null) {
            if (ObjectHelper.isNotEmpty(model.getName())) {
                name = model.getName();
            }
            if (ObjectHelper.isNotEmpty(model.getSchema())) {
                schema = model.getSchema();
            }
        }
        in.setHeader("CamelKudu.name", name);
        in.setHeader("CamelKudu.schema", schema);
        in.setHeader("CamelKudu.builder", cto);
    }

    private void afterProducer(Exchange exchange) {

    }

    private Schema toSchema(String columns) {
        String[] c = columns.split(":", -1);
        List<ColumnSchema> tableColumns = new ArrayList<>(c.length);

        Type t = Type.STRING;
        for (int i = 0; i < c.length; i++ ) {
            String[] column = c[i].split(",", 2);

            switch (column[1]) {
                case "java.lang.String":
                    t = Type.STRING;
                    break;
                case "java.lang.Integer":
                    t = Type.INT32;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid row type " + t);
            }

            tableColumns.add(new ColumnSchema.ColumnSchemaBuilder(column[0], t)
                    .key((i == 0))
                    .build());
        }

        return new Schema(tableColumns);
    }

    private CreateTableOptions toCreateTableOptions(String tableOptionsKey, int tableOptionsBucket) {
        CreateTableOptions builder = new CreateTableOptions();

        List<String> hashKeys = new ArrayList<>(1);
        hashKeys.add(tableOptionsKey);

        builder.addHashPartitions(hashKeys, tableOptionsBucket);
        return builder;
    }
}
