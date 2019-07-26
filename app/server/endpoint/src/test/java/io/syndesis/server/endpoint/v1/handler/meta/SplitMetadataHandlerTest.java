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
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 */
public class SplitMetadataHandlerTest {

    private SplitMetadataHandler metadataHandler = new SplitMetadataHandler();

    private Step splitStep = new Step.Builder()
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

        Assert.assertEquals(StepMetadataHelper.NO_SHAPE, metadata.inputShape());
        Assert.assertNotNull(metadata.outputShape());
        Assert.assertEquals(DataShapeKinds.JAVA, metadata.outputShape().getKind());
        Assert.assertEquals(DataShapeMetaData.VARIANT_COLLECTION, metadata.outputShape().getMetadata(DataShapeMetaData.VARIANT).orElse(""));
        Assert.assertEquals(getSpecification("person-list-spec.json"), metadata.outputShape().getSpecification());
        Assert.assertEquals(2, metadata.outputShape().getVariants().size());
        Assert.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, metadata.outputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
        Assert.assertEquals("person-spec", metadata.outputShape().getVariants().get(0).getDescription());
        Assert.assertEquals("dummy", metadata.outputShape().getVariants().get(1).getMetadata().get(DataShapeMetaData.VARIANT));
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

        Assert.assertEquals(StepMetadataHelper.NO_SHAPE, metadata.inputShape());
        Assert.assertEquals(StepMetadataHelper.ANY_SHAPE, metadata.outputShape());
    }

    @Test
    public void shouldCreateMetaDataFromEmptyPreviousSteps() {
        DynamicActionMetadata metadata = metadataHandler.createMetadata(splitStep, Collections.emptyList(), Collections.emptyList());

        Assert.assertEquals(StepMetadataHelper.NO_SHAPE, metadata.inputShape());
        Assert.assertEquals(StepMetadataHelper.NO_SHAPE, metadata.outputShape());
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

        Assert.assertEquals(StepMetadataHelper.NO_SHAPE, enrichedMetadata.inputShape());
        Assert.assertNotNull(enrichedMetadata.outputShape());
        Assert.assertEquals(DataShapeKinds.JAVA, enrichedMetadata.outputShape().getKind());
        Assert.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, enrichedMetadata.outputShape().getMetadata(DataShapeMetaData.VARIANT).orElse(""));
        Assert.assertEquals(getSpecification("person-spec.json"), enrichedMetadata.outputShape().getSpecification());
        Assert.assertEquals(2, enrichedMetadata.outputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
        Assert.assertEquals(DataShapeMetaData.VARIANT_COLLECTION, enrichedMetadata.outputShape().getVariants().get(1).getMetadata().get(DataShapeMetaData.VARIANT));
        Assert.assertEquals("person-list", enrichedMetadata.outputShape().getVariants().get(1).getDescription());
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

        Assert.assertEquals(StepMetadataHelper.NO_SHAPE, enrichedMetadata.inputShape());
        Assert.assertNotNull(enrichedMetadata.outputShape());
        Assert.assertEquals(DataShapeKinds.JSON_SCHEMA, enrichedMetadata.outputShape().getKind());
        Assert.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, enrichedMetadata.outputShape().getMetadata(DataShapeMetaData.VARIANT).orElse(""));
        Assert.assertEquals(getSpecification("person-schema.json"), enrichedMetadata.outputShape().getSpecification());
        Assert.assertEquals(2, enrichedMetadata.outputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
        Assert.assertEquals(DataShapeMetaData.VARIANT_COLLECTION, enrichedMetadata.outputShape().getVariants().get(1).getMetadata().get(DataShapeMetaData.VARIANT));
        Assert.assertEquals("person-list-schema", enrichedMetadata.outputShape().getVariants().get(1).getDescription());
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

        Assert.assertEquals(StepMetadataHelper.NO_SHAPE, enrichedMetadata.inputShape());
        Assert.assertNotNull(enrichedMetadata.outputShape());
        Assert.assertEquals(DataShapeKinds.JSON_SCHEMA, enrichedMetadata.outputShape().getKind());
        Assert.assertEquals(StringUtils.trimAllWhitespace(getSpecification("person-schema.json")), enrichedMetadata.outputShape().getSpecification());
        Assert.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, enrichedMetadata.outputShape().getMetadata(DataShapeMetaData.VARIANT).orElse(""));
        Assert.assertEquals(2, enrichedMetadata.outputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
        Assert.assertEquals(DataShapeMetaData.VARIANT_COLLECTION, enrichedMetadata.outputShape().getVariants().get(1).getMetadata().get(DataShapeMetaData.VARIANT));
        Assert.assertEquals("person-list-schema", enrichedMetadata.outputShape().getVariants().get(1).getDescription());
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

        Assert.assertEquals(StepMetadataHelper.NO_SHAPE, enrichedMetadata.inputShape());
        Assert.assertNotNull(enrichedMetadata.outputShape());
        Assert.assertEquals(DataShapeKinds.JSON_SCHEMA, enrichedMetadata.outputShape().getKind());
        Assert.assertEquals(metadata.outputShape().getSpecification(), enrichedMetadata.outputShape().getSpecification());
        Assert.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, enrichedMetadata.outputShape().getMetadata(DataShapeMetaData.VARIANT).orElse(""));
        Assert.assertEquals(1, enrichedMetadata.outputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
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

        Assert.assertEquals(StepMetadataHelper.NO_SHAPE, enrichedMetadata.inputShape());
        Assert.assertNotNull(enrichedMetadata.outputShape());
        Assert.assertEquals(DataShapeKinds.JSON_INSTANCE, enrichedMetadata.outputShape().getKind());
        Assert.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, enrichedMetadata.outputShape().getMetadata(DataShapeMetaData.VARIANT).orElse(""));
        Assert.assertEquals(getSpecification("person-instance.json"), enrichedMetadata.outputShape().getSpecification());
        Assert.assertEquals(2, enrichedMetadata.outputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
        Assert.assertEquals(DataShapeMetaData.VARIANT_COLLECTION, enrichedMetadata.outputShape().getVariants().get(1).getMetadata().get(DataShapeMetaData.VARIANT));
        Assert.assertEquals("person-list-instance", enrichedMetadata.outputShape().getVariants().get(1).getDescription());
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

        Assert.assertEquals(StepMetadataHelper.NO_SHAPE, enrichedMetadata.inputShape());
        Assert.assertNotNull(enrichedMetadata.outputShape());
        Assert.assertEquals(DataShapeKinds.JSON_INSTANCE, enrichedMetadata.outputShape().getKind());
        Assert.assertEquals(StringUtils.trimAllWhitespace(getSpecification("person-instance.json")), enrichedMetadata.outputShape().getSpecification());
        Assert.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, enrichedMetadata.outputShape().getMetadata(DataShapeMetaData.VARIANT).orElse(""));
        Assert.assertEquals(2, enrichedMetadata.outputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
        Assert.assertEquals(DataShapeMetaData.VARIANT_COLLECTION, enrichedMetadata.outputShape().getVariants().get(1).getMetadata().get(DataShapeMetaData.VARIANT));
        Assert.assertEquals("person-list-instance", enrichedMetadata.outputShape().getVariants().get(1).getDescription());
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

        Assert.assertEquals(StepMetadataHelper.NO_SHAPE, enrichedMetadata.inputShape());
        Assert.assertNotNull(enrichedMetadata.outputShape());
        Assert.assertEquals(DataShapeKinds.JSON_INSTANCE, enrichedMetadata.outputShape().getKind());
        Assert.assertEquals(metadata.outputShape().getSpecification(), enrichedMetadata.outputShape().getSpecification());
        Assert.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, enrichedMetadata.outputShape().getMetadata(DataShapeMetaData.VARIANT).orElse(""));
        Assert.assertEquals(1, enrichedMetadata.outputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
    }

    @Test
    public void shouldAutoConvertAndExtractUnifiedJsonSchemaElement() throws IOException {
        DynamicActionMetadata metadata = new DynamicActionMetadata.Builder()
                .inputShape(StepMetadataHelper.NO_SHAPE)
                .outputShape(new DataShape.Builder()
                        .kind(DataShapeKinds.JSON_SCHEMA)
                        .specification(getSpecification("person-unified-schema.json"))
                        .putMetadata(DataShapeMetaData.UNIFIED, "true")
                        .description("person-unified-schema")
                        .type(Person.class.getName())
                        .addVariant(dummyShape(DataShapeKinds.JAVA))
                        .build())
                .build();

        DynamicActionMetadata enrichedMetadata = metadataHandler.handle(metadata);

        Assert.assertEquals(StepMetadataHelper.NO_SHAPE, enrichedMetadata.inputShape());
        Assert.assertNotNull(enrichedMetadata.outputShape());
        Assert.assertEquals(DataShapeKinds.JSON_SCHEMA, enrichedMetadata.outputShape().getKind());
        Assert.assertEquals(StringUtils.trimAllWhitespace(getSpecification("person-schema.json")), enrichedMetadata.outputShape().getSpecification());
        Assert.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, enrichedMetadata.outputShape().getMetadata(DataShapeMetaData.VARIANT).orElse(""));
        Assert.assertEquals(1, enrichedMetadata.outputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
    }

    @Test
    public void shouldAutoConvertAndExtractUnifiedJsonArraySchemaElement() throws IOException {
        DynamicActionMetadata metadata = new DynamicActionMetadata.Builder()
                .inputShape(StepMetadataHelper.NO_SHAPE)
                .outputShape(new DataShape.Builder()
                        .kind(DataShapeKinds.JSON_SCHEMA)
                        .specification(getSpecification("person-list-unified-schema.json"))
                        .putMetadata(DataShapeMetaData.UNIFIED, "true")
                        .description("person-list-unified-schema")
                        .type(Person.class.getName())
                        .addVariant(dummyShape(DataShapeKinds.JAVA))
                        .build())
                .build();

        DynamicActionMetadata enrichedMetadata = metadataHandler.handle(metadata);

        Assert.assertEquals(StepMetadataHelper.NO_SHAPE, enrichedMetadata.inputShape());
        Assert.assertNotNull(enrichedMetadata.outputShape());
        Assert.assertEquals(DataShapeKinds.JSON_SCHEMA, enrichedMetadata.outputShape().getKind());
        Assert.assertEquals(StringUtils.trimAllWhitespace(getSpecification("person-schema.json")), enrichedMetadata.outputShape().getSpecification());
        Assert.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, enrichedMetadata.outputShape().getMetadata(DataShapeMetaData.VARIANT).orElse(""));
        Assert.assertEquals(1, enrichedMetadata.outputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
    }

    private DataShape dummyShape(DataShapeKinds kind) {
        return new DataShape.Builder()
                .kind(kind)
                .specification("{}")
                .description("dummyShape")
                .putMetadata(DataShapeMetaData.VARIANT, "dummy")
                .build();
    }

    private String getSpecification(String path) throws IOException {
        return IOUtils.toString(new ClassPathResource(path, SplitMetadataHandlerTest.class).getInputStream(), StandardCharsets.UTF_8);
    }
}
