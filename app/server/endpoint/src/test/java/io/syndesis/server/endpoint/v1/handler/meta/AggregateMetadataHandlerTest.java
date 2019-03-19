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
import java.util.List;

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.connection.DynamicActionMetadata;
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

    @Test
    public void shouldExtractJavaOutputCollectionVariant() throws IOException {
        DynamicActionMetadata metadata = new DynamicActionMetadata.Builder()
                .inputShape(StepActionHandler.NO_SHAPE)
                .outputShape(new DataShape.Builder()
                        .kind(DataShapeKinds.JAVA)
                        .specification(getSpecification("person-spec.json"))
                        .description("person")
                        .type(Person.class.getName())
                        .putMetadata(StepMetadataHandler.VARIANT_METADATA_KEY, StepMetadataHandler.VARIANT_ELEMENT)
                        .addVariant(new DataShape.Builder()
                                .kind(DataShapeKinds.JAVA)
                                .specification(getSpecification("person-list-spec.json"))
                                .collectionType("List")
                                .type(Person.class.getName())
                                .collectionClassName(List.class.getName())
                                .putMetadata(StepMetadataHandler.VARIANT_METADATA_KEY, StepMetadataHandler.VARIANT_COLLECTION)
                                .build())
                        .addVariant(dummyShape(DataShapeKinds.JSON_INSTANCE))
                        .build())
                .build();

        DynamicActionMetadata enrichedMetadata = metadataHandler.handle(metadata);

        Assert.assertEquals(StepActionHandler.NO_SHAPE, enrichedMetadata.inputShape());
        Assert.assertNotNull(enrichedMetadata.outputShape());
        Assert.assertEquals(DataShapeKinds.JAVA, enrichedMetadata.outputShape().getKind());
        Assert.assertEquals(StepMetadataHandler.VARIANT_COLLECTION, enrichedMetadata.outputShape().getMetadata(StepMetadataHandler.VARIANT_METADATA_KEY).orElse(""));
        Assert.assertEquals(getSpecification("person-list-spec.json"), enrichedMetadata.outputShape().getSpecification());
        Assert.assertEquals(2, enrichedMetadata.outputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(StepMetadataHandler.VARIANT_METADATA_KEY));
        Assert.assertEquals(StepMetadataHandler.VARIANT_ELEMENT, enrichedMetadata.outputShape().getVariants().get(1).getMetadata().get(StepMetadataHandler.VARIANT_METADATA_KEY));
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
                        .putMetadata(StepMetadataHandler.VARIANT_METADATA_KEY, StepMetadataHandler.VARIANT_COLLECTION)
                        .addVariant(new DataShape.Builder()
                                .kind(DataShapeKinds.JAVA)
                                .specification(getSpecification("person-spec.json"))
                                .type(Person.class.getName())
                                .putMetadata(StepMetadataHandler.VARIANT_METADATA_KEY, StepMetadataHandler.VARIANT_ELEMENT)
                                .build())
                        .addVariant(dummyShape(DataShapeKinds.JSON_INSTANCE))
                        .build())
                .outputShape(StepActionHandler.NO_SHAPE)
                .build();

        DynamicActionMetadata enrichedMetadata = metadataHandler.handle(metadata);

        Assert.assertNotNull(enrichedMetadata.inputShape());
        Assert.assertEquals(DataShapeKinds.JAVA, enrichedMetadata.inputShape().getKind());
        Assert.assertEquals(StepMetadataHandler.VARIANT_ELEMENT, enrichedMetadata.inputShape().getMetadata(StepMetadataHandler.VARIANT_METADATA_KEY).orElse(""));
        Assert.assertEquals(getSpecification("person-spec.json"), enrichedMetadata.inputShape().getSpecification());
        Assert.assertEquals(2, enrichedMetadata.inputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.inputShape().getVariants().get(0).getMetadata().get(StepMetadataHandler.VARIANT_METADATA_KEY));
        Assert.assertEquals(StepMetadataHandler.VARIANT_COLLECTION, enrichedMetadata.inputShape().getVariants().get(1).getMetadata().get(StepMetadataHandler.VARIANT_METADATA_KEY));
        Assert.assertEquals("person-list", enrichedMetadata.inputShape().getVariants().get(1).getDescription());

        Assert.assertEquals(StepActionHandler.NO_SHAPE, enrichedMetadata.outputShape());
    }

    @Test
    public void shouldExtractJsonSchemaOutputCollectionVariant() throws IOException {
        DynamicActionMetadata metadata = new DynamicActionMetadata.Builder()
                .inputShape(StepActionHandler.NO_SHAPE)
                .outputShape(new DataShape.Builder()
                        .kind(DataShapeKinds.JSON_SCHEMA)
                        .specification(getSpecification("person-schema.json"))
                        .description("person-schema")
                        .type(Person.class.getName())
                        .putMetadata(StepMetadataHandler.VARIANT_METADATA_KEY, StepMetadataHandler.VARIANT_ELEMENT)
                        .addVariant(new DataShape.Builder()
                                .kind(DataShapeKinds.JSON_SCHEMA)
                                .specification(getSpecification("person-list-schema.json"))
                                .collectionType("List")
                                .type(Person.class.getName())
                                .collectionClassName(List.class.getName())
                                .putMetadata(StepMetadataHandler.VARIANT_METADATA_KEY, StepMetadataHandler.VARIANT_COLLECTION)
                                .build())
                        .addVariant(dummyShape(DataShapeKinds.JAVA))
                        .build())
                .build();

        DynamicActionMetadata enrichedMetadata = metadataHandler.handle(metadata);

        Assert.assertEquals(StepActionHandler.NO_SHAPE, enrichedMetadata.inputShape());
        Assert.assertNotNull(enrichedMetadata.outputShape());
        Assert.assertEquals(DataShapeKinds.JSON_SCHEMA, enrichedMetadata.outputShape().getKind());
        Assert.assertEquals(StepMetadataHandler.VARIANT_COLLECTION, enrichedMetadata.outputShape().getMetadata(StepMetadataHandler.VARIANT_METADATA_KEY).orElse(""));
        Assert.assertEquals(getSpecification("person-list-schema.json"), enrichedMetadata.outputShape().getSpecification());
        Assert.assertEquals(2, enrichedMetadata.outputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(StepMetadataHandler.VARIANT_METADATA_KEY));
        Assert.assertEquals(StepMetadataHandler.VARIANT_ELEMENT, enrichedMetadata.outputShape().getVariants().get(1).getMetadata().get(StepMetadataHandler.VARIANT_METADATA_KEY));
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
                        .putMetadata(StepMetadataHandler.VARIANT_METADATA_KEY, StepMetadataHandler.VARIANT_COLLECTION)
                        .addVariant(new DataShape.Builder()
                                .kind(DataShapeKinds.JSON_SCHEMA)
                                .specification(getSpecification("person-schema.json"))
                                .type(Person.class.getName())
                                .putMetadata(StepMetadataHandler.VARIANT_METADATA_KEY, StepMetadataHandler.VARIANT_ELEMENT)
                                .build())
                        .addVariant(dummyShape(DataShapeKinds.JAVA))
                        .build())
                .outputShape(StepActionHandler.NO_SHAPE)
                .build();

        DynamicActionMetadata enrichedMetadata = metadataHandler.handle(metadata);

        Assert.assertNotNull(enrichedMetadata.inputShape());
        Assert.assertEquals(DataShapeKinds.JSON_SCHEMA, enrichedMetadata.inputShape().getKind());
        Assert.assertEquals(StepMetadataHandler.VARIANT_ELEMENT, enrichedMetadata.inputShape().getMetadata(StepMetadataHandler.VARIANT_METADATA_KEY).orElse(""));
        Assert.assertEquals(getSpecification("person-schema.json"), enrichedMetadata.inputShape().getSpecification());
        Assert.assertEquals(2, enrichedMetadata.inputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.inputShape().getVariants().get(0).getMetadata().get(StepMetadataHandler.VARIANT_METADATA_KEY));
        Assert.assertEquals(StepMetadataHandler.VARIANT_COLLECTION, enrichedMetadata.inputShape().getVariants().get(1).getMetadata().get(StepMetadataHandler.VARIANT_METADATA_KEY));
        Assert.assertEquals("person-list-schema", enrichedMetadata.inputShape().getVariants().get(1).getDescription());

        Assert.assertEquals(StepActionHandler.NO_SHAPE, enrichedMetadata.outputShape());
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
        Assert.assertEquals(StepMetadataHandler.VARIANT_ELEMENT, enrichedMetadata.inputShape().getMetadata().get(StepMetadataHandler.VARIANT_METADATA_KEY));
        Assert.assertEquals(2, enrichedMetadata.inputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.inputShape().getVariants().get(0).getMetadata().get(StepMetadataHandler.VARIANT_METADATA_KEY));
        Assert.assertEquals(StepMetadataHandler.VARIANT_COLLECTION, enrichedMetadata.inputShape().getVariants().get(1).getMetadata().get(StepMetadataHandler.VARIANT_METADATA_KEY));
        Assert.assertEquals("person-list-schema", enrichedMetadata.inputShape().getVariants().get(1).getDescription());

        Assert.assertNotNull(enrichedMetadata.outputShape());
        Assert.assertEquals(DataShapeKinds.JSON_SCHEMA, enrichedMetadata.outputShape().getKind());
        Assert.assertEquals(StringUtils.trimAllWhitespace(getSpecification("person-list-schema.json")), enrichedMetadata.outputShape().getSpecification());
        Assert.assertEquals(StepMetadataHandler.VARIANT_COLLECTION, enrichedMetadata.outputShape().getMetadata().get(StepMetadataHandler.VARIANT_METADATA_KEY));
        Assert.assertEquals(2, enrichedMetadata.outputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(StepMetadataHandler.VARIANT_METADATA_KEY));
        Assert.assertEquals(StepMetadataHandler.VARIANT_ELEMENT, enrichedMetadata.outputShape().getVariants().get(1).getMetadata().get(StepMetadataHandler.VARIANT_METADATA_KEY));
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
        Assert.assertEquals(StepMetadataHandler.VARIANT_ELEMENT, enrichedMetadata.inputShape().getMetadata().get(StepMetadataHandler.VARIANT_METADATA_KEY));
        Assert.assertEquals(1, enrichedMetadata.inputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.inputShape().getVariants().get(0).getMetadata().get(StepMetadataHandler.VARIANT_METADATA_KEY));

        Assert.assertNotNull(enrichedMetadata.outputShape());
        Assert.assertEquals(DataShapeKinds.JSON_SCHEMA, enrichedMetadata.outputShape().getKind());
        Assert.assertEquals(metadata.outputShape().getSpecification(), enrichedMetadata.outputShape().getSpecification());
        Assert.assertEquals(StepMetadataHandler.VARIANT_COLLECTION, enrichedMetadata.outputShape().getMetadata().get(StepMetadataHandler.VARIANT_METADATA_KEY));
        Assert.assertEquals(1, enrichedMetadata.outputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(StepMetadataHandler.VARIANT_METADATA_KEY));
    }

    @Test
    public void shouldExtractJsonInstanceOutputCollectionVariant() throws IOException {
        DynamicActionMetadata metadata = new DynamicActionMetadata.Builder()
                .inputShape(StepActionHandler.NO_SHAPE)
                .outputShape(new DataShape.Builder()
                        .kind(DataShapeKinds.JSON_INSTANCE)
                        .specification(getSpecification("person-instance.json"))
                        .description("person-instance")
                        .type(Person.class.getName())
                        .putMetadata(StepMetadataHandler.VARIANT_METADATA_KEY, StepMetadataHandler.VARIANT_ELEMENT)
                        .addVariant(new DataShape.Builder()
                                .kind(DataShapeKinds.JSON_INSTANCE)
                                .specification(getSpecification("person-list-instance.json"))
                                .collectionType("List")
                                .type(Person.class.getName())
                                .collectionClassName(List.class.getName())
                                .putMetadata(StepMetadataHandler.VARIANT_METADATA_KEY, StepMetadataHandler.VARIANT_COLLECTION)
                                .build())
                        .addVariant(dummyShape(DataShapeKinds.JAVA))
                        .build())
                .build();

        DynamicActionMetadata enrichedMetadata = metadataHandler.handle(metadata);

        Assert.assertEquals(StepActionHandler.NO_SHAPE, enrichedMetadata.inputShape());
        Assert.assertNotNull(enrichedMetadata.outputShape());
        Assert.assertEquals(DataShapeKinds.JSON_INSTANCE, enrichedMetadata.outputShape().getKind());
        Assert.assertEquals(StepMetadataHandler.VARIANT_COLLECTION, enrichedMetadata.outputShape().getMetadata(StepMetadataHandler.VARIANT_METADATA_KEY).orElse(""));
        Assert.assertEquals(getSpecification("person-list-instance.json"), enrichedMetadata.outputShape().getSpecification());
        Assert.assertEquals(2, enrichedMetadata.outputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(StepMetadataHandler.VARIANT_METADATA_KEY));
        Assert.assertEquals(StepMetadataHandler.VARIANT_ELEMENT, enrichedMetadata.outputShape().getVariants().get(1).getMetadata().get(StepMetadataHandler.VARIANT_METADATA_KEY));
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
                        .putMetadata(StepMetadataHandler.VARIANT_METADATA_KEY, StepMetadataHandler.VARIANT_COLLECTION)
                        .addVariant(new DataShape.Builder()
                                .kind(DataShapeKinds.JSON_SCHEMA)
                                .specification(getSpecification("person-instance.json"))
                                .type(Person.class.getName())
                                .putMetadata(StepMetadataHandler.VARIANT_METADATA_KEY, StepMetadataHandler.VARIANT_ELEMENT)
                                .build())
                        .addVariant(dummyShape(DataShapeKinds.JAVA))
                        .build())
                .outputShape(StepActionHandler.NO_SHAPE)
                .build();

        DynamicActionMetadata enrichedMetadata = metadataHandler.handle(metadata);

        Assert.assertNotNull(enrichedMetadata.inputShape());
        Assert.assertEquals(DataShapeKinds.JSON_SCHEMA, enrichedMetadata.inputShape().getKind());
        Assert.assertEquals(StepMetadataHandler.VARIANT_ELEMENT, enrichedMetadata.inputShape().getMetadata(StepMetadataHandler.VARIANT_METADATA_KEY).orElse(""));
        Assert.assertEquals(getSpecification("person-instance.json"), enrichedMetadata.inputShape().getSpecification());
        Assert.assertEquals(2, enrichedMetadata.inputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.inputShape().getVariants().get(0).getMetadata().get(StepMetadataHandler.VARIANT_METADATA_KEY));
        Assert.assertEquals(StepMetadataHandler.VARIANT_COLLECTION, enrichedMetadata.inputShape().getVariants().get(1).getMetadata().get(StepMetadataHandler.VARIANT_METADATA_KEY));
        Assert.assertEquals("person-list-instance", enrichedMetadata.inputShape().getVariants().get(1).getDescription());

        Assert.assertEquals(StepActionHandler.NO_SHAPE, enrichedMetadata.outputShape());
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
        Assert.assertEquals(StepMetadataHandler.VARIANT_ELEMENT, enrichedMetadata.inputShape().getMetadata().get(StepMetadataHandler.VARIANT_METADATA_KEY));
        Assert.assertEquals(2, enrichedMetadata.inputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.inputShape().getVariants().get(0).getMetadata().get(StepMetadataHandler.VARIANT_METADATA_KEY));
        Assert.assertEquals(StepMetadataHandler.VARIANT_COLLECTION, enrichedMetadata.inputShape().getVariants().get(1).getMetadata().get(StepMetadataHandler.VARIANT_METADATA_KEY));
        Assert.assertEquals("person-list-instance", enrichedMetadata.inputShape().getVariants().get(1).getDescription());

        Assert.assertNotNull(enrichedMetadata.outputShape());
        Assert.assertEquals(DataShapeKinds.JSON_INSTANCE, enrichedMetadata.outputShape().getKind());
        Assert.assertEquals(StringUtils.trimAllWhitespace(getSpecification("person-list-instance.json")), StringUtils.trimAllWhitespace(enrichedMetadata.outputShape().getSpecification()));
        Assert.assertEquals(StepMetadataHandler.VARIANT_COLLECTION, enrichedMetadata.outputShape().getMetadata().get(StepMetadataHandler.VARIANT_METADATA_KEY));
        Assert.assertEquals(2, enrichedMetadata.outputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(StepMetadataHandler.VARIANT_METADATA_KEY));
        Assert.assertEquals(StepMetadataHandler.VARIANT_ELEMENT, enrichedMetadata.outputShape().getVariants().get(1).getMetadata().get(StepMetadataHandler.VARIANT_METADATA_KEY));
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
        Assert.assertEquals(StepMetadataHandler.VARIANT_ELEMENT, enrichedMetadata.inputShape().getMetadata().get(StepMetadataHandler.VARIANT_METADATA_KEY));
        Assert.assertEquals(1, enrichedMetadata.inputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.inputShape().getVariants().get(0).getMetadata().get(StepMetadataHandler.VARIANT_METADATA_KEY));

        Assert.assertNotNull(enrichedMetadata.outputShape());
        Assert.assertEquals(DataShapeKinds.JSON_INSTANCE, enrichedMetadata.outputShape().getKind());
        Assert.assertEquals(metadata.outputShape().getSpecification(), enrichedMetadata.outputShape().getSpecification());
        Assert.assertEquals(StepMetadataHandler.VARIANT_COLLECTION, enrichedMetadata.outputShape().getMetadata().get(StepMetadataHandler.VARIANT_METADATA_KEY));
        Assert.assertEquals(1, enrichedMetadata.outputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(StepMetadataHandler.VARIANT_METADATA_KEY));
    }

    private DataShape dummyShape(DataShapeKinds kind) {
        return new DataShape.Builder()
                .kind(kind)
                .description("dummyShape")
                .specification("{}")
                .putMetadata(StepMetadataHandler.VARIANT_METADATA_KEY, "dummy")
                .build();
    }

    private String getSpecification(String path) throws IOException {
        return IOUtils.toString(new ClassPathResource(path, AggregateMetadataHandlerTest.class).getInputStream(), StandardCharsets.UTF_8);
    }
}