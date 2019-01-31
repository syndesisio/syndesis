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

import org.apache.camel.EndpointInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.kudu.ColumnSchema;
import org.apache.kudu.Schema;
import org.apache.kudu.Type;
import org.apache.kudu.client.CreateTableOptions;
import org.junit.Before;
import org.junit.Ignore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class KuduProducerTest extends AbstractKuduTest {

    private static final String TABLE = "KuduTestTable";
    private static final String HOST = "quickstart.cloudera";
    private static final String PORT = "7051";

    @EndpointInject(uri = "mock:test")
    MockEndpoint successEndpoint;

    @EndpointInject(uri = "mock:error")
    MockEndpoint errorEndpoint;

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() {

                errorHandler(deadLetterChannel("mock:error").redeliveryDelay(0).maximumRedeliveries(0));

                //integration test route
                from("direct:create")
                        .to("kudu:create?host=" + HOST +
                                "&port=" + PORT +
                                "&tableName=" + TABLE +
                                "&operation=create_table"
                        )
                        .to("mock:test");

                from("direct:scan")
                        .to("kudu:scan?host=" + HOST +
                                "&port=" + PORT +
                                "&tableName=" + TABLE +
                                "&operation=scan"
                        )
                        .to("mock:test");

                from("direct:insert")
                        .to("kudu:insert?host=" + HOST +
                                "&port=" + PORT +
                                "&tableName=" + TABLE +
                                "&operation=insert"
                        )
                         .to("mock:test");

                from("direct:data")
                        .to("kudu:insert?host=" + HOST +
                                "&port=" + PORT +
                                "&tableName=impala::default.syndesis_test" +
                                "&operation=insert"
                        )
                        .to("mock:test");
            }
        };
    }

    @Before
    public void resetEndpoints() {
        errorEndpoint.reset();
        successEndpoint.reset();
    }

    @Ignore
    public void createTable() throws InterruptedException {
        deleteTestTable(TABLE, HOST + ":" + PORT);

        errorEndpoint.expectedMessageCount(0);
        successEndpoint.expectedMessageCount(1);

        final Map<String, Object> headers = new HashMap<>();

        List<ColumnSchema> columns = new ArrayList<>(5);
        final List<String> columnNames = Arrays.asList("id", "title", "name", "lastname", "address");

        for (int i = 0; i < columnNames.size(); i++) {
            Type type = i == 0 ? Type.INT32 : Type.STRING;
            columns.add(
                    new ColumnSchema.ColumnSchemaBuilder(columnNames.get(i), Type.STRING)
                    .key(i == 0)
                    .build()
            );
        }

        List<String> rangeKeys = new ArrayList<>();
        rangeKeys.add("id");

        headers.put("Schema", new Schema(columns));
        headers.put("TableOptions", new CreateTableOptions().setRangePartitionColumns(rangeKeys));

        requestBodyAndHeaders("direct://create", null, headers);

        errorEndpoint.assertIsSatisfied();
        successEndpoint.assertIsSatisfied();
    }

    @Ignore
    public void insertRow() throws InterruptedException {
        deleteTestTable(TABLE, HOST + ":" + PORT);
        createTestTable(TABLE, HOST + ":" + PORT);

        errorEndpoint.expectedMessageCount(0);
        successEndpoint.expectedMessageCount(1);

        // Create a sample row that can be inserted in the test table
        Map<String, Object> row = new HashMap<>();
        row.put("id", 5);
        row.put("title", "Mr.");
        row.put("name", "Samuel");
        row.put("lastname", "Smith");
        row.put("address", "4359  Plainfield Avenue");

        sendBody("direct:insert", row);

        errorEndpoint.assertIsSatisfied();
        successEndpoint.assertIsSatisfied();
    }

    @Ignore
    public void insertRowDifferentData() throws InterruptedException {
        errorEndpoint.expectedMessageCount(0);
        successEndpoint.expectedMessageCount(1);

        Map<String, Object> row = new HashMap<>();
        row.put("id", ThreadLocalRandom.current().nextInt(1, 99));
        row.put("_integer", ThreadLocalRandom.current().nextInt(1, 99));
        row.put("_long", ThreadLocalRandom.current().nextLong(500, 600));
        row.put("_double", ThreadLocalRandom.current().nextDouble(9000, 9999));
        row.put("_float", ThreadLocalRandom.current().nextFloat() * (499 - 100) + 100);

        sendBody("direct:data", row);

        errorEndpoint.assertIsSatisfied();
        successEndpoint.assertIsSatisfied();
    }

    @Ignore
    public void scanTable() throws InterruptedException {
        deleteTestTable(TABLE, HOST + ":" + PORT);
        createTestTable(TABLE, HOST + ":" + PORT);

        errorEndpoint.expectedMessageCount(0);
        successEndpoint.expectedMessageCount(1);

        sendBody("direct:scan", null);

        errorEndpoint.assertIsSatisfied();
        successEndpoint.assertIsSatisfied();
    }
}
