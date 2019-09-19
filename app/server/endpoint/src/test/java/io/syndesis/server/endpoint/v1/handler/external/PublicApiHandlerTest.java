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
package io.syndesis.server.endpoint.v1.handler.external;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;

import org.junit.Before;
import org.junit.Test;

import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.WithResourceId;
import io.syndesis.common.model.environment.Environment;
import io.syndesis.common.model.integration.ContinuousDeliveryEnvironment;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.IntegrationDeploymentState;
import io.syndesis.common.model.integration.IntegrationOverview;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.dao.manager.EncryptionComponent;
import io.syndesis.server.endpoint.monitoring.MonitoringProvider;
import io.syndesis.server.endpoint.v1.handler.connection.ConnectionHandler;
import io.syndesis.server.endpoint.v1.handler.environment.EnvironmentHandler;
import io.syndesis.server.endpoint.v1.handler.integration.IntegrationDeploymentHandler;
import io.syndesis.server.endpoint.v1.handler.integration.IntegrationHandler;
import io.syndesis.server.endpoint.v1.handler.integration.support.IntegrationSupportHandler;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link PublicApiHandler}.
 */
public class PublicApiHandlerTest {

    private static final String INTEGRATION_ID = "integration-id";
    private static final String INTEGRATION_NAME = "integration-name";
    private static final String ENVIRONMENT = "environment";
    private static final String ENVIRONMENT_ID = ENVIRONMENT + "-id";
    private static final String ENVIRONMENT2 = "new-" + ENVIRONMENT;
    private static final String ENVIRONMENT2_ID = ENVIRONMENT2 + "-id";
    private static final String NAME_PROPERTY = "name";
    private static final String INTEGRATION_ID_PROPERTY = "integrationId";
    private static final String RENAMED_SUFFIX = "-renamed";

    private final DataManager dataManager = mock(DataManager.class);
    private final IntegrationSupportHandler supportHandler = mock(IntegrationSupportHandler.class);
    private final EncryptionComponent encryptionComponent = mock(EncryptionComponent.class);
    private final IntegrationHandler integrationHandler = mock(IntegrationHandler.class);
    private final IntegrationDeploymentHandler deploymentHandler = mock(IntegrationDeploymentHandler.class);
    private final ConnectionHandler connectionHandler = mock(ConnectionHandler.class);
    private final MonitoringProvider monitoringProvider = mock(MonitoringProvider.class);

    // initialized after mock objects in setup
    private PublicApiHandler handler;
    private Integration integration;
    private IntegrationDeploymentState targetState;
    private EnvironmentHandler environmentHandler;
    private List<Environment> environments;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        // prime mock objects
        final HashMap<String, ContinuousDeliveryEnvironment> deliveryState = new HashMap<>();
        deliveryState.put(ENVIRONMENT_ID, ContinuousDeliveryEnvironment.Builder.createFrom(ENVIRONMENT_ID, new Date()));
        integration = new Integration.Builder()
                .id(INTEGRATION_ID)
                .name(INTEGRATION_NAME)
                .continuousDeliveryState(deliveryState)
                .build();

        // handle dataManager calls for environments
        environments = new ArrayList<>();
        environments.add(new Environment.Builder().id(ENVIRONMENT_ID).name(ENVIRONMENT).build());
        doAnswer(invocation -> ListResult.of(environments)).when(dataManager).fetchAll(Environment.class);
        doAnswer(invocation -> {
            final Environment env = invocation.getArgument(0);
            environments.removeIf(e -> e.getId().get().equals(env.getId().get()));
            environments.add(env);
            return env;
        }).when(dataManager).update(any(Environment.class));
        doAnswer(invocation -> {
            final String id = invocation.getArgument(1);
            return environments.stream().filter(e -> e.getId().get().equals(id)).findFirst().get();
        }).when(dataManager).fetch(eq(Environment.class), any());
        doAnswer(invocation -> {
            final String name = invocation.getArgument(2);
            return environments.stream().filter(e -> e.getName().equals(name)).findFirst();
        }).when(dataManager).fetchByPropertyValue(eq(Environment.class), eq(NAME_PROPERTY), any());
        doAnswer(invocation -> {
            final Environment env = invocation.getArgument(0);
            final Environment newEnv = new Environment.Builder().createFrom(env).id(ENVIRONMENT2_ID).build();
            environments.add(newEnv);
            return newEnv;
        }).when(dataManager).create(any(Environment.class));
        doAnswer(invocation -> {
            final String id = invocation.getArgument(1);
            return environments.removeIf(e -> e.getId().get().equals(id));
        }).when(dataManager).delete(eq(Environment.class), any());

