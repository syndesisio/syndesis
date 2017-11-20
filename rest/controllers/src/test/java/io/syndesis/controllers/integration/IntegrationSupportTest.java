/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.controllers.integration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import io.syndesis.dao.manager.EncryptionComponent;
import io.syndesis.model.action.ConnectorAction;
import io.syndesis.model.action.ConnectorDescriptor;
import io.syndesis.model.connection.ConfigurationProperty;
import io.syndesis.model.connection.Connection;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.integration.Integration;
import io.syndesis.model.integration.SimpleStep;
import io.syndesis.model.integration.Step;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IntegrationSupportTest {

    @Test
    public void testBuildApplicationProperties() {
        Map<String, Connector> connectors = new HashMap<>();
        connectors.put(
            "twitter",
            new Connector.Builder()
                .putProperty(
                    "accessToken",
                    new ConfigurationProperty.Builder()
                        .componentProperty(true)
                        .secret(true)
                        .build())
                .build()
        );
        connectors.put(
            "http",
            new Connector.Builder()
                .putProperty("username",
                    new ConfigurationProperty.Builder()
                        .componentProperty(false)
                        .secret(true)
                        .build())
                .putProperty("password",
                    new ConfigurationProperty.Builder()
                        .componentProperty(false)
                        .secret(true)
                        .build())
                .putProperty("token",
                    new ConfigurationProperty.Builder()
                        .componentProperty(true)
                        .secret(true)
                        .build())
                .build()
        );

        Step s1 = new SimpleStep.Builder()
            .stepKind("endpoint")
            .connection(new Connection.Builder()
                .id("1")
                .build())
            .putConfiguredProperty("keys", "test")
            .putConfiguredProperty("accessToken", "myAccessToken")
            .action(new ConnectorAction.Builder()
                .descriptor(new ConnectorDescriptor.Builder()
                    .connectorId("twitter")
                    .camelConnectorPrefix("twitter-search-connector")
                    .build())
                .build())
            .build();
        Step s2 = new SimpleStep.Builder()
            .stepKind("endpoint")
            .connection(new Connection.Builder()
                .id("2")
                .build())
            .putConfiguredProperty("httpUri", "http://localhost:8080/1")
            .putConfiguredProperty("username", "admin1")
            .putConfiguredProperty("password", "password1")
            .putConfiguredProperty("token", "mytoken1")
            .action(new ConnectorAction.Builder()
                .descriptor(new ConnectorDescriptor.Builder()
                    .connectorId("http")
                    .camelConnectorPrefix("http-get-connector")
                    .build())
                .build())
            .build();
        Step s3 = new SimpleStep.Builder()
            .stepKind("endpoint")
            .connection(new Connection.Builder()
                .id("3")
                .build())
            .putConfiguredProperty("httpUri", "http://localhost:8080/2")
            .putConfiguredProperty("username", "admin2")
            .putConfiguredProperty("password", "password2")
            .putConfiguredProperty("token", "mytoken2")
            .action(new ConnectorAction.Builder()
                .descriptor(new ConnectorDescriptor.Builder()
                    .connectorId("http")
                    .camelConnectorPrefix("http-get-connector")
                    .build())
                .build())
            .build();

        Properties properties = IntegrationSupport.buildApplicationProperties(
            new Integration.Builder()
                .steps(Arrays.asList(s1, s2, s3))
                .build(),
            connectors,
            new EncryptionComponent(null)
        );

        assertThat(properties.size()).isEqualTo(7);

        // There is a single twitter-search-connector so no need to use 'configurations'
        assertThat(properties.getProperty("twitter-search-connector.accessToken")).isEqualTo("myAccessToken");

        // There more than one http-get-connector so component specific parts are
        // set using 'configurations' whereas secret endpoint options are set
        // using the computed prefix
        assertThat(properties.getProperty("http-get-connector-1.username")).isEqualTo("admin1");
        assertThat(properties.getProperty("http-get-connector-1.password")).isEqualTo("password1");
        assertThat(properties.getProperty("http-get-connector.configurations.http-get-connector-1.token")).isEqualTo("mytoken1");

        assertThat(properties.getProperty("http-get-connector-2.username")).isEqualTo("admin2");
        assertThat(properties.getProperty("http-get-connector-2.password")).isEqualTo("password2");
        assertThat(properties.getProperty("http-get-connector.configurations.http-get-connector-2.token")).isEqualTo("mytoken2");
    }
}
