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
package io.syndesis.integration.runtime.camelk.logging;

import io.syndesis.integration.runtime.logging.IntegrationLoggingListener;
import org.apache.camel.CamelContext;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultUuidGenerator;
import org.apache.camel.k.InMemoryRegistry;
import org.apache.camel.k.support.RuntimeSupport;
import org.apache.camel.spi.LogListener;
import org.assertj.core.api.Condition;
import org.junit.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class IntegrationLoggingDisabledTest {

    @Test
    public void testDisabledContextConfiguration() throws Exception {
        CamelContext context = new DefaultCamelContext();

        Properties properties = new Properties();

        PropertiesComponent pc = context.getComponent("properties", PropertiesComponent.class);
        pc.setInitialProperties(properties);

        RuntimeSupport.configureContext(context, new InMemoryRegistry());

        context.start();

        assertThat(context.getLogListeners()).have(new Condition<LogListener>() {
            @Override
            public boolean matches(LogListener value) {
                return !(value instanceof IntegrationLoggingListener);
            }
        });

        assertThat(context.getUuidGenerator()).isInstanceOf(DefaultUuidGenerator.class);

        context.stop();
    }

}
