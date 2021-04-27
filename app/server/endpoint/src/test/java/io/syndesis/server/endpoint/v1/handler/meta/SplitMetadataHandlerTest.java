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
import java.util.Arrays;
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
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

public class SplitMetadataHandlerTest {

    private final SplitMetadataHandler metadataHandler = new SplitMetadataHandler();

    private final Step splitStep = new Step.Builder()
            .stepKind(StepKind.split)
            .build();

    @Test
    public void shouldCreateMetaDataFromPreviousStep() throws IOException {
        Step firstStep = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .action(new ConnectorAction.Builder()
                        .descriptor(new ConnectorDescriptor.Builder()
                                .inputDataShape(StepMetadataHelper.NO_SHAPE)
                                .outputDataShape(new DataShape.Builder()
                                        .kind(DataShapeKinds.JSON_INSTANCE)
                                        .specification("[{\"message\": \"Should be ignored\"}]")
                                        .putMetadata(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_COLLECTION)
                                        .addVariant(new DataShape.Builder()
                                                .kind(DataShapeKinds.JSON_INSTANCE)
                                                .specification("{\"message\": \"Should be ignored\"}")
                                                .putMetadata(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_ELEMENT)
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();

        Step previousStep = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .action(new ConnectorAction.Builder()
                    .descriptor(new ConnectorDescriptor.Builder()
                        .inputDataShape(StepMetadataHelper.NO_SHAPE)
                        .outputDataShape(new DataShape.Builder()
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
                                    .description("person-spec")
                                    .type(Person.class.getName())
                                    .putMetadata(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_ELEMENT)
                                    .build())
                            .addVariant(dummyShape(DataShapeKinds.JSON_INSTANCE))
                        .build())
                    .build())
                .build())
            .build();

        Step subsequentStep = new Step.Builder()
                .stepKind(StepKind.log)
                .build();

        DynamicActionMetadata metadata = metadataHandler.createMetadata(splitStep, Arrays.asList(firstStep, previousStep), Collections.singletonList(subsequentStep));

        Assertions.assertEquals(StepMetadataHelper.NO_SHAPE, metadata.inputShape());
        Assertions.assertNotNull(metadata.outputShape());
        Assertions.assertEquals(DataShapeKinds.JAVA, metadata.outputShape().getKind());
        Assertions.assertEquals(DataShapeMetaData.VARIANT_COLLECTION, metadata.outputShape().getMetadata(DataShapeMetaData.VARIANT).orElse(""));
        Assertions.assertEquals(getSpecification("person-list-spec.json"), metadata.outputShape().getSpecification());
        Assertions.assertEquals(2, metadata.outputShape().getVariants().size());
        Assertions.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, metadata.outputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
        Assertions.assertEquals("person-spec", metadata.outputShape().getVariants().get(0).getDescription());
        Assertions.assertEquals("dummy", metadata.outputShape().getVariants().get(1).getMetadata().get(DataShapeMetaData.VARIANT));
    }

    @Test
    public void shouldCreateMetaDataFromAnyShape() {
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
                .stepKind(StepKind.log)
                .build();

        DynamicActionMetadata metadata = metadataHandler.createMetadata(splitStep, Collections.singletonList(previousStep), Collections.singletonList(subsequentStep));

        Assertions.assertEquals(StepMetadataHelper.NO_SHAPE, metadata.inputShape());
        Assertions.assertEquals(StepMetadataHelper.ANY_SHAPE, metadata.outputShape());
    }

    @Test
    public void shouldCreateMetaDataFromEmptyPreviousSteps() {
        DynamicActionMetadata metadata = metadataHandler.createMetadata(splitStep, Collections.emptyList(), Collections.emptyList());

        Assertions.assertEquals(StepMetadataHelper.NO_SHAPE, metadata.inputShape());
        Assertions.assertEquals(StepMetadataHelper.NO_SHAPE, metadata.outputShape());
    }

    @Test
    public void shouldExtractJavaElementVariant() throws IOException {
        DynamicActionMetadata metadata = new DynamicActionMetadata.Builder()
                .inputShape(StepMetadataHelper.NO_SHAPE)
                .outputShape(new DataShape.Builder()
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
                .build();

        DynamicActionMetadata enrichedMetadata = metadataHandler.handle(metadata);

        Assertions.assertEquals(StepMetadataHelper.NO_SHAPE, enrichedMetadata.inputShape());
        Assertions.assertNotNull(enrichedMetadata.outputShape());
        Assertions.assertEquals(DataShapeKinds.JAVA, enrichedMetadata.outputShape().getKind());
        Assertions.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, enrichedMetadata.outputShape().getMetadata(DataShapeMetaData.VARIANT).orElse(""));
        Assertions.assertEquals(getSpecification("person-spec.json"), enrichedMetadata.outputShape().getSpecification());
        Assertions.assertEquals(2, enrichedMetadata.outputShape().getVariants().size());
        Assertions.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
        Assertions.assertEquals(DataShapeMetaData.VARIANT_COLLECTION, enrichedMetadata.outputShape().getVariants().get(1).getMetadata().get(DataShapeMetaData.VARIANT));
        Assertions.assertEquals("person-list", enrichedMetadata.outputShape().getVariants().get(1).getDescription());
    }

    @Test
    public void shouldExtractJsonSchemaElementVariant() throws IOException {
        DynamicActionMetadata metadata = new DynamicActionMetadata.Builder()
                .inputShape(StepMetadataHelper.NO_SHAPE)
                .outputShape(new DataShape.Builder()
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
                .build();

        DynamicActionMetadata enrichedMetadata = metadataHandler.handle(metadata);

        Assertions.assertEquals(StepMetadataHelper.NO_SHAPE, enrichedMetadata.inputShape());
        Assertions.assertNotNull(enrichedMetadata.outputShape());
        Assertions.assertEquals(DataShapeKinds.JSON_SCHEMA, enrichedMetadata.outputShape().getKind());
        Assertions.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, enrichedMetadata.outputShape().getMetadata(DataShapeMetaData.VARIANT).orElse(""));
        Assertions.assertEquals(getSpecification("person-schema.json"), enrichedMetadata.outputShape().getSpecification());
        Assertions.assertEquals(2, enrichedMetadata.outputShape().getVariants().size());
        Assertions.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
        Assertions.assertEquals(DataShapeMetaData.VARIANT_COLLECTION, enrichedMetadata.outputShape().getVariants().get(1).getMetadata().get(DataShapeMetaData.VARIANT));
        Assertions.assertEquals("person-list-schema", enrichedMetadata.outputShape().getVariants().get(1).getDescription());
    }

    @Test
    public void shouldAutoConvertAndExtractJsonSchemaElement() throws IOException {
        DynamicActionMetadata metadata = new DynamicActionMetadata.Builder()
                .inputShape(StepMetadataHelper.NO_SHAPE)
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

        Assertions.assertEquals(StepMetadataHelper.NO_SHAPE, enrichedMetadata.inputShape());
        Assertions.assertNotNull(enrichedMetadata.outputShape());
        Assertions.assertEquals(DataShapeKinds.JSON_SCHEMA, enrichedMetadata.outputShape().getKind());
        Assertions.assertEquals(StringUtils.trimAllWhitespace(getSpecification("person-schema.json")), enrichedMetadata.outputShape().getSpecification());
        Assertions.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, enrichedMetadata.outputShape().getMetadata(DataShapeMetaData.VARIANT).orElse(""));
        Assertions.assertEquals(2, enrichedMetadata.outputShape().getVariants().size());
        Assertions.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
        Assertions.assertEquals(DataShapeMetaData.VARIANT_COLLECTION, enrichedMetadata.outputShape().getVariants().get(1).getMetadata().get(DataShapeMetaData.VARIANT));
        Assertions.assertEquals("person-list-schema", enrichedMetadata.outputShape().getVariants().get(1).getDescription());
    }

    @Test
    public void shouldExtractAlreadyGivenJsonSchemaVariant() throws IOException {
        DynamicActionMetadata metadata = new DynamicActionMetadata.Builder()
                .inputShape(StepMetadataHelper.NO_SHAPE)
                .outputShape(new DataShape.Builder()
                        .kind(DataShapeKinds.JSON_SCHEMA)
                        .specification(getSpecification("person-schema.json"))
                        .description("person-schema")
                        .type(Person.class.getName())
                        .addVariant(dummyShape(DataShapeKinds.JAVA))
                        .build())
                .build();

        DynamicActionMetadata enrichedMetadata = metadataHandler.handle(metadata);

        Assertions.assertEquals(StepMetadataHelper.NO_SHAPE, enrichedMetadata.inputShape());
        Assertions.assertNotNull(enrichedMetadata.outputShape());
        Assertions.assertEquals(DataShapeKinds.JSON_SCHEMA, enrichedMetadata.outputShape().getKind());
        Assertions.assertEquals(metadata.outputShape().getSpecification(), enrichedMetadata.outputShape().getSpecification());
        Assertions.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, enrichedMetadata.outputShape().getMetadata(DataShapeMetaData.VARIANT).orElse(""));
        Assertions.assertEquals(1, enrichedMetadata.outputShape().getVariants().size());
        Assertions.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
    }

    @Test
    public void shouldExtractJsonInstanceElementVariant() throws IOException {
        DynamicActionMetadata metadata = new DynamicActionMetadata.Builder()
                .inputShape(StepMetadataHelper.NO_SHAPE)
                .outputShape(new DataShape.Builder()
                        .kind(DataShapeKinds.JSON_INSTANCE)
                        .specification(getSpecification("person-list-instance.json"))
                        .description("person-list-instance")
                        .collectionType("List")
                        .type(Person.class.getName())
                        .collectionClassName(List.class.getName())
                        .putMetadata(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_COLLECTION)
                        .addVariant(new DataShape.Builder()
                                .kind(DataShapeKinds.JSON_INSTANCE)
                                .specification(getSpecification("person-instance.json"))
                                .type(Person.class.getName())
                                .putMetadata(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_ELEMENT)
                                .build())
                        .addVariant(dummyShape(DataShapeKinds.JAVA))
                        .build())
                .build();

        DynamicActionMetadata enrichedMetadata = metadataHandler.handle(metadata);

        Assertions.assertEquals(StepMetadataHelper.NO_SHAPE, enrichedMetadata.inputShape());
        Assertions.assertNotNull(enrichedMetadata.outputShape());
        Assertions.assertEquals(DataShapeKinds.JSON_INSTANCE, enrichedMetadata.outputShape().getKind());
        Assertions.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, enrichedMetadata.outputShape().getMetadata(DataShapeMetaData.VARIANT).orElse(""));
        Assertions.assertEquals(getSpecification("person-instance.json"), enrichedMetadata.outputShape().getSpecification());
        Assertions.assertEquals(2, enrichedMetadata.outputShape().getVariants().size());
        Assertions.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
        Assertions.assertEquals(DataShapeMetaData.VARIANT_COLLECTION, enrichedMetadata.outputShape().getVariants().get(1).getMetadata().get(DataShapeMetaData.VARIANT));
        Assertions.assertEquals("person-list-instance", enrichedMetadata.outputShape().getVariants().get(1).getDescription());
    }

