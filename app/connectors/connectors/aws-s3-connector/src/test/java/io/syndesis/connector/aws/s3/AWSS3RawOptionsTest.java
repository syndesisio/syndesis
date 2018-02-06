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
package io.syndesis.connector.aws.s3;


import java.io.IOException;
import java.util.Collections;
import java.util.Properties;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Ignore;
import org.junit.Test;

import io.syndesis.integration.runtime.IntegrationRouteBuilder;
import io.syndesis.model.action.ConnectorAction;
import io.syndesis.model.action.ConnectorDescriptor;
import io.syndesis.model.connection.ConfigurationProperty;
import io.syndesis.model.connection.Connection;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.integration.Integration;
import io.syndesis.model.integration.Step;

@Ignore("AWS SDK starts even if route is set to not autostart")
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
public class AWSS3RawOptionsTest extends CamelTestSupport {
    @Override
    protected CamelContext createCamelContext() throws Exception {
        CamelContext camelContext = super.createCamelContext();
        camelContext.setAutoStartup(false);

        return camelContext;
    }

    @Override
    protected Properties useOverridePropertiesWithPropertiesComponent() {
        Properties properties = new Properties();
        properties.setProperty("aws-s3-1.accessKey", "my-accessKey");
        properties.setProperty("aws-s3-1.secretKey", "my-secretKey");

        return properties;
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new IntegrationRouteBuilder("", Collections.emptyList()) {
            @Override
            protected Integration loadIntegration() throws IOException {
                return new Integration.Builder()
                        .id("asw-integration")
                        .name("asw-integration")
                        .addStep(new Step.Builder()
                            .stepKind("endpoint")
                            .connection(new Connection.Builder()
                                .putConfiguredProperty("accessKey", "my-accessKey")
                                .putConfiguredProperty("secretKey", "my-secretKey")
                                .putConfiguredProperty("region", "EU_CENTRAL_1")
                                .putConfiguredProperty("bucketNameOrArn", "my-bucketNameOrArn")
                                .connector(new Connector.Builder()
                                    .putProperty(
                                        "accessKey",
                                        new ConfigurationProperty.Builder()
                                            .kind("accessKey")
                                            .secret(true)
                                            .raw(true)
                                            .componentProperty(false)
                                            .build())
                                    .putProperty(
                                        "secretKey",
                                        new ConfigurationProperty.Builder()
                                            .kind("secretKey")
                                            .secret(true)
                                            .raw(true)
                                            .componentProperty(false)
                                            .build())
                                    .putProperty(
                                        "region",
                                        new ConfigurationProperty.Builder()
                                            .kind("region")
                                            .secret(false)
                                            .componentProperty(false)
                                            .build())
                                    .putProperty(
                                        "bucketNameOrArn",
                                        new ConfigurationProperty.Builder()
                                            .kind("bucketNameOrArn")
                                            .secret(false)
                                            .componentProperty(false)
                                            .build())
                                    .build())
                                .build())
                            .action(new ConnectorAction.Builder()
                                .descriptor(new ConnectorDescriptor.Builder()
                                    .componentScheme("aws-s3")
                                    .build())
                                .build())
                            .build())
                        .addStep(new Step.Builder()
                            .stepKind("endpoint")
                            .action(new ConnectorAction.Builder()
                                .descriptor(new ConnectorDescriptor.Builder()
                                    .componentScheme("mock")
                                    .putConfiguredProperty("name", "result")
                                    .build())
                                .build())
                            .build())
                        .build();
            }
        };
    }

    @Test
    public void testCustomizer() throws Exception {
        assertNotNull(context());
        //Collection<Endpoint> endpoints = context.getEndpoints();
    }
}
