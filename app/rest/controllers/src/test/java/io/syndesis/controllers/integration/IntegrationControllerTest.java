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

import java.util.Date;
import java.util.EnumSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import io.syndesis.controllers.integration.StatusChangeHandlerProvider.StatusChangeHandler;
import io.syndesis.controllers.integration.StatusChangeHandlerProvider.StatusChangeHandler.StatusUpdate;
import io.syndesis.core.EventBus;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.model.integration.Integration;
import io.syndesis.model.integration.Integration.Status;
import io.syndesis.model.integration.IntegrationRevision;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IntegrationControllerTest {

    private static final String INTEGRATION_ID = "test-integration";

    @Test
    @SuppressWarnings("PMD.DoNotUseThreads")
    public void shouldReplaceIntegrationRevisions() {
        final DataManager dataManager = mock(DataManager.class);
        final EventBus eventBus = mock(EventBus.class);
        final StatusChangeHandlerProvider handlerFactory = mock(StatusChangeHandlerProvider.class);

        final IntegrationController integrationController = new IntegrationController(dataManager, eventBus,
            handlerFactory);

        integrationController.executor = mock(ExecutorService.class);
        integrationController.scheduler = mock(ScheduledExecutorService.class);

        final StatusChangeHandler handler = mock(StatusChangeHandler.class);
        when(handler.getTriggerStatuses()).thenReturn(EnumSet.allOf(Integration.Status.class));
        when(handler.execute(any(Integration.class))).thenReturn(new StatusUpdate(Status.Pending),
            new StatusUpdate(Status.Pending), new StatusUpdate(Status.Activated));

        final Integration integration = new Integration.Builder().id(INTEGRATION_ID)
            .desiredStatus(Integration.Status.Activated).createdDate(new Date())
            .addRevision(new IntegrationRevision.Builder().version(1).build())
            .addRevision(new IntegrationRevision.Builder().version(2).build()).build();

        final AtomicReference<Integration> currentIntegration = new AtomicReference<>(integration);
        when(dataManager.fetch(Integration.class, INTEGRATION_ID)).thenAnswer(invocation -> currentIntegration.get());

        doAnswer(invocation -> {
            invocation.getArgumentAt(0, Runnable.class).run();
            return null;
        }).when(integrationController.executor).execute(any(Runnable.class));

        final ArgumentCaptor<Integration> updatedIntegrations = ArgumentCaptor.forClass(Integration.class);
        doNothing().when(dataManager).update(updatedIntegrations.capture());

        integrationController.callStatusChangeHandler(handler, INTEGRATION_ID);
        Integration newIntegration = updatedIntegrations.getValue();
        assertThat(newIntegration.getRevisions()).hasSize(3);
        currentIntegration.set(newIntegration);

        integrationController.callStatusChangeHandler(handler, INTEGRATION_ID);
        assertThat(updatedIntegrations.getAllValues().get(1).getRevisions()).hasSize(3);

        // status update is now Activated
        integrationController.callStatusChangeHandler(handler, INTEGRATION_ID);
        assertThat(updatedIntegrations.getAllValues().get(2).getRevisions()).hasSize(4);
    }
}
