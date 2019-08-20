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
package io.syndesis.integration.api;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.extension.Extension;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Scheduler;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.model.openapi.OpenApi;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;

public class IntegrationResourceManagerTest {

    IntegrationResourceManager resourceManager;

    @Before
    public void setup() {
        resourceManager = createResourceManager();
    }

    @Test
    public void testSanitizeConnectors() {
        Integration source = newIntegration(
            new Step.Builder()
                .stepKind(StepKind.endpoint)
                .connection(new Connection.Builder()
                                .id("timer-connection")
                                .connectorId(getTimerConnector().getId().get())
                                .build())
                .putConfiguredProperty("period", "5000")
                .action(getPeriodicTimerAction())
                .build());
        final Optional<Connection> unsanitizedConnection = source.getFlows().get(0).getSteps().get(0).getConnection();

        assertThat(unsanitizedConnection.isPresent()).isTrue();
        assertThat(unsanitizedConnection.get().getConnector().isPresent()).isFalse();

        Integration sanitized = resourceManager.sanitize(source);

        final Optional<Connection> sanitizedConnection = sanitized.getFlows().get(0).getSteps().get(0).getConnection();
        assertThat(sanitizedConnection.isPresent()).isTrue();
        assertThat(sanitizedConnection.get().getConnector().isPresent()).isTrue();
        assertThat(sanitizedConnection.get().getConnector().get()).isEqualTo(getTimerConnector());
    }

    @Test
    public void testSanitizeEmptyFlowIntegrationName() {
        Integration source = new Integration.Builder()
            .id("test-integration")
            .name("_Test-Integration, with a l0t of ?¿ str@nge {hars`!")
            .description("This is a test integration!")
            .build();

        Integration sanitized = resourceManager.sanitize(source);

        assertThat(sanitized.getName()).isEqualTo("test-integration-with-a-l0t-of-strnge-hars");
    }

    @Test
    public void testSanitizeNullIntegrationName() {
        Integration source = new Integration.Builder()
            .id("test-integration")
            .description("This is a test integration!")
            .build();

        Integration sanitized = resourceManager.sanitize(source);

        assertNull(sanitized.getName());
    }

    @Test
    public void testSanitizeVeryLongIntegrationName() {
        Integration source = new Integration.Builder()
            .id("test-integration")
            .name("This is a test integration name that wants to exceed sixtyfour character lenghts... " +
                "not even sure where it will be truncated at, but it will somewhere...")
            .description("This is a test integration!")
            .build();

        Integration sanitized = resourceManager.sanitize(source);

        assertThat(sanitized.getName()).isEqualTo("this-is-a-test-integration-name-that-wants-to-exceed-sixtyfour0");
    }

    @Test
    public void testSanitizeFullFlowIntegrationName() {
        Integration source = newIntegration(
            new Step.Builder()
                .stepKind(StepKind.endpoint)
                .connection(new Connection.Builder()
                    .id("timer-connection")
                    .connectorId(getTimerConnector().getId().get())
                    .build())
                .putConfiguredProperty("period", "5000")
                .action(getPeriodicTimerAction())
                .build());

        Integration sanitized = resourceManager.sanitize(source);

        assertThat(sanitized.getName()).isEqualTo("test-integration");
    }

    private String getSyndesisVersion() {
        return "1.0";
    }

    @Test
    public void testSanitizeScheduler() {
        Integration source = newIntegration(
            new Step.Builder()
                .stepKind(StepKind.endpoint)
                .connection(new Connection.Builder()
                                .id("timer-connection")
                                .connector(getHttpConnector())
                                .build())
                .putConfiguredProperty("schedulerType", "timer")
                .putConfiguredProperty("schedulerExpression", "1s")
                .action(getHttpGetAction())
                .build());

        assertThat(source.getFlows().get(0).getScheduler().isPresent()).isFalse();

        Integration sanitized = resourceManager.sanitize(source);

        final Flow sanitizedFlow = sanitized.getFlows().get(0);
        assertThat(sanitizedFlow.getScheduler().isPresent()).isTrue();
        assertThat(sanitizedFlow.getScheduler().get()).hasFieldOrPropertyWithValue("type", Scheduler.Type.timer);
        assertThat(sanitizedFlow.getScheduler().get()).hasFieldOrPropertyWithValue("expression", "1s");
        assertThat(sanitizedFlow.getSteps().get(0).getStepKind()).isEqualTo(StepKind.endpoint);
        assertThat(sanitizedFlow.getSteps().get(0).getConfiguredProperties()).doesNotContainKey("scheduler-type");
        assertThat(sanitizedFlow.getSteps().get(0).getConfiguredProperties()).doesNotContainKey("scheduler-expression");
    }

