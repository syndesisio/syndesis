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
public class SplitMetadataHandlerTest {

    private SplitMetadataHandler metadataHandler = new SplitMetadataHandler();

    @Test
    public void shouldExtractJavaElementVariant() throws IOException {
        DynamicActionMetadata metadata = new DynamicActionMetadata.Builder()
                .inputShape(StepActionHandler.NO_SHAPE)
                .outputShape(new DataShape.Builder()
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
                .build();

        DynamicActionMetadata enrichedMetadata = metadataHandler.handle(metadata);

        Assert.assertEquals(StepActionHandler.NO_SHAPE, enrichedMetadata.inputShape());
        Assert.assertNotNull(enrichedMetadata.outputShape());
        Assert.assertEquals(DataShapeKinds.JAVA, enrichedMetadata.outputShape().getKind());
        Assert.assertEquals(StepMetadataHandler.VARIANT_ELEMENT, enrichedMetadata.outputShape().getMetadata(StepMetadataHandler.VARIANT_METADATA_KEY).orElse(""));
        Assert.assertEquals(getSpecification("person-spec.json"), enrichedMetadata.outputShape().getSpecification());
        Assert.assertEquals(2, enrichedMetadata.outputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(StepMetadataHandler.VARIANT_METADATA_KEY));
        Assert.assertEquals(StepMetadataHandler.VARIANT_COLLECTION, enrichedMetadata.outputShape().getVariants().get(1).getMetadata().get(StepMetadataHandler.VARIANT_METADATA_KEY));
        Assert.assertEquals("person-list", enrichedMetadata.outputShape().getVariants().get(1).getDescription());
    }

    @Test
    public void shouldExtractJsonSchemaElementVariant() throws IOException {
        DynamicActionMetadata metadata = new DynamicActionMetadata.Builder()
                .inputShape(StepActionHandler.NO_SHAPE)
                .outputShape(new DataShape.Builder()
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
                .build();

        DynamicActionMetadata enrichedMetadata = metadataHandler.handle(metadata);

        Assert.assertEquals(StepActionHandler.NO_SHAPE, enrichedMetadata.inputShape());
        Assert.assertNotNull(enrichedMetadata.outputShape());
        Assert.assertEquals(DataShapeKinds.JSON_SCHEMA, enrichedMetadata.outputShape().getKind());
        Assert.assertEquals(StepMetadataHandler.VARIANT_ELEMENT, enrichedMetadata.outputShape().getMetadata(StepMetadataHandler.VARIANT_METADATA_KEY).orElse(""));
        Assert.assertEquals(getSpecification("person-schema.json"), enrichedMetadata.outputShape().getSpecification());
        Assert.assertEquals(2, enrichedMetadata.outputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(StepMetadataHandler.VARIANT_METADATA_KEY));
        Assert.assertEquals(StepMetadataHandler.VARIANT_COLLECTION, enrichedMetadata.outputShape().getVariants().get(1).getMetadata().get(StepMetadataHandler.VARIANT_METADATA_KEY));
        Assert.assertEquals("person-list-schema", enrichedMetadata.outputShape().getVariants().get(1).getDescription());
    }

    @Test
    public void shouldAutoConvertAndExtractJsonSchemaElement() throws IOException {
        DynamicActionMetadata metadata = new DynamicActionMetadata.Builder()
                .inputShape(StepActionHandler.NO_SHAPE)
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

        Assert.assertEquals(StepActionHandler.NO_SHAPE, enrichedMetadata.inputShape());
        Assert.assertNotNull(enrichedMetadata.outputShape());
        Assert.assertEquals(DataShapeKinds.JSON_SCHEMA, enrichedMetadata.outputShape().getKind());
        Assert.assertEquals(StringUtils.trimAllWhitespace(getSpecification("person-schema.json")), enrichedMetadata.outputShape().getSpecification());
        Assert.assertEquals(StepMetadataHandler.VARIANT_ELEMENT, enrichedMetadata.outputShape().getMetadata(StepMetadataHandler.VARIANT_METADATA_KEY).orElse(""));
        Assert.assertEquals(2, enrichedMetadata.outputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(StepMetadataHandler.VARIANT_METADATA_KEY));
        Assert.assertEquals(StepMetadataHandler.VARIANT_COLLECTION, enrichedMetadata.outputShape().getVariants().get(1).getMetadata().get(StepMetadataHandler.VARIANT_METADATA_KEY));
        Assert.assertEquals("person-list-schema", enrichedMetadata.outputShape().getVariants().get(1).getDescription());
    }

    @Test
    public void shouldExtractAlreadyGivenJsonSchemaVariant() throws IOException {
        DynamicActionMetadata metadata = new DynamicActionMetadata.Builder()
                .inputShape(StepActionHandler.NO_SHAPE)
                .outputShape(new DataShape.Builder()
                        .kind(DataShapeKinds.JSON_SCHEMA)
                        .specification(getSpecification("person-schema.json"))
                        .description("person-schema")
                        .type(Person.class.getName())
                        .addVariant(dummyShape(DataShapeKinds.JAVA))
                        .build())
                .build();

        DynamicActionMetadata enrichedMetadata = metadataHandler.handle(metadata);

        Assert.assertEquals(StepActionHandler.NO_SHAPE, enrichedMetadata.inputShape());
        Assert.assertNotNull(enrichedMetadata.outputShape());
        Assert.assertEquals(DataShapeKinds.JSON_SCHEMA, enrichedMetadata.outputShape().getKind());
        Assert.assertEquals(metadata.outputShape().getSpecification(), enrichedMetadata.outputShape().getSpecification());
        Assert.assertEquals(StepMetadataHandler.VARIANT_ELEMENT, enrichedMetadata.outputShape().getMetadata(StepMetadataHandler.VARIANT_METADATA_KEY).orElse(""));
        Assert.assertEquals(1, enrichedMetadata.outputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(StepMetadataHandler.VARIANT_METADATA_KEY));
    }

    @Test
    public void shouldExtractJsonInstanceElementVariant() throws IOException {
        DynamicActionMetadata metadata = new DynamicActionMetadata.Builder()
                .inputShape(StepActionHandler.NO_SHAPE)
                .outputShape(new DataShape.Builder()
                        .kind(DataShapeKinds.JSON_INSTANCE)
                        .specification(getSpecification("person-list-instance.json"))
                        .description("person-list-instance")
                        .collectionType("List")
                        .type(Person.class.getName())
                        .collectionClassName(List.class.getName())
                        .putMetadata(StepMetadataHandler.VARIANT_METADATA_KEY, StepMetadataHandler.VARIANT_COLLECTION)
                        .addVariant(new DataShape.Builder()
                                .kind(DataShapeKinds.JSON_INSTANCE)
                                .specification(getSpecification("person-instance.json"))
                                .type(Person.class.getName())
                                .putMetadata(StepMetadataHandler.VARIANT_METADATA_KEY, StepMetadataHandler.VARIANT_ELEMENT)
                                .build())
                        .addVariant(dummyShape(DataShapeKinds.JAVA))
                        .build())
                .build();

        DynamicActionMetadata enrichedMetadata = metadataHandler.handle(metadata);

        Assert.assertEquals(StepActionHandler.NO_SHAPE, enrichedMetadata.inputShape());
        Assert.assertNotNull(enrichedMetadata.outputShape());
        Assert.assertEquals(DataShapeKinds.JSON_INSTANCE, enrichedMetadata.outputShape().getKind());
        Assert.assertEquals(StepMetadataHandler.VARIANT_ELEMENT, enrichedMetadata.outputShape().getMetadata(StepMetadataHandler.VARIANT_METADATA_KEY).orElse(""));
        Assert.assertEquals(getSpecification("person-instance.json"), enrichedMetadata.outputShape().getSpecification());
        Assert.assertEquals(2, enrichedMetadata.outputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(StepMetadataHandler.VARIANT_METADATA_KEY));
        Assert.assertEquals(StepMetadataHandler.VARIANT_COLLECTION, enrichedMetadata.outputShape().getVariants().get(1).getMetadata().get(StepMetadataHandler.VARIANT_METADATA_KEY));
        Assert.assertEquals("person-list-instance", enrichedMetadata.outputShape().getVariants().get(1).getDescription());
    }

    @Test
    public void shouldAutoConvertAndExtractJsonInstanceElement() throws IOException {
        DynamicActionMetadata metadata = new DynamicActionMetadata.Builder()
                .inputShape(StepActionHandler.NO_SHAPE)
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

        Assert.assertEquals(StepActionHandler.NO_SHAPE, enrichedMetadata.inputShape());
        Assert.assertNotNull(enrichedMetadata.outputShape());
        Assert.assertEquals(DataShapeKinds.JSON_INSTANCE, enrichedMetadata.outputShape().getKind());
        Assert.assertEquals(StringUtils.trimAllWhitespace(getSpecification("person-instance.json")), enrichedMetadata.outputShape().getSpecification());
        Assert.assertEquals(StepMetadataHandler.VARIANT_ELEMENT, enrichedMetadata.outputShape().getMetadata(StepMetadataHandler.VARIANT_METADATA_KEY).orElse(""));
        Assert.assertEquals(2, enrichedMetadata.outputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(StepMetadataHandler.VARIANT_METADATA_KEY));
        Assert.assertEquals(StepMetadataHandler.VARIANT_COLLECTION, enrichedMetadata.outputShape().getVariants().get(1).getMetadata().get(StepMetadataHandler.VARIANT_METADATA_KEY));
        Assert.assertEquals("person-list-instance", enrichedMetadata.outputShape().getVariants().get(1).getDescription());
    }

    @Test
    public void shouldExtractAlreadyGivenJsonInstanceVariant() throws IOException {
        DynamicActionMetadata metadata = new DynamicActionMetadata.Builder()
                .inputShape(StepActionHandler.NO_SHAPE)
                .outputShape(new DataShape.Builder()
                        .kind(DataShapeKinds.JSON_INSTANCE)
                        .specification(getSpecification("person-instance.json"))
                        .description("person-instance")
                        .type(Person.class.getName())
                        .addVariant(dummyShape(DataShapeKinds.JAVA))
                        .build())
                .build();

        DynamicActionMetadata enrichedMetadata = metadataHandler.handle(metadata);

        Assert.assertEquals(StepActionHandler.NO_SHAPE, enrichedMetadata.inputShape());
        Assert.assertNotNull(enrichedMetadata.outputShape());
        Assert.assertEquals(DataShapeKinds.JSON_INSTANCE, enrichedMetadata.outputShape().getKind());
        Assert.assertEquals(metadata.outputShape().getSpecification(), enrichedMetadata.outputShape().getSpecification());
        Assert.assertEquals(StepMetadataHandler.VARIANT_ELEMENT, enrichedMetadata.outputShape().getMetadata(StepMetadataHandler.VARIANT_METADATA_KEY).orElse(""));
        Assert.assertEquals(1, enrichedMetadata.outputShape().getVariants().size());
        Assert.assertEquals("dummy", enrichedMetadata.outputShape().getVariants().get(0).getMetadata().get(StepMetadataHandler.VARIANT_METADATA_KEY));
    }

    private DataShape dummyShape(DataShapeKinds kind) {
        return new DataShape.Builder()
                .kind(kind)
                .specification("{}")
                .description("dummyShape")
                .putMetadata(StepMetadataHandler.VARIANT_METADATA_KEY, "dummy")
                .build();
    }

    private String getSpecification(String path) throws IOException {
        return IOUtils.toString(new ClassPathResource(path, SplitMetadataHandlerTest.class).getInputStream(), StandardCharsets.UTF_8);
    }
}