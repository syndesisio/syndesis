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

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;
import org.mockito.ArgumentMatchers;

import io.syndesis.common.model.Kind;
import io.syndesis.common.model.ResourceIdentifier;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.openapi.OpenApi;
import io.syndesis.integration.api.IntegrationResourceManager;
import io.syndesis.server.api.generator.APIGenerator;
import io.syndesis.server.api.generator.APIIntegration;
import io.syndesis.server.api.generator.ProvidedApiTemplate;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.dao.manager.EncryptionComponent;
import io.syndesis.server.endpoint.v1.handler.api.ApiHandler.APIFormData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class IntegrationSpecificationHandlerTest {

    APIGenerator apiGenerator = mock(APIGenerator.class);

    DataManager dataManager = mock(DataManager.class);

    EncryptionComponent encryptionSupport = mock(EncryptionComponent.class);

    IntegrationSpecificationHandler handler;

    IntegrationResourceManager resourceManager = mock(IntegrationResourceManager.class);

    public IntegrationSpecificationHandlerTest() {
        final IntegrationHandler integrationHandler = new IntegrationHandler(dataManager, null, null, null,
                encryptionSupport, apiGenerator, null);
        handler = new IntegrationSpecificationHandler(integrationHandler, resourceManager);
    }

    @Test
    public void ifNoSpecificationIsPresentShouldRespondWithNotFound() {
        when(dataManager.fetch(Integration.class, "integration-id")).thenReturn(new Integration.Builder().build());

        try (Response response = handler.fetch("integration-id")) {
            assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
        }
    }

    @Test
    public void shouldPerformUpdatesBasedOnNewSpecification() {
        final Integration existing = new Integration.Builder().id("integration-1")
            .addFlow(new Flow.Builder()
                .putMetadata(OpenApi.OPERATION_ID, "flow1")
                .build())
            .build();

        final Integration given = new Integration.Builder()
            .id("integration-2")
            .addFlow(new Flow.Builder()
                .putMetadata(OpenApi.OPERATION_ID, "flow2")
                .build())
            .build();

        final Integration expected = new Integration.Builder()
            .id("integration-1")
            .addFlow(new Flow.Builder()
                .putMetadata(OpenApi.OPERATION_ID, "flow2")
                .build())
            .build();

        final OpenApi updatedSpecification = new OpenApi.Builder().build();
        final APIIntegration updatedApiIntegration = new APIIntegration(given, updatedSpecification);

        when(dataManager.fetch(Connection.class, "api-provider")).thenReturn(new Connection.Builder().connectorId("api-provider-connector").build());
        when(dataManager.fetch(Connector.class, "api-provider-connector")).thenReturn(new Connector.Builder().build());
        when(dataManager.fetch(Integration.class, "integration-1")).thenReturn(existing);
        when(encryptionSupport.encrypt(expected)).thenReturn(expected);
        when(apiGenerator.generateIntegration(any(String.class), any(ProvidedApiTemplate.class))).thenReturn(updatedApiIntegration);
        when(apiGenerator.updateFlowExcerpts(any(Integration.class))).then(ctx -> ctx.getArguments()[0]);

        final APIFormData openApiUpdate = new APIFormData();
        openApiUpdate.setSpecification(new ByteArrayInputStream("updated specification".getBytes(StandardCharsets.UTF_8)));

        handler.update("integration-1", openApiUpdate);

        verify(dataManager).store(updatedSpecification, OpenApi.class);
        verify(dataManager).update(ArgumentMatchers.<Integration>argThat(v -> {
            assertThat(v).isEqualToIgnoringGivenFields(expected, "version", "updatedAt");
            assertThat(v.getVersion()).isEqualTo(2);
            return true;
        }));
    }

    @Test
    public void shouldServeSpecifications() {
        final byte[] specificationBytes = "this is the specification".getBytes(StandardCharsets.UTF_8);

        when(dataManager.fetch(Integration.class, "integration-id")).thenReturn(
            new Integration.Builder()
                .addResource(
                    new ResourceIdentifier.Builder()
                        .id("resource-id")
                        .kind(Kind.OpenApi)
                        .build())
                .build());
        when(resourceManager.loadOpenApiDefinition("resource-id"))
            .thenReturn(Optional.of(new OpenApi.Builder()
                .putMetadata(HttpHeaders.CONTENT_TYPE, "application/vnd.oai.openapi")
                .document(specificationBytes)
                .build()));

        try (Response response = handler.fetch("integration-id")) {
            assertThat(response.getHeaderString(HttpHeaders.CONTENT_TYPE)).isEqualTo("application/vnd.oai.openapi");
            assertThat(response.getHeaderString(HttpHeaders.CONTENT_DISPOSITION)).isEqualTo("attachment; filename=openapi.yaml");
            assertThat((byte[]) response.getEntity()).isEqualTo(specificationBytes);
        }
    }

    @Test
    public void shouldServeSpecificationsWithoutContentTypeMeta() {
        final byte[] specificationBytes = "this is the specification".getBytes(StandardCharsets.UTF_8);

        when(dataManager.fetch(Integration.class, "integration-id")).thenReturn(
            new Integration.Builder()
                .addResource(
                    new ResourceIdentifier.Builder()
                        .id("resource-id")
                        .kind(Kind.OpenApi)
                        .build())
                .build());
        when(resourceManager.loadOpenApiDefinition("resource-id"))
            .thenReturn(Optional.of(new OpenApi.Builder()
                .document(specificationBytes)
                .build()));

        try (Response response = handler.fetch("integration-id")) {
            assertThat(response.getHeaderString(HttpHeaders.CONTENT_TYPE)).isEqualTo(IntegrationSpecificationHandler.DEFAULT_CONTENT_TYPE);
            assertThat(response.getHeaderString(HttpHeaders.CONTENT_DISPOSITION))
                .isEqualTo("attachment; filename=" + IntegrationSpecificationHandler.DEFAULT_FILE_NAME);
            assertThat((byte[]) response.getEntity()).isEqualTo(specificationBytes);
        }
    }

    @Test
    public void shouldStoreUpdatedSpecificationForNonFlowChanges() {
        final Step step = new Step.Builder()
            .action(new ConnectorAction.Builder()
                .descriptor(new ConnectorDescriptor.Builder().build())
                .build())
            .build();

        final Integration integration = new Integration.Builder()
            .id("integration-1")
            .addFlow(new Flow.Builder()
                .putMetadata(OpenApi.OPERATION_ID, "flow1")
                .addSteps(step, step)
                .build())
            .build();

        final byte[] updatedSpecificationDocument = "updated specification".getBytes(StandardCharsets.UTF_8);
        final OpenApi updatedSpecification = new OpenApi.Builder().document(updatedSpecificationDocument).build();
        final APIIntegration updatedApiIntegration = new APIIntegration(integration, updatedSpecification);

        when(dataManager.fetch(Connection.class, "api-provider")).thenReturn(new Connection.Builder().connectorId("api-provider-connector").build());
        when(dataManager.fetch(Connector.class, "api-provider-connector")).thenReturn(new Connector.Builder().build());
        when(dataManager.fetch(Integration.class, "integration-1")).thenReturn(integration);
        when(encryptionSupport.encrypt(integration)).thenReturn(integration);
        when(apiGenerator.generateIntegration(any(String.class), any(ProvidedApiTemplate.class))).thenReturn(updatedApiIntegration);
        when(apiGenerator.updateFlowExcerpts(any(Integration.class))).then(ctx -> ctx.getArguments()[0]);

        final APIFormData openApiUpdate = new APIFormData();
        openApiUpdate.setSpecification(new ByteArrayInputStream(updatedSpecificationDocument));

        handler.update("integration-1", openApiUpdate);

        verify(dataManager).store(updatedSpecification, OpenApi.class);
        verify(dataManager).update(ArgumentMatchers.<Integration>argThat(v -> {
            assertThat(v).isEqualToIgnoringGivenFields(integration, "version", "updatedAt");
            assertThat(v.getVersion()).isEqualTo(2);
            return true;
        }));
    }

}
