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
package io.syndesis.server.endpoint.v1.handler.extension;

import java.util.Collections;

import io.syndesis.common.model.Dependency;
import io.syndesis.common.model.Dependency.Type;
import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.extension.Extension;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.IntegrationDeploymentState;
import io.syndesis.common.model.integration.Step;
import io.syndesis.integration.api.IntegrationResourceManager;
import io.syndesis.server.dao.manager.DataManager;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExtensionHandlerTest {

    DataManager data = mock(DataManager.class);

    Extension extension = new Extension.Builder().extensionId("extension-1").build();

    Integration integration = new Integration.Builder().id("integration-1").addFlow(new Flow.Builder()
        .addStep(new Step.Builder().addDependency(new Dependency.Builder().id("extension-1").build()).build()).build())
        .build();

    IntegrationDeployment publishedDeployment = new IntegrationDeployment.Builder().spec(integration)
        .targetState(IntegrationDeploymentState.Published).build();

    IntegrationResourceManager resource = mock(IntegrationResourceManager.class);

    IntegrationDeployment unpublishedDeployment = new IntegrationDeployment.Builder().spec(integration)
        .targetState(IntegrationDeploymentState.Unpublished).build();

    @Test
    public void shouldCountUsedExtensions() {
        final ExtensionHandler handler = new ExtensionHandler(data, null, null, null, resource);

        when(data.fetchAll(Integration.class)).thenReturn(ListResult.of(integration));
        when(data.fetchAll(IntegrationDeployment.class)).thenReturn(ListResult.of(publishedDeployment));
        when(resource.collectDependencies(integration))
            .thenReturn(Collections.singleton(new Dependency.Builder().id("extension-1").type(Type.EXTENSION).build()));

        assertThat(handler.enhance(extension).getUses()).hasValue(1);
    }

    @Test
    public void shouldNotCountUsedExtensionsInDeletedIntegrations() {
        final ExtensionHandler handler = new ExtensionHandler(data, null, null, null, resource);

        when(data.fetchAll(Integration.class))
            .thenReturn(ListResult.of(new Integration.Builder().createFrom(integration).isDeleted(true).build()));
        when(data.fetchAll(IntegrationDeployment.class)).thenReturn(ListResult.of(publishedDeployment));
        when(resource.collectDependencies(integration))
            .thenReturn(Collections.singleton(new Dependency.Builder().id("extension-1").type(Type.EXTENSION).build()));

        assertThat(handler.enhance(extension).getUses()).hasValue(0);
    }

    @Test
    public void shouldNotCountUsedExtensionsOfUnpublishedDeployments() {
        final ExtensionHandler handler = new ExtensionHandler(data, null, null, null, resource);

        when(data.fetchAll(Integration.class)).thenReturn(ListResult.of(integration));
        when(data.fetchAll(IntegrationDeployment.class)).thenReturn(ListResult.of(unpublishedDeployment));
        when(resource.collectDependencies(integration))
            .thenReturn(Collections.singleton(new Dependency.Builder().id("extension-1").type(Type.EXTENSION).build()));

        assertThat(handler.enhance(extension).getUses()).hasValue(0);
    }

    @Test
    public void shouldNotCountUsedExtensionsWhenIntegrationDoesntDependOnIt() {
        final ExtensionHandler handler = new ExtensionHandler(data, null, null, null, resource);

        when(data.fetchAll(Integration.class)).thenReturn(ListResult.of(integration));
        when(data.fetchAll(IntegrationDeployment.class)).thenReturn(ListResult.of(publishedDeployment));
        when(resource.collectDependencies(integration)).thenReturn(Collections.emptyList());

        assertThat(handler.enhance(extension).getUses()).hasValue(0);
    }
}
