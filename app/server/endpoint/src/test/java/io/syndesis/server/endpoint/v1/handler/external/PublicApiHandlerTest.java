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
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
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
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.stubbing.Answer;

import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.WithName;
import io.syndesis.common.model.WithResourceId;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.environment.Environment;
import io.syndesis.common.model.integration.ContinuousDeliveryEnvironment;
import io.syndesis.common.model.integration.ContinuousDeliveryImportResults;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.IntegrationDeploymentState;
import io.syndesis.common.model.integration.IntegrationOverview;
import io.syndesis.common.model.monitoring.IntegrationDeploymentStateDetails;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.dao.manager.EncryptionComponent;
import io.syndesis.server.endpoint.monitoring.MonitoringProvider;
import io.syndesis.server.endpoint.v1.handler.environment.EnvironmentHandler;
import io.syndesis.server.endpoint.v1.handler.integration.IntegrationDeploymentHandler;
import io.syndesis.server.endpoint.v1.handler.integration.IntegrationDeploymentHandler.TargetStateRequest;
import io.syndesis.server.endpoint.v1.handler.integration.IntegrationHandler;
import io.syndesis.server.endpoint.v1.handler.integration.support.IntegrationSupportHandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PublicApiHandlerTest {

    @Test
    public void exportResources() throws IOException {
        final DataManager dataManager = mock(DataManager.class);

        final Environment env = newEnvironment("env");
        final String envId = env.getId().get();

        final Integration integration1 = new Integration.Builder()
            .id("integration1")
            .putContinuousDeliveryState(envId, new ContinuousDeliveryEnvironment.Builder().build())
            .build();
        final Integration integration2 = new Integration.Builder()
            .id("integration2")
            .putContinuousDeliveryState(envId, new ContinuousDeliveryEnvironment.Builder().build())
            .putContinuousDeliveryState("different-env", new ContinuousDeliveryEnvironment.Builder().build())
            .build();

        @SuppressWarnings({"unchecked", "rawtypes"})
        final Class<Function<ListResult<Integration>, ListResult<Integration>>[]> filterVarArgsType = (Class) Function[].class;

        final ArgumentCaptor<Function<ListResult<Integration>, ListResult<Integration>>[]> filter = ArgumentCaptor.forClass(filterVarArgsType);

        when(dataManager.fetchByPropertyValue(Environment.class, "name", "env")).thenReturn(Optional.of(env));
        when(dataManager.fetchAll(eq(Integration.class), filter.capture())).thenReturn(ListResult.of(integration1, integration2));

        final StreamingOutput someStream = mock(StreamingOutput.class);

        // too convoluted to use the implementation directly
        final IntegrationSupportHandler integrationSupportHandler = mock(IntegrationSupportHandler.class);
        when(integrationSupportHandler.export(Arrays.asList("integration1", "integration2"))).thenReturn(someStream);

        final EnvironmentHandler environmentHandler = new EnvironmentHandler(dataManager);
        // null's are not used
        final PublicApiHandler handler = new PublicApiHandler(dataManager, null, null, null, null, environmentHandler, integrationSupportHandler, null);

        try (Response response = handler.exportResources("env", false, false)) {
            assertThat(response).isNotNull();
            assertThat(response.getStatusInfo().toEnum()).isEqualTo(Status.OK);
            assertThat(response.getEntity()).isSameAs(someStream);
        }

        final Integration integration3 = new Integration.Builder()
            .id("integration3")
            .putContinuousDeliveryState("different-env", new ContinuousDeliveryEnvironment.Builder().build())
            .build();

        final List<Function<ListResult<Integration>, ListResult<Integration>>[]> filters = filter.getAllValues();
        assertThat(filters).hasSize(1).as("Varargs capture should give us single Function").hasOnlyElementsOfType(Function.class);

        @SuppressWarnings("unchecked")
        final Function<ListResult<Integration>, ListResult<Integration>> filterFunction = (Function<ListResult<Integration>, ListResult<Integration>>) (Object) filters
            .get(0);

        assertThat(filterFunction.apply(ListResult.of(integration1, integration2, integration3))).isEqualTo(ListResult.of(integration1, integration2));

        try (Response response = handler.exportResources("env", false, true)) {
            assertThat(response).isNotNull();
            assertThat(response.getStatusInfo().toEnum()).isEqualTo(Status.OK);
            assertThat(response.getEntity()).isSameAs(someStream);
        }

        verify(dataManager, times(2)).update(withLastExportedTimestamp(integration1, envId));
        verify(dataManager, times(2)).update(withLastExportedTimestamp(integration2, envId));
    }

    @Test
    public void importResources() {
        final DataManager dataManager = mock(DataManager.class);

        final SecurityContext securityContext = newMockSecurityContext();

        final InputStream givenDataStream = new ByteArrayInputStream(new byte[0]);

        // too convoluted to use the implementation directly
        final IntegrationSupportHandler supportHandler = mock(IntegrationSupportHandler.class);

        final Connection testConnection = new Connection.Builder()
            .id("connection-id")
            .connectorId("connector-id")
            .name("test-connection").build();
        final Integration integration = new Integration.Builder()
            .id("integration-id")
            .addConnection(testConnection)
            .build();
        Map<String, List<WithResourceId>> resultMap = new HashMap<>();
        resultMap.put("integration-id", Collections.singletonList(integration));
        resultMap.put("connection-id", Collections.singletonList(testConnection));
        when(supportHandler.importIntegration(securityContext, givenDataStream))
            .thenReturn(resultMap);

        final Environment env = newEnvironment("env");
        when(dataManager.fetchByPropertyValue(Environment.class, "name", "env")).thenReturn(Optional.of(env));
        when(dataManager.fetch(Connector.class, "connector-id")).thenReturn(
            new Connector.Builder()
                .putProperty("prop", new ConfigurationProperty.Builder().build())
                .build());
        when(dataManager.fetchAll(eq(Integration.class), any())).then((Answer<ListResult<Integration>>) invocation -> {
            final Object[] arguments = invocation.getArguments();
            ListResult<Integration> result = new ListResult.Builder<Integration>().addItem(integration).build();
            for (int i = 1; i < arguments.length; i++) {
                @SuppressWarnings("unchecked")
                Function<ListResult<Integration>, ListResult<Integration>> operator = (Function<ListResult<Integration>, ListResult<Integration>>) arguments[i];
                result = operator.apply(result);
            }
            return result;
        });

        final EnvironmentHandler environmentHandler = new EnvironmentHandler(dataManager);
        final IntegrationDeploymentHandler deploymentHandler = mock(IntegrationDeploymentHandler.class);
        final EncryptionComponent encryptionComponent = mock(EncryptionComponent.class);
        when(encryptionComponent.encryptPropertyValues(any(), any())).thenReturn(Collections.singletonMap("prop", "value"));

        IntegrationHandler integrationHandler = mock(IntegrationHandler.class);
        when(integrationHandler.getOverview(any())).thenReturn(
            new IntegrationOverview.Builder().createFrom(integration)
                .connections(Collections.singletonList(testConnection.builder().putConfiguredProperty("prop", "value").build()))
                .build());

        // null's are not used
        final PublicApiHandler handler = new PublicApiHandler(dataManager, encryptionComponent, deploymentHandler, null, null, environmentHandler, supportHandler, integrationHandler);

        final PublicApiHandler.ImportFormDataInput formInput = new PublicApiHandler.ImportFormDataInput();
        formInput.setData(givenDataStream);
        formInput.setProperties(new ByteArrayInputStream("test-connection.prop=value".getBytes(StandardCharsets.UTF_8)));
        formInput.setRefreshIntegrations(Boolean.TRUE);
        formInput.setEnvironment("env");
        formInput.setDeploy(Boolean.TRUE);

        final ContinuousDeliveryImportResults importResults = handler.importResources(securityContext, formInput);

        verify(deploymentHandler).update(securityContext, "integration-id");

        assertThat(importResults.getLastImportedAt()).isNotNull();
        assertThat(importResults.getResults().size()).isEqualTo(2);

        // verify the integration connection was refreshed
        final Integration importedIntegration = (Integration) importResults.getResults().get(0);

        assertThat(importedIntegration.getConnections().get(0).getConfiguredProperties().get("prop")).isEqualTo("value");
    }

    @Test
    public void testAddNewEnvironment() {
        final DataManager dataManager = mock(DataManager.class);

        final EnvironmentHandler environmentHandler = new EnvironmentHandler(dataManager);
        // null's are not used
        final PublicApiHandler handler = new PublicApiHandler(null, null, null, null, null, environmentHandler, null, null);

        handler.addNewEnvironment("new-env");

        verify(dataManager).create(withName(Environment.class, "new-env"));
    }

    @Test
    public void testDeleteEnvironment() {
        final DataManager dataManager = mock(DataManager.class);

        final Environment env = newEnvironment("env");
        when(dataManager.fetchByPropertyValue(Environment.class, "name", "env")).thenReturn(Optional.of(env));

        final String envId = env.getId().get();
        final Integration integration1 = new Integration.Builder()
            .id("integration1")
            .putContinuousDeliveryState(envId, new ContinuousDeliveryEnvironment.Builder().build())
            .build();
        final Integration integration2 = new Integration.Builder()
            .id("integration2")
            .putContinuousDeliveryState(envId, new ContinuousDeliveryEnvironment.Builder().build())
            .putContinuousDeliveryState("different-env", new ContinuousDeliveryEnvironment.Builder().build())
            .build();
        final Integration integration3 = new Integration.Builder()
            .id("integration3")
            .putContinuousDeliveryState("different-env", new ContinuousDeliveryEnvironment.Builder().build())
            .build();

        when(dataManager.fetchAll(Integration.class)).thenReturn(ListResult.of(integration1, integration2, integration3));

        final EnvironmentHandler environmentHandler = new EnvironmentHandler(dataManager);
        // null's are not used
        final PublicApiHandler handler = new PublicApiHandler(null, null, null, null, null, environmentHandler, null, null);

        handler.deleteEnvironment("env");

        verify(dataManager).update(integration1.builder()
            .continuousDeliveryState(Collections.emptyMap())
            .build());

        verify(dataManager).update(integration2.builder()
            .continuousDeliveryState(Collections.singletonMap("different-env", new ContinuousDeliveryEnvironment.Builder().build()))
            .build());

        verify(dataManager).delete(Environment.class, envId);
    }

    @Test
    public void testDeleteReleaseTag() {
        final DataManager dataManager = mock(DataManager.class);

        final Environment env = newEnvironment("env");
        final Integration integration = new Integration.Builder()
            .putContinuousDeliveryState(env.getId().get(), new ContinuousDeliveryEnvironment.Builder().build())
            .build();

        when(dataManager.fetchByPropertyValue(Environment.class, "name", "env")).thenReturn(Optional.of(env));
        when(dataManager.fetch(Integration.class, "integration-id")).thenReturn(integration);

        final EnvironmentHandler environmentHandler = new EnvironmentHandler(dataManager);
        // null's are not used
        final PublicApiHandler handler = new PublicApiHandler(null, null, null, null, null, environmentHandler, null, null);

        handler.deleteReleaseTag("integration-id", "env");

        verify(dataManager).update(integration.builder()
            .continuousDeliveryState(Collections.emptyMap())
            .build());
    }

    @Test
    public void testDuplicateRenameEnvironment() {
        final DataManager dataManager = mock(DataManager.class);

        final Environment env1 = newEnvironment("env1");
        when(dataManager.fetchByPropertyValue(Environment.class, "name", "env1")).thenReturn(Optional.of(env1));
        final Environment env2 = newEnvironment("env2");
        when(dataManager.fetchByPropertyValue(Environment.class, "name", "env2")).thenReturn(Optional.of(env2));

        final EnvironmentHandler environmentHandler = new EnvironmentHandler(dataManager);
        // null's are not used
        final PublicApiHandler handler = new PublicApiHandler(null, null, null, null, null, environmentHandler, null, null);

        assertThatExceptionOfType(ClientErrorException.class).isThrownBy(() -> handler.renameEnvironment("env1", "env2"))
            .withMessage("Duplicate environment env2")
            .satisfies(e -> assertThat(e.getResponse().getStatusInfo().toEnum()).isEqualTo(Status.BAD_REQUEST));
    }

    @Test
    public void testEmptyTagForRelease() {
        final DataManager dataManager = mock(DataManager.class);

        final EnvironmentHandler environmentHandler = new EnvironmentHandler(dataManager);
        // null's are not used
        final PublicApiHandler handler = new PublicApiHandler(null, null, null, null, null, environmentHandler, null, null);

        assertThatExceptionOfType(ClientErrorException.class)
            .isThrownBy(() -> handler.putTagsForRelease("some-integration-id", Collections.singletonList("")))
            .withMessageStartingWith("Missing environment")
            .satisfies(e -> assertThat(e.getResponse().getStatusInfo().toEnum()).isEqualTo(Status.NOT_FOUND));
    }

    @Test
    public void testGetIntegrationState() {
        final DataManager dataManager = mock(DataManager.class);

        final Integration integration = new Integration.Builder()
            .id("integration-id")
            .name("integration-name")
            .build();
        when(dataManager.fetch(Integration.class, "integration-name")).thenReturn(integration);

        final IntegrationHandler integrationHandler = mock(IntegrationHandler.class);
        when(integrationHandler.get("integration-id")).thenReturn(new IntegrationOverview.Builder()
            .createFrom(integration)
            .currentState(IntegrationDeploymentState.Unpublished)
            .build());

        final MonitoringProvider monitoringProvider = mock(MonitoringProvider.class);
        final IntegrationDeploymentStateDetails stateDetails = new IntegrationDeploymentStateDetails.Builder()
            .build();
        when(monitoringProvider.getIntegrationStateDetails("integration-id")).thenReturn(stateDetails);

        // null's are not used
        final PublicApiHandler handler = new PublicApiHandler(dataManager, null, null, null, monitoringProvider, new EnvironmentHandler(dataManager), null, integrationHandler);

        final PublicApiHandler.IntegrationState integrationState = handler.getIntegrationState(newMockSecurityContext(),
            "integration-name");

        assertThat(integrationState.getCurrentState()).isEqualTo(IntegrationDeploymentState.Unpublished);
        assertThat(integrationState.getStateDetails()).isEqualTo(stateDetails);
    }

    @Test
    public void testGetReleaseEnvironments() {
        final DataManager dataManager = mock(DataManager.class);
        when(dataManager.fetchAll(Environment.class)).thenReturn(ListResult.of(newEnvironment("env1"), newEnvironment("env2")));

        final EnvironmentHandler environmentHandler = new EnvironmentHandler(dataManager);
        // null's are not used
        final PublicApiHandler handler = new PublicApiHandler(null, null, null, null, null, environmentHandler, null, null);

        final List<String> environments = handler.getReleaseEnvironments();

        assertThat(environments).containsOnly("env1", "env2");
    }

    @Test
    public void testGetReleaseTags() {
        final DataManager dataManager = mock(DataManager.class);

        final Environment env = newEnvironment("env");
        final String envId = env.getId().get();
        final ContinuousDeliveryEnvironment deliveryEnvironment = new ContinuousDeliveryEnvironment.Builder()
            .environmentId(envId)
            .build();
        final Integration integration = new Integration.Builder()
            .putContinuousDeliveryState(envId, deliveryEnvironment)
            .build();
        when(dataManager.fetch(Integration.class, "integration-id")).thenReturn(integration);
        when(dataManager.fetch(Environment.class, envId)).thenReturn(env);

        final EnvironmentHandler environmentHandler = new EnvironmentHandler(dataManager);
        // null's are not used
        final PublicApiHandler handler = new PublicApiHandler(null, null, null, null, null, environmentHandler, null, null);

        final Map<String, ContinuousDeliveryEnvironment> releaseTags = handler.getReleaseTags("integration-id");

        assertThat(releaseTags).containsOnly(entry("env", deliveryEnvironment));
    }

    @Test
    public void testInvalidTagForRelease() {
        final DataManager dataManager = mock(DataManager.class);

        final EnvironmentHandler environmentHandler = new EnvironmentHandler(dataManager);
        // null's are not used
        final PublicApiHandler handler = new PublicApiHandler(null, null, null, null, null, environmentHandler, null, null);

        assertThatExceptionOfType(ClientErrorException.class)
            .isThrownBy(() -> handler.putTagsForRelease("some-integration-id", Collections.singletonList("%test}")))
            .withMessageStartingWith("Missing environment")
            .satisfies(e -> assertThat(e.getResponse().getStatusInfo().toEnum()).isEqualTo(Status.NOT_FOUND));
    }

    @Test
    @SuppressWarnings("JavaUtilDate") // TODO refactor
    public void testPatchTagsForRelease() throws Exception {
        final DataManager dataManager = mock(DataManager.class);

        final Environment env1 = newEnvironment("env1");
        when(dataManager.fetchByPropertyValue(Environment.class, "name", "env1"))
            .thenReturn(Optional.of(env1));
        when(dataManager.fetch(Environment.class, env1.getId().get())).thenReturn(env1);

        final Environment env2 = newEnvironment("env2");
        when(dataManager.fetchByPropertyValue(Environment.class, "name", "env2"))
            .thenReturn(Optional.of(env2));
        when(dataManager.fetch(Environment.class, env2.getId().get())).thenReturn(env2);

        final ContinuousDeliveryEnvironment existingContinuousDeliveryEntry = new ContinuousDeliveryEnvironment.Builder()
            .environmentId(env1.getId().get())
            .lastTaggedAt(new Date(0))
            .build();
        final Integration integration = new Integration.Builder()
            .putContinuousDeliveryState(env1.getId().get(), existingContinuousDeliveryEntry)
            .build();
        when(dataManager.fetch(Integration.class, "integration-id")).thenReturn(integration);

        final EnvironmentHandler environmentHandler = new EnvironmentHandler(dataManager);

        // null's are not used
        final PublicApiHandler handler = new PublicApiHandler(null, null, null, null, null, environmentHandler, null, null);

        final Map<String, ContinuousDeliveryEnvironment> continuousDeliveryEnvironment = handler.patchTagsForRelease("integration-id",
            Collections.singletonList("env2"));

        assertThat(continuousDeliveryEnvironment).containsEntry("env1", existingContinuousDeliveryEntry);
        assertThat(continuousDeliveryEnvironment).hasEntrySatisfying("env2", containsContinuousDeliveryEntry());
        assertThat(continuousDeliveryEnvironment.get("env1").getLastTaggedAt()).isBefore(continuousDeliveryEnvironment.get("env2").getLastTaggedAt());

        verify(dataManager).update(integration.builder()
            // see javadoc for continous delivery state
            .putContinuousDeliveryState(env2.getId().get(), continuousDeliveryEnvironment.get("env2"))
            .build());
    }

    @Test
    public void testPublishIntegration() {
        final DataManager dataManager = mock(DataManager.class);
        final Integration integration = new Integration.Builder()
            .id("integration-id")
            .build();

        final SecurityContext securityContext = newMockSecurityContext();

        when(dataManager.fetch(Integration.class, "integration-id")).thenReturn(integration);

        final IntegrationDeploymentHandler deploymentHandler = mock(IntegrationDeploymentHandler.class);
        when(deploymentHandler.update(securityContext, "integration-id")).thenReturn(new IntegrationDeployment.Builder()
            .spec(integration)
            .targetState(IntegrationDeploymentState.Published)
            .build());

        // null's are not used
        final PublicApiHandler handler = new PublicApiHandler(dataManager, null, deploymentHandler, null, null, new EnvironmentHandler(dataManager), null, null);

        final IntegrationDeployment deployment = handler.publishIntegration(securityContext, "integration-id");

        assertThat(deployment.getTargetState()).isEqualTo(IntegrationDeploymentState.Published);
    }

    @Test
    public void testPutTagsForRelease() throws Exception {
        final Environment environment = newEnvironment("env");
        final Integration integration = new Integration.Builder().build();

        final DataManager dataManager = mock(DataManager.class);
        when(dataManager.fetchByPropertyValue(Environment.class, "name", "env"))
            .thenReturn(Optional.of(environment));
        when(dataManager.fetch(Environment.class, environment.getId().get())).thenReturn(environment);
        when(dataManager.fetch(Integration.class, "integration-id")).thenReturn(integration);

        final EnvironmentHandler environmentHandler = new EnvironmentHandler(dataManager);

        // null's are not used
        final PublicApiHandler handler = new PublicApiHandler(null, null, null, null, null, environmentHandler, null, null);

        final Map<String, ContinuousDeliveryEnvironment> continuousDeliveryEnvironment = handler.putTagsForRelease("integration-id",
            Collections.singletonList("env"));

        assertThat(continuousDeliveryEnvironment).hasEntrySatisfying("env", containsContinuousDeliveryEntry());

        verify(dataManager).update(integration.builder()
            // see javadoc for continous delivery state
            .putContinuousDeliveryState(environment.getId().get(), continuousDeliveryEnvironment.get("env"))
            .build());
    }

    @Test
    public void testPutTagsForReleaseByName() throws Exception {
        final Environment environment = newEnvironment("env");
        final Integration integration = new Integration.Builder().build();

        final DataManager dataManager = mock(DataManager.class);
        when(dataManager.fetchByPropertyValue(Environment.class, "name", "env"))
            .thenReturn(Optional.of(environment));
        when(dataManager.fetch(Environment.class, environment.getId().get())).thenReturn(environment);
        when(dataManager.fetchAllByPropertyValue(Integration.class, "name", "integration-name")).thenReturn(Stream.of(integration));

        final EnvironmentHandler environmentHandler = new EnvironmentHandler(dataManager);

        // null's are not used
        final PublicApiHandler handler = new PublicApiHandler(null, null, null, null, null, environmentHandler, null, null);

        final Map<String, ContinuousDeliveryEnvironment> continuousDeliveryEnvironment = handler.putTagsForRelease("integration-name",
            Collections.singletonList("env"));

        assertThat(continuousDeliveryEnvironment).hasEntrySatisfying("env", containsContinuousDeliveryEntry());

        verify(dataManager).update(integration.builder()
            // see javadoc for continous delivery state
            .putContinuousDeliveryState(environment.getId().get(), continuousDeliveryEnvironment.get("env"))
            .build());
    }

    @Test
    public void testRenameEnvironment() {
        final DataManager dataManager = mock(DataManager.class);

        final Environment env = newEnvironment("env");
        when(dataManager.fetchByPropertyValue(Environment.class, "name", "env")).thenReturn(Optional.of(env));

        final EnvironmentHandler environmentHandler = new EnvironmentHandler(dataManager);
        // null's are not used
        final PublicApiHandler handler = new PublicApiHandler(null, null, null, null, null, environmentHandler, null, null);

        handler.renameEnvironment("env", "new-env");

        verify(dataManager).update(new Environment.Builder().createFrom(env)
            .name("new-env")
            .build());
    }

    @Test
    public void testStopIntegration() {
        final DataManager dataManager = mock(DataManager.class);
        final Integration integration = new Integration.Builder()
            .id("integration-id")
            .build();

        final SecurityContext securityContext = newMockSecurityContext();

        when(dataManager.fetch(Integration.class, "integration-id")).thenReturn(integration);
        when(dataManager.fetchAllByPropertyValue(IntegrationDeployment.class, "integrationId", "integration-id"))
            .thenReturn(Stream.of(new IntegrationDeployment.Builder()
                .targetState(IntegrationDeploymentState.Published)
                .version(2)
                .spec(integration)
                .build()));

        final IntegrationDeploymentHandler deploymentHandler = mock(IntegrationDeploymentHandler.class);

        // null's are not used
        final PublicApiHandler handler = new PublicApiHandler(dataManager, null, deploymentHandler, null, null, new EnvironmentHandler(dataManager), null, null);

        handler.stopIntegration(securityContext, "integration-id");

        verify(deploymentHandler).updateTargetState("integration-id", 2, new TargetStateRequest(IntegrationDeploymentState.Unpublished));
    }

    private static Condition<ContinuousDeliveryEnvironment> containsContinuousDeliveryEntry() {
        return new Condition<ContinuousDeliveryEnvironment>("With releaseTag and lastTaggedAt") {
            @Override
            public boolean matches(final ContinuousDeliveryEnvironment value) {
                return !value.getReleaseTag().isEmpty()
                    && value.getLastTaggedAt() != null;
            }
        };
    }

    private static Environment newEnvironment(final String name) {
        return new Environment.Builder()
            .id(RandomStringUtils.randomAlphabetic(5))
            .name(name)
            .build();
    }

    private static SecurityContext newMockSecurityContext() {
        final SecurityContext security = mock(SecurityContext.class);
        final Principal principal = mock(Principal.class);
        when(security.getUserPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn("user");

        return security;
    }

    private static Integration withLastExportedTimestamp(final Integration base, final String envId) {
        return ArgumentMatchers.argThat(integration -> integration.idEquals(base.getId().get())
            && integration.getContinuousDeliveryState().get(envId).getLastExportedAt() != null);
    }

    private static <T extends WithName> T withName(final Class<T> type, final String name) {
        return ArgumentMatchers.argThat(argument -> type.isInstance(argument) && name.equals(argument.getName()));
    }
}
