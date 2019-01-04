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

import java.util.*;

public class KuduProducerTest extends AbstractKuduTest {

    private static final String TABLE = "CamelTestFolder";
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

                //test route
                /*
                from("direct:test")
                        .to("kudu:kuduClientBean?tableName={{influxdb.testDb}}")
                        .to("mock:test");
                */

                //integration test route
                from("direct:create")
                        .to("kudu?host=" + HOST +
                                "&port=" + PORT +
                                "&tableName=" + TABLE +
                                "&operation=create_table")
                        .to("mock:test");

                from("direct:insert")
                        .to("kudu?host=" + HOST +
                                "&port=" + PORT +
                                "&tableName=" + TABLE)
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
    public void insertRow()  throws InterruptedException {
        deleteTestTable(TABLE, HOST + ":" + PORT);
        createTestTable(TABLE, HOST + ":" + PORT);

        errorEndpoint.expectedMessageCount(0);
        successEndpoint.expectedMessageCount(1);

        // Create a sample row that can be inserted in the test table
        Object[] row =
                {
                        //ThreadLocalRandom.current().nextInt(0, 9999),
                        5,
                        "Mr.", "Samuel", "Smith", "4359  Plainfield Avenue"
                };

        sendBody("direct:insert", row);

        errorEndpoint.assertIsSatisfied();
        successEndpoint.assertIsSatisfied();
    }
}
