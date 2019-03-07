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
import java.util.List;

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.action.StepDescriptor;
import io.syndesis.common.model.connection.DynamicActionMetadata;
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
        assertThat(descriptor.getInputDataShape()).contains(StepActionHandler.NO_SHAPE);
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
        assertThat(descriptor.getInputDataShape()).contains(StepActionHandler.NO_SHAPE);
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
        assertThat(descriptor.getInputDataShape()).contains(StepActionHandler.NO_SHAPE);
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
        assertThat(descriptor.getInputDataShape()).contains(StepActionHandler.NO_SHAPE);
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
                                .putMetadata(DataShape.Builder.COMPRESSION_METADATA_KEY, "true")
                                .compress()
                                .build())
                        .addVariant(dummyShape())
                        .build())
                .build();

        final Response response = handler.enrichStepMetadata(StepKind.split.name(), givenMetadata);

        @SuppressWarnings("unchecked")
        final Meta<StepDescriptor> meta = (Meta<StepDescriptor>) response.getEntity();

        final StepDescriptor descriptor = meta.getValue();
        assertThat(descriptor.getInputDataShape()).contains(StepActionHandler.NO_SHAPE);
        assertThat(descriptor.getOutputDataShape()).isPresent();
        assertThat(descriptor.getOutputDataShape()).get().isEqualToComparingFieldByField(new DataShape.Builder()
                .createFrom(elementShape)
                .putMetadata(DataShape.Builder.COMPRESSION_METADATA_KEY, "true")
                .putMetadata("compressed", "false")
                .addVariant(dummyShape())
                .addVariant(collectionShape)
                .build());
    }

    @Test
    public void shouldCreateNoShapesForNoShape() {
        final DynamicActionMetadata givenMetadata = new DynamicActionMetadata.Builder()
                .inputShape(StepActionHandler.NO_SHAPE)
                .outputShape(StepActionHandler.NO_SHAPE)
                .build();

        final Response response = handler.enrichStepMetadata(StepKind.split.name(), givenMetadata);

        @SuppressWarnings("unchecked")
        final Meta<StepDescriptor> meta = (Meta<StepDescriptor>) response.getEntity();

        final StepDescriptor descriptor = meta.getValue();
        assertThat(descriptor.getInputDataShape()).contains(StepActionHandler.NO_SHAPE);
        assertThat(descriptor.getOutputDataShape()).contains(StepActionHandler.NO_SHAPE);
    }

    @Test
    public void shouldCreateNoShapesForEmptyShapes() {
        final DynamicActionMetadata givenMetadata = new DynamicActionMetadata.Builder().build();

        final Response response = handler.enrichStepMetadata(StepKind.split.name(), givenMetadata);

        @SuppressWarnings("unchecked")
        final Meta<StepDescriptor> meta = (Meta<StepDescriptor>) response.getEntity();

        final StepDescriptor descriptor = meta.getValue();
        assertThat(descriptor.getInputDataShape()).contains(StepActionHandler.NO_SHAPE);
        assertThat(descriptor.getOutputDataShape()).contains(StepActionHandler.NO_SHAPE);
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

    private DataShape dummyShape() {
        return new DataShape.Builder()
                .kind(DataShapeKinds.JAVA)
                .specification("{}")
                .description("dummyShape")
                .putMetadata(StepMetadataHandler.VARIANT_METADATA_KEY, "dummy")
                .build();
    }
}
