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
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 */
public class AggregateMetadataHandlerTest {

    private AggregateMetadataHandler metadataHandler = new AggregateMetadataHandler();

    private Step aggregateStep = new Step.Builder()
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

        Assert.assertNotNull(metadata.inputShape());
        Assert.assertEquals(DataShapeKinds.JAVA, metadata.inputShape().getKind());
        Assert.assertEquals(DataShapeMetaData.VARIANT_COLLECTION, metadata.inputShape().getMetadata(DataShapeMetaData.VARIANT).orElse(""));
        Assert.assertEquals(getSpecification("person-list-spec.json"), metadata.inputShape().getSpecification());
        Assert.assertEquals(2, metadata.inputShape().getVariants().size());
        Assert.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, metadata.inputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
        Assert.assertEquals("person", metadata.inputShape().getVariants().get(0).getDescription());
        Assert.assertEquals("dummy", metadata.inputShape().getVariants().get(1).getMetadata().get(DataShapeMetaData.VARIANT));

        Assert.assertNotNull(metadata.outputShape());
        Assert.assertEquals(DataShapeKinds.JAVA, metadata.outputShape().getKind());
        Assert.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, metadata.outputShape().getMetadata(DataShapeMetaData.VARIANT).orElse(""));
        Assert.assertEquals(getSpecification("person-spec.json"), metadata.outputShape().getSpecification());
        Assert.assertEquals(2, metadata.outputShape().getVariants().size());
        Assert.assertEquals(DataShapeMetaData.VARIANT_COLLECTION, metadata.outputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
        Assert.assertEquals("person-list", metadata.outputShape().getVariants().get(0).getDescription());
        Assert.assertEquals("dummy", metadata.outputShape().getVariants().get(1).getMetadata().get(DataShapeMetaData.VARIANT));
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

        Assert.assertEquals(StepMetadataHelper.ANY_SHAPE, metadata.inputShape());
        Assert.assertEquals(StepMetadataHelper.ANY_SHAPE, metadata.outputShape());
    }

    @Test
    public void shouldCreateMetaDataFromEmptySurroundingSteps() {
        DynamicActionMetadata metadata = metadataHandler.createMetadata(aggregateStep, Collections.emptyList(), Collections.emptyList());

        Assert.assertEquals(StepMetadataHelper.NO_SHAPE, metadata.inputShape());
        Assert.assertEquals(StepMetadataHelper.NO_SHAPE, metadata.outputShape());
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

        Assert.assertEquals(StepMetadataHelper.NO_SHAPE, enrichedMetadata.inputShape());
        Assert.assertNotNull(enrichedMetadata.outputShape());
        Assert.assertEquals(DataShapeKinds.JAVA, enrichedMetadata.outputShape().getKind());
        Assert.assertEquals(DataShapeMetaData.VARIANT_COLLECTION, enrichedMetadata.outputShape().getMetadata(DataShapeMetaData.VARIANT).orElse(""));
        Assert.assertEquals(getSpecification("person-list-spec.json"), enrichedMetadata.outputShape().getSpecification());
        Assert.assertEquals(2, enrichedMetadata.outputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
        Assert.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, enrichedMetadata.outputShape().getVariants().get(1).getMetadata().get(DataShapeMetaData.VARIANT));
        Assert.assertEquals("person", enrichedMetadata.outputShape().getVariants().get(1).getDescription());
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

        Assert.assertNotNull(enrichedMetadata.inputShape());
        Assert.assertEquals(DataShapeKinds.JAVA, enrichedMetadata.inputShape().getKind());
        Assert.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, enrichedMetadata.inputShape().getMetadata(DataShapeMetaData.VARIANT).orElse(""));
        Assert.assertEquals(getSpecification("person-spec.json"), enrichedMetadata.inputShape().getSpecification());
        Assert.assertEquals(2, enrichedMetadata.inputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.inputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
        Assert.assertEquals(DataShapeMetaData.VARIANT_COLLECTION, enrichedMetadata.inputShape().getVariants().get(1).getMetadata().get(DataShapeMetaData.VARIANT));
        Assert.assertEquals("person-list", enrichedMetadata.inputShape().getVariants().get(1).getDescription());

        Assert.assertEquals(StepMetadataHelper.NO_SHAPE, enrichedMetadata.outputShape());
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

        Assert.assertEquals(StepMetadataHelper.NO_SHAPE, enrichedMetadata.inputShape());
        Assert.assertNotNull(enrichedMetadata.outputShape());
        Assert.assertEquals(DataShapeKinds.JSON_SCHEMA, enrichedMetadata.outputShape().getKind());
        Assert.assertEquals(DataShapeMetaData.VARIANT_COLLECTION, enrichedMetadata.outputShape().getMetadata(DataShapeMetaData.VARIANT).orElse(""));
        Assert.assertEquals(getSpecification("person-list-schema.json"), enrichedMetadata.outputShape().getSpecification());
        Assert.assertEquals(2, enrichedMetadata.outputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
        Assert.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, enrichedMetadata.outputShape().getVariants().get(1).getMetadata().get(DataShapeMetaData.VARIANT));
        Assert.assertEquals("person-schema", enrichedMetadata.outputShape().getVariants().get(1).getDescription());
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

        Assert.assertNotNull(enrichedMetadata.inputShape());
        Assert.assertEquals(DataShapeKinds.JSON_SCHEMA, enrichedMetadata.inputShape().getKind());
        Assert.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, enrichedMetadata.inputShape().getMetadata(DataShapeMetaData.VARIANT).orElse(""));
        Assert.assertEquals(getSpecification("person-schema.json"), enrichedMetadata.inputShape().getSpecification());
        Assert.assertEquals(2, enrichedMetadata.inputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.inputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
        Assert.assertEquals(DataShapeMetaData.VARIANT_COLLECTION, enrichedMetadata.inputShape().getVariants().get(1).getMetadata().get(DataShapeMetaData.VARIANT));
        Assert.assertEquals("person-list-schema", enrichedMetadata.inputShape().getVariants().get(1).getDescription());

        Assert.assertEquals(StepMetadataHelper.NO_SHAPE, enrichedMetadata.outputShape());
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

        Assert.assertNotNull(enrichedMetadata.inputShape());
        Assert.assertEquals(DataShapeKinds.JSON_SCHEMA, enrichedMetadata.inputShape().getKind());
        Assert.assertEquals(StringUtils.trimAllWhitespace(getSpecification("person-schema.json")), enrichedMetadata.inputShape().getSpecification());
        Assert.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, enrichedMetadata.inputShape().getMetadata().get(DataShapeMetaData.VARIANT));
        Assert.assertEquals(2, enrichedMetadata.inputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.inputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
        Assert.assertEquals(DataShapeMetaData.VARIANT_COLLECTION, enrichedMetadata.inputShape().getVariants().get(1).getMetadata().get(DataShapeMetaData.VARIANT));
        Assert.assertEquals("person-list-schema", enrichedMetadata.inputShape().getVariants().get(1).getDescription());

        Assert.assertNotNull(enrichedMetadata.outputShape());
        Assert.assertEquals(DataShapeKinds.JSON_SCHEMA, enrichedMetadata.outputShape().getKind());
        Assert.assertEquals(StringUtils.trimAllWhitespace(getSpecification("person-list-schema.json")), enrichedMetadata.outputShape().getSpecification());
        Assert.assertEquals(DataShapeMetaData.VARIANT_COLLECTION, enrichedMetadata.outputShape().getMetadata().get(DataShapeMetaData.VARIANT));
        Assert.assertEquals(2, enrichedMetadata.outputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
        Assert.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, enrichedMetadata.outputShape().getVariants().get(1).getMetadata().get(DataShapeMetaData.VARIANT));
        Assert.assertEquals("person-schema", enrichedMetadata.outputShape().getVariants().get(1).getDescription());
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

        Assert.assertNotNull(enrichedMetadata.inputShape());
        Assert.assertEquals(DataShapeKinds.JSON_SCHEMA, enrichedMetadata.inputShape().getKind());
        Assert.assertEquals(metadata.inputShape().getSpecification(), enrichedMetadata.inputShape().getSpecification());
        Assert.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, enrichedMetadata.inputShape().getMetadata().get(DataShapeMetaData.VARIANT));
        Assert.assertEquals(1, enrichedMetadata.inputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.inputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));

        Assert.assertNotNull(enrichedMetadata.outputShape());
        Assert.assertEquals(DataShapeKinds.JSON_SCHEMA, enrichedMetadata.outputShape().getKind());
        Assert.assertEquals(metadata.outputShape().getSpecification(), enrichedMetadata.outputShape().getSpecification());
        Assert.assertEquals(DataShapeMetaData.VARIANT_COLLECTION, enrichedMetadata.outputShape().getMetadata().get(DataShapeMetaData.VARIANT));
        Assert.assertEquals(1, enrichedMetadata.outputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
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

        Assert.assertEquals(StepMetadataHelper.NO_SHAPE, enrichedMetadata.inputShape());
        Assert.assertNotNull(enrichedMetadata.outputShape());
        Assert.assertEquals(DataShapeKinds.JSON_INSTANCE, enrichedMetadata.outputShape().getKind());
        Assert.assertEquals(DataShapeMetaData.VARIANT_COLLECTION, enrichedMetadata.outputShape().getMetadata(DataShapeMetaData.VARIANT).orElse(""));
        Assert.assertEquals(getSpecification("person-list-instance.json"), enrichedMetadata.outputShape().getSpecification());
        Assert.assertEquals(2, enrichedMetadata.outputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
        Assert.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, enrichedMetadata.outputShape().getVariants().get(1).getMetadata().get(DataShapeMetaData.VARIANT));
        Assert.assertEquals("person-instance", enrichedMetadata.outputShape().getVariants().get(1).getDescription());
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

        Assert.assertNotNull(enrichedMetadata.inputShape());
        Assert.assertEquals(DataShapeKinds.JSON_SCHEMA, enrichedMetadata.inputShape().getKind());
        Assert.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, enrichedMetadata.inputShape().getMetadata(DataShapeMetaData.VARIANT).orElse(""));
        Assert.assertEquals(getSpecification("person-instance.json"), enrichedMetadata.inputShape().getSpecification());
        Assert.assertEquals(2, enrichedMetadata.inputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.inputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
        Assert.assertEquals(DataShapeMetaData.VARIANT_COLLECTION, enrichedMetadata.inputShape().getVariants().get(1).getMetadata().get(DataShapeMetaData.VARIANT));
        Assert.assertEquals("person-list-instance", enrichedMetadata.inputShape().getVariants().get(1).getDescription());

        Assert.assertEquals(StepMetadataHelper.NO_SHAPE, enrichedMetadata.outputShape());
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

        Assert.assertNotNull(enrichedMetadata.inputShape());
        Assert.assertEquals(DataShapeKinds.JSON_INSTANCE, enrichedMetadata.inputShape().getKind());
        Assert.assertEquals(StringUtils.trimAllWhitespace(getSpecification("person-instance.json")), StringUtils.trimAllWhitespace(enrichedMetadata.inputShape().getSpecification()));
        Assert.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, enrichedMetadata.inputShape().getMetadata().get(DataShapeMetaData.VARIANT));
        Assert.assertEquals(2, enrichedMetadata.inputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.inputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
        Assert.assertEquals(DataShapeMetaData.VARIANT_COLLECTION, enrichedMetadata.inputShape().getVariants().get(1).getMetadata().get(DataShapeMetaData.VARIANT));
        Assert.assertEquals("person-list-instance", enrichedMetadata.inputShape().getVariants().get(1).getDescription());

        Assert.assertNotNull(enrichedMetadata.outputShape());
        Assert.assertEquals(DataShapeKinds.JSON_INSTANCE, enrichedMetadata.outputShape().getKind());
        Assert.assertEquals(StringUtils.trimAllWhitespace(getSpecification("person-list-instance.json")), StringUtils.trimAllWhitespace(enrichedMetadata.outputShape().getSpecification()));
        Assert.assertEquals(DataShapeMetaData.VARIANT_COLLECTION, enrichedMetadata.outputShape().getMetadata().get(DataShapeMetaData.VARIANT));
        Assert.assertEquals(2, enrichedMetadata.outputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
        Assert.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, enrichedMetadata.outputShape().getVariants().get(1).getMetadata().get(DataShapeMetaData.VARIANT));
        Assert.assertEquals("person-instance", enrichedMetadata.outputShape().getVariants().get(1).getDescription());
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

        Assert.assertNotNull(enrichedMetadata.inputShape());
        Assert.assertEquals(DataShapeKinds.JSON_INSTANCE, enrichedMetadata.inputShape().getKind());
        Assert.assertEquals(metadata.inputShape().getSpecification(), enrichedMetadata.inputShape().getSpecification());
        Assert.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, enrichedMetadata.inputShape().getMetadata().get(DataShapeMetaData.VARIANT));
        Assert.assertEquals(1, enrichedMetadata.inputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.inputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));

        Assert.assertNotNull(enrichedMetadata.outputShape());
        Assert.assertEquals(DataShapeKinds.JSON_INSTANCE, enrichedMetadata.outputShape().getKind());
        Assert.assertEquals(metadata.outputShape().getSpecification(), enrichedMetadata.outputShape().getSpecification());
        Assert.assertEquals(DataShapeMetaData.VARIANT_COLLECTION, enrichedMetadata.outputShape().getMetadata().get(DataShapeMetaData.VARIANT));
        Assert.assertEquals(1, enrichedMetadata.outputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
    }

    @Test
    public void shouldAutoConvertAndExtractJsonUnifiedSchemaVariants() throws IOException {
        DynamicActionMetadata metadata = new DynamicActionMetadata.Builder()
                .inputShape(new DataShape.Builder()
                        .kind(DataShapeKinds.JSON_SCHEMA)
                        .specification(getSpecification("person-list-unified-response-schema.json"))
                        .putMetadata(DataShapeMetaData.UNIFIED, "true")
                        .description("person-list-schema")
                        .collectionType("List")
                        .type(Person.class.getName())
                        .collectionClassName(List.class.getName())
                        .addVariant(dummyShape(DataShapeKinds.JAVA))
                        .build())
                .outputShape(new DataShape.Builder()
                        .kind(DataShapeKinds.JSON_SCHEMA)
                        .specification(getSpecification("person-unified-response-schema.json"))
                        .putMetadata(DataShapeMetaData.UNIFIED, "true")
                        .description("person-schema")
                        .type(Person.class.getName())
                        .addVariant(dummyShape(DataShapeKinds.JAVA))
                        .build())
                .build();

        DynamicActionMetadata enrichedMetadata = metadataHandler.handle(metadata);

        Assert.assertNotNull(enrichedMetadata.inputShape());
        Assert.assertEquals(DataShapeKinds.JSON_SCHEMA, enrichedMetadata.inputShape().getKind());
        Assert.assertEquals(StringUtils.trimAllWhitespace(getSpecification("person-unified-response-schema.json")), enrichedMetadata.inputShape().getSpecification());
        Assert.assertEquals(DataShapeMetaData.VARIANT_ELEMENT, enrichedMetadata.inputShape().getMetadata().get(DataShapeMetaData.VARIANT));
        Assert.assertEquals("true", enrichedMetadata.inputShape().getMetadata().get(DataShapeMetaData.UNIFIED));
        Assert.assertEquals(1, enrichedMetadata.inputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.inputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));

        Assert.assertNotNull(enrichedMetadata.outputShape());
        Assert.assertEquals(DataShapeKinds.JSON_SCHEMA, enrichedMetadata.outputShape().getKind());
        Assert.assertEquals(StringUtils.trimAllWhitespace(getSpecification("person-list-unified-response-schema.json")), enrichedMetadata.outputShape().getSpecification());
        Assert.assertEquals(DataShapeMetaData.VARIANT_COLLECTION, enrichedMetadata.outputShape().getMetadata().get(DataShapeMetaData.VARIANT));
        Assert.assertEquals("true", enrichedMetadata.outputShape().getMetadata().get(DataShapeMetaData.UNIFIED));
        Assert.assertEquals(1, enrichedMetadata.outputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(DataShapeMetaData.VARIANT));
    }

    private DataShape dummyShape(DataShapeKinds kind) {
        return new DataShape.Builder()
                .kind(kind)
                .description("dummyShape")
                .specification("{}")
                .putMetadata(DataShapeMetaData.VARIANT, "dummy")
                .build();
    }

    private String getSpecification(String path) throws IOException {
        return IOUtils.toString(new ClassPathResource(path, AggregateMetadataHandlerTest.class).getInputStream(), StandardCharsets.UTF_8);
    }
}
