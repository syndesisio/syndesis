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
package io.syndesis.controllers.integration;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.syndesis.core.KeyGenerator;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.dao.manager.EncryptionComponent;
import io.syndesis.model.action.ConnectorAction;
import io.syndesis.model.action.ConnectorDescriptor;
import io.syndesis.model.connection.ConfigurationProperty;
import io.syndesis.model.connection.Connection;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.integration.Integration;
import io.syndesis.model.integration.IntegrationDeployment;
import io.syndesis.model.integration.IntegrationDeploymentSpec;
import io.syndesis.model.integration.SimpleStep;
import io.syndesis.model.integration.Step;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IntegrationSupportTest {

    private static final ConnectorAction TWITTER_ACTION = new ConnectorAction.Builder()
        .id(KeyGenerator.createKey())
        .descriptor(new ConnectorDescriptor.Builder()
            .connectorId("twitter")
            .camelConnectorPrefix("twitter-search-connector")
            .build())
        .build();

    private static final Connector TWITTER = new Connector.Builder()
        .id("twitter")
        .addAction(TWITTER_ACTION)
        .putProperty(
            "accessToken",
            new ConfigurationProperty.Builder()
                .componentProperty(true)
                .secret(true)
                .build())
        .build();

    private static final ConnectorAction HTTP_ACTION = new ConnectorAction.Builder()
        .id(KeyGenerator.createKey())
        .descriptor(new ConnectorDescriptor.Builder()
            .connectorId("http")
            .camelConnectorPrefix("http-get-connector")
            .build())
        .build();

    private static final Connector HTTP = new Connector.Builder()
        .id("http")
        .addAction(HTTP_ACTION)
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
        .build();

    // ***************************
    // Tests
    // ***************************

    @Test
    public void testBuildApplicationPropertiesForEndpoints() {

        Step s1 = new SimpleStep.Builder()
            .stepKind("endpoint")
            .connection(new Connection.Builder()
                .id(KeyGenerator.createKey())
                .connector(TWITTER)
                .build())
            .putConfiguredProperty("keys", "test")
            .putConfiguredProperty("accessToken", "myAccessToken")
            .action(TWITTER_ACTION)
            .build();
        Step s2 = new SimpleStep.Builder()
            .stepKind("endpoint")
            .connection(new Connection.Builder()
                .id(KeyGenerator.createKey())
                .connector(HTTP)
                .build())
            .putConfiguredProperty("httpUri", "http://localhost:8080/1")
            .putConfiguredProperty("username", "admin1")
            .putConfiguredProperty("password", "password1")
            .putConfiguredProperty("token", "mytoken1")
            .action(HTTP_ACTION)
            .build();
        Step s3 = new SimpleStep.Builder()
            .stepKind("endpoint")
            .connection(new Connection.Builder()
                .id(KeyGenerator.createKey())
                .connector(HTTP)
                .build())
            .putConfiguredProperty("httpUri", "http://localhost:8080/2")
            .putConfiguredProperty("username", "admin2")
            .putConfiguredProperty("password", "password2")
            .putConfiguredProperty("token", "mytoken2")
            .action(HTTP_ACTION)
            .build();

        final EncryptionComponent encryptionComponent = new EncryptionComponent(null);
        final ConcurrentMap<String, Object> resources = new ConcurrentHashMap<>();
        final DataManager dataManager = mock(DataManager.class);

        // mock data manager
        when(dataManager.fetch(anyObject(), anyString())).then(invocation -> {
            final String id = invocation.getArgumentAt(1, String.class);
            final Object resource = resources.get(id);

            return resource;
        });

        Properties properties = IntegrationSupport.buildApplicationProperties(
            newIntegrationRevision(resources, s1, s2, s3),
            dataManager,
            encryptionComponent
        );

        assertThat(properties.size()).isEqualTo(7);

        assertThat(properties.getProperty("twitter-search-connector.configurations.twitter-search-connector-1.accessToken")).isEqualTo("myAccessToken");

        assertThat(properties.getProperty("http-get-connector-2.username")).isEqualTo("admin1");
        assertThat(properties.getProperty("http-get-connector-2.password")).isEqualTo("password1");
        assertThat(properties.getProperty("http-get-connector.configurations.http-get-connector-2.token")).isEqualTo("mytoken1");

        assertThat(properties.getProperty("http-get-connector-3.username")).isEqualTo("admin2");
        assertThat(properties.getProperty("http-get-connector-3.password")).isEqualTo("password2");
        assertThat(properties.getProperty("http-get-connector.configurations.http-get-connector-3.token")).isEqualTo("mytoken2");
    }

    // ***************************
    // Helpers
    // ***************************

    private Integration newIntegration(Map<String, Object> resources, Step... steps) {


        return new Integration.Builder()
            .id("test-integration")
            .name("Test Integration")
            .description("This is a test integration!")
            .build();
    }

    private IntegrationDeployment newIntegrationRevision(Map<String, Object> resources, Step... steps) {
        for (Step step : steps) {
            step.getConnection().ifPresent(
                resource -> resources.put(resource.getId().get(), resource)
            );
            step.getAction().filter(ConnectorAction.class::isInstance).map(ConnectorAction.class::cast).ifPresent(
                resource -> resources.put(resource.getId().get(), resource)
            );
            step.getExtension().ifPresent(
                resource -> resources.put(resource.getId().get(), resource)
            );
        }

        return new IntegrationDeployment.Builder()
            .integrationId("test-integration")
            .name("Test Integration")
            .spec(new IntegrationDeploymentSpec.Builder().steps(Arrays.asList(steps)).build())
            .build();
    }
}
