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
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.validation.Validator;
import javax.ws.rs.core.SecurityContext;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.bulletin.IntegrationBulletinBoard;
import io.syndesis.common.model.filter.FilterOptions;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.IntegrationDeploymentState;
import io.syndesis.server.api.generator.APIGenerator;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.dao.manager.EncryptionComponent;
import io.syndesis.server.inspector.Inspectors;
import io.syndesis.server.openshift.OpenShiftService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
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
                apiGenerator, mock(IntegrationOverviewHelper.class));
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
    public void shouldDeleteIntegrationsAndDeletingAssociatedResources() {
        String id = "to-delete";
        Integration integration = new Integration.Builder().id(id).build();

        when(dataManager.fetch(Integration.class, id)).thenReturn(integration);

        Integration integration1 = new Integration.Builder().createFrom(integration).name("first to delete")
            .build();
        Integration integration2 = new Integration.Builder().createFrom(integration).name("second to delete")
            .build();

        String deploymentId1 = id + "-deploy1";
        IntegrationDeployment deployment1 = new IntegrationDeployment.Builder().spec(integration1).version(1)
            .id(deploymentId1)
            .targetState(IntegrationDeploymentState.Unpublished)
            .stepsDone(Collections.singletonMap("deploy", "something")).build();

        String deploymentId2 = id + "-deploy2";
        IntegrationDeployment deployment2 = new IntegrationDeployment.Builder().spec(integration2).version(2)
            .id(deploymentId2)
            .currentState(IntegrationDeploymentState.Published).targetState(IntegrationDeploymentState.Published)
            .stepsDone(Collections.singletonMap("deploy", "something")).build();

        // Integration bulletin boards identifiers
        Set<String> ibbIds = new HashSet<>();
        for (int i = 1; i <= 2; ++i) {
            ibbIds.add(id + "-ibb" + i);
        }

        when(dataManager.fetchAllByPropertyValue(IntegrationDeployment.class, "integrationId", id))
            .thenReturn(Stream.of(deployment1, deployment2));
        when(dataManager.fetchIdsByPropertyValue(IntegrationBulletinBoard.class, "targetResourceId", id))
            .thenReturn(ibbIds);
        when(dataManager.delete(IntegrationDeployment.class, deploymentId1)).thenReturn(true);
        when(dataManager.delete(IntegrationDeployment.class, deploymentId2)).thenReturn(true);
        for (String ibbId : ibbIds) {
            when(dataManager.delete(IntegrationBulletinBoard.class, ibbId)).thenReturn(true);
        }
        when(dataManager.delete(Integration.class, id)).thenReturn(true);

        handler.delete(id);

        verify(dataManager).delete(Integration.class, id);
        verify(dataManager).delete(IntegrationDeployment.class, deploymentId1);
        verify(dataManager).delete(IntegrationDeployment.class, deploymentId2);
        for (String ibbId : ibbIds) {
            verify(dataManager).delete(IntegrationBulletinBoard.class, ibbId);
        }
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
