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
package io.syndesis.integration.runtime.sb.logging;

import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultUuidGenerator;
import org.apache.camel.spi.LogListener;
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

import io.syndesis.integration.runtime.logging.IntegrationLoggingListener;
import io.syndesis.integration.runtime.sb.IntegrationRuntimeAutoConfiguration;
import io.syndesis.integration.runtime.sb.logging.IntegrationLoggingAutoConfiguration;

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
        "debug = false",
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
        assertThat(applicationContext.getBeansOfType(CamelContextConfiguration.class)).is(new Condition<Map<String, CamelContextConfiguration>>() {
            @Override
            public boolean matches(Map<String, CamelContextConfiguration> value) {
                return value.size() == 1 && value.containsKey("integrationContextRuntimeConfiguration");
            }
        });
        assertThat(camelContext.getLogListeners()).have(new Condition<LogListener>() {
            @Override
            public boolean matches(LogListener value) {
                return !(value instanceof IntegrationLoggingListener);
            }
        });

        assertThat(camelContext.getUuidGenerator()).isInstanceOf(DefaultUuidGenerator.class);
    }

}