        // handle dataManager calls for integrations
        doAnswer(invocation -> integration).when(dataManager).fetch(Integration.class, INTEGRATION_ID);
        doAnswer(invocation -> Optional.of(integration)).when(dataManager).fetchByPropertyValue(Integration.class,
                NAME_PROPERTY, INTEGRATION_NAME);
        doAnswer(invocation -> Stream.of(integration)).when(dataManager).fetchAllByPropertyValue(Integration.class,
                NAME_PROPERTY, INTEGRATION_NAME);
        doAnswer(invocation -> ListResult.of(integration)).when(dataManager).fetchAll(eq(Integration.class));
        doAnswer(invocation -> {
            ListResult<Integration> result = ListResult.of(integration);
            final Object[] args = invocation.getArguments();
            if (args.length < 2) {
                return result;
            }

            for (Object arg : Arrays.copyOfRange(args, 1, args.length)) {
                Function<ListResult<Integration>, ListResult<Integration>> operator = (Function<ListResult<Integration>, ListResult<Integration>>) arg;
                result = operator.apply(result);
            }
            return result;
        }).when(dataManager).fetchAll(eq(Integration.class), any());
        doAnswer(invocation -> integration = invocation.getArgument(0)).when(dataManager).update(any(Integration.class));

        when(supportHandler.export(any())).thenReturn(out -> out.write('b'));
        final HashMap<String, List<WithResourceId>> importResult = new HashMap<>();
        importResult.put(INTEGRATION_ID, Collections.singletonList(integration));
        when(supportHandler.importIntegration(any(), any())).thenReturn(importResult);

        targetState = IntegrationDeploymentState.Unpublished;
        doAnswer(invocation -> new IntegrationOverview.Builder()
                .id(INTEGRATION_ID)
                .currentState(IntegrationDeploymentState.Unpublished)
                .targetState(targetState)
                .deploymentVersion(1)
                .build()).when(integrationHandler).get(any());

        final IntegrationDeployment.Builder deploymentBuilder = new IntegrationDeployment.Builder()
                .integrationId(Optional.of(INTEGRATION_ID))
                .currentState(IntegrationDeploymentState.Unpublished)
                .targetState(IntegrationDeploymentState.Unpublished);
        IntegrationDeployment[] deployments = new IntegrationDeployment[] {
                deploymentBuilder.targetState(IntegrationDeploymentState.Unpublished).version(1).build(),
                deploymentBuilder.version(2).targetState(IntegrationDeploymentState.Published).build()
        };

        doAnswer(invocation -> Stream.of(deployments))
                .when(dataManager)
                .fetchAllByPropertyValue(IntegrationDeployment.class, INTEGRATION_ID_PROPERTY, INTEGRATION_ID);
        doAnswer(invocation -> deploymentBuilder.targetState(targetState = IntegrationDeploymentState.Published).build())
                .when(deploymentHandler).update(any(), any());

