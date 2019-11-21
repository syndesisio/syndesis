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
package io.syndesis.integration.runtime.camelk;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

import io.syndesis.integration.runtime.util.SyndesisHeaderStrategy;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.k.Constants;
import org.apache.camel.k.support.RuntimeSupport;
import org.apache.camel.spi.HeaderFilterStrategy;
import org.junit.Test;

public class SyndesisContextCustomizerTest {

    @Test
    public void testSyndesisHeaderStrategy() throws Exception {
        DefaultCamelContext context = new DefaultCamelContext();

        Properties properties = new Properties();
        properties.setProperty(Constants.PROPERTY_CAMEL_K_CUSTOMIZER, "syndesis");

        PropertiesComponent pc  = new PropertiesComponent();
        pc.setInitialProperties(properties);
        context.setPropertiesComponent(pc);

        RuntimeSupport.configureContextCustomizers(context);

        context.start();

        assertThat(context.getRegistry().findByType(HeaderFilterStrategy.class)).hasSize(1);
        assertThat(context.getRegistry().lookupByName("syndesisHeaderStrategy")).isInstanceOf(SyndesisHeaderStrategy.class);

        context.stop();
    }
}
