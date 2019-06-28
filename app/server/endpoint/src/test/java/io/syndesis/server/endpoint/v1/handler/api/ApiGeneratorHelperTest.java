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
package io.syndesis.server.endpoint.v1.handler.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.openapi.OpenApi;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ApiGeneratorHelperTest {

    @Test
    public void shouldAddNewFlowsNonTrivialCase() {
        final Step step = new Step.Builder()
            .action(new ConnectorAction.Builder()
                .descriptor(new ConnectorDescriptor.Builder().build())
                .build())
            .build();

        final Flow flow1 = new Flow.Builder().putMetadata(OpenApi.OPERATION_ID, "flow1").addSteps(step, step).build();
        final Flow flow2 = new Flow.Builder().putMetadata(OpenApi.OPERATION_ID, "flow2").addSteps(step, step).build();
        final Flow flow3 = new Flow.Builder().putMetadata(OpenApi.OPERATION_ID, "flow3").addSteps(step, step).build();

        final Integration existing = new Integration.Builder().addFlows(flow1, flow3).build();
        final Integration given = new Integration.Builder().addFlows(flow1, flow2, flow3).build();

        final Integration updated = ApiGeneratorHelper.updateFlowsAndStartAndEndDataShapes(existing, given);

        assertThat(updated).as("there should be three flows").isEqualTo(given);
    }

    @Test
    public void shouldAddNewFlowsTrivialCase() {
        final Step step = new Step.Builder()
            .action(new ConnectorAction.Builder()
                .descriptor(new ConnectorDescriptor.Builder().build())
                .build())
            .build();

        final Flow flow = new Flow.Builder().putMetadata(OpenApi.OPERATION_ID, "flow1").addSteps(step, step).build();

        final Integration existing = new Integration.Builder().build();
        final Integration given = new Integration.Builder().addFlow(flow).build();

        final Integration updated = ApiGeneratorHelper.updateFlowsAndStartAndEndDataShapes(existing, given);

        assertThat(updated).as("there should be one flow").isEqualTo(given);
    }

    @Test
    public void shouldDeleteFlowsThatHaveBeenRemovedNonTrivialCase() {
        final Step step = new Step.Builder()
            .action(new ConnectorAction.Builder()
                .descriptor(new ConnectorDescriptor.Builder().build())
                .build())
            .build();

        final Flow existingFlow1 = new Flow.Builder().putMetadata(OpenApi.OPERATION_ID, "flow1").addSteps(step, step).build();
        final Flow existingFlow2 = new Flow.Builder().putMetadata(OpenApi.OPERATION_ID, "flow2").addSteps(step, step).build();

        final Integration existing = new Integration.Builder().addFlows(existingFlow1, existingFlow2).build();
        final Integration given = new Integration.Builder().addFlow(existingFlow2).build();

        final Integration updated = ApiGeneratorHelper.updateFlowsAndStartAndEndDataShapes(existing, given);

        assertThat(updated).as("there should be only one flow").isEqualTo(given);
    }

    @Test
    public void shouldDeleteFlowsThatHaveBeenRemovedTrivialCase() {
        final Step step = new Step.Builder()
            .action(new ConnectorAction.Builder()
                .descriptor(new ConnectorDescriptor.Builder().build())
                .build())
            .build();

        final Flow existingFlow = new Flow.Builder().id("flow1").addSteps(step, step).build();

        final Integration existing = new Integration.Builder().addFlow(existingFlow).build();
        final Integration given = new Integration.Builder().build();

        final Integration updated = ApiGeneratorHelper.updateFlowsAndStartAndEndDataShapes(existing, given);

        assertThat(updated.getFlows()).as("there shouldn't be any flows as we removed the single existing flow").isEmpty();
    }

    @Test
    public void shouldUpdateFlowNameAndDescription() {
        final Step step = new Step.Builder()
            .action(new ConnectorAction.Builder()
                .descriptor(new ConnectorDescriptor.Builder().build())
                .build())
            .build();

        final Flow flow = new Flow.Builder().putMetadata(OpenApi.OPERATION_ID, "flow1").name("name").description("description").addSteps(step, step).build();
        final Flow flowUpdated = new Flow.Builder().putMetadata(OpenApi.OPERATION_ID, "flow1").name("updated name").description("updated description")
            .addSteps(step, step).build();

        final Integration existing = new Integration.Builder().addFlow(flow).build();
        final Integration given = new Integration.Builder().addFlow(flowUpdated).build();

        final Integration updated = ApiGeneratorHelper.updateFlowsAndStartAndEndDataShapes(existing, given);

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

        final Flow existingFlow = new Flow.Builder().putMetadata(OpenApi.OPERATION_ID, "flow1").addSteps(startStep, stepWithShapes, endStep).build();
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
            .putMetadata(OpenApi.OPERATION_ID, "flow1")
            .steps(Arrays.asList(
                givenStartStep,
                givenEndStep))
            .build();
        final Integration given = new Integration.Builder().addFlow(givenFlow).build();

        final Integration updated = ApiGeneratorHelper.updateFlowsAndStartAndEndDataShapes(existing, given);

        final Flow expectedFlow = new Flow.Builder()
            .putMetadata(OpenApi.OPERATION_ID, "flow1")
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

        final Flow flow = new Flow.Builder().putMetadata(OpenApi.OPERATION_ID, "flow1").addSteps(step, step).build();

        final Integration existing = new Integration.Builder().addFlow(flow).build();
        final Integration given = new Integration.Builder().addFlow(flow).build();

        final Integration updated = ApiGeneratorHelper.updateFlowsAndStartAndEndDataShapes(existing, given);

        assertThat(updated).as("there should be no changes in trivial case").isEqualTo(existing);
    }

}