    @Test
    public void testSanitizeDefaultScheduler() {
        Integration source = newIntegration(
            new Step.Builder()
                .stepKind(StepKind.endpoint)
                .connection(new Connection.Builder()
                                .id("timer-connection")
                                .connector(getHttpConnector())
                                .build())
                .putConfiguredProperty("schedulerExpression", "1s")
                .action(getHttpGetAction())
                .build()
                                                           );

        assertThat(source.getFlows().get(0).getScheduler().isPresent()).isFalse();

        Integration sanitized = resourceManager.sanitize(source);

        final Flow sanitizedFlow = sanitized.getFlows().get(0);
        assertThat(sanitizedFlow.getScheduler().isPresent()).isTrue();
        assertThat(sanitizedFlow.getScheduler().get()).hasFieldOrPropertyWithValue("type", Scheduler.Type.timer);
        assertThat(sanitizedFlow.getScheduler().get()).hasFieldOrPropertyWithValue("expression", "1s");
        assertThat(sanitizedFlow.getSteps().get(0).getStepKind()).isEqualTo(StepKind.endpoint);
        assertThat(sanitizedFlow.getSteps().get(0).getConfiguredProperties()).doesNotContainKey("scheduler-type");
        assertThat(sanitizedFlow.getSteps().get(0).getConfiguredProperties()).doesNotContainKey("scheduler-expression");
    }


    private Integration newIntegration(Step... steps) {
        return new Integration.Builder()
            .id("test-integration")
            .name("Test Integration")
            .description("This is a test integration!")
            .addFlow(new Flow.Builder()
                         .steps(Arrays.asList(steps))
                         .build())
            .build();
    }

    private Connector getHttpConnector() {
        return new Connector.Builder()
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
            .addAction(getHttpGetAction())
            .addAction(getHttpPostAction())
            .build();

    }

    private ConnectorAction getHttpPostAction() {

        return new ConnectorAction.Builder()
            .id("http-post-action")
            .descriptor(new ConnectorDescriptor.Builder()
                .connectorId("http")
                .componentScheme("http-post-connector")
                .build())
            .build();
    }

    private ConnectorAction getHttpGetAction() {
        return new ConnectorAction.Builder()
            .id("http-get-action")
            .descriptor(new ConnectorDescriptor.Builder()
                .connectorId("http")
                .connectorId("http")
                .componentScheme("http-get-connector")
                .build())
            .build();
    }

    private Action getPeriodicTimerAction() {
        return new ConnectorAction.Builder()
            .id("periodic-timer-action")
            .descriptor(new ConnectorDescriptor.Builder()
                            .connectorId("timer")
                            .componentScheme("periodic-timer-connector")
                            .build())
            .build();
    }

    private Connector getTimerConnector() {
        return new Connector.Builder()
            .id("timer")
            .putProperty(
                "period",
                new ConfigurationProperty.Builder()
                    .kind("property")
                    .secret(false)
                    .componentProperty(false)
                    .build())
            .build();
    }


    private IntegrationResourceManager createResourceManager() {
        return new IntegrationResourceManager() {
            @Override
            public Optional<Connector> loadConnector(String id) {
                if (id.equals("timer")) {
                    return Optional.of(getTimerConnector());
                } else {
                    return Optional.empty();
                }
            }

            @Override
            public Optional<Extension> loadExtension(String id) {
                return Optional.empty();
            }

            @Override
            public List<Extension> loadExtensionsByTag(String tag) {
                return null;
            }

            @Override
            public Optional<InputStream> loadExtensionBLOB(String id) {
                return Optional.empty();
            }

            @Override
            public Optional<OpenApi> loadOpenApiDefinition(String id) {
                return Optional.empty();
            }

            @Override
            public String decrypt(String encrypted) {
                return null;
            }
        };
    }





}