    @Test
    public void shouldAutoConvertAndExtractJsonInstanceElement() throws IOException {
        DynamicActionMetadata metadata = new DynamicActionMetadata.Builder()
                .inputShape(StepMetadataHelper.NO_SHAPE)
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

        Assertions.assertEquals(StepMetadataHelper.NO_SHAPE, enrichedMetadata.inputShape());
        Assertions.assertNotNull(enrichedMetadata.outputShape());
        Assertions.assertEquals(DataShapeKinds.JSON_INSTANCE, enrichedMetadata.outputShape().getKind());
        Assertions.assertEquals(StringUtils.trimAllWhitespace(getSpecification("person-instance.json")), enrichedMetadata.outputShape().getSpecification());
        Assertions.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, enrichedMetadata.outputShape().getMetadata(DataShapeMetaData.VARIANT).orElse(""));
        Assertions.assertEquals(2, enrichedMetadata.outputShape().getVariants().size());
        Assertions.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
        Assertions.assertEquals(DataShapeMetaData.VARIANT_COLLECTION, enrichedMetadata.outputShape().getVariants().get(1).getMetadata().get(DataShapeMetaData.VARIANT));
        Assertions.assertEquals("person-list-instance", enrichedMetadata.outputShape().getVariants().get(1).getDescription());
    }

    @Test
    public void shouldExtractAlreadyGivenJsonInstanceVariant() throws IOException {
        DynamicActionMetadata metadata = new DynamicActionMetadata.Builder()
                .inputShape(StepMetadataHelper.NO_SHAPE)
                .outputShape(new DataShape.Builder()
                        .kind(DataShapeKinds.JSON_INSTANCE)
                        .specification(getSpecification("person-instance.json"))
                        .description("person-instance")
                        .type(Person.class.getName())
                        .addVariant(dummyShape(DataShapeKinds.JAVA))
                        .build())
                .build();

        DynamicActionMetadata enrichedMetadata = metadataHandler.handle(metadata);

        Assertions.assertEquals(StepMetadataHelper.NO_SHAPE, enrichedMetadata.inputShape());
        Assertions.assertNotNull(enrichedMetadata.outputShape());
        Assertions.assertEquals(DataShapeKinds.JSON_INSTANCE, enrichedMetadata.outputShape().getKind());
        Assertions.assertEquals(metadata.outputShape().getSpecification(), enrichedMetadata.outputShape().getSpecification());
        Assertions.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, enrichedMetadata.outputShape().getMetadata(DataShapeMetaData.VARIANT).orElse(""));
        Assertions.assertEquals(1, enrichedMetadata.outputShape().getVariants().size());
        Assertions.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
    }

    @ParameterizedTest
    @ValueSource(strings = {"person-unified-schema.json",
                 "person-unified-schema-draft-4.json",
                 "person-unified-schema-draft-6.json"})
    public void shouldAutoConvertAndExtractUnifiedJsonSchemaElement(final String schemaPath) throws IOException {
        DynamicActionMetadata metadata = new DynamicActionMetadata.Builder()
                .inputShape(StepMetadataHelper.NO_SHAPE)
                .outputShape(new DataShape.Builder()
                        .kind(DataShapeKinds.JSON_SCHEMA)
                        .specification(getSpecification(schemaPath))
                        .putMetadata(DataShapeMetaData.UNIFIED, "true")
                        .description("person-unified-schema")
                        .type(Person.class.getName())
                        .addVariant(dummyShape(DataShapeKinds.JAVA))
                        .build())
                .build();

        DynamicActionMetadata enrichedMetadata = metadataHandler.handle(metadata);

        Assertions.assertEquals(StepMetadataHelper.NO_SHAPE, enrichedMetadata.inputShape());
        Assertions.assertNotNull(enrichedMetadata.outputShape());
        Assertions.assertEquals(DataShapeKinds.JSON_SCHEMA, enrichedMetadata.outputShape().getKind());
        Assertions.assertEquals(StringUtils.trimAllWhitespace(getSpecification("person-schema.json")), enrichedMetadata.outputShape().getSpecification());
        Assertions.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, enrichedMetadata.outputShape().getMetadata(DataShapeMetaData.VARIANT).orElse(""));
        Assertions.assertEquals(1, enrichedMetadata.outputShape().getVariants().size());
        Assertions.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
    }

    @ParameterizedTest
    @ValueSource(strings = {"person-list-unified-schema.json",
                 "person-list-unified-schema-draft-4.json",
                 "person-list-unified-schema-draft-6.json"})
    public void shouldAutoConvertAndExtractUnifiedJsonArraySchemaElement(final String schemaPath) throws IOException {
        DynamicActionMetadata metadata = new DynamicActionMetadata.Builder()
                .inputShape(StepMetadataHelper.NO_SHAPE)
                .outputShape(new DataShape.Builder()
                        .kind(DataShapeKinds.JSON_SCHEMA)
                        .specification(getSpecification(schemaPath))
                        .putMetadata(DataShapeMetaData.UNIFIED, "true")
                        .description("person-list-unified-schema")
                        .type(Person.class.getName())
                        .addVariant(dummyShape(DataShapeKinds.JAVA))
                        .build())
                .build();

        DynamicActionMetadata enrichedMetadata = metadataHandler.handle(metadata);

        Assertions.assertEquals(StepMetadataHelper.NO_SHAPE, enrichedMetadata.inputShape());
        Assertions.assertNotNull(enrichedMetadata.outputShape());
        Assertions.assertEquals(DataShapeKinds.JSON_SCHEMA, enrichedMetadata.outputShape().getKind());
        Assertions.assertEquals(StringUtils.trimAllWhitespace(getSpecification("person-schema.json")), enrichedMetadata.outputShape().getSpecification());
        Assertions.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, enrichedMetadata.outputShape().getMetadata(DataShapeMetaData.VARIANT).orElse(""));
        Assertions.assertEquals(1, enrichedMetadata.outputShape().getVariants().size());
        Assertions.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
    }

    private static DataShape dummyShape(DataShapeKinds kind) {
        return new DataShape.Builder()
                .kind(kind)
                .specification("{}")
                .description("dummyShape")
                .putMetadata(DataShapeMetaData.VARIANT, "dummy")
                .build();
    }

    private static String getSpecification(String path) throws IOException {
        return IOUtils.toString(new ClassPathResource(path, SplitMetadataHandlerTest.class).getInputStream(), StandardCharsets.UTF_8);
    }
}
