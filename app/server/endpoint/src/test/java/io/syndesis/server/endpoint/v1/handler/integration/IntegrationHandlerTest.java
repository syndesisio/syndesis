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
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import javax.validation.Validator;
import javax.ws.rs.core.SecurityContext;

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.filter.FilterOptions;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.IntegrationDeploymentState;
import io.syndesis.server.api.generator.APIGenerator;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.dao.manager.EncryptionComponent;
import io.syndesis.server.inspector.Inspectors;
import io.syndesis.server.openshift.OpenShiftService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class IntegrationHandlerTest {

    private IntegrationHandler handler;
    private Inspectors inspectors;
    private OpenShiftService openShiftService;
    private APIGenerator apiGenerator;
    private DataManager dataManager;
    private EncryptionComponent encryptionSupport;

    @Test
    public void filterOptionsNoOutputShape() {
        DataShape dataShape = dataShape(DataShapeKinds.NONE);

        FilterOptions options = handler.getFilterOptions(dataShape);
        assertThat(options.getPaths()).isEmpty();
    }

    @Test
    public void filterOptionsSimple() {
        when(inspectors.getPaths(DataShapeKinds.JAVA.toString(), "twitter4j.Status", null, Optional.empty()))
            .thenReturn(Arrays.asList("paramA", "paramB"));
        DataShape dataShape = dataShape(DataShapeKinds.JAVA, "twitter4j.Status");

        FilterOptions options = handler.getFilterOptions(dataShape);
        assertThat(options.getPaths()).hasSize(2).contains("paramA", "paramB");
    }

    @Before
    public void setUp() {
        dataManager = mock(DataManager.class);
        Validator validator = mock(Validator.class);
        openShiftService = mock(OpenShiftService.class);
        inspectors = mock(Inspectors.class);
        apiGenerator = mock(APIGenerator.class);
        when(apiGenerator.updateFlowExcerpts(any(Integration.class))).then(ctx -> ctx.getArguments()[0]);
        encryptionSupport = mock(EncryptionComponent.class);
        handler = new IntegrationHandler(dataManager, openShiftService, validator, inspectors, encryptionSupport,
            apiGenerator);
    }

    @Test
    public void shouldCreateIntegrations() {
        final SecurityContext security = mock(SecurityContext.class);
        final Principal principal = mock(Principal.class);
        when(security.getUserPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn("user");

        final Integration integration = new Integration.Builder().build();
        final Integration created = new Integration.Builder().build();
        final Integration encrypted = new Integration.Builder().addTag("encrypted").build();

        when(encryptionSupport.encrypt(integration)).thenReturn(encrypted);
        final ArgumentCaptor<Integration> persisted = ArgumentCaptor.forClass(Integration.class);
        when(dataManager.create(persisted.capture())).thenReturn(created);

        assertThat(handler.create(security, integration)).isSameAs(created);

        assertThat(persisted.getValue()).isEqualToIgnoringGivenFields(encrypted, "createdAt");

        verify(encryptionSupport).encrypt(same(integration));
    }

    @Test
    public void shouldDeleteIntegrationsUpdatingDeploymentsAndDeletingOpensShiftResources() {
        final Integration integration = new Integration.Builder().id("to-delete").build();

        when(dataManager.fetch(Integration.class, "to-delete")).thenReturn(integration);

        final Integration firstIntegration = new Integration.Builder().createFrom(integration).name("first to delete")
            .build();
        final IntegrationDeployment deployment1 = new IntegrationDeployment.Builder().spec(firstIntegration).version(1)
            .targetState(IntegrationDeploymentState.Unpublished)
            .stepsDone(Collections.singletonMap("deploy", "something")).build();
        final Integration secondIntegration = new Integration.Builder().createFrom(integration).name("second to delete")
            .build();
        final IntegrationDeployment deployment2 = new IntegrationDeployment.Builder().spec(secondIntegration).version(2)
            .currentState(IntegrationDeploymentState.Published).targetState(IntegrationDeploymentState.Published)
            .stepsDone(Collections.singletonMap("deploy", "something")).build();
        when(dataManager.fetchAllByPropertyValue(IntegrationDeployment.class, "integrationId", "to-delete"))
            .thenReturn(Stream.of(deployment1, deployment2));

        final ArgumentCaptor<Integration> updated = ArgumentCaptor.forClass(Integration.class);
        doNothing().when(dataManager).update(updated.capture());

        handler.delete("to-delete");

        assertThat(updated.getValue()).isEqualToIgnoringGivenFields(
            new Integration.Builder().createFrom(integration).isDeleted(true).build(), "updatedAt");

        verify(dataManager).update(new IntegrationDeployment.Builder().spec(firstIntegration).version(1)
            .currentState(IntegrationDeploymentState.Pending).targetState(IntegrationDeploymentState.Unpublished)
            .build().deleted());
        verify(dataManager).update(new IntegrationDeployment.Builder().spec(secondIntegration).version(2)
            .currentState(IntegrationDeploymentState.Published).targetState(IntegrationDeploymentState.Unpublished)
            .build().deleted().deleted());
        verify(openShiftService).delete("first to delete");
        verify(openShiftService).delete("second to delete");
    }

    private static DataShape dataShape(DataShapeKinds kind) {
        return dataShape(kind, null);
    }

    private static DataShape dataShape(DataShapeKinds kind, String type) {
        return new DataShape.Builder().kind(kind).type(type).build();
    }
}
