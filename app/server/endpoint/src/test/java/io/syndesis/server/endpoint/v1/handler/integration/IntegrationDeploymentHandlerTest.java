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

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import io.fabric8.openshift.api.model.DeploymentConfig;
import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.IntegrationDeploymentState;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.endpoint.util.PaginationFilter;
import io.syndesis.server.endpoint.util.ReflectiveSorter;
import io.syndesis.server.endpoint.v1.handler.integration.IntegrationDeploymentHandler.TargetStateRequest;
import io.syndesis.server.endpoint.v1.handler.user.UserConfigurationProperties;
import io.syndesis.server.openshift.OpenShiftService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static io.syndesis.common.model.integration.IntegrationDeployment.compositeId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({IntegrationDeploymentHandler.class})
public class IntegrationDeploymentHandlerTest {

    private static final String INTEGRATION_ID = "integration-id";

    final DataManager dataManager = mock(DataManager.class);
    UserConfigurationProperties properties = new UserConfigurationProperties(0, 1);
    OpenShiftService openShiftService = mock(OpenShiftService.class);

    final IntegrationDeploymentHandler handler = new IntegrationDeploymentHandler(
            dataManager, openShiftService, properties);

    @Test
    public void shouldFetchIntegrationDeployments() {
        final IntegrationDeployment expected = deployment(0);

        when(dataManager.fetch(IntegrationDeployment.class, compositeId(INTEGRATION_ID, 3))).thenReturn(expected);

        final IntegrationDeployment got = handler.get(INTEGRATION_ID, 3);

        assertThat(got).isEqualTo(expected);
    }

    @Test
    public void shouldListDeploymentsOfAnIntegration() {
        final IntegrationDeployment deployment1 = deployment(1);
        final IntegrationDeployment deployment2 = deployment(2);
        final IntegrationDeployment deployment3 = deployment(3);

        @SuppressWarnings("unchecked")
        final ArgumentCaptor<Function<ListResult<IntegrationDeployment>, ListResult<IntegrationDeployment>>> args = ArgumentCaptor
            .forClass(Function.class);
        when(dataManager.fetchAll(eq(IntegrationDeployment.class), args.capture()))
            .thenReturn(ListResult.of(deployment1, deployment2, deployment3));

        final UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getQueryParameters()).thenReturn(new MultivaluedHashMap<>());

        final ListResult<IntegrationDeployment> results = handler.list(INTEGRATION_ID, uriInfo);

        assertThat(results).containsExactly(deployment1, deployment2, deployment3);

        final List<Function<ListResult<IntegrationDeployment>, ListResult<IntegrationDeployment>>> filters = args
            .getAllValues();

        assertThat(filters).hasSize(3);

        assertThat(filters.get(0)).isInstanceOf(IntegrationIdFilter.class)
            .satisfies(f -> assertThat(((IntegrationIdFilter) f).integrationId).isEqualTo(INTEGRATION_ID));
        assertThat(filters.get(1)).isInstanceOf(ReflectiveSorter.class);
        assertThat(filters.get(2)).isInstanceOf(PaginationFilter.class);
    }

    @Test
    public void shouldSetVersionTo1ForInitialUpdate() {
        final SecurityContext security = mock(SecurityContext.class);
        final Principal principal = mock(Principal.class);
        when(security.getUserPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn("user");
        PowerMockito.mockStatic(System.class);
        PowerMockito.when(System.currentTimeMillis()).thenReturn(52l);

        final Integration integration = new Integration.Builder().build();
        when(dataManager.fetch(Integration.class, INTEGRATION_ID)).thenReturn(integration);
        Map<String, String> labels = new HashMap<>();
        List<DeploymentConfig> emptyList = new ArrayList<>();
        when(openShiftService.getDeploymentsByLabel(labels)).thenReturn(emptyList);
        when(dataManager.fetchAllByPropertyValue(IntegrationDeployment.class, "integrationId", INTEGRATION_ID))
            .thenReturn(Stream.empty());

        handler.update(security, INTEGRATION_ID);

        final IntegrationDeployment expected = new IntegrationDeployment.Builder().id(compositeId(INTEGRATION_ID, 1))
            .spec(integration).version(1).userId("user").createdAt(System.currentTimeMillis()).build();

        verify(dataManager).create(expected);
    }

    @Test
    public void shouldUpdateIntegrationDeployments() {
        final SecurityContext security = mock(SecurityContext.class);
        final Principal principal = mock(Principal.class);
        when(security.getUserPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn("user");
        PowerMockito.mockStatic(System.class);
        PowerMockito.when(System.currentTimeMillis()).thenReturn(52l);

        final Integration integration = new Integration.Builder().build();
        when(dataManager.fetch(Integration.class, INTEGRATION_ID)).thenReturn(integration);

        final IntegrationDeployment deployment1 = new IntegrationDeployment.Builder().createFrom(deployment(0))
            .targetState(IntegrationDeploymentState.Unpublished).build();
        final IntegrationDeployment deployment2 = deployment(1);
        final IntegrationDeployment deployment3 = deployment(2);

        when(dataManager.fetchAllByPropertyValue(IntegrationDeployment.class, "integrationId", INTEGRATION_ID))
            .thenReturn(Stream.of(deployment1, deployment2, deployment3));

        handler.update(security, INTEGRATION_ID);

        final IntegrationDeployment expected = new IntegrationDeployment.Builder().id(compositeId(INTEGRATION_ID, 3))
            .spec(integration).version(3).userId("user").createdAt(System.currentTimeMillis()).build();

        verify(dataManager).update(unpublished(1));
        verify(dataManager).update(unpublished(2));
        verify(dataManager).create(expected);
    }

    @Test
    public void shouldUpdateIntegrationDeploymentTargetState() {
        final TargetStateRequest targetState = new TargetStateRequest();
        targetState.setTargetState(IntegrationDeploymentState.Published);

        final IntegrationDeployment existing = deployment(0);

        when(dataManager.fetch(IntegrationDeployment.class, compositeId(INTEGRATION_ID, 3))).thenReturn(existing);

        handler.updateTargetState(INTEGRATION_ID, 3, targetState);

        final IntegrationDeployment expected = new IntegrationDeployment.Builder().createFrom(existing)
            .targetState(IntegrationDeploymentState.Published).build();
        verify(dataManager).update(expected);
    }

    private static IntegrationDeployment deployment(final int version) {
        return new IntegrationDeployment.Builder().spec(new Integration.Builder().build()).version(version).build();
    }

    private static IntegrationDeployment unpublished(final int version) {
        return new IntegrationDeployment.Builder().createFrom(deployment(version))
            .targetState(IntegrationDeploymentState.Unpublished).build();
    }
}
