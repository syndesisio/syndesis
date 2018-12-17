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

import io.syndesis.connector.kudu.model.KuduModelTable;
import org.apache.camel.component.kudu.internal.KuduApiCollection;
import org.apache.camel.component.kudu.internal.KuduClientApiMethod;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.kudu.client.DeleteTableResponse;
import org.apache.kudu.client.KuduSession;
import org.apache.kudu.client.KuduTable;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class KuduTablesManagerIntegrationTest extends AbstractKuduTestSupport {
    private static final Logger LOG = LoggerFactory.getLogger(KuduTablesManagerIntegrationTest.class);

    // Use the kudu provided VM for testing cases
    private static final String PATH_PREFIX = KuduApiCollection.getCollection().getApiName(KuduClientApiMethod.class).getName();
    private static final String CAMEL_TEST_TABLE = "CamelTestFolder";

    @Ignore
    public void createTable() {
        // Delete the test folder if it is already created
        deleteTestTable(CAMEL_TEST_TABLE);

        final Map<String, Object> headers = new HashMap<>();
        KuduModelTable table = createTestKuduModelTable(CAMEL_TEST_TABLE);

        headers.put("CamelKudu.name", table.getName());
        headers.put("CamelKudu.schema", table.getSchema());
        headers.put("CamelKudu.builder", table.getCto());

        testTable = (KuduTable) requestBodyAndHeaders("direct://CREATETABLE", null, headers);

        assertNotNull("createTable result", testTable);
        assertEquals("createTable result table name", CAMEL_TEST_TABLE, testTable.getName());
    }

    @Ignore
    public void insertRow() {
        // Delete the test folder if it is already created
        createTestTable(CAMEL_TEST_TABLE);

        // Create a sample row that can be inserted in the test table
        final Map<String, Object> row = new HashMap<>();
        row.put("key", 1);
        row.put("value", "Some value");

        final Map<String, Object> headers = new HashMap<>();
        headers.put("CamelKudu.tableName", CAMEL_TEST_TABLE);
        headers.put("CamelKudu.insertRow", row);

        KuduSession session = (KuduSession) requestBodyAndHeaders("direct://INSERTROW", null, headers);
        assertEquals("insertRow result", 0, session.countPendingErrors());
    }

    @Ignore
    public void deleteTable() {
        // Create sample test first so it can be deleted
        createTestTable(CAMEL_TEST_TABLE);

        final Map<String, Object> headers = new HashMap<>();
        headers.put("CamelKudu.name", CAMEL_TEST_TABLE);

        DeleteTableResponse res = (DeleteTableResponse) requestBodyAndHeaders("direct://DELETETABLE", null, headers);
        assertNotNull("deleteTable result", res);
    }

    @Override
    protected RoutesBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // test route for creating a table
                from("direct://CREATETABLE")
                        .to("kudu://" + PATH_PREFIX + "/create");

                // test route for deleting a table
                from("direct://DELETETABLE")
                        .to("kudu://" + PATH_PREFIX + "/delete");

            }
        };
    }
}