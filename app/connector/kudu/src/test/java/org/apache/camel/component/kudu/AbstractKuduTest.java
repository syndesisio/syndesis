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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.camel.CamelContext;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.spring.SpringCamelContext;
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

public class AbstractKuduTest {

    protected ApplicationContext applicationContext;

    private CamelContext context;

    protected CamelContext createCamelContext() throws Exception {
        applicationContext = new AnnotationConfigApplicationContext(MockedKuduConfiguration.class);

        context = new SpringCamelContext(applicationContext);
        final PropertiesComponent pc = new PropertiesComponent("classpath:test-options.properties");
        context.addComponent("properties", pc);
        return context;
    }

    protected void createTestTable(final String tableName, final String connection) throws KuduException {
        try (KuduClient client = new KuduClient.KuduClientBuilder(connection).build()) {

            final List<ColumnSchema> columns = new ArrayList<>(5);
            final List<String> columnNames = Arrays.asList("id", "title", "name", "lastname", "address");

            for (int i = 0; i < columnNames.size(); i++) {
                final Type type = i == 0 ? Type.INT32 : Type.STRING;
                columns.add(
                    new ColumnSchema.ColumnSchemaBuilder(columnNames.get(i), type)
                        .key(i == 0)
                        .build());
            }

            final List<String> rangeKeys = new ArrayList<>();
            rangeKeys.add("id");

            client.createTable(tableName,
                new Schema(columns),
                new CreateTableOptions().setRangePartitionColumns(rangeKeys));
        }
    }

    protected void insertRowInTestTable(final String tableName, final String connection) throws KuduException {
        try (KuduClient client = new KuduClient.KuduClientBuilder(connection).build()) {

            final KuduTable table = client.openTable(tableName);

            final Insert insert = table.newInsert();
            final PartialRow row = insert.getRow();

            row.addInt("id", ThreadLocalRandom.current().nextInt(1, 99));
            row.addString("title", "Mr.");
            row.addString("name", "Samuel");
            row.addString("lastname", "Smith");
            row.addString("address", "4359  Plainfield Avenue");

            client.newSession().apply(insert);
        }
    }

    protected Object requestBody(final String endpoint, final Object body) {
        return context.createProducerTemplate().requestBody(endpoint, body);
    }

    protected Object requestBodyAndHeaders(final String endpointUri, final Object body, final Map<String, Object> headers) {
        return context.createProducerTemplate().requestBodyAndHeaders(endpointUri, body, headers);
    }

    protected static void deleteTestTable(final String tableName, final String connection) throws KuduException {
        try (KuduClient client = new KuduClient.KuduClientBuilder(connection).build()) {
            client.deleteTable(tableName);
        }
    }
}
