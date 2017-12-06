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
package io.syndesis.connector.sql;

import io.syndesis.connector.sql.SqlParam.SqlSampleValue;
import io.syndesis.connector.sql.stored.JSONBeanUtil;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SqlConnectorComponentTest {

    private static Connection connection;
    private static Properties properties = new Properties();
    private static SqlCommon sqlCommon;
    private static String schema;
    private static Map<String, Object> parameters = new HashMap<>();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        sqlCommon = new SqlCommon();
        connection = sqlCommon.setupConnection(connection, properties);
        for (final String name : properties.stringPropertyNames()) {
            parameters.put(name.substring(name.indexOf(".") + 1), properties.getProperty(name));
        }
        schema = DatabaseMetaDataHelper.getDefaultSchema(
                connection.getMetaData().getDatabaseProductName(), parameters.get("user").toString());
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
        //stmt.executeUpdate("INSERT INTO NAME VALUES (2, 'Roger', 'Waters')");


        SimpleRegistry registry = new SimpleRegistry();
        registry.put("query", "myquery");

        CamelContext context = new DefaultCamelContext(registry);

        SqlConnectorComponent component = new SqlConnectorComponent();
        component.addOption("user", properties.getProperty("sql-connector.user"));
        component.addOption("password", properties.getProperty("sql-connector.password"));
        component.addOption("url", properties.getProperty("sql-connector.url"));

        // bind the component to the camel context
        context.addComponent("sql-connector", component);

        CountDownLatch latch = new CountDownLatch(1);
        final Result result = new Result();

        try {
            context.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from("sql-connector:SELECT * FROM NAME ORDER BY id")
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange)
                                throws Exception {
                            result.setResult(exchange.getIn().getBody(String.class));
                            latch.countDown();
                        }
                    }).to("stream:out");
                }
            });
            context.start();
            latch.await(5l,TimeUnit.SECONDS);
            Assert.assertEquals("{ID=1, FIRSTNAME=Joe, LASTNAME=Jackson}", result.getJsonBean());
        } finally {
            context.stop();
        }
    }

    @Test
    public void camelConnectorInputParamTest() throws Exception {

        Statement stmt = connection.createStatement();

        String createTable = "CREATE TABLE ALLTYPES (charType CHAR, varcharType VARCHAR(255), " +
                "numericType NUMERIC, decimalType DECIMAL, smallType SMALLINT," +
                "dateType DATE, timeType TIME )";
        stmt.executeUpdate(createTable);

        CamelContext context = new DefaultCamelContext();

        SqlConnectorComponent component = new SqlConnectorComponent();
        component.addOption("user", properties.getProperty("sql-connector.user"));
        component.addOption("password", properties.getProperty("sql-connector.password"));
        component.addOption("url", properties.getProperty("sql-connector.url"));

        // bind the component to the camel context
        context.addComponent("sql-connector", component);

        CountDownLatch latch = new CountDownLatch(1);

        final Result result = new Result();

        Map<String,Object> values = new HashMap<>();
        values.put("CHARVALUE", SqlSampleValue.charValue);
        values.put("VARCHARVALUE", SqlSampleValue.stringValue);
        values.put("NUMERICVALUE", SqlSampleValue.decimalValue);
        values.put("DECIMALVALUE", SqlSampleValue.decimalValue);
        values.put("SMALLINTVALUE", SqlSampleValue.integerValue);

        String jsonBody = JSONBeanUtil.toJSONBean(values);

        final String insertStatement = "INSERT INTO ALLTYPES "
                + "(charType, varcharType, numericType, decimalType, smallType) VALUES "
                + "(:#CHARVALUE, :#VARCHARVALUE, :#NUMERICVALUE, :#DECIMALVALUE, :#SMALLINTVALUE)";
        SqlStatementParser parser = new SqlStatementParser(connection, schema, insertStatement);

        try {
            context.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from("timer://myTimer?period=2000")
                    .setBody().constant(jsonBody)
                    .to("sql-connector:" + insertStatement)
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange)
                                throws Exception {
                            result.setResult(exchange.getIn().getBody(String.class));
                            latch.countDown();
                        }
                    }).to("stream:out");
                }
            });
            context.start();
            latch.await(5l,TimeUnit.SECONDS);
            Properties props = JSONBeanUtil.parsePropertiesFromJSONBean(result.getJsonBean());
            Assert.assertEquals(values.size(),props.size());
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
