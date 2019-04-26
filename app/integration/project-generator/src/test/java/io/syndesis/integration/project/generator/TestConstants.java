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
package io.syndesis.integration.project.generator;

import java.util.ResourceBundle;

import io.syndesis.common.model.Dependency;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.util.MavenProperties;

final class TestConstants {

    protected static final MavenProperties MAVEN_PROPERTIES;

    protected static final String SYNDESIS_VERSION;
    protected static final String CAMEL_VERSION;
    protected static final ConnectorAction PERIODIC_TIMER_ACTION;
    protected static final Connector TIMER_CONNECTOR;
    protected static final ConnectorAction HTTP_GET_ACTION;
    protected static final ConnectorAction HTTP_POST_ACTION;
    protected static final Connector HTTP_CONNECTOR;
    protected static final ConnectorAction TWITTER_MENTION_ACTION;
    protected static final Connector TWITTER_CONNECTOR;

    static {
        MAVEN_PROPERTIES = new MavenProperties();
        MAVEN_PROPERTIES.addRepository("central", "https://repo.maven.apache.org/maven2/");
        MAVEN_PROPERTIES.addRepository("redhat-ga", "https://maven.repository.redhat.com/ga/");
        MAVEN_PROPERTIES.addRepository("jboss-ea", "https://repository.jboss.org/nexus/content/groups/ea/");

        SYNDESIS_VERSION = ResourceBundle.getBundle("test").getString("syndesis.version");
        CAMEL_VERSION = ResourceBundle.getBundle("test").getString("camel.version");

        PERIODIC_TIMER_ACTION = new ConnectorAction.Builder()
            .id("periodic-timer-action")
            .descriptor(new ConnectorDescriptor.Builder()
                .connectorId("timer")
                .componentScheme("periodic-timer-connector")
                .build())
            .build();

        TIMER_CONNECTOR = new Connector.Builder()
            .id("timer")
            .putProperty(
                "period",
                new ConfigurationProperty.Builder()
                    .kind("property")
                    .secret(false)
                    .componentProperty(false)
                    .build())
            .build();


        HTTP_GET_ACTION = new ConnectorAction.Builder()
            .id("http-get-action")
            .descriptor(new ConnectorDescriptor.Builder()
                .connectorId("http")
                .componentScheme("http-get-connector")
                .build())
            .build();


        HTTP_POST_ACTION = new ConnectorAction.Builder()
            .id("http-post-action")
            .descriptor(new ConnectorDescriptor.Builder()
                .connectorId("http")
                .componentScheme("http-post-connector")
                .build())
            .build();

        HTTP_CONNECTOR = new Connector.Builder()
            .id("http")
            .putProperty(
                "httpUri",
                new ConfigurationProperty.Builder()
                    .kind("property")
                    .secret(false)
                    .componentProperty(false)
                    .build())
            .putProperty(
                "username",
                new ConfigurationProperty.Builder()
                    .kind("property")
                    .secret(true)
                    .componentProperty(false)
                    .build())
            .putProperty(
                "password",
                new ConfigurationProperty.Builder()
                    .kind("property")
                    .secret(true)
                    .build())
            .putProperty(
                "token",
                new ConfigurationProperty.Builder()
                    .kind("property")
                    .secret(true)
                    .componentProperty(false)
                    .build())
            .addAction(HTTP_GET_ACTION)
            .addAction(HTTP_POST_ACTION)
            .build();

        TWITTER_MENTION_ACTION = new ConnectorAction.Builder()
            .id("twitter-mention-action")
            .descriptor(new ConnectorDescriptor.Builder()
                .componentScheme("twitter-timeline")
                .putConfiguredProperty("timelineType", "MENTIONS")
                .putConfiguredProperty("delay", "30000")
                .build())
            .build();

        TWITTER_CONNECTOR = new Connector.Builder()
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
            .addDependency(Dependency.maven("io.syndesis.integration:integration-component-proxy:" + SYNDESIS_VERSION))
            .addDependency(Dependency.maven("org.apache.camel:camel-twitter:" + CAMEL_VERSION))
            .addAction(TWITTER_MENTION_ACTION)
            .build();
    }

    private TestConstants() {
    }
}
