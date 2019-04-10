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
package io.syndesis.server.update.controller.usage;

import java.util.Arrays;
import java.util.stream.Collectors;

import io.syndesis.common.model.ChangeEvent;
import io.syndesis.common.model.Dependency;
import io.syndesis.common.model.Dependency.Type;
import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.extension.Extension;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.server.dao.manager.DataManager;

import org.junit.Before;
import org.junit.Test;

import static java.util.Collections.emptyList;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class UsageUpdateHandlerTest {

    private static final ChangeEvent NOT_USED = null;

    private final Connection c1 = newConnectionWithId("c1");

    private final Connection c2 = newConnectionWithId("c2");

    private final Connection c3 = newConnectionWithId("c3");

    private final DataManager dataManager = mock(DataManager.class);

    private final Extension extension = new Extension.Builder().extensionId("extension-1").build();

    private final UsageUpdateHandler handler = new UsageUpdateHandler(dataManager);

    private final Integration integrationWithExtension = new Integration.Builder()
        .id("integration-1")
        .addFlow(new Flow.Builder()
            .addStep(new Step.Builder()
                .addDependency(new Dependency.Builder()
                    .id("extension-1")
                    .type(Type.EXTENSION)
                    .build())
                .build())
            .build())
        .build();

    static class TestIntegrationBuilder extends Integration.Builder {
        TestIntegrationBuilder withFlowConnections(final Connection... connections) {
            return (TestIntegrationBuilder) addFlow(new Flow.Builder().addConnections(connections).build());
        }

        TestIntegrationBuilder withFlowStepsUsingConnections(final Connection... connections) {
            return (TestIntegrationBuilder) addFlow(
                new Flow.Builder().addAllSteps(Arrays.stream(connections).map(c -> new Step.Builder().connection(c).build()).collect(Collectors.toList()))
                    .build());
        }

    }

    @Test
    public void connectionsReferencedFromTheIntegrationShouldHaveTheirUseCounted() {
        final Integration usesC1 = testIntegration().withFlowConnections(c1).build();
        final Integration usesC1andC2 = testIntegration().withFlowConnections(c1, c2).build();
        final Integration usesC2andC3 = testIntegration().withFlowConnections(c2, c3).build();
        final Integration usesC1andC2andC3 = testIntegration().withFlowConnections(c1, c2, c3).build();
        when(dataManager.fetchAll(Integration.class)).thenReturn(ListResult.of(usesC1, usesC1andC2, usesC2andC3, usesC1andC2andC3));

        handler.processInternal(NOT_USED);

        verify(dataManager).fetchAll(Integration.class);
        verify(dataManager).fetchAll(Connection.class);
        verify(dataManager).fetchAll(Extension.class);
        verify(dataManager).update(UsageUpdateHandler.withUpdatedUsage(c1, 3));
        verify(dataManager).update(UsageUpdateHandler.withUpdatedUsage(c2, 3));
        verify(dataManager).update(UsageUpdateHandler.withUpdatedUsage(c3, 2));
        verifyNoMoreInteractions(dataManager);
    }

    @Test
    public void connectionsReferencedFromTheStepsShouldHaveTheirUseCounted() {
        final Integration usesC1 = testIntegration().withFlowStepsUsingConnections(c1).build();
        final Integration usesC1andC2 = testIntegration().withFlowStepsUsingConnections(c1, c2).build();
        final Integration usesC2andC3 = testIntegration().withFlowStepsUsingConnections(c2, c3).build();
        final Integration usesC1andC2andC3 = testIntegration().withFlowStepsUsingConnections(c1, c2, c3).build();
        when(dataManager.fetchAll(Integration.class)).thenReturn(ListResult.of(usesC1, usesC1andC2, usesC2andC3, usesC1andC2andC3));

        handler.processInternal(NOT_USED);

        verify(dataManager).fetchAll(Integration.class);
        verify(dataManager).fetchAll(Connection.class);
        verify(dataManager).fetchAll(Extension.class);
        verify(dataManager).update(UsageUpdateHandler.withUpdatedUsage(c1, 3));
        verify(dataManager).update(UsageUpdateHandler.withUpdatedUsage(c2, 3));
        verify(dataManager).update(UsageUpdateHandler.withUpdatedUsage(c3, 2));
        verifyNoMoreInteractions(dataManager);
    }

    @Test
    public void mixedUseOfConnectionsFromIntegrationsAndStepsShouldBeCounted() {
        final Integration usesC1 = testIntegration().withFlowConnections(c1).build();
        final Integration usesC1andC2 = testIntegration().withFlowConnections(c1).withFlowStepsUsingConnections(c2).build();
        final Integration usesC2andC3 = testIntegration().withFlowStepsUsingConnections(c2, c3).build();
        final Integration usesC1andC2andC3 = testIntegration().withFlowConnections(c1, c2).withFlowStepsUsingConnections(c3).build();
        when(dataManager.fetchAll(Integration.class)).thenReturn(ListResult.of(usesC1, usesC1andC2, usesC2andC3, usesC1andC2andC3));

        handler.processInternal(NOT_USED);

        verify(dataManager).fetchAll(Integration.class);
        verify(dataManager).fetchAll(Connection.class);
        verify(dataManager).fetchAll(Extension.class);
        verify(dataManager).update(UsageUpdateHandler.withUpdatedUsage(c1, 3));
        verify(dataManager).update(UsageUpdateHandler.withUpdatedUsage(c2, 3));
        verify(dataManager).update(UsageUpdateHandler.withUpdatedUsage(c3, 2));
        verifyNoMoreInteractions(dataManager);
    }

    @Before
    public void setupMocks() {
        when(dataManager.fetchAll(Connection.class)).thenReturn(ListResult.of(c1, c2, c3));
        when(dataManager.fetchAll(Extension.class)).thenReturn(ListResult.of(extension));
    }

    @Test
    public void shouldCountUsedExtensions() {
        when(dataManager.fetchAll(Integration.class)).thenReturn(ListResult.of(integrationWithExtension));

        handler.processInternal(NOT_USED);

        verify(dataManager).fetchAll(Integration.class);
        verify(dataManager).fetchAll(Connection.class);
        verify(dataManager).fetchAll(Extension.class);
        verify(dataManager).update(UsageUpdateHandler.withUpdatedUsage(extension, 1));
        verifyNoMoreInteractions(dataManager);
    }

    @Test
    public void shouldNotCountUsedExtensionsInDeletedIntegrations() {

        when(dataManager.fetchAll(Integration.class))
            .thenReturn(ListResult.of(new Integration.Builder().createFrom(integrationWithExtension).isDeleted(true).build()));

        handler.processInternal(NOT_USED);

        verify(dataManager).fetchAll(Integration.class);
        verify(dataManager).fetchAll(Connection.class);
        verify(dataManager).fetchAll(Extension.class);
        verifyNoMoreInteractions(dataManager);
    }

    @Test
    public void someStepsDoNotUseConnectionsAndShouldNotBeConsidered() {
        final Step stepWithoutConnection = new Step.Builder().build();
        final Integration integration = testIntegration().withFlowConnections(c1, c2).withFlowStepsUsingConnections(c1, c3)
            .addFlow(new Flow.Builder().addStep(stepWithoutConnection).build()).build();
        when(dataManager.fetchAll(Integration.class)).thenReturn(ListResult.of(integration));

        handler.processInternal(NOT_USED);

        verify(dataManager).fetchAll(Integration.class);
        verify(dataManager).fetchAll(Connection.class);
        verify(dataManager).fetchAll(Extension.class);
        verify(dataManager).update(UsageUpdateHandler.withUpdatedUsage(c1, 2));
        verify(dataManager).update(UsageUpdateHandler.withUpdatedUsage(c2, 1));
        verify(dataManager).update(UsageUpdateHandler.withUpdatedUsage(c3, 1));
        verifyNoMoreInteractions(dataManager);
    }

    @Test
    public void unusedConnectionsShouldHaveUseOfZero() {
        final Integration emptyIntegration = new Integration.Builder().build();
        when(dataManager.fetchAll(Integration.class)).thenReturn(ListResult.of(emptyIntegration, emptyIntegration));

        handler.processInternal(NOT_USED);

        verify(dataManager).fetchAll(Integration.class);
        verify(dataManager).fetchAll(Connection.class);
        verify(dataManager).fetchAll(Extension.class);
        verifyNoMoreInteractions(dataManager);
    }

    @Test
    public void withNoIntegrationsConnectionUsageShouldBeZero() {
        when(dataManager.fetchAll(Integration.class)).thenReturn(ListResult.of(emptyList()));

        handler.processInternal(NOT_USED);

        verify(dataManager).fetchAll(Integration.class);
        verify(dataManager).fetchAll(Connection.class);
        verify(dataManager).fetchAll(Extension.class);
        verifyNoMoreInteractions(dataManager);
    }

    private static Connection newConnectionWithId(final String id) {
        return new Connection.Builder().id(id).build();
    }

    private static TestIntegrationBuilder testIntegration() {
        return new TestIntegrationBuilder();
    }
}
