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
package io.syndesis.server.endpoint.v1.handler.meta;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.DataShapeMetaData;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.action.StepAction;
import io.syndesis.common.model.action.StepDescriptor;
import io.syndesis.common.model.connection.DynamicActionMetadata;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.endpoint.v1.dto.Meta;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class StepActionHandlerTest {

    private final StepActionHandler handler;

    private DataShape elementShape = new DataShape.Builder()
                                        .kind(DataShapeKinds.JAVA)
                                        .specification("person-element-spec")
                                        .description("person")
                                        .type(Person.class.getName())
                                        .putMetadata("variant", "element")
                                    .build();

    private DataShape collectionShape = new DataShape.Builder()
                                        .kind(DataShapeKinds.JAVA)
                                        .specification("person-collection-spec")
                                        .description("person-collection")
                                        .collectionType("List")
                                        .type(Person.class.getName())
                                        .collectionClassName(List.class.getName())
                                        .putMetadata("variant", "collection")
                                    .build();

    public StepActionHandlerTest() {
        handler = new StepActionHandler(mock(DataManager.class));
    }

    @Test
    public void shouldSelectElementVariantForSplitStep() {
        final DynamicActionMetadata givenMetadata = new DynamicActionMetadata.Builder()
                .outputShape(new DataShape.Builder()
                            .createFrom(collectionShape)
                            .addVariant(elementShape)
                            .addVariant(dummyShape())
                        .build())
                .build();

        final Response response = handler.enrichStepMetadata(StepKind.split.name(), givenMetadata);

        @SuppressWarnings("unchecked")
        final Meta<StepDescriptor> meta = (Meta<StepDescriptor>) response.getEntity();

        final StepDescriptor descriptor = meta.getValue();
        assertThat(descriptor.getInputDataShape()).contains(StepMetadataHelper.NO_SHAPE);
        assertThat(descriptor.getOutputDataShape()).isPresent();
        assertThat(descriptor.getOutputDataShape()).get().isEqualToComparingFieldByField(new DataShape.Builder()
                .createFrom(elementShape)
                .addVariant(dummyShape())
                .addVariant(collectionShape)
                .build());
    }

    @Test
    public void shouldKeepElementVariantForSplitStep() {
        final DynamicActionMetadata givenMetadata = new DynamicActionMetadata.Builder()
                .outputShape(new DataShape.Builder()
                        .createFrom(elementShape)
                        .addVariant(collectionShape)
                        .addVariant(dummyShape())
                        .build())
                .build();

        final Response response = handler.enrichStepMetadata(StepKind.split.name(), givenMetadata);

        @SuppressWarnings("unchecked")
        final Meta<StepDescriptor> meta = (Meta<StepDescriptor>) response.getEntity();

        final StepDescriptor descriptor = meta.getValue();
        assertThat(descriptor.getInputDataShape()).contains(StepMetadataHelper.NO_SHAPE);
        assertThat(descriptor.getOutputDataShape()).contains(givenMetadata.outputShape());
    }

    @Test
    public void shouldSelectCollectionVariantForAggregateStep() {
        final DynamicActionMetadata givenMetadata = new DynamicActionMetadata.Builder()
                .outputShape(new DataShape.Builder()
                            .createFrom(elementShape)
                            .addVariant(collectionShape)
                            .addVariant(dummyShape())
                        .build())
                .build();

        final Response response = handler.enrichStepMetadata(StepKind.aggregate.name(), givenMetadata);

        @SuppressWarnings("unchecked")
        final Meta<StepDescriptor> meta = (Meta<StepDescriptor>) response.getEntity();

        final StepDescriptor descriptor = meta.getValue();
        assertThat(descriptor.getInputDataShape()).contains(StepMetadataHelper.NO_SHAPE);
        assertThat(descriptor.getOutputDataShape()).isPresent();
        assertThat(descriptor.getOutputDataShape()).get().isEqualToComparingFieldByField(new DataShape.Builder()
                .createFrom(collectionShape)
                .addVariant(dummyShape())
                .addVariant(elementShape)
                .build());
    }

    @Test
    public void shouldKeepCollectionVariantForAggregateStep() {
        final DynamicActionMetadata givenMetadata = new DynamicActionMetadata.Builder()
                .outputShape(new DataShape.Builder()
                            .createFrom(collectionShape)
                            .addVariant(elementShape)
                            .addVariant(dummyShape())
                        .build())
                .build();

        final Response response = handler.enrichStepMetadata(StepKind.aggregate.name(), givenMetadata);

        @SuppressWarnings("unchecked")
        final Meta<StepDescriptor> meta = (Meta<StepDescriptor>) response.getEntity();

        final StepDescriptor descriptor = meta.getValue();
        assertThat(descriptor.getInputDataShape()).contains(StepMetadataHelper.NO_SHAPE);
        assertThat(descriptor.getOutputDataShape()).isPresent();
        assertThat(descriptor.getOutputDataShape()).contains(givenMetadata.outputShape());
    }

    @Test
    public void shouldHandleVariantCompression() {
        final DynamicActionMetadata givenMetadata = new DynamicActionMetadata.Builder()
                .outputShape(new DataShape.Builder()
                        .createFrom(collectionShape)
                        .addVariant(new DataShape.Builder()
                                .createFrom(elementShape)
                                .putMetadata(DataShapeMetaData.SHOULD_COMPRESS, "true")
                                .compress()
                                .build())
                        .addVariant(dummyShape())
                        .build())
                .build();

        final Response response = handler.enrichStepMetadata(StepKind.split.name(), givenMetadata);

        @SuppressWarnings("unchecked")
        final Meta<StepDescriptor> meta = (Meta<StepDescriptor>) response.getEntity();

        final StepDescriptor descriptor = meta.getValue();
        assertThat(descriptor.getInputDataShape()).contains(StepMetadataHelper.NO_SHAPE);
        assertThat(descriptor.getOutputDataShape()).isPresent();
        assertThat(descriptor.getOutputDataShape()).get().isEqualToComparingFieldByField(new DataShape.Builder()
                .createFrom(elementShape)
                .putMetadata(DataShapeMetaData.SHOULD_COMPRESS, "true")
                .putMetadata("compressed", "false")
                .addVariant(dummyShape())
                .addVariant(collectionShape)
                .build());
    }

    @Test
    public void shouldCreateNoShapesForNoShape() {
        final DynamicActionMetadata givenMetadata = new DynamicActionMetadata.Builder()
                .inputShape(StepMetadataHelper.NO_SHAPE)
                .outputShape(StepMetadataHelper.NO_SHAPE)
                .build();

        final Response response = handler.enrichStepMetadata(StepKind.split.name(), givenMetadata);

        @SuppressWarnings("unchecked")
        final Meta<StepDescriptor> meta = (Meta<StepDescriptor>) response.getEntity();

        final StepDescriptor descriptor = meta.getValue();
        assertThat(descriptor.getInputDataShape()).contains(StepMetadataHelper.NO_SHAPE);
        assertThat(descriptor.getOutputDataShape()).contains(StepMetadataHelper.NO_SHAPE);
    }

    @Test
    public void shouldCreateNoShapesForEmptyShapes() {
        final DynamicActionMetadata givenMetadata = new DynamicActionMetadata.Builder().build();

        final Response response = handler.enrichStepMetadata(StepKind.split.name(), givenMetadata);

        @SuppressWarnings("unchecked")
        final Meta<StepDescriptor> meta = (Meta<StepDescriptor>) response.getEntity();

        final StepDescriptor descriptor = meta.getValue();
        assertThat(descriptor.getInputDataShape()).contains(StepMetadataHelper.NO_SHAPE);
        assertThat(descriptor.getOutputDataShape()).contains(StepMetadataHelper.ANY_SHAPE);
    }

    @Test
    public void shouldPreserveShapesForUnsupportedShapeKind() {
        DataShape xmlSchape = new DataShape.Builder()
                .kind(DataShapeKinds.XML_SCHEMA_INSPECTED)
                .specification("</>")
                .putMetadata("something", "else")
                .build();

        final DynamicActionMetadata givenMetadata = new DynamicActionMetadata.Builder()
                .inputShape(xmlSchape)
                .outputShape(xmlSchape)
                .build();

        final Response response = handler.enrichStepMetadata(StepKind.split.name(), givenMetadata);

        @SuppressWarnings("unchecked")
        final Meta<StepDescriptor> meta = (Meta<StepDescriptor>) response.getEntity();

        final StepDescriptor descriptor = meta.getValue();
        assertThat(descriptor.getInputDataShape()).contains(xmlSchape);
        assertThat(descriptor.getOutputDataShape()).contains(xmlSchape);
    }

    @Test
    public void shouldPreserveShapesForUnsupportedStepKind() {
        final DynamicActionMetadata givenMetadata = new DynamicActionMetadata.Builder()
                .inputShape(dummyShape())
                .outputShape(dummyShape())
                .build();

        final Response response = handler.enrichStepMetadata(StepKind.log.name(), givenMetadata);

        @SuppressWarnings("unchecked")
        final Meta<StepDescriptor> meta = (Meta<StepDescriptor>) response.getEntity();

        final StepDescriptor descriptor = meta.getValue();
        assertThat(descriptor.getInputDataShape()).contains(dummyShape());
        assertThat(descriptor.getOutputDataShape()).contains(dummyShape());
    }

    @Test
    public void shouldEnrichSplitSteps() {
        List<Step> steps = Arrays.asList(new Step.Builder()
                .stepKind(StepKind.endpoint)
                .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                                .inputDataShape(StepMetadataHelper.NO_SHAPE)
                                .outputDataShape(new DataShape.Builder()
                                        .createFrom(collectionShape)
                                        .addVariant(elementShape)
                                        .addVariant(dummyShape())
                                        .build())
                                .build())
                        .build())
                .build(),
                new Step.Builder()
                        .stepKind(StepKind.split)
                        .build(),
                new Step.Builder()
                        .stepKind(StepKind.log)
                        .build());

        final Response response = handler.enrichStepMetadata(steps);

        @SuppressWarnings("unchecked")
        final List<Step> enriched = (List<Step>) response.getEntity();

        assertThat(enriched).hasSize(3);
        assertThat(enriched.get(0).getStepKind()).isEqualTo(StepKind.endpoint);
        assertThat(enriched.get(0).getAction()).isPresent();
        assertThat(enriched.get(0).getAction().orElseThrow(AssertionError::new).getDescriptor().getInputDataShape()).contains(StepMetadataHelper.NO_SHAPE);
        assertThat(enriched.get(0).getAction().orElseThrow(AssertionError::new).getDescriptor().getOutputDataShape()).get().isEqualToComparingFieldByField(new DataShape.Builder()
                .createFrom(collectionShape)
                .addVariant(elementShape)
                .addVariant(dummyShape())
                .build());
        assertThat(enriched.get(1).getStepKind()).isEqualTo(StepKind.split);
        assertThat(enriched.get(1).getAction()).isPresent();
        assertThat(enriched.get(1).getAction().orElseThrow(AssertionError::new).getDescriptor().getInputDataShape()).contains(StepMetadataHelper.NO_SHAPE);
        assertThat(enriched.get(1).getAction().orElseThrow(AssertionError::new).getDescriptor().getOutputDataShape()).get().isEqualToComparingFieldByField(new DataShape.Builder()
                .createFrom(elementShape)
                .addVariant(dummyShape())
                .addVariant(collectionShape)
                .build());
        assertThat(enriched.get(2).getAction()).isNotPresent();
        assertThat(enriched.get(2).getStepKind()).isEqualTo(StepKind.log);
    }

    @Test
    public void shouldEnrichAggregateSteps() {
        List<Step> steps = Arrays.asList(new Step.Builder()
                        .stepKind(StepKind.endpoint)
                        .action(new ConnectorAction.Builder()
                                .descriptor(new ConnectorDescriptor.Builder()
                                        .inputDataShape(StepMetadataHelper.NO_SHAPE)
                                        .outputDataShape(new DataShape.Builder()
                                                .createFrom(collectionShape)
                                                .addVariant(elementShape)
                                                .addVariant(dummyShape())
                                                .build())
                                        .build())
                                .build())
                        .build(),
                new Step.Builder()
                        .stepKind(StepKind.split)
                        .action(new StepAction.Builder()
                                .descriptor(new StepDescriptor.Builder()
                                        .inputDataShape(StepMetadataHelper.NO_SHAPE)
                                        .outputDataShape(new DataShape.Builder()
                                                .createFrom(elementShape)
                                                .addVariant(collectionShape)
                                                .addVariant(dummyShape())
                                                .build())
                                        .build())
                                .build())
                        .build(),
                new Step.Builder()
                        .stepKind(StepKind.log)
                        .build(),
                new Step.Builder()
                        .stepKind(StepKind.aggregate)
                        .build(),
                new Step.Builder()
                        .stepKind(StepKind.endpoint)
                        .action(new ConnectorAction.Builder()
                                .descriptor(new ConnectorDescriptor.Builder()
                                        .inputDataShape(new DataShape.Builder()
                                                .createFrom(collectionShape)
                                                .addVariant(dummyShape())
                                                .build())
                                        .outputDataShape(StepMetadataHelper.NO_SHAPE)
                                        .build())
                                .build())
                        .build());

        final Response response = handler.enrichStepMetadata(steps);

        @SuppressWarnings("unchecked")
        final List<Step> enriched = (List<Step>) response.getEntity();

        assertThat(enriched).hasSize(5);
        assertThat(enriched.get(0).getStepKind()).isEqualTo(StepKind.endpoint);
        assertThat(enriched.get(0).getAction()).isPresent();
        assertThat(enriched.get(0).getAction().orElseThrow(AssertionError::new).getDescriptor().getInputDataShape()).contains(StepMetadataHelper.NO_SHAPE);
        assertThat(enriched.get(0).getAction().orElseThrow(AssertionError::new).getDescriptor().getOutputDataShape()).get().isEqualToComparingFieldByField(new DataShape.Builder()
                .createFrom(collectionShape)
                .addVariant(elementShape)
                .addVariant(dummyShape())
                .build());
        assertThat(enriched.get(1).getStepKind()).isEqualTo(StepKind.split);
        assertThat(enriched.get(1).getAction()).isPresent();
        assertThat(enriched.get(1).getAction().orElseThrow(AssertionError::new).getDescriptor().getInputDataShape()).contains(StepMetadataHelper.NO_SHAPE);
        assertThat(enriched.get(1).getAction().orElseThrow(AssertionError::new).getDescriptor().getOutputDataShape()).get().isEqualToComparingFieldByField(new DataShape.Builder()
                .createFrom(elementShape)
                .addVariant(dummyShape())
                .addVariant(collectionShape)
                .build());
        assertThat(enriched.get(2).getAction()).isNotPresent();
        assertThat(enriched.get(2).getStepKind()).isEqualTo(StepKind.log);
        assertThat(enriched.get(3).getStepKind()).isEqualTo(StepKind.aggregate);
        assertThat(enriched.get(3).getAction()).isPresent();
        assertThat(enriched.get(3).getAction().orElseThrow(AssertionError::new).getDescriptor().getInputDataShape()).get().isEqualToComparingFieldByField(new DataShape.Builder()
                .createFrom(collectionShape)
                .addVariant(dummyShape())
                .build());
        assertThat(enriched.get(3).getAction().orElseThrow(AssertionError::new).getDescriptor().getOutputDataShape()).get().isEqualToComparingFieldByField(new DataShape.Builder()
                .createFrom(collectionShape)
                .addVariant(dummyShape())
                .addVariant(elementShape)
                .build());
        assertThat(enriched.get(4).getStepKind()).isEqualTo(StepKind.endpoint);
        assertThat(enriched.get(4).getAction()).isPresent();
        assertThat(enriched.get(4).getAction().orElseThrow(AssertionError::new).getDescriptor().getInputDataShape()).get().isEqualToComparingFieldByField(new DataShape.Builder()
                .createFrom(collectionShape)
                .addVariant(dummyShape())
                .build());
        assertThat(enriched.get(4).getAction().orElseThrow(AssertionError::new).getDescriptor().getOutputDataShape()).contains(StepMetadataHelper.NO_SHAPE);
    }

    @Test
    public void shouldPreserveStepsWithShapes() {
        DataShape dummyShape = dummyShape();

        List<Step> steps = Collections.singletonList(new Step.Builder()
                .stepKind(StepKind.endpoint)
                .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                                .inputDataShape(dummyShape)
                                .outputDataShape(dummyShape)
                                .build())
                        .build())
                .build());

        final Response response = handler.enrichStepMetadata(steps);

        @SuppressWarnings("unchecked")
        final List<Step> enriched = (List<Step>) response.getEntity();

        assertThat(enriched).hasSize(1);
        assertThat(enriched.get(0).getStepKind()).isEqualTo(StepKind.endpoint);
        assertThat(enriched.get(0).getAction()).isPresent();
        assertThat(enriched.get(0).getAction().orElseThrow(AssertionError::new).getDescriptor().getInputDataShape()).get().isEqualTo(dummyShape);
        assertThat(enriched.get(0).getAction().orElseThrow(AssertionError::new).getDescriptor().getOutputDataShape()).get().isEqualTo(dummyShape);
    }

    @Test
    public void shouldPreserveStepsWithNoShape() {
        List<Step> steps = Arrays.asList(new Step.Builder()
                .stepKind(StepKind.endpoint)
                .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                                .inputDataShape(StepMetadataHelper.NO_SHAPE)
                                .outputDataShape(StepMetadataHelper.NO_SHAPE)
                                .build())
                        .build())
                .build(),
                new Step.Builder()
                        .stepKind(StepKind.log)
                        .build());

        final Response response = handler.enrichStepMetadata(steps);

        @SuppressWarnings("unchecked")
        final List<Step> enriched = (List<Step>) response.getEntity();
        assertThat(enriched).hasSize(2);
        assertThat(enriched.get(0).getStepKind()).isEqualTo(StepKind.endpoint);
        assertThat(enriched.get(0).getAction()).isPresent();
        assertThat(enriched.get(0).getAction().orElseThrow(AssertionError::new).getDescriptor().getInputDataShape()).contains(StepMetadataHelper.NO_SHAPE);
        assertThat(enriched.get(0).getAction().orElseThrow(AssertionError::new).getDescriptor().getOutputDataShape()).contains(StepMetadataHelper.NO_SHAPE);
        assertThat(enriched.get(1).getAction()).isNotPresent();
        assertThat(enriched.get(1).getStepKind()).isEqualTo(StepKind.log);
    }

    @Test
    public void shouldPropagateShapeChangesToAllPrecedingSteps() {
        DataShape oldElementShape = new DataShape.Builder()
                .kind(DataShapeKinds.JAVA)
                .specification("person-old-spec")
                .description("person")
                .type(Person.class.getName())
                .putMetadata("variant", "element")
                .build();

        List<Step> steps = Arrays.asList(new Step.Builder()
                        .stepKind(StepKind.endpoint)
                        .action(new ConnectorAction.Builder()
                                .descriptor(new ConnectorDescriptor.Builder()
                                        .inputDataShape(StepMetadataHelper.NO_SHAPE)
                                        .outputDataShape(new DataShape.Builder()
                                                .createFrom(collectionShape)
                                                .addVariant(elementShape)
                                                .addVariant(dummyShape())
                                                .build())
                                        .build())
                                .build())
                        .build(),
                new Step.Builder()
                        .stepKind(StepKind.split)
                        .action(new StepAction.Builder()
                                .descriptor(new StepDescriptor.Builder()
                                        .inputDataShape(StepMetadataHelper.NO_SHAPE)
                                        .outputDataShape(new DataShape.Builder()
                                                .createFrom(oldElementShape)
                                                .build())
                                        .build())
                                .build())
                        .build(),
                new Step.Builder()
                        .stepKind(StepKind.choice)
                        .action(new StepAction.Builder()
                                .descriptor(new StepDescriptor.Builder()
                                        .inputDataShape(new DataShape.Builder()
                                                .createFrom(oldElementShape)
                                                .build())
                                        .outputDataShape(StepMetadataHelper.ANY_SHAPE)
                                        .build())
                                .build())
                        .build());

        final Response response = handler.enrichStepMetadata(steps);

        @SuppressWarnings("unchecked")
        final List<Step> enriched = (List<Step>) response.getEntity();

        assertThat(enriched).hasSize(3);
        assertThat(enriched.get(0).getStepKind()).isEqualTo(StepKind.endpoint);
        assertThat(enriched.get(0).getAction()).isPresent();
        assertThat(enriched.get(0).getAction().orElseThrow(AssertionError::new).getDescriptor().getInputDataShape()).contains(StepMetadataHelper.NO_SHAPE);
        assertThat(enriched.get(0).getAction().orElseThrow(AssertionError::new).getDescriptor().getOutputDataShape()).get().isEqualToComparingFieldByField(new DataShape.Builder()
                .createFrom(collectionShape)
                .addVariant(elementShape)
                .addVariant(dummyShape())
                .build());
        assertThat(enriched.get(1).getStepKind()).isEqualTo(StepKind.split);
        assertThat(enriched.get(1).getAction()).isPresent();
        assertThat(enriched.get(1).getAction().orElseThrow(AssertionError::new).getDescriptor().getInputDataShape()).contains(StepMetadataHelper.NO_SHAPE);
        assertThat(enriched.get(1).getAction().orElseThrow(AssertionError::new).getDescriptor().getOutputDataShape()).get().isEqualToComparingFieldByField(new DataShape.Builder()
                .createFrom(elementShape)
                .addVariant(dummyShape())
                .addVariant(collectionShape)
                .build());
        assertThat(enriched.get(2).getStepKind()).isEqualTo(StepKind.choice);
        assertThat(enriched.get(2).getAction()).isPresent();
        assertThat(enriched.get(2).getAction().orElseThrow(AssertionError::new).getDescriptor().getInputDataShape()).get().isEqualToComparingFieldByField(new DataShape.Builder()
                .createFrom(elementShape)
                .addVariant(dummyShape())
                .addVariant(collectionShape)
                .build());
        assertThat(enriched.get(2).getAction().orElseThrow(AssertionError::new).getDescriptor().getOutputDataShape()).contains(StepMetadataHelper.ANY_SHAPE);
    }

    private DataShape dummyShape() {
        return new DataShape.Builder()
                .kind(DataShapeKinds.JAVA)
                .specification("{}")
                .description("dummyShape")
                .putMetadata(DataShapeMetaData.VARIANT, "dummy")
                .build();
    }
}
