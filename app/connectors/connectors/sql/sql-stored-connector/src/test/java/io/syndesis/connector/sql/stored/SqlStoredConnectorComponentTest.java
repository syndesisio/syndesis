/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.connector.sql.stored;

import io.syndesis.connector.sql.SqlCommon;
import org.apache.camel.CamelContext;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class SqlStoredConnectorComponentTest extends CamelTestSupport {

    private static Connection connection;
    private static Properties properties = new Properties();
    private static SqlCommon sqlCommon;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        sqlCommon = new SqlCommon();
        connection = sqlCommon.setupConnection(connection, properties);
        SqlStoredCommon.setupStoredProcedure(connection, properties);
    }

    @AfterClass
    public static void afterClass() throws SQLException {
        sqlCommon.closeConnection(connection);
    }

    @Test
    public void camelConnectorTest() throws Exception {
        String jsonBody = "{\"a\":20,\"b\":30}";
        String result = template().requestBody("direct:start", jsonBody, String.class);

        Assert.assertEquals("{\"c\":50}", result);
    }

    @Override
    protected RoutesBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                from("direct:start")
                    .to("sql-stored-connector:DEMO_ADD( INTEGER ${body[a]}, INTEGER ${body[b]}, OUT INTEGER c)");
            }
        };
    }

    @Override
    protected CamelContext createCamelContext() throws Exception {
        CamelContext context = super.createCamelContext();

        SqlStoredConnectorComponent component = new SqlStoredConnectorComponent();
        component.addOption("user", properties.getProperty("sql-connector.user"));
        component.addOption("password", properties.getProperty("sql-connector.password"));
        component.addOption("url", properties.getProperty("sql-connector.url"));

        // bind the component to the camel context
        context.addComponent("sql-stored-connector", component);

        return context;
    }
}
