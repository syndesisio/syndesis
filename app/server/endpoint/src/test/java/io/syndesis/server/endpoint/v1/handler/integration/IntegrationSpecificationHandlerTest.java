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
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.openapi.OpenApi;
import io.syndesis.server.api.generator.APIGenerator;
import io.syndesis.server.api.generator.APIIntegration;
import io.syndesis.server.api.generator.ProvidedApiTemplate;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.dao.manager.EncryptionComponent;
import io.syndesis.server.endpoint.v1.handler.api.ApiHandler.APIFormData;

import org.junit.Test;
import org.mockito.ArgumentMatchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class IntegrationSpecificationHandlerTest {

    APIGenerator apiGenerator = mock(APIGenerator.class);

    DataManager dataManager = mock(DataManager.class);

    EncryptionComponent encryptionSupport = mock(EncryptionComponent.class);

    IntegrationSpecificationHandler handler = new IntegrationSpecificationHandler(
        new IntegrationHandler(dataManager, null, null, null, encryptionSupport, apiGenerator));

    @Test
    public void shouldAddNewFlowsNonTrivialCase() {
        final Step step = new Step.Builder()
            .action(new ConnectorAction.Builder()
                .descriptor(new ConnectorDescriptor.Builder().build())
                .build())
            .build();

        final Flow flow1 = new Flow.Builder().id("flow1").addStep(step, step).build();
        final Flow flow2 = new Flow.Builder().id("flow2").addStep(step, step).build();
        final Flow flow3 = new Flow.Builder().id("flow3").addStep(step, step).build();

        final Integration existing = new Integration.Builder().addFlow(flow1, flow3).build();
        final Integration given = new Integration.Builder().addFlow(flow1, flow2, flow3).build();

        final Integration updated = IntegrationSpecificationHandler.updateFlowsAndStartAndEndDataShapes(existing, given);

        assertThat(updated).as("there should be three flows").isEqualTo(given);
    }

    @Test
    public void shouldAddNewFlowsTrivialCase() {
        final Step step = new Step.Builder()
            .action(new ConnectorAction.Builder()
                .descriptor(new ConnectorDescriptor.Builder().build())
                .build())
            .build();

        final Flow flow = new Flow.Builder().id("flow1").addStep(step, step).build();

        final Integration existing = new Integration.Builder().build();
        final Integration given = new Integration.Builder().addFlow(flow).build();

        final Integration updated = IntegrationSpecificationHandler.updateFlowsAndStartAndEndDataShapes(existing, given);

        assertThat(updated).as("there should be one flow").isEqualTo(given);
    }

    @Test
    public void shouldDeleteFlowsThatHaveBeenRemovedNonTrivialCase() {
        final Step step = new Step.Builder()
            .action(new ConnectorAction.Builder()
                .descriptor(new ConnectorDescriptor.Builder().build())
                .build())
            .build();

        final Flow existingFlow1 = new Flow.Builder().id("flow1").addStep(step, step).build();
        final Flow existingFlow2 = new Flow.Builder().id("flow2").addStep(step, step).build();

        final Integration existing = new Integration.Builder().addFlow(existingFlow1, existingFlow2).build();
        final Integration given = new Integration.Builder().addFlow(existingFlow2).build();

        final Integration updated = IntegrationSpecificationHandler.updateFlowsAndStartAndEndDataShapes(existing, given);

        assertThat(updated).as("there should be only one flow").isEqualTo(given);
    }

    @Test
    public void shouldDeleteFlowsThatHaveBeenRemovedTrivialCase() {
        final Step step = new Step.Builder()
            .action(new ConnectorAction.Builder()
                .descriptor(new ConnectorDescriptor.Builder().build())
                .build())
            .build();

        final Flow existingFlow = new Flow.Builder().id("flow1").addStep(step, step).build();

        final Integration existing = new Integration.Builder().addFlow(existingFlow).build();
        final Integration given = new Integration.Builder().build();

        final Integration updated = IntegrationSpecificationHandler.updateFlowsAndStartAndEndDataShapes(existing, given);

        assertThat(updated.getFlows()).as("there shouldn't be any flows as we removed the single existing flow").isEmpty();
    }

    @Test
    public void shouldPerformUpdatesBasedOnNewSpecification() {
        final Integration existing = new Integration.Builder().id("integration-1").addFlow(new Flow.Builder().id("integration-1:flows:flow1").build()).build();
        final Integration given = new Integration.Builder().id("integration-2").addFlow(new Flow.Builder().id("integration-2:flows:flow2").build()).build();
        final Integration expected = new Integration.Builder().id("integration-1").addFlow(new Flow.Builder().id("integration-1:flows:flow2").build()).build();

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

        handler.updateSpecification("integration-1", openApiUpdate);

        verify(dataManager).store(updatedSpecification, OpenApi.class);
        verify(dataManager).update(ArgumentMatchers.<Integration>argThat(v -> {
            assertThat(v).isEqualToIgnoringGivenFields(expected, "version", "updatedAt");
            assertThat(v.getVersion()).isEqualTo(2);
            return true;
        }));
    }

    @Test
    public void shouldUpdateFlowNameAndDescription() {
        final Step step = new Step.Builder()
            .action(new ConnectorAction.Builder()
                .descriptor(new ConnectorDescriptor.Builder().build())
                .build())
            .build();

        final Flow flow = new Flow.Builder().id("flow1").name("name").description("description").addStep(step, step).build();
        final Flow flowUpdated = new Flow.Builder().id("flow1").name("updated name").description("updated description").addStep(step, step).build();

        final Integration existing = new Integration.Builder().addFlow(flow).build();
        final Integration given = new Integration.Builder().addFlow(flowUpdated).build();

        final Integration updated = IntegrationSpecificationHandler.updateFlowsAndStartAndEndDataShapes(existing, given);

        assertThat(updated).as("name and description should be updated").isEqualTo(given);
    }

    @Test
    public void shouldUpdateFlowsDataShapes() {
        final Step startStep = new Step.Builder()
            .action(new ConnectorAction.Builder()
                .descriptor(new ConnectorDescriptor.Builder()
                    .outputDataShape(new DataShape.Builder()
                        .kind(DataShapeKinds.JSON_INSTANCE)
                        .specification("{\"start\":\"existing\"}")
                        .build())
                    .build())
                .build())
            .build();

        final Step endStep = new Step.Builder()
            .action(new ConnectorAction.Builder()
                .descriptor(new ConnectorDescriptor.Builder()
                    .inputDataShape(new DataShape.Builder()
                        .kind(DataShapeKinds.JSON_INSTANCE)
                        .specification("{\"end\":\"existing\"}")
                        .build())
                    .build())
                .build())
            .build();

        final Step stepWithShapes = new Step.Builder()
            .action(new ConnectorAction.Builder()
                .descriptor(new ConnectorDescriptor.Builder()
                    .inputDataShape(new DataShape.Builder()
                        .kind(DataShapeKinds.ANY)
                        .build())
                    .outputDataShape(new DataShape.Builder()
                        .kind(DataShapeKinds.NONE)
                        .build())
                    .build())
                .build())
            .build();

        final Flow existingFlow = new Flow.Builder().id("flow1").addStep(startStep, stepWithShapes, endStep).build();
        final Integration existing = new Integration.Builder().addFlow(existingFlow).build();

        final DataShape givenStartShape = new DataShape.Builder()
            .kind(DataShapeKinds.JSON_INSTANCE)
            .specification("{\"start\":\"updated\"}")
            .build();

        final DataShape givenEndShape = new DataShape.Builder()
            .kind(DataShapeKinds.JSON_INSTANCE)
            .specification("{\"end\":\"updated\"}")
            .build();

        final Step givenStartStep = startStep.updateOutputDataShape(Optional.of(givenStartShape));
        final Step givenEndStep = endStep.updateInputDataShape(Optional.of(givenEndShape));
        final Flow givenFlow = new Flow.Builder()
            .id("flow1")
            .steps(Arrays.asList(
                givenStartStep,
                givenEndStep))
            .build();
        final Integration given = new Integration.Builder().addFlow(givenFlow).build();

        final Integration updated = IntegrationSpecificationHandler.updateFlowsAndStartAndEndDataShapes(existing, given);

        final Flow expectedFlow = new Flow.Builder()
            .id("flow1")
            .steps(Arrays.asList(
                givenStartStep,
                stepWithShapes,
                givenEndStep))
            .build();
        final Integration expected = existing.builder()
            .flows(Collections.singleton(expectedFlow))
            .build();

        assertThat(updated).as("should update only the data shapes").isEqualTo(expected);
    }

    @Test
    public void shouldUpdateFlowsStartAndEndDataShapesWithoutChanges() {
        final Step step = new Step.Builder()
            .action(new ConnectorAction.Builder()
                .descriptor(new ConnectorDescriptor.Builder().build())
                .build())
            .build();

        final Flow flow = new Flow.Builder().id("flow1").addStep(step, step).build();

        final Integration existing = new Integration.Builder().addFlow(flow).build();
        final Integration given = new Integration.Builder().addFlow(flow).build();

        final Integration updated = IntegrationSpecificationHandler.updateFlowsAndStartAndEndDataShapes(existing, given);

        assertThat(updated).as("there should be no changes in trivial case").isEqualTo(existing);
    }
}
