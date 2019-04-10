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

import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.spring.SpringCamelContext;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.kudu.ColumnSchema;
import org.apache.kudu.Schema;
import org.apache.kudu.Type;
import org.apache.kudu.client.CreateTableOptions;
import org.apache.kudu.client.Insert;
import org.apache.kudu.client.KuduClient;
import org.apache.kudu.client.KuduException;
import org.apache.kudu.client.KuduTable;
import org.apache.kudu.client.PartialRow;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class AbstractKuduTest extends CamelTestSupport {

    protected ApplicationContext applicationContext;

    @Override
    protected CamelContext createCamelContext() throws Exception {
        applicationContext = new AnnotationConfigApplicationContext(MockedKuduConfiguration.class);

        CamelContext ctx = new SpringCamelContext(applicationContext);
        PropertiesComponent pc = new PropertiesComponent("classpath:test-options.properties");
        ctx.addComponent("properties", pc);
        return ctx;
    }

    @SuppressWarnings("unchecked")
    protected Object requestBodyAndHeaders(String endpointUri, Object body, Map<String, Object> headers)
            throws CamelExecutionException {
        return template().requestBodyAndHeaders(endpointUri, body, headers);
    }

    @SuppressWarnings("unchecked")
    protected Object requestBody(String endpoint, Object body) throws CamelExecutionException {
        return template().requestBody(endpoint, body);
    }

    protected void deleteTestTable(String tableName, String connection) {
        KuduClient client = new KuduClient.KuduClientBuilder(connection).build();
        try {
            client.deleteTable(tableName);
        } catch (Exception e) {

        }
    }

    protected void createTestTable(String tableName, String connection) {
        KuduClient client = new KuduClient.KuduClientBuilder(connection).build();

        List<ColumnSchema> columns = new ArrayList<>(5);
        final List<String> columnNames = Arrays.asList("id", "title", "name", "lastname", "address");

        for (int i = 0; i < columnNames.size(); i++) {
            Type type = i == 0 ? Type.INT32 : Type.STRING;
            columns.add(
                    new ColumnSchema.ColumnSchemaBuilder(columnNames.get(i), type)
                            .key(i == 0)
                            .build()
            );
        }

        List<String> rangeKeys = new ArrayList<>();
        rangeKeys.add("id");

        try {
            client.createTable(tableName,
                    new Schema(columns),
                    new CreateTableOptions().setRangePartitionColumns(rangeKeys));
        } catch (Exception e) {

        }
    }

    protected void insertRowInTestTable(String tableName, String connection) {
        KuduClient client = new KuduClient.KuduClientBuilder(connection).build();

        try{
            KuduTable table = client.openTable(tableName);

            Insert insert = table.newInsert();
            PartialRow row = insert.getRow();

            row.addInt("id", ThreadLocalRandom.current().nextInt(1, 99));
            row.addString("title", "Mr.");
            row.addString("name", "Samuel");
            row.addString("lastname", "Smith");
            row.addString("address", "4359  Plainfield Avenue");

            client.newSession().apply(insert);
        } catch (KuduException e) {

        }
    }
}
