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
package io.syndesis.integration.runtime.handlers;

import java.util.Properties;

import io.syndesis.common.model.Dependency;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.integration.component.proxy.ComponentProxyEndpoint;

import org.apache.camel.Endpoint;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.component.twitter.timeline.TwitterTimelineEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Test;

import static io.syndesis.integration.runtime.IntegrationTestSupport.dumpRoutes;
import static io.syndesis.integration.runtime.IntegrationTestSupport.newIntegrationRouteBuilder;

import static org.assertj.core.api.Assertions.assertThat;

public class ConnectorStepHandlerTest {
    private static final ConnectorAction TWITTER_MENTION_ACTION = new ConnectorAction.Builder()
        .id("twitter-mention-action")
        .descriptor(new ConnectorDescriptor.Builder()
            .componentScheme("twitter-timeline")
            .putConfiguredProperty("timelineType", "MENTIONS")
            .build())
        .build();

    private static final Connector TWITTER_CONNECTOR = new Connector.Builder()
        .id("twitter")
        .putProperty(
            "accessToken",
            new ConfigurationProperty.Builder()
                .kind("property")
                .secret(true)
                .componentProperty(true)
                .build())
        .putProperty(
            "accessTokenSecret",
            new ConfigurationProperty.Builder()
                .kind("property")
                .secret(true)
                .build())
        .putProperty(
            "consumerKey",
            new ConfigurationProperty.Builder()
                .kind("property")
                .secret(true)
                .build())
        .putProperty(
            "consumerSecret",
            new ConfigurationProperty.Builder()
                .kind("property")
                .secret(true)
                .build())
        .componentScheme("twitter")
        .addDependency(Dependency.maven("io.syndesis:integration-component-proxy:latest"))
        .addDependency(Dependency.maven("org.apache.camel:camel-twitter:latest"))
        .addAction(TWITTER_MENTION_ACTION)
        .build();


    @Test
    public void testConnectorStepHandler() throws Exception {
        final DefaultCamelContext context = new DefaultCamelContext();

        PropertiesComponent propertiesComponent = new PropertiesComponent();

        Properties extra = new Properties();
        extra.setProperty("flow-0.twitter-timeline-0.accessToken", "at");
        extra.setProperty("flow-0.twitter-timeline-0.accessTokenSecret", "ats");
        extra.setProperty("flow-0.twitter-timeline-0.consumerKey", "ck");
        extra.setProperty("flow-0.twitter-timeline-0.consumerSecret", "cs");
        propertiesComponent.setOverrideProperties(extra);

        propertiesComponent.setInitialProperties(extra);
        context.setPropertiesComponent(propertiesComponent);

        try {
            final RouteBuilder routes = newIntegrationRouteBuilder(
                new Step.Builder()
                    .stepKind(StepKind.endpoint)
                    .action(TWITTER_MENTION_ACTION)
                    .connection(
                        new Connection.Builder()
                            .connector(TWITTER_CONNECTOR)
                            .putConfiguredProperty("accessToken", "at")
                            .putConfiguredProperty("accessTokenSecret", "ats")
                            .putConfiguredProperty("consumerKey", "ck")
                            .putConfiguredProperty("consumerSecret", "cs")
                            .build())
                    .putConfiguredProperty("delay", "1234")
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

            // Set up the camel context
            context.addRoutes(routes);
            context.setAutoStartup(false);
            context.start();

            assertThat(context.getComponentNames()).contains("twitter-timeline");
            assertThat(context.getComponentNames()).doesNotContain("twitter-timeline-twitter-timeline-0-1");

            for (Endpoint endpoint: context.getEndpoints()) {
                if (endpoint instanceof TwitterTimelineEndpoint) {
                    assertThat(endpoint.getEndpointUri()).isEqualTo(
                        "twitter-timeline://MENTIONS?accessToken=at&accessTokenSecret=ats&consumerKey=ck&consumerSecret=cs&delay=1234"
                    );
                }
                if (endpoint instanceof ComponentProxyEndpoint) {
                    assertThat(endpoint.getEndpointUri()).isEqualTo(
                        "twitter-timeline-0-0"
                    );
                }
            }

            // Dump routes as XML for troubleshooting
            dumpRoutes(context);
        } finally {
            context.stop();
        }
    }
}
