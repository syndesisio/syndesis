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
package io.syndesis.integration.runtime.camelk.jmx;

import org.apache.camel.CamelContext;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.k.Constants;
import org.apache.camel.k.InMemoryRegistry;
import org.apache.camel.k.support.RuntimeSupport;
import org.junit.Test;

import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Properties;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
public class CamelContextMetadataMBeanTest {

    private static final String[] ATTRIBUTES = {
        "StartTimestamp",
        "LastExchangeCompletedTimestamp"
    };

    @Test
    public void testBuilder() throws Exception {
        CamelContext context = new DefaultCamelContext();

        Properties properties = new Properties();
        properties.setProperty(Constants.PROPERTY_CAMEL_K_CUSTOMIZER, "metadata");

        PropertiesComponent pc = context.getComponent("properties", PropertiesComponent.class);
        pc.setInitialProperties(properties);

        RuntimeSupport.configureContext(context, new InMemoryRegistry());

        context.start();

        final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        final Set<ObjectInstance> mBeans = mBeanServer.queryMBeans(ObjectName.getInstance("io.syndesis.camel:*"), null);
        assertThat(mBeans).hasSize(1);

        final ObjectName objectName = mBeans.iterator().next().getObjectName();
        final AttributeList attributes = mBeanServer.getAttributes(objectName, ATTRIBUTES);
        assertThat(attributes.asList()).hasSize(ATTRIBUTES.length);

        context.stop();
    }
}
