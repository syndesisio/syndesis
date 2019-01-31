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
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.kudu.ColumnSchema;
import org.apache.kudu.client.KuduException;
import org.apache.kudu.client.KuduScanner;
import org.apache.kudu.client.RowResult;
import org.apache.kudu.client.RowResultIterator;
import org.junit.Ignore;

import java.util.List;

public class KuduConsumerTest extends AbstractKuduTest {
    private static final String HOST = "quickstart.cloudera";
    private static final String PORT = "7051";

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                //integration test route
                from("kudu:scan?host=" + HOST +
                                "&port=" + PORT +
                                "&tableName=" + "impala::default.syndesis_test" +
                                "&operation=scan"
                        )
                        .to("mock:result");
            }
        };
    }

    @Ignore
    public void insertRow() throws KuduException, InterruptedException {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);

        assertMockEndpointsSatisfied();

        List<Exchange> exchanges = mock.getReceivedExchanges();
        assertEquals(1, exchanges.size());

        KuduScanner scanner = exchanges.get(0).getIn().getBody(KuduScanner.class);

        RowResultIterator results = scanner.nextRows();
        RowResult result = results.next();

        ColumnSchema columnByIndex = result.getSchema().getColumnByIndex(0);
        String name = columnByIndex.getName();

        assertEquals("id", name);
        assertEquals(46, result.getInt(0));
        assertEquals(10, result.getInt(1));
    }
}