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
package io.syndesis.connector.sql;

import io.syndesis.connector.sql.stored.JSONBeanUtil;
import lombok.Data;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SqlStartConnectorComponentTest {

    private static Connection connection;
    private static Properties properties = new Properties();
    private static SqlCommon sqlCommon;
    private static Map<String, Object> parameters = new HashMap<String, Object>();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        sqlCommon = new SqlCommon();
        connection = sqlCommon.setupConnection(connection, properties);
        for (final String name : properties.stringPropertyNames()) {
            parameters.put(name.substring(name.indexOf(".") + 1), properties.getProperty(name));
        }
    }

    @AfterClass
    public static void afterClass() throws SQLException {
        sqlCommon.closeConnection(connection);
    }

    @Test
    public void camelConnectorTest() throws Exception {

        Statement stmt = connection.createStatement();
        String createTable = "CREATE TABLE NAME (id INTEGER PRIMARY KEY, firstName VARCHAR(255), " + 
                             "lastName VARCHAR(255))"; 
        stmt.executeUpdate(createTable);
        stmt.executeUpdate("INSERT INTO NAME VALUES (1, 'Joe', 'Jackson')");
        stmt.executeUpdate("INSERT INTO NAME VALUES (2, 'Roger', 'Waters')");
        int numberOfRecords = 2;

        CamelContext context = new DefaultCamelContext();

        SqlStartConnectorComponent component = new SqlStartConnectorComponent();
        component.addOption("user", properties.getProperty("sql-connector.user"));
        component.addOption("password", properties.getProperty("sql-connector.password"));
        component.addOption("url", properties.getProperty("sql-connector.url"));

        // bind the component to the camel context
        context.addComponent("sql-start-connector", component);

        CountDownLatch latch = new CountDownLatch(numberOfRecords);
        final Result result = new Result();

        try {
            context.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from("sql-start-connector:SELECT * FROM NAME ORDER BY id?schedulerPeriod=31000")
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange)
                                throws Exception {
                            result.add(exchange.getIn().getBody(String.class));
                            latch.countDown();
                        }
                    }).to("stream:out");
                }
            });
            context.start();
            latch.await(30l,TimeUnit.SECONDS);
            Assert.assertEquals(numberOfRecords, result.getJsonBeans().size());
            Collection<Object> values = new ArrayList<>();
            for (String  jsonBean: result.getJsonBeans()) {
                values.addAll(JSONBeanUtil.parsePropertiesFromJSONBean(jsonBean).values());
            }
            Assert.assertTrue(values.contains("1"));
            Assert.assertTrue(values.contains("2"));
        } finally {
            context.stop();
        }
    }

    @Data
    class Result {
        List<String> jsonBeans = new ArrayList<>();
        
        public void add(String jsonBean) {
            jsonBeans.add(jsonBean);
        }
    }
}