        environmentHandler = new EnvironmentHandler(dataManager);
        handler = new PublicApiHandler(dataManager, encryptionComponent, deploymentHandler, connectionHandler,
                monitoringProvider, environmentHandler, supportHandler, integrationHandler);
    }


    @Test
    public void testGetReleaseEnvironments() {
        final List<String> environments = handler.getReleaseEnvironments();

        assertThat(environments, is(notNullValue()));
        assertThat(environments, hasItem(ENVIRONMENT));

        verify(dataManager).fetchAll(eq(Environment.class));
    }

    @Test
    public void testAddNewEnvironment() {
        handler.addNewEnvironment(ENVIRONMENT2);
        final List<String> environments = handler.getReleaseEnvironments();

        assertThat(environments, is(notNullValue()));
        assertThat(environments, hasItem(ENVIRONMENT2));

        verify(dataManager).fetchAll(eq(Environment.class));
        verify(dataManager).create(any(Environment.class));
    }

    @Test
    public void testPutTagsForRelease() throws Exception {
        final Date now = new Date();
        // delay to avoid false positives in Date::after
        Thread.sleep(10);

        final Map<String, ContinuousDeliveryEnvironment> continuousDeliveryEnvironment = handler.putTagsForRelease(INTEGRATION_ID,
                Collections.singletonList(ENVIRONMENT));

        assertThat(continuousDeliveryEnvironment, is(notNullValue()));
        assertThat(continuousDeliveryEnvironment.keySet(), hasItem(ENVIRONMENT));
        assertThat(continuousDeliveryEnvironment.get(ENVIRONMENT).getLastTaggedAt().after(now), is(true));

        verify(dataManager).update(notNull());
        verify(dataManager).fetch(Integration.class, INTEGRATION_ID);
    }

    @Test
    public void testPatchTagsForRelease() throws Exception {
        // delay to avoid false positives in Date::before
        Thread.sleep(10);
        final Date now = new Date();
        // delay to avoid false positives in Date::after
        Thread.sleep(10);

        // add new environment first
        handler.addNewEnvironment(ENVIRONMENT2);

        final Map<String, ContinuousDeliveryEnvironment> continuousDeliveryEnvironment = handler.patchTagsForRelease(INTEGRATION_ID,
                Collections.singletonList(ENVIRONMENT2));

        assertThat(continuousDeliveryEnvironment, is(notNullValue()));
        assertThat(continuousDeliveryEnvironment.keySet(), hasItem(ENVIRONMENT));
        assertThat(continuousDeliveryEnvironment.keySet(), hasItem(ENVIRONMENT2));
        assertThat(continuousDeliveryEnvironment.get(ENVIRONMENT).getLastTaggedAt().before(now), is(true));
        assertThat(continuousDeliveryEnvironment.get(ENVIRONMENT2).getLastTaggedAt().after(now), is(true));

        verify(dataManager).update(notNull());
        verify(dataManager).fetch(Integration.class, INTEGRATION_ID);
    }

    @Test(expected = ClientErrorException.class)
    public void testEmptyTagForRelease() {
        handler.putTagsForRelease(INTEGRATION_ID, Collections.singletonList(""));
    }

    @Test(expected = ClientErrorException.class)
    public void testInvalidTagForRelease() {
        handler.putTagsForRelease(INTEGRATION_ID, Collections.singletonList("%test}"));
    }

    @Test
    public void testPutTagsForReleaseByName() throws Exception {
        final Date now = new Date();
        // delay to avoid false positives in Date::after
        Thread.sleep(10);

        final Map<String, ContinuousDeliveryEnvironment> continuousDeliveryEnvironment = handler.putTagsForRelease(INTEGRATION_NAME,
                Collections.singletonList(ENVIRONMENT));

        assertThat(continuousDeliveryEnvironment, is(notNullValue()));
        assertThat(continuousDeliveryEnvironment.keySet(), hasItem(ENVIRONMENT));
        assertThat(continuousDeliveryEnvironment.get(ENVIRONMENT).getLastTaggedAt().after(now), is(true));

        verify(dataManager).update(notNull());
        verify(dataManager).fetchAllByPropertyValue(Integration.class, NAME_PROPERTY, INTEGRATION_NAME);
    }

    @Test
    public void testGetReleaseTags() {
        final Map<String, ContinuousDeliveryEnvironment> releaseTags = handler.getReleaseTags(INTEGRATION_ID);

        assertThat(releaseTags, notNullValue());
        assertThat(releaseTags.keySet(), hasItem(ENVIRONMENT));

        verify(dataManager).fetch(Integration.class, INTEGRATION_ID);
    }

    @Test
    public void testDeleteReleaseTag() {
        handler.deleteReleaseTag(INTEGRATION_ID, ENVIRONMENT);

        final Map<String, ContinuousDeliveryEnvironment> releaseTags = handler.getReleaseTags(INTEGRATION_ID);

        assertThat(releaseTags, notNullValue());
        // only integration, therefore environment is deleted and map will be empty
        assertThat(releaseTags.isEmpty(), is(true));

        verify(dataManager, times(2)).fetch(Integration.class, INTEGRATION_ID);
        verify(dataManager).update(any(Integration.class));
    }

    @Test
    public void testDeleteEnvironment() {
        handler.deleteEnvironment(ENVIRONMENT);

        final List<String> environments = handler.getReleaseEnvironments();

        assertThat(environments, notNullValue());
        assertThat(environments.isEmpty(), is(true));

        final Map<String, ContinuousDeliveryEnvironment> releaseTags = handler.getReleaseTags(INTEGRATION_ID);

        assertThat(releaseTags, notNullValue());
        assertThat(releaseTags.isEmpty(), is(true));

        // create an unassigned environment and delete it
        handler.addNewEnvironment(ENVIRONMENT2);
        handler.deleteEnvironment(ENVIRONMENT2);

        verify(dataManager).update(any(Integration.class));
        verify(dataManager).fetch(Integration.class, INTEGRATION_ID);
    }

    @Test
    public void testRenameEnvironment() {
        handler.renameEnvironment(ENVIRONMENT, ENVIRONMENT + RENAMED_SUFFIX);
        List<String> environments = handler.getReleaseEnvironments();

        assertThat(environments, notNullValue());
        assertThat(environments, hasItem(ENVIRONMENT + RENAMED_SUFFIX));

        // do the same for an unassigned environment
        handler.addNewEnvironment(ENVIRONMENT2);
        handler.renameEnvironment(ENVIRONMENT2, ENVIRONMENT2 + RENAMED_SUFFIX);
        environments = handler.getReleaseEnvironments();

        assertThat(environments, notNullValue());
        assertThat(environments, hasItem(ENVIRONMENT2 + RENAMED_SUFFIX));
    }

    @Test(expected = ClientErrorException.class)
    public void testDuplicateRenameEnvironment() {
        handler.addNewEnvironment(ENVIRONMENT2);
        handler.renameEnvironment(ENVIRONMENT, ENVIRONMENT2);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void exportResources() throws Exception {
        final Response response = handler.exportResources(ENVIRONMENT, false);
        assertThat(response, is(notNullValue()));
        assertThat(response.getStatusInfo().toEnum(), is(Response.Status.OK));

        verify(dataManager).fetchAll(eq(Integration.class), any(Function.class));
        verify(dataManager).update(any(Integration.class));
    }

    @Test
    public void importResources() throws Exception {
        // export integration
        final Response response = handler.exportResources(ENVIRONMENT, false);

        // import it back
        final SecurityContext security = getSecurityContext();

        PublicApiHandler.ImportFormDataInput formInput = new PublicApiHandler.ImportFormDataInput();
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ((StreamingOutput)response.getEntity()).write(bytes);
        formInput.setData(new ByteArrayInputStream(bytes.toByteArray()));
        formInput.setProperties(new ByteArrayInputStream("test-connection.prop=value".getBytes(StandardCharsets.UTF_8)));
        formInput.setEnvironment(ENVIRONMENT);
        formInput.setDeploy(Boolean.TRUE);

        handler.importResources(security, formInput);

        // assert that integration was recreated
        verify(dataManager).fetchAll(eq(Integration.class), any());
        verify(dataManager, times(2)).update(any(Integration.class));

    }

    @Test
    public void importResourcesNewEnvironment() throws Exception {
        // export integration
        final Response response = handler.exportResources(ENVIRONMENT, false);

        handler.deleteEnvironment(ENVIRONMENT);
        handler.addNewEnvironment(ENVIRONMENT2);

        // import it back
        final SecurityContext security = getSecurityContext();

        PublicApiHandler.ImportFormDataInput formInput = new PublicApiHandler.ImportFormDataInput();
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ((StreamingOutput)response.getEntity()).write(bytes);
        formInput.setData(new ByteArrayInputStream(bytes.toByteArray()));
        formInput.setProperties(new ByteArrayInputStream("test-connection.prop=value".getBytes(StandardCharsets.UTF_8)));
        formInput.setEnvironment(ENVIRONMENT2);
        formInput.setDeploy(Boolean.TRUE);

        handler.importResources(security, formInput);

        // validate that new environment tag was created
        final Integration integration = dataManager.fetch(Integration.class, INTEGRATION_ID);
        assertThat(integration.getContinuousDeliveryState().containsKey(ENVIRONMENT_ID), is(true));
        assertThat(integration.getContinuousDeliveryState().containsKey(ENVIRONMENT2_ID), is(true));

        // assert that integration was recreated
        verify(dataManager).fetchAll(eq(Integration.class), any());
        verify(dataManager, times(3)).update(any(Integration.class));
    }

    private SecurityContext getSecurityContext() {
        final SecurityContext security = mock(SecurityContext.class);
        final Principal principal = mock(Principal.class);
        when(security.getUserPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn("user");
        return security;
    }

    @Test
    public void testGetIntegrationState() {
        final PublicApiHandler.IntegrationState integrationState = handler.getIntegrationState(getSecurityContext(),
                INTEGRATION_NAME);
        assertThat(integrationState, is(notNullValue()));
        assertThat(integrationState.getCurrentState(), is(IntegrationDeploymentState.Unpublished));
    }

    @Test
    public void testPublishIntegration() {
        final IntegrationDeployment deployment = handler.publishIntegration(getSecurityContext(), INTEGRATION_NAME);

        assertThat(deployment, is(notNullValue()));
        assertThat(deployment.getTargetState(), is(IntegrationDeploymentState.Published));
    }

    @Test
    public void testStopIntegration() {
        targetState = IntegrationDeploymentState.Published;
        handler.stopIntegration(getSecurityContext(), INTEGRATION_NAME);

        verify(deploymentHandler).updateTargetState(eq(INTEGRATION_ID), eq(2), argThat(targetState -> targetState.getTargetState() == IntegrationDeploymentState.Unpublished));
    }
}