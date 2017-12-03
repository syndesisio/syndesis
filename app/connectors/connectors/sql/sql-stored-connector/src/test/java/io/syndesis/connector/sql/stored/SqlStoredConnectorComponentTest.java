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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import io.syndesis.connector.sql.SqlCommon;

public class SqlStoredConnectorComponentTest {

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

        BasicDataSource ds = new BasicDataSource();
        ds.setUsername(properties.getProperty("sql-connector.user"));
        ds.setPassword(properties.getProperty("sql-connector.password"));
        ds.setUrl(     properties.getProperty("sql-connector.url"));

        SimpleRegistry registry = new SimpleRegistry();
        registry.put("dataSource", ds);
        CamelContext context = new DefaultCamelContext(registry);

        String jsonBody = "{\"a\":20,\"b\":30}";
        CountDownLatch latch = new CountDownLatch(1);

        final Result result = new Result();

        try {
            context.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from("timer://myTimer?period=2000")
                    .setBody().constant(jsonBody)
                    .to("sql-stored-connector:DEMO_ADD( INTEGER ${body[a]}, INTEGER ${body[b]}, OUT INTEGER c)")
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange)
                                throws Exception {
                            String jsonBean = (String) exchange.getIn().getBody();
                            result.setResult(jsonBean);
                            latch.countDown();
                        }
                    });
                }
            });
            context.start();
            latch.await(5l,TimeUnit.SECONDS);
            Assert.assertEquals("{\"c\":50}", result.getJsonBean());
        } finally {
            context.stop();
        }
    }

    class Result {
        String jsonBean;

        public String getJsonBean() {
            return jsonBean;
        }
        public void setResult(String jsonBean) {
            this.jsonBean = jsonBean;
        }
    }
}
