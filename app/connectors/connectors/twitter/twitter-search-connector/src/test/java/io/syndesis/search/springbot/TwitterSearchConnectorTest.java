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
package io.syndesis.search.springbot;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.twitter.search.TwitterSearchEndpoint;
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
        TwitterSearchConnectorTest.TestConfiguration.class
    },
    properties = {
        "spring.main.banner-mode = log"
    }
)
public class TwitterSearchConnectorTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterSearchConnectorTest.class);

    @Autowired
    private CamelContext camelContext;

    @Test
    public void testConfiguration() throws Exception {
        TwitterSearchEndpoint twitterEnpoint = null;

        for (Endpoint endpoint : camelContext.getEndpoints()) {
            LOGGER.debug("instance:" + endpoint.getClass());
            if (endpoint instanceof TwitterSearchEndpoint) {
                twitterEnpoint = (TwitterSearchEndpoint)endpoint;
                break;
            }
        }

        String uri = twitterEnpoint.getEndpointUri();

        Assert.assertNotNull("No TwitterSearchEndpoint found", twitterEnpoint);
        Assert.assertTrue(uri.startsWith("twitter-search-twitter-search-connector:") || uri.startsWith("twitter-search-twitter-search-connector-component:"));
        Assert.assertEquals("camelsearchtest", twitterEnpoint.getKeywords());
        Assert.assertFalse(twitterEnpoint.isFilterOld());
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
                    from("twitter-search-connector:camelsearchtest?filterOld=false")
                        .noAutoStartup()
                        .to("mock:result");
                }
            };
        }
    }
}
