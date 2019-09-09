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
package io.syndesis.server.endpoint.v1.handler.integration;

import com.google.common.collect.Sets;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.action.ActionDescriptor;
import io.syndesis.common.model.action.StepAction;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.IntegrationDeploymentState;
import io.syndesis.common.model.integration.IntegrationOverview;
import io.syndesis.common.model.integration.Step;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.dao.manager.operators.IdPrefixFilter;
import io.syndesis.server.dao.manager.operators.ReverseFilter;
import io.syndesis.server.openshift.ExposureHelper;
import org.junit.Test;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class IntegrationOverviewHelperTest {

    private static final String INTEGRATION_ID = "integration-id";
    private static final String INTEGRATION_DEPLOYMENT_ID = "integration-deployment-id";

    final DataManager dataManager = mock(DataManager.class);
    final ExposureHelper exposueHelper = mock(ExposureHelper.class);

    final IntegrationOverviewHelper handler = new IntegrationOverviewHelper(dataManager, exposueHelper);

    @Test
    public void shouldNotSetManagementUrlIfEmpty() {
        Integration integration = new Integration.Builder().id(INTEGRATION_ID).build();
        IntegrationDeployment deployment = new IntegrationDeployment.Builder().spec(integration).version(3)
            .currentState(IntegrationDeploymentState.Published).id(INTEGRATION_DEPLOYMENT_ID).build();

        when(dataManager.fetchAll(eq(IntegrationDeployment.class),
            any(), any())).thenReturn(ListResult.of(deployment));

        when(exposueHelper.getManagementUrl(any())).thenReturn(null);

        IntegrationOverview integrationOverview = handler.toCurrentIntegrationOverview(integration);

        assertThat(integrationOverview.getManagementUrl()).isEqualTo(Optional.empty());
    }

    @Test
    public void shouldSetManagementUrl() {
        Integration integration = new Integration.Builder().id(INTEGRATION_ID).build();
        IntegrationDeployment deployment = new IntegrationDeployment.Builder().spec(integration).version(3)
            .currentState(IntegrationDeploymentState.Published).id(INTEGRATION_DEPLOYMENT_ID).build();

        when(dataManager.fetchAll(eq(IntegrationDeployment.class),
            any(), any())).thenReturn(ListResult.of(deployment));

        when(exposueHelper.getManagementUrl(any())).thenReturn("https://3scale");

        IntegrationOverview integrationOverview = handler.toCurrentIntegrationOverview(integration);

        assertThat(integrationOverview.getManagementUrl()).isEqualTo(Optional.of("https://3scale"));
    }

    @Test
    public void shouldSetExposureMeansIfExposable() {
        Integration integration = new Integration.Builder().id(INTEGRATION_ID).addFlow(
            new Flow.Builder().addStep(new Step.Builder().action(
                new StepAction.Builder().addTag("expose").build()).build()).build()).build();
        Set<String> exposureMeans = Sets.newHashSet("ROUTE");

        when(dataManager.fetchAll(eq(IntegrationDeployment.class),
            any(), any())).thenReturn(ListResult.of());

        when(exposueHelper.getExposureMeans()).thenReturn(exposureMeans);

        IntegrationOverview integrationOverview = handler.toCurrentIntegrationOverview(integration);

        assertThat(integrationOverview.getExposureMeans()).isEqualTo(exposureMeans);
    }

    @Test
    public void shouldNotSetExposureMeansIfNotExposable() {
        Integration integration = new Integration.Builder().id(INTEGRATION_ID).addFlow(
            new Flow.Builder().addStep(new Step.Builder().action(
                new StepAction.Builder().build()).build()).build()).build();
        Set<String> exposureMeans = Sets.newHashSet("ROUTE");

        when(dataManager.fetchAll(eq(IntegrationDeployment.class),
            any(), any())).thenReturn(ListResult.of());

        when(exposueHelper.getExposureMeans()).thenReturn(exposureMeans);

        IntegrationOverview integrationOverview = handler.toCurrentIntegrationOverview(integration);

        assertThat(integrationOverview.getExposureMeans()).isEqualTo(Collections.emptySet());
    }
}
