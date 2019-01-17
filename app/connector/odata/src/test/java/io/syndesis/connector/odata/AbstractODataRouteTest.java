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
package io.syndesis.connector.odata;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.camel.component.properties.DefaultPropertiesParser;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.component.properties.PropertiesParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.PropertyResolver;
import io.syndesis.common.model.Dependency;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.integration.runtime.IntegrationTestSupport;

public abstract class AbstractODataRouteTest extends IntegrationTestSupport implements ODataConstants {

    @Autowired
    protected ApplicationContext applicationContext;

    @Configuration
    public static class TestConfiguration {
        @Bean
        public PropertiesParser propertiesParser(PropertyResolver propertyResolver) {
            return new DefaultPropertiesParser() {
                @Override
                public String parseProperty(String key, String value, Properties properties) {
                    return propertyResolver.getProperty(key);
                }
            };
        }

        @Bean(destroyMethod = "")
        public PropertiesComponent properties(PropertiesParser parser) {
            PropertiesComponent pc = new PropertiesComponent();
            pc.setPropertiesParser(parser);
            return pc;
        }
    }

    protected Connector createODataConnector(PropertyBuilder<String> configurePropBuilder,
                                                                                       PropertyBuilder<ConfigurationProperty> propBuilder) {
        Connector.Builder builder = new Connector.Builder()
            .id("odata")
            .name("OData")
            .componentScheme("olingo4")
            .description("Communicate with an OData service")
            .addDependency(Dependency.maven("org.apache.camel:camel-olingo4:latest"));

        if (configurePropBuilder != null) {
            builder.configuredProperties(configurePropBuilder.build());
        }

        if (propBuilder != null) {
            builder.properties(propBuilder.build());
        }

        return builder.build();
    }

    protected Connector createODataConnector(PropertyBuilder<String> configurePropBuilder) {
        return createODataConnector(configurePropBuilder, null);
    }

    protected Integration createIntegration(Step odataStep, Step mockStep) {
        Integration odataIntegration = new Integration.Builder()
            .id("i-LTS2tYXwF8odCm87k6gz")
            .name("MyODataInt")
            .addTag("log", "odata")
            .addFlow(new Flow.Builder()
                     .addStep(odataStep)
                     .addStep(mockStep)
                     .build())
            .build();
        return odataIntegration;
    }

    protected String testData(String fileName) throws IOException {
        InputStream in = this.getClass().getResourceAsStream(fileName);
        String expected = streamToString(in);
        return expected;
    }

    public AbstractODataRouteTest() {
        super();
    }

}
