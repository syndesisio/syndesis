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
package io.syndesis.connector.email;

import org.apache.camel.CamelContext;
import org.apache.camel.component.properties.DefaultPropertiesParser;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.component.properties.PropertiesLocation;
import org.apache.camel.component.properties.PropertiesLookup;
import org.apache.camel.component.properties.PropertiesParser;
import org.apache.camel.spring.SpringCamelContext;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.PropertyResolver;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public abstract class AbstractEmailTest implements EMailConstants {

    protected static final String NO_HOST = "not.a.running.host";
    protected static final String TEST_USER_NAME = "bob";
    protected static final String TEST_PASSWORD = "MyReallySecurePassword";

    @Configuration
    public static class TestConfiguration {
        @Bean
        public PropertiesParser propertiesParser(PropertyResolver propertyResolver) {
            return new DefaultPropertiesParser() {
                @Override
                public String parseProperty(String key, String value, PropertiesLookup properties) {
                    return propertyResolver.getProperty(key);
                }
            };
        }

        @Bean
        public PropertiesComponent properties(PropertiesParser parser) {
            PropertiesComponent pc = new PropertiesComponent();
            pc.setPropertiesParser(parser);
            return pc;
        }
    }

    @Autowired
    private ApplicationContext applicationContext;
    protected CamelContext context;

    /**
     * Creates a camel context complete with a properties component that handles
     * lookups of secret values such as passwords. Fetches the values from external
     * properties file.
     *
     * @return CamelContext
     */
    protected CamelContext createCamelContext() {
        CamelContext ctx = new SpringCamelContext(applicationContext);
        ctx.disableJMX();
        ctx.init();

        PropertiesComponent pc = new PropertiesComponent();
        pc.addLocation(new PropertiesLocation("classpath:mail-test-options.properties"));
        ctx.setPropertiesComponent(pc);
        return ctx;
    }

    @Before
    public void setup() {
        context = createCamelContext();
    }

    @After
    public void tearDown() throws Exception {
        if (context != null) {
            context.stop();
            context = null;
        }
    }
}
