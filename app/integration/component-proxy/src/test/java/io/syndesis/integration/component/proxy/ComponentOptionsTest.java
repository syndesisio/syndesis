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
package io.syndesis.integration.component.proxy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.sql.SqlComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert", "PMD.SignatureDeclareThrowsException"})
public class ComponentOptionsTest {
    private DataSource ds;

    @Before
    public void setUp() {
        this.ds = new BasicDataSource();
    }

    // *************************
    // Tests
    // *************************

    @Test
    public void testPojoOption() throws Exception{
        Map<String, Object> properties = new HashMap<>();
        properties.put("dataSource", this.ds);
        properties.put("query", "select from dual");

        ComponentProxyComponent component = new ComponentProxyComponent("my-sql-proxy", "sql");
        component.setOptions(properties);

        SimpleRegistry registry = new SimpleRegistry();
        registry.put(component.getComponentId() + "-component", component);

        DefaultCamelContext context = new DefaultCamelContext(registry);
        try {
            context.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from("direct:start")
                        .id("test-route")
                        .to("my-sql-proxy")
                        .to("mock:result");
                }
            });

            validatePojoOption(context);
            validatePojoOption(context);
        } finally {
            context.stop();
        }
    }

    private void validatePojoOption(CamelContext context) throws Exception {
        context.start();

        Collection<String> names = context.getComponentNames();

        assertThat(names).contains("my-sql-proxy");
        assertThat(names).contains("sql-my-sql-proxy");

        SqlComponent sqlComponent = context.getComponent("sql-my-sql-proxy", SqlComponent.class);
        DataSource sqlDatasource = sqlComponent.getDataSource();

        assertThat(sqlDatasource).isEqualTo(this.ds);

        Map<String, Endpoint> endpoints = context.getEndpointMap();
        assertThat(endpoints).containsKey("sql-my-sql-proxy://select%20from%20dual");

        context.stop();
    }

    @Test
    public void testRegistryOption() throws Exception{
        Map<String, Object> properties = new HashMap<>();
        properties.put("dataSource", "#ds");
        properties.put("query", "select from dual");


        ComponentProxyComponent component = new ComponentProxyComponent("my-sql-proxy", "sql");
        component.setOptions(properties);

        SimpleRegistry registry = new SimpleRegistry();
        registry.put("ds", this.ds);
        registry.put(component.getComponentId() + "-component", component);

        DefaultCamelContext context = new DefaultCamelContext(registry);
        try {
            context.setAutoStartup(false);
            context.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from("direct:start")
                        .to("my-sql-proxy")
                        .to("mock:result");
                }
            });

            validateRegistryOption(context);
            validateRegistryOption(context);
        } finally {
            context.stop();
        }
    }

    private void validateRegistryOption(CamelContext context) throws Exception {
        context.start();

        Collection<String> names = context.getComponentNames();

        assertThat(names).contains("my-sql-proxy");
        assertThat(names).contains("sql");
        assertThat(names).doesNotContain("sql-my-sql-proxy");

        SqlComponent sqlComponent = context.getComponent("sql", SqlComponent.class);
        DataSource sqlDatasource = sqlComponent.getDataSource();

        assertThat(sqlDatasource).isNull();

        Map<String, Endpoint> endpoints = context.getEndpointMap();
        assertThat(endpoints).containsKey("sql://select%20from%20dual?dataSource=%23ds");


        context.stop();
    }
}
