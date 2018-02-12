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
package io.syndesis.integration.runtime.jmx;

import java.util.Set;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import io.syndesis.integration.runtime.IntegrationRouteBuilder;
import io.syndesis.integration.runtime.IntegrationRouteBuilderTest;
import io.syndesis.integration.runtime.IntegrationRuntimeAutoConfiguration;
import org.apache.camel.model.RoutesDefinition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jmx.support.JmxUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {
                IntegrationRuntimeAutoConfiguration.class,
                IntegrationRouteBuilderTest.TestConfiguration.class
        },
        properties = {
                "debug = true",
                "spring.main.banner-mode = off",
                "logging.level.io.syndesis.integration.runtime = DEBUG"
        }
)
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
public class CamelContextMetadataMBeanTest {

    private static final String[] ATTRIBUTES = {"StartTimestamp",
            "LastExchangeCompletedTimestamp"};

    @Autowired
    private IntegrationRouteBuilder routeBuilder;

    @Before
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void beforeTest() throws Exception {
        // initialize routes
        routeBuilder.configure();

        RoutesDefinition routes = routeBuilder.getRouteCollection();

        assertThat(routes.getRoutes()).hasSize(1);

        routeBuilder.getContext().start();
    }

    @Test
    public void testBuilder() throws Exception {
        final MBeanServer mBeanServer = JmxUtils.locateMBeanServer();
        final Set<ObjectInstance> mBeans = mBeanServer.queryMBeans(
                ObjectName.getInstance("io.syndesis.camel:*"), null);
        assertThat(mBeans).hasSize(1);

        final ObjectName objectName = mBeans.iterator().next().getObjectName();
        final AttributeList attributes = mBeanServer.getAttributes(objectName, ATTRIBUTES);
        assertThat(attributes.asList()).hasSize(ATTRIBUTES.length);
    }
}
