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


import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.connector.support.test.ConnectorTestSupport;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.component.aws.s3.S3Component;
import org.apache.camel.component.aws.s3.S3Configuration;
import org.apache.camel.component.aws.s3.S3Endpoint;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
public class AWSS3RawOptionsTest extends ConnectorTestSupport {
    @Override
    protected List<Step> createSteps() {
        return Arrays.asList(
            new Step.Builder()
                .stepKind(StepKind.endpoint)
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
                .build(),
        new Step.Builder()
            .stepKind(StepKind.endpoint)
            .action(new ConnectorAction.Builder()
                .descriptor(new ConnectorDescriptor.Builder()
                    .componentScheme("mock")
                    .putConfiguredProperty("name", "result")
                    .build())
                .build())
            .build()
        );
    }

    @Override
    protected CamelContext createCamelContext() throws Exception {
        CamelContext camelContext = super.createCamelContext();
        camelContext.setAutoStartup(false);
        camelContext.addComponent("aws-s3", new S3Component() {
            @Override
            protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
                final S3Configuration configuration = new S3Configuration();
                setProperties(configuration, parameters);

                return new S3Endpoint(uri, this, configuration) {
                    @Override
                    public void doStart() throws Exception {
                        // don't let the endpoint to start as it would try to
                        // process the keys
                    }
                };
            }
        });

        return camelContext;
    }

    @Override
    protected Properties useOverridePropertiesWithPropertiesComponent() {
        Properties properties = new Properties();
        properties.setProperty("flow-0.aws-s3-0.accessKey", "my-accessKey");
        properties.setProperty("flow-0.aws-s3-0.secretKey", "my-secretKey");

        return properties;
    }

    @Test
    public void testRawOptions() {
        assertNotNull(context());

        Optional<Endpoint> endpoint = context.getEndpoints().stream().filter(e -> e instanceof S3Endpoint).findFirst();

        Assertions.assertThat(endpoint.isPresent()).isTrue();
        Assertions.assertThat(endpoint.get().getEndpointUri()).isEqualTo("aws-s3://my-bucketNameOrArn?accessKey=RAW(my-accessKey)&region=EU_CENTRAL_1&secretKey=RAW(my-secretKey)");
    }
}
