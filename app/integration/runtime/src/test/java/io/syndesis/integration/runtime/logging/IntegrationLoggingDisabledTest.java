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
package io.syndesis.integration.runtime.logging;

import io.syndesis.integration.runtime.IntegrationRuntimeAutoConfiguration;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultUuidGenerator;
import org.apache.camel.spi.InterceptStrategy;
import org.apache.camel.spi.LogListener;
import org.apache.camel.spi.RoutePolicyFactory;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.assertj.core.api.Condition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {
        CamelAutoConfiguration.class,
        IntegrationLoggingAutoConfiguration.class,
        IntegrationRuntimeAutoConfiguration.class,
    },
    properties = {
        "spring.main.banner-mode = off",
        "logging.level.io.syndesis.integration.runtime = DEBUG",
        "syndesis.integration.runtime.logging.enabled = false"
    }
)
public class IntegrationLoggingDisabledTest {
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private CamelContext camelContext;

    @Test
    public void testDisabledContextConfiguration() {
        assertThat(applicationContext.getBeansOfType(CamelContextConfiguration.class)).isNullOrEmpty();
        assertThat(camelContext.getLogListeners()).have(new Condition<LogListener>() {
            @Override
            public boolean matches(LogListener value) {
                return !(value instanceof IntegrationLoggingListener);
            }
        });

        assertThat(camelContext.getUuidGenerator()).isInstanceOf(DefaultUuidGenerator.class);
    }

    @Test
    public void testDisabledLoggingInterceptor() {
        assertThat(camelContext.getInterceptStrategies()).have(new Condition<InterceptStrategy>() {
            @Override
            public boolean matches(InterceptStrategy value) {
                return !(value instanceof IntegrationLoggingInterceptStrategy);
            }
        });
    }
    
    @Test
    public void testDisabledLoggingRoutePolicyFactory() {
        assertThat(camelContext.getRoutePolicyFactories()).have(new Condition<RoutePolicyFactory>() {
            @Override
            public boolean matches(RoutePolicyFactory value) {
                return !(value instanceof IntegrationLoggingRoutePolicyFactory);
            }
        });
    }
}
