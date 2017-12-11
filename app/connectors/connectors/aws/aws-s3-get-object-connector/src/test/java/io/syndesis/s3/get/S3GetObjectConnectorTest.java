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
package io.syndesis.s3.get;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.aws.s3.S3Endpoint;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DirtiesContext
@SpringBootApplication
@SpringBootTest(
    classes = {
        S3GetObjectConnectorTest.TestConfiguration.class
    },
    properties = {
        "spring.main.banner-mode = log"
    }
)
public class S3GetObjectConnectorTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(S3GetObjectConnectorTest.class);

    @Autowired
    private CamelContext camelContext;

    @Test
    public void testConfiguration() throws Exception {
        S3Endpoint s3Endpoint = null;

        for (Endpoint endpoint : camelContext.getEndpoints()) {
            LOGGER.debug("instance:" + endpoint.getClass());
            if (endpoint instanceof S3Endpoint) {
            	s3Endpoint = (S3Endpoint)endpoint;
                break;
            }
        }

        String uri = s3Endpoint.getEndpointUri();

        Assert.assertNotNull("No s3Endpoint found", s3Endpoint);
        Assert.assertTrue(uri.startsWith("aws-s3-aws-s3-get-object-connector:") || uri.startsWith("aws-s3-aws-s3-get-object-connector-component:"));
        Assert.assertEquals("test", s3Endpoint.getConfiguration().getBucketName());
        Assert.assertFalse(s3Endpoint.getConfiguration().isDeleteAfterRead());
    }

    // ***********************************
    // Configuration
    // ***********************************

    @Configuration
    public static class TestConfiguration {
        @Bean
        public RouteBuilder routeBuilder() {
            return new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from("aws-s3-get-object-connector:test?amazonS3Client=#amazonS3Client")
                        .to("mock:result");
                }
            };
        }
    }

    public static class S3Configuration {
        @Bean
        AmazonS3ClientMock  amazonS3Client() {
    		return new AmazonS3ClientMock();
        }
    }
}
