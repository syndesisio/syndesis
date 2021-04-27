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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.DataShapeMetaData;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.DynamicActionMetadata;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

public class AggregateMetadataHandlerTest {

    private final AggregateMetadataHandler metadataHandler = new AggregateMetadataHandler();

    private final Step aggregateStep = new Step.Builder()
            .stepKind(StepKind.aggregate)
            .build();

    @Test
    public void shouldCreateMetaDataFromSurroundingSteps() throws IOException {
        Step previousStep = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                                .inputDataShape(StepMetadataHelper.NO_SHAPE)
                                .outputDataShape(new DataShape.Builder()
                                        .kind(DataShapeKinds.JAVA)
                                        .specification(getSpecification("person-spec.json"))
                                        .description("person")
                                        .type(Person.class.getName())
                                        .putMetadata(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_ELEMENT)
                                        .addVariant(new DataShape.Builder()
                                                .kind(DataShapeKinds.JAVA)
                                                .specification(getSpecification("person-list-spec.json"))
                                                .description("person-list")
                                                .collectionType("List")
                                                .type(Person.class.getName())
                                                .collectionClassName(List.class.getName())
                                                .putMetadata(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_COLLECTION)
                                                .build())
                                        .addVariant(dummyShape(DataShapeKinds.JSON_INSTANCE))
                                        .build())
                                .build())
                        .build())
                .build();

        Step subsequentStep = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                                .inputDataShape(new DataShape.Builder()
                                        .kind(DataShapeKinds.JAVA)
                                        .specification(getSpecification("person-list-spec.json"))
                                        .description("person-list")
                                        .collectionType("List")
                                        .type(Person.class.getName())
                                        .collectionClassName(List.class.getName())
                                        .putMetadata(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_COLLECTION)
                                        .addVariant(new DataShape.Builder()
                                                .kind(DataShapeKinds.JAVA)
                                                .specification(getSpecification("person-spec.json"))
                                                .description("person")
                                                .type(Person.class.getName())
                                                .putMetadata(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_ELEMENT)
                                                .build())
                                        .addVariant(dummyShape(DataShapeKinds.JSON_INSTANCE))
                                        .build())
                                .outputDataShape(StepMetadataHelper.NO_SHAPE)
                                .build())
                        .build())
                .build();

        DynamicActionMetadata metadata = metadataHandler.createMetadata(aggregateStep, Collections.singletonList(previousStep), Collections.singletonList(subsequentStep));

        Assertions.assertNotNull(metadata.inputShape());
        Assertions.assertEquals(DataShapeKinds.JAVA, metadata.inputShape().getKind());
        Assertions.assertEquals(DataShapeMetaData.VARIANT_COLLECTION, metadata.inputShape().getMetadata(DataShapeMetaData.VARIANT).orElse(""));
        Assertions.assertEquals(getSpecification("person-list-spec.json"), metadata.inputShape().getSpecification());
        Assertions.assertEquals(2, metadata.inputShape().getVariants().size());
        Assertions.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, metadata.inputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
        Assertions.assertEquals("person", metadata.inputShape().getVariants().get(0).getDescription());
        Assertions.assertEquals("dummy", metadata.inputShape().getVariants().get(1).getMetadata().get(DataShapeMetaData.VARIANT));

        Assertions.assertNotNull(metadata.outputShape());
        Assertions.assertEquals(DataShapeKinds.JAVA, metadata.outputShape().getKind());
        Assertions.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, metadata.outputShape().getMetadata(DataShapeMetaData.VARIANT).orElse(""));
        Assertions.assertEquals(getSpecification("person-spec.json"), metadata.outputShape().getSpecification());
        Assertions.assertEquals(2, metadata.outputShape().getVariants().size());
        Assertions.assertEquals(DataShapeMetaData.VARIANT_COLLECTION, metadata.outputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
        Assertions.assertEquals("person-list", metadata.outputShape().getVariants().get(0).getDescription());
        Assertions.assertEquals("dummy", metadata.outputShape().getVariants().get(1).getMetadata().get(DataShapeMetaData.VARIANT));
    }

    @Test
    public void shouldCreateMetaDataFromAnyShapes() {
        Step previousStep = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                                .inputDataShape(StepMetadataHelper.NO_SHAPE)
                                .outputDataShape(StepMetadataHelper.ANY_SHAPE)
                                .build())
                        .build())
                .build();

        Step subsequentStep = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                                .inputDataShape(StepMetadataHelper.ANY_SHAPE)
                                .outputDataShape(StepMetadataHelper.NO_SHAPE)
                                .build())
                        .build())
                .build();

        DynamicActionMetadata metadata = metadataHandler.createMetadata(aggregateStep, Collections.singletonList(previousStep), Collections.singletonList(subsequentStep));

        Assertions.assertEquals(StepMetadataHelper.ANY_SHAPE, metadata.inputShape());
        Assertions.assertEquals(StepMetadataHelper.ANY_SHAPE, metadata.outputShape());
    }

    @Test
    public void shouldCreateMetaDataFromEmptySurroundingSteps() {
        DynamicActionMetadata metadata = metadataHandler.createMetadata(aggregateStep, Collections.emptyList(), Collections.emptyList());

        Assertions.assertEquals(StepMetadataHelper.NO_SHAPE, metadata.inputShape());
        Assertions.assertEquals(StepMetadataHelper.NO_SHAPE, metadata.outputShape());
    }

    @Test
    public void shouldExtractJavaOutputCollectionVariant() throws IOException {
        DynamicActionMetadata metadata = new DynamicActionMetadata.Builder()
                .inputShape(StepMetadataHelper.NO_SHAPE)
                .outputShape(new DataShape.Builder()
                        .kind(DataShapeKinds.JAVA)
                        .specification(getSpecification("person-spec.json"))
                        .description("person")
                        .type(Person.class.getName())
                        .putMetadata(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_ELEMENT)
                        .addVariant(new DataShape.Builder()
                                .kind(DataShapeKinds.JAVA)
                                .specification(getSpecification("person-list-spec.json"))
                                .collectionType("List")
                                .type(Person.class.getName())
                                .collectionClassName(List.class.getName())
                                .putMetadata(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_COLLECTION)
                                .build())
                        .addVariant(dummyShape(DataShapeKinds.JSON_INSTANCE))
                        .build())
                .build();

        DynamicActionMetadata enrichedMetadata = metadataHandler.handle(metadata);

        Assertions.assertEquals(StepMetadataHelper.NO_SHAPE, enrichedMetadata.inputShape());
        Assertions.assertNotNull(enrichedMetadata.outputShape());
        Assertions.assertEquals(DataShapeKinds.JAVA, enrichedMetadata.outputShape().getKind());
        Assertions.assertEquals(DataShapeMetaData.VARIANT_COLLECTION, enrichedMetadata.outputShape().getMetadata(DataShapeMetaData.VARIANT).orElse(""));
        Assertions.assertEquals(getSpecification("person-list-spec.json"), enrichedMetadata.outputShape().getSpecification());
        Assertions.assertEquals(2, enrichedMetadata.outputShape().getVariants().size());
        Assertions.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
        Assertions.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, enrichedMetadata.outputShape().getVariants().get(1).getMetadata().get(DataShapeMetaData.VARIANT));
        Assertions.assertEquals("person", enrichedMetadata.outputShape().getVariants().get(1).getDescription());
    }

    @Test
    public void shouldExtractJavaInputElementVariant() throws IOException {
        DynamicActionMetadata metadata = new DynamicActionMetadata.Builder()
                .inputShape(new DataShape.Builder()
                        .kind(DataShapeKinds.JAVA)
                        .specification(getSpecification("person-list-spec.json"))
                        .description("person-list")
                        .collectionType("List")
                        .type(Person.class.getName())
                        .collectionClassName(List.class.getName())
                        .putMetadata(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_COLLECTION)
                        .addVariant(new DataShape.Builder()
                                .kind(DataShapeKinds.JAVA)
                                .specification(getSpecification("person-spec.json"))
                                .type(Person.class.getName())
                                .putMetadata(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_ELEMENT)
                                .build())
                        .addVariant(dummyShape(DataShapeKinds.JSON_INSTANCE))
                        .build())
                .outputShape(StepMetadataHelper.NO_SHAPE)
                .build();

        DynamicActionMetadata enrichedMetadata = metadataHandler.handle(metadata);

        Assertions.assertNotNull(enrichedMetadata.inputShape());
        Assertions.assertEquals(DataShapeKinds.JAVA, enrichedMetadata.inputShape().getKind());
        Assertions.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, enrichedMetadata.inputShape().getMetadata(DataShapeMetaData.VARIANT).orElse(""));
        Assertions.assertEquals(getSpecification("person-spec.json"), enrichedMetadata.inputShape().getSpecification());
        Assertions.assertEquals(2, enrichedMetadata.inputShape().getVariants().size());
        Assertions.assertEquals("dummy", enrichedMetadata.inputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
        Assertions.assertEquals(DataShapeMetaData.VARIANT_COLLECTION, enrichedMetadata.inputShape().getVariants().get(1).getMetadata().get(DataShapeMetaData.VARIANT));
        Assertions.assertEquals("person-list", enrichedMetadata.inputShape().getVariants().get(1).getDescription());

        Assertions.assertEquals(StepMetadataHelper.NO_SHAPE, enrichedMetadata.outputShape());
    }

    @Test
    public void shouldExtractJsonSchemaOutputCollectionVariant() throws IOException {
        DynamicActionMetadata metadata = new DynamicActionMetadata.Builder()
                .inputShape(StepMetadataHelper.NO_SHAPE)
                .outputShape(new DataShape.Builder()
                        .kind(DataShapeKinds.JSON_SCHEMA)
                        .specification(getSpecification("person-schema.json"))
                        .description("person-schema")
                        .type(Person.class.getName())
                        .putMetadata(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_ELEMENT)
                        .addVariant(new DataShape.Builder()
                                .kind(DataShapeKinds.JSON_SCHEMA)
                                .specification(getSpecification("person-list-schema.json"))
                                .collectionType("List")
                                .type(Person.class.getName())
                                .collectionClassName(List.class.getName())
                                .putMetadata(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_COLLECTION)
                                .build())
                        .addVariant(dummyShape(DataShapeKinds.JAVA))
                        .build())
                .build();

        DynamicActionMetadata enrichedMetadata = metadataHandler.handle(metadata);

        Assertions.assertEquals(StepMetadataHelper.NO_SHAPE, enrichedMetadata.inputShape());
        Assertions.assertNotNull(enrichedMetadata.outputShape());
        Assertions.assertEquals(DataShapeKinds.JSON_SCHEMA, enrichedMetadata.outputShape().getKind());
        Assertions.assertEquals(DataShapeMetaData.VARIANT_COLLECTION, enrichedMetadata.outputShape().getMetadata(DataShapeMetaData.VARIANT).orElse(""));
        Assertions.assertEquals(getSpecification("person-list-schema.json"), enrichedMetadata.outputShape().getSpecification());
        Assertions.assertEquals(2, enrichedMetadata.outputShape().getVariants().size());
        Assertions.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
        Assertions.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, enrichedMetadata.outputShape().getVariants().get(1).getMetadata().get(DataShapeMetaData.VARIANT));
        Assertions.assertEquals("person-schema", enrichedMetadata.outputShape().getVariants().get(1).getDescription());
    }

    @Test
    public void shouldExtractJsonSchemaInputElementVariant() throws IOException {
        DynamicActionMetadata metadata = new DynamicActionMetadata.Builder()
                .inputShape(new DataShape.Builder()
                        .kind(DataShapeKinds.JSON_SCHEMA)
                        .specification(getSpecification("person-list-schema.json"))
                        .description("person-list-schema")
                        .collectionType("List")
                        .type(Person.class.getName())
                        .collectionClassName(List.class.getName())
                        .putMetadata(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_COLLECTION)
                        .addVariant(new DataShape.Builder()
                                .kind(DataShapeKinds.JSON_SCHEMA)
                                .specification(getSpecification("person-schema.json"))
                                .type(Person.class.getName())
                                .putMetadata(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_ELEMENT)
                                .build())
                        .addVariant(dummyShape(DataShapeKinds.JAVA))
                        .build())
                .outputShape(StepMetadataHelper.NO_SHAPE)
                .build();

        DynamicActionMetadata enrichedMetadata = metadataHandler.handle(metadata);

        Assertions.assertNotNull(enrichedMetadata.inputShape());
        Assertions.assertEquals(DataShapeKinds.JSON_SCHEMA, enrichedMetadata.inputShape().getKind());
        Assertions.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, enrichedMetadata.inputShape().getMetadata(DataShapeMetaData.VARIANT).orElse(""));
        Assertions.assertEquals(getSpecification("person-schema.json"), enrichedMetadata.inputShape().getSpecification());
        Assertions.assertEquals(2, enrichedMetadata.inputShape().getVariants().size());
        Assertions.assertEquals("dummy", enrichedMetadata.inputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
        Assertions.assertEquals(DataShapeMetaData.VARIANT_COLLECTION, enrichedMetadata.inputShape().getVariants().get(1).getMetadata().get(DataShapeMetaData.VARIANT));
        Assertions.assertEquals("person-list-schema", enrichedMetadata.inputShape().getVariants().get(1).getDescription());

        Assertions.assertEquals(StepMetadataHelper.NO_SHAPE, enrichedMetadata.outputShape());
    }

    @Test
    public void shouldAutoConvertAndExtractJsonSchemaVariants() throws IOException {
        DynamicActionMetadata metadata = new DynamicActionMetadata.Builder()
                .inputShape(new DataShape.Builder()
                        .kind(DataShapeKinds.JSON_SCHEMA)
                        .specification(getSpecification("person-list-schema.json"))
                        .description("person-list-schema")
                        .collectionType("List")
                        .type(Person.class.getName())
                        .collectionClassName(List.class.getName())
                        .addVariant(dummyShape(DataShapeKinds.JAVA))
                        .build())
                .outputShape(new DataShape.Builder()
                        .kind(DataShapeKinds.JSON_SCHEMA)
                        .specification(getSpecification("person-schema.json"))
                        .description("person-schema")
                        .type(Person.class.getName())
                        .addVariant(dummyShape(DataShapeKinds.JAVA))
                        .build())
                .build();

        DynamicActionMetadata enrichedMetadata = metadataHandler.handle(metadata);

        Assertions.assertNotNull(enrichedMetadata.inputShape());
        Assertions.assertEquals(DataShapeKinds.JSON_SCHEMA, enrichedMetadata.inputShape().getKind());
        Assertions.assertEquals(StringUtils.trimAllWhitespace(getSpecification("person-schema.json")), enrichedMetadata.inputShape().getSpecification());
        Assertions.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, enrichedMetadata.inputShape().getMetadata().get(DataShapeMetaData.VARIANT));
        Assertions.assertEquals(2, enrichedMetadata.inputShape().getVariants().size());
        Assertions.assertEquals("dummy", enrichedMetadata.inputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
        Assertions.assertEquals(DataShapeMetaData.VARIANT_COLLECTION, enrichedMetadata.inputShape().getVariants().get(1).getMetadata().get(DataShapeMetaData.VARIANT));
        Assertions.assertEquals("person-list-schema", enrichedMetadata.inputShape().getVariants().get(1).getDescription());

        Assertions.assertNotNull(enrichedMetadata.outputShape());
        Assertions.assertEquals(DataShapeKinds.JSON_SCHEMA, enrichedMetadata.outputShape().getKind());
        Assertions.assertEquals(StringUtils.trimAllWhitespace(getSpecification("person-list-schema.json")), enrichedMetadata.outputShape().getSpecification());
        Assertions.assertEquals(DataShapeMetaData.VARIANT_COLLECTION, enrichedMetadata.outputShape().getMetadata().get(DataShapeMetaData.VARIANT));
        Assertions.assertEquals(2, enrichedMetadata.outputShape().getVariants().size());
        Assertions.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
        Assertions.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, enrichedMetadata.outputShape().getVariants().get(1).getMetadata().get(DataShapeMetaData.VARIANT));
        Assertions.assertEquals("person-schema", enrichedMetadata.outputShape().getVariants().get(1).getDescription());
    }

    @Test
    public void shouldExtractAlreadyGivenJsonSchemaVariants() throws IOException {
        DynamicActionMetadata metadata = new DynamicActionMetadata.Builder()
                .inputShape(new DataShape.Builder()
                        .kind(DataShapeKinds.JSON_SCHEMA)
                        .specification(getSpecification("person-schema.json"))
                        .description("person-schema")
                        .type(Person.class.getName())
                        .addVariant(dummyShape(DataShapeKinds.JAVA))
                        .build())
                .outputShape(new DataShape.Builder()
                        .kind(DataShapeKinds.JSON_SCHEMA)
                        .specification(getSpecification("person-list-schema.json"))
                        .description("person-list-schema")
                        .collectionType("List")
                        .type(Person.class.getName())
                        .collectionClassName(List.class.getName())
                        .addVariant(dummyShape(DataShapeKinds.JAVA))
                        .build())
                .build();

        DynamicActionMetadata enrichedMetadata = metadataHandler.handle(metadata);

        Assertions.assertNotNull(enrichedMetadata.inputShape());
        Assertions.assertEquals(DataShapeKinds.JSON_SCHEMA, enrichedMetadata.inputShape().getKind());
        Assertions.assertEquals(metadata.inputShape().getSpecification(), enrichedMetadata.inputShape().getSpecification());
        Assertions.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, enrichedMetadata.inputShape().getMetadata().get(DataShapeMetaData.VARIANT));
        Assertions.assertEquals(1, enrichedMetadata.inputShape().getVariants().size());
        Assertions.assertEquals("dummy", enrichedMetadata.inputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));

        Assertions.assertNotNull(enrichedMetadata.outputShape());
        Assertions.assertEquals(DataShapeKinds.JSON_SCHEMA, enrichedMetadata.outputShape().getKind());
        Assertions.assertEquals(metadata.outputShape().getSpecification(), enrichedMetadata.outputShape().getSpecification());
        Assertions.assertEquals(DataShapeMetaData.VARIANT_COLLECTION, enrichedMetadata.outputShape().getMetadata().get(DataShapeMetaData.VARIANT));
        Assertions.assertEquals(1, enrichedMetadata.outputShape().getVariants().size());
        Assertions.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
    }

    @Test
    public void shouldExtractJsonInstanceOutputCollectionVariant() throws IOException {
        DynamicActionMetadata metadata = new DynamicActionMetadata.Builder()
                .inputShape(StepMetadataHelper.NO_SHAPE)
                .outputShape(new DataShape.Builder()
                        .kind(DataShapeKinds.JSON_INSTANCE)
                        .specification(getSpecification("person-instance.json"))
                        .description("person-instance")
                        .type(Person.class.getName())
                        .putMetadata(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_ELEMENT)
                        .addVariant(new DataShape.Builder()
                                .kind(DataShapeKinds.JSON_INSTANCE)
                                .specification(getSpecification("person-list-instance.json"))
                                .collectionType("List")
                                .type(Person.class.getName())
                                .collectionClassName(List.class.getName())
                                .putMetadata(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_COLLECTION)
                                .build())
                        .addVariant(dummyShape(DataShapeKinds.JAVA))
                        .build())
                .build();

        DynamicActionMetadata enrichedMetadata = metadataHandler.handle(metadata);

        Assertions.assertEquals(StepMetadataHelper.NO_SHAPE, enrichedMetadata.inputShape());
        Assertions.assertNotNull(enrichedMetadata.outputShape());
        Assertions.assertEquals(DataShapeKinds.JSON_INSTANCE, enrichedMetadata.outputShape().getKind());
        Assertions.assertEquals(DataShapeMetaData.VARIANT_COLLECTION, enrichedMetadata.outputShape().getMetadata(DataShapeMetaData.VARIANT).orElse(""));
        Assertions.assertEquals(getSpecification("person-list-instance.json"), enrichedMetadata.outputShape().getSpecification());
        Assertions.assertEquals(2, enrichedMetadata.outputShape().getVariants().size());
        Assertions.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
        Assertions.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, enrichedMetadata.outputShape().getVariants().get(1).getMetadata().get(DataShapeMetaData.VARIANT));
        Assertions.assertEquals("person-instance", enrichedMetadata.outputShape().getVariants().get(1).getDescription());
    }

    @Test
    public void shouldExtractJsonInstanceInputElementVariant() throws IOException {
        DynamicActionMetadata metadata = new DynamicActionMetadata.Builder()
                .inputShape(new DataShape.Builder()
                        .kind(DataShapeKinds.JSON_SCHEMA)
                        .specification(getSpecification("person-list-instance.json"))
                        .description("person-list-instance")
                        .collectionType("List")
                        .type(Person.class.getName())
                        .collectionClassName(List.class.getName())
                        .putMetadata(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_COLLECTION)
                        .addVariant(new DataShape.Builder()
                                .kind(DataShapeKinds.JSON_SCHEMA)
                                .specification(getSpecification("person-instance.json"))
                                .type(Person.class.getName())
                                .putMetadata(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_ELEMENT)
                                .build())
                        .addVariant(dummyShape(DataShapeKinds.JAVA))
                        .build())
                .outputShape(StepMetadataHelper.NO_SHAPE)
                .build();

        DynamicActionMetadata enrichedMetadata = metadataHandler.handle(metadata);

        Assertions.assertNotNull(enrichedMetadata.inputShape());
        Assertions.assertEquals(DataShapeKinds.JSON_SCHEMA, enrichedMetadata.inputShape().getKind());
        Assertions.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, enrichedMetadata.inputShape().getMetadata(DataShapeMetaData.VARIANT).orElse(""));
        Assertions.assertEquals(getSpecification("person-instance.json"), enrichedMetadata.inputShape().getSpecification());
        Assertions.assertEquals(2, enrichedMetadata.inputShape().getVariants().size());
        Assertions.assertEquals("dummy", enrichedMetadata.inputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
        Assertions.assertEquals(DataShapeMetaData.VARIANT_COLLECTION, enrichedMetadata.inputShape().getVariants().get(1).getMetadata().get(DataShapeMetaData.VARIANT));
        Assertions.assertEquals("person-list-instance", enrichedMetadata.inputShape().getVariants().get(1).getDescription());

        Assertions.assertEquals(StepMetadataHelper.NO_SHAPE, enrichedMetadata.outputShape());
    }

    @Test
    public void shouldAutoConvertAndExtractJsonInstanceVariants() throws IOException {
        DynamicActionMetadata metadata = new DynamicActionMetadata.Builder()
                .inputShape(new DataShape.Builder()
                        .kind(DataShapeKinds.JSON_INSTANCE)
                        .specification(getSpecification("person-list-instance.json"))
                        .description("person-list-instance")
                        .collectionType("List")
                        .type(Person.class.getName())
                        .collectionClassName(List.class.getName())
                        .addVariant(dummyShape(DataShapeKinds.JAVA))
                        .build())
                .outputShape(new DataShape.Builder()
                        .kind(DataShapeKinds.JSON_INSTANCE)
                        .specification(getSpecification("person-instance.json"))
                        .description("person-instance")
                        .type(Person.class.getName())
                        .addVariant(dummyShape(DataShapeKinds.JAVA))
                        .build())
                .build();

        DynamicActionMetadata enrichedMetadata = metadataHandler.handle(metadata);

        Assertions.assertNotNull(enrichedMetadata.inputShape());
        Assertions.assertEquals(DataShapeKinds.JSON_INSTANCE, enrichedMetadata.inputShape().getKind());
        Assertions.assertEquals(StringUtils.trimAllWhitespace(getSpecification("person-instance.json")), StringUtils.trimAllWhitespace(enrichedMetadata.inputShape().getSpecification()));
        Assertions.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, enrichedMetadata.inputShape().getMetadata().get(DataShapeMetaData.VARIANT));
        Assertions.assertEquals(2, enrichedMetadata.inputShape().getVariants().size());
        Assertions.assertEquals("dummy", enrichedMetadata.inputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
        Assertions.assertEquals(DataShapeMetaData.VARIANT_COLLECTION, enrichedMetadata.inputShape().getVariants().get(1).getMetadata().get(DataShapeMetaData.VARIANT));
        Assertions.assertEquals("person-list-instance", enrichedMetadata.inputShape().getVariants().get(1).getDescription());

        Assertions.assertNotNull(enrichedMetadata.outputShape());
        Assertions.assertEquals(DataShapeKinds.JSON_INSTANCE, enrichedMetadata.outputShape().getKind());
        Assertions.assertEquals(StringUtils.trimAllWhitespace(getSpecification("person-list-instance.json")), StringUtils.trimAllWhitespace(enrichedMetadata.outputShape().getSpecification()));
        Assertions.assertEquals(DataShapeMetaData.VARIANT_COLLECTION, enrichedMetadata.outputShape().getMetadata().get(DataShapeMetaData.VARIANT));
        Assertions.assertEquals(2, enrichedMetadata.outputShape().getVariants().size());
        Assertions.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
        Assertions.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, enrichedMetadata.outputShape().getVariants().get(1).getMetadata().get(DataShapeMetaData.VARIANT));
        Assertions.assertEquals("person-instance", enrichedMetadata.outputShape().getVariants().get(1).getDescription());
    }

    @Test
    public void shouldExtractAlreadyGivenJsonInstanceVariants() throws IOException {
        DynamicActionMetadata metadata = new DynamicActionMetadata.Builder()
                .inputShape(new DataShape.Builder()
                        .kind(DataShapeKinds.JSON_INSTANCE)
                        .specification(getSpecification("person-instance.json"))
                        .description("person-instance")
                        .type(Person.class.getName())
                        .addVariant(dummyShape(DataShapeKinds.JAVA))
                        .build())
                .outputShape(new DataShape.Builder()
                        .kind(DataShapeKinds.JSON_INSTANCE)
                        .specification(getSpecification("person-list-instance.json"))
                        .description("person-list-instance")
                        .collectionType("List")
                        .type(Person.class.getName())
                        .collectionClassName(List.class.getName())
                        .addVariant(dummyShape(DataShapeKinds.JAVA))
                        .build())
                .build();

        DynamicActionMetadata enrichedMetadata = metadataHandler.handle(metadata);

        Assertions.assertNotNull(enrichedMetadata.inputShape());
        Assertions.assertEquals(DataShapeKinds.JSON_INSTANCE, enrichedMetadata.inputShape().getKind());
        Assertions.assertEquals(metadata.inputShape().getSpecification(), enrichedMetadata.inputShape().getSpecification());
        Assertions.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, enrichedMetadata.inputShape().getMetadata().get(DataShapeMetaData.VARIANT));
        Assertions.assertEquals(1, enrichedMetadata.inputShape().getVariants().size());
        Assertions.assertEquals("dummy", enrichedMetadata.inputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));

        Assertions.assertNotNull(enrichedMetadata.outputShape());
        Assertions.assertEquals(DataShapeKinds.JSON_INSTANCE, enrichedMetadata.outputShape().getKind());
        Assertions.assertEquals(metadata.outputShape().getSpecification(), enrichedMetadata.outputShape().getSpecification());
        Assertions.assertEquals(DataShapeMetaData.VARIANT_COLLECTION, enrichedMetadata.outputShape().getMetadata().get(DataShapeMetaData.VARIANT));
        Assertions.assertEquals(1, enrichedMetadata.outputShape().getVariants().size());
        Assertions.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
    }

    @ParameterizedTest
    @CsvSource({"person-list-unified-response-schema.json, person-unified-response-schema.json",
                 "person-list-unified-response-schema-draft-4.json, person-unified-response-schema-draft-4.json",
                 "person-list-unified-response-schema-draft-6.json, person-unified-response-schema-draft-6.json"})
    public void shouldAutoConvertAndExtractJsonUnifiedSchemaVariants(final String collectionSchemaPath, final String schemaPath) throws IOException {
        DynamicActionMetadata metadata = new DynamicActionMetadata.Builder()
                .inputShape(new DataShape.Builder()
                        .kind(DataShapeKinds.JSON_SCHEMA)
                        .specification(getSpecification(collectionSchemaPath))
                        .putMetadata(DataShapeMetaData.UNIFIED, "true")
                        .description("person-list-schema")
                        .collectionType("List")
                        .type(Person.class.getName())
                        .collectionClassName(List.class.getName())
                        .addVariant(dummyShape(DataShapeKinds.JAVA))
                        .build())
                .outputShape(new DataShape.Builder()
                        .kind(DataShapeKinds.JSON_SCHEMA)
                        .specification(getSpecification(schemaPath))
                        .putMetadata(DataShapeMetaData.UNIFIED, "true")
                        .description("person-schema")
                        .type(Person.class.getName())
                        .addVariant(dummyShape(DataShapeKinds.JAVA))
                        .build())
                .build();

        DynamicActionMetadata enrichedMetadata = metadataHandler.handle(metadata);

        Assertions.assertNotNull(enrichedMetadata.inputShape());
        Assertions.assertEquals(DataShapeKinds.JSON_SCHEMA, enrichedMetadata.inputShape().getKind());
        Assertions.assertEquals(StringUtils.trimAllWhitespace(getSpecification("person-unified-response-schema.json")), enrichedMetadata.inputShape().getSpecification());
        Assertions.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, enrichedMetadata.inputShape().getMetadata().get(DataShapeMetaData.VARIANT));
        Assertions.assertEquals("true", enrichedMetadata.inputShape().getMetadata().get(DataShapeMetaData.UNIFIED));
        Assertions.assertEquals(1, enrichedMetadata.inputShape().getVariants().size());
        Assertions.assertEquals("dummy", enrichedMetadata.inputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));

        Assertions.assertNotNull(enrichedMetadata.outputShape());
        Assertions.assertEquals(DataShapeKinds.JSON_SCHEMA, enrichedMetadata.outputShape().getKind());
        Assertions.assertEquals(StringUtils.trimAllWhitespace(getSpecification("person-list-unified-response-schema.json")), enrichedMetadata.outputShape().getSpecification());
        Assertions.assertEquals(DataShapeMetaData.VARIANT_COLLECTION, enrichedMetadata.outputShape().getMetadata().get(DataShapeMetaData.VARIANT));
        Assertions.assertEquals("true", enrichedMetadata.outputShape().getMetadata().get(DataShapeMetaData.UNIFIED));
        Assertions.assertEquals(1, enrichedMetadata.outputShape().getVariants().size());
        Assertions.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
    }

    private static DataShape dummyShape(DataShapeKinds kind) {
        return new DataShape.Builder()
                .kind(kind)
                .description("dummyShape")
                .specification("{}")
                .putMetadata(DataShapeMetaData.VARIANT, "dummy")
                .build();
    }

    private static String getSpecification(String path) throws IOException {
        return IOUtils.toString(new ClassPathResource(path, AggregateMetadataHandlerTest.class).getInputStream(), StandardCharsets.UTF_8);
    }
}
