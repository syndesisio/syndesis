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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import javax.sql.DataSource;

import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.Endpoint;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.sql.SqlComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.spi.Registry;
import org.apache.commons.dbcp.BasicDataSource;
import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({"PMD.SignatureDeclareThrowsException", "PMD.JUnitTestsShouldIncludeAssert"})
public class ComponentProxyWithCustomComponentTest {
    private DataSource ds;

    @Before
    public void setUp() {
        this.ds = new BasicDataSource();
    }

    // *************************
    // Tests
    // *************************

    @Test
    public void testCreateDelegateComponent() throws Exception{
        Map<String, Object> properties = new HashMap<>();
        properties.put("dataSource", ds);
        properties.put("query", "select from dual");

        ComponentProxyComponent component = new ComponentProxyComponent("my-sql-proxy", "sql") {
            @Override
            protected Optional<Component> createDelegateComponent(ComponentDefinition definition, Map<String, Object> options) throws Exception {
                return Optional.of(new SqlComponent());
            }
        };

        component.setOptions(properties);

        SimpleRegistry registry = new SimpleRegistry();
        registry.put(component.getComponentId() + "-component", component);

        validate(registry);
    }

    @Test
    public void testConfigureDelegateComponent() throws Exception {
        Map<String, Object> properties = new HashMap<>();
        properties.put("dataSource", ds);
        properties.put("query", "select from dual");

        ComponentProxyComponent component = new ComponentProxyComponent("my-sql-proxy", "sql") {
            @Override
            protected void configureDelegateComponent(ComponentDefinition definition, Component component, Map<String, Object> options) throws Exception {
                assertThat(component).isInstanceOf(SqlComponent.class);
                assertThat(options).containsKey("dataSource");
                assertThat(options).hasEntrySatisfying("dataSource", new Condition<Object>() {
                    @Override
                    public boolean matches(Object value) {
                        return value instanceof DataSource;
                    }
                });

                ((SqlComponent)component).setDataSource((DataSource) options.remove("dataSource"));
            }
        };

        component.setOptions(properties);

        SimpleRegistry registry = new SimpleRegistry();
        registry.put(component.getComponentId() + "-component", component);

        validate(registry);
    }

    @Test
    public void testCreateAndConfigureDelegateComponent() throws Exception {
        AtomicReference<Component> componentRef = new AtomicReference<>();

        Map<String, Object> properties = new HashMap<>();
        properties.put("dataSource", ds);
        properties.put("query", "select from dual");

        ComponentProxyComponent component = new ComponentProxyComponent("my-sql-proxy", "sql") {
            @Override
            protected Optional<Component> createDelegateComponent(ComponentDefinition definition, Map<String, Object> options) throws Exception {
                componentRef.set(new SqlComponent());

                return Optional.of(componentRef.get());
            }
            @Override
            protected void configureDelegateComponent(ComponentDefinition definition, Component component, Map<String, Object> options) throws Exception {
                assertThat(component).isSameAs(componentRef.get());
                assertThat(component).isInstanceOf(SqlComponent.class);
                assertThat(options).containsKey("dataSource");
                assertThat(options).hasEntrySatisfying("dataSource", new Condition<Object>() {
                    @Override
                    public boolean matches(Object value) {
                        return value instanceof DataSource;
                    }
                });

                ((SqlComponent)component).setDataSource((DataSource) options.remove("dataSource"));
            }
        };

        component.setOptions(properties);

        SimpleRegistry registry = new SimpleRegistry();
        registry.put(component.getComponentId() + "-component", component);

        validate(registry);
    }

    // *************************
    // Helpers
    // *************************

    private void validate(Registry registry) throws Exception {
        final CamelContext context = new DefaultCamelContext(registry);

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

            context.start();

            Collection<String> names = context.getComponentNames();

            assertThat(names).contains("my-sql-proxy");
            assertThat(names).contains("sql-my-sql-proxy");

            SqlComponent sqlComponent = context.getComponent("sql-my-sql-proxy", SqlComponent.class);
            DataSource sqlDatasource = sqlComponent.getDataSource();

            assertThat(sqlDatasource).isEqualTo(this.ds);

            Map<String, Endpoint> endpoints = context.getEndpointMap();
            assertThat(endpoints).containsKey("sql-my-sql-proxy://select%20from%20dual");
        } finally {
            context.stop();
        }
    }
}
