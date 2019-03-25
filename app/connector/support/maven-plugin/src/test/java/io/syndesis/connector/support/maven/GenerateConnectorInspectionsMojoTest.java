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
package io.syndesis.connector.support.maven;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.zip.GZIPInputStream;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import io.atlasmap.java.v2.JavaClass;
import io.atlasmap.v2.CollectionType;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.DataShapeMetaData;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertNotNull;

public class GenerateConnectorInspectionsMojoTest {

    @Test
    public void generateJavaInspectionTest() throws Exception {
        DataShape source = new DataShape.Builder()
            .type(MyShape.class.getTypeName())
            .kind(DataShapeKinds.JAVA)
            .build();

        DataShape enriched = GenerateConnectorInspectionsMojo.generateInspections(new URL[0], source);

        assertThat(enriched.getSpecification()).isNotEmpty();

        JavaClass clazz = io.atlasmap.v2.Json.mapper().readValue(enriched.getSpecification(), JavaClass.class);
        assertThat(clazz.getCollectionClassName()).isNull();
        assertThat(clazz.getCollectionType()).isEqualTo(CollectionType.NONE);
    }

    @Test
    public void generateJavaCollectionInspectionTest() throws Exception {
        DataShape source = new DataShape.Builder()
            .type(MyShape.class.getTypeName())
            .kind(DataShapeKinds.JAVA)
            .collectionType(CollectionType.LIST.value())
            .collectionClassName(ArrayList.class.getName())
            .build();

        DataShape enriched = GenerateConnectorInspectionsMojo.generateInspections(new URL[0], source);

        assertThat(enriched.getSpecification()).isNotEmpty();

        JavaClass clazz = io.atlasmap.v2.Json.mapper().readValue(enriched.getSpecification(), JavaClass.class);
        assertThat(clazz.getCollectionClassName()).isEqualTo(ArrayList.class.getName());
        assertThat(clazz.getCollectionType()).isEqualTo(CollectionType.LIST);
    }

    @Test
    public void generateJavaInspectionWithCompressionTest() throws Exception {
        DataShape source1 = new DataShape.Builder()
            .type(MyShape.class.getTypeName())
            .kind(DataShapeKinds.JAVA)
            .build();
        DataShape source2 = new DataShape.Builder()
            .type(MyShape.class.getTypeName())
            .kind(DataShapeKinds.JAVA)
            .putMetadata(DataShapeMetaData.SHOULD_COMPRESS, "true")
            .build();

        DataShape enriched1 = GenerateConnectorInspectionsMojo.generateInspections(new URL[0], source1);
        DataShape enriched2 = GenerateConnectorInspectionsMojo.generateInspections(new URL[0], source2);

        assertThat(enriched1.getSpecification()).isNotEmpty();
        assertThat(enriched2.getSpecification()).isNotEmpty();

        byte[] data = Base64.getDecoder().decode(enriched2.getSpecification());

        try (InputStream is = new GZIPInputStream(new ByteArrayInputStream(data))) {
            String expected = enriched1.getSpecification();
            String decoded = IOUtils.toString(is, StandardCharsets.UTF_8);

            assertThat(decoded).isEqualTo(expected);
        }
    }

    @Test
    public void generateJavaInspectionWithVariantTest() throws Exception {
        DataShape source = new DataShape.Builder()
            .type(MyShape.class.getTypeName())
            .kind(DataShapeKinds.JAVA)
            .name("collection")
            .collectionType(CollectionType.LIST.value())
            .collectionClassName(ArrayList.class.getName())
            .putMetadata(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_COLLECTION)
            .addVariant(
                new DataShape.Builder()
                    .type(MyShapeVariant.class.getTypeName())
                    .kind(DataShapeKinds.JAVA)
                    .name("element")
                    .putMetadata(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_ELEMENT)
                    .build())
            .build();

        DataShape enriched = GenerateConnectorInspectionsMojo.generateInspections(new URL[0], source);

        assertThat(enriched.getSpecification()).isNotEmpty();
        assertThat(enriched.findVariantByMeta(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_ELEMENT)).isPresent();
        assertThat(enriched.findVariantByMeta(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_ELEMENT)).get().hasFieldOrPropertyWithValue("name", "element");
        assertThat(enriched.findVariantByMeta(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_ELEMENT)).get().extracting("specification").isNotEmpty();
        assertThat(enriched.findVariantByMeta(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_COLLECTION)).isPresent();
        assertThat(enriched.findVariantByMeta(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_COLLECTION)).get().hasFieldOrPropertyWithValue("name", "collection");
        assertThat(enriched.findVariantByMeta(DataShapeMetaData.VARIANT, DataShapeMetaData.VARIANT_COLLECTION)).get().extracting("specification").isNotEmpty();
    }

    private File getFile(String fileName) throws Exception {
        URL resourcePath = getClass().getResource(fileName);
        assertNotNull(resourcePath);
        URI resourceURI = resourcePath.toURI();

        return new File(resourceURI);
    }

    @Test
    public void validFileAgainstSchema() throws Exception {
        GenerateConnectorInspectionsMojo mojo = new GenerateConnectorInspectionsMojo();

        JsonNode jsonNode = mojo.validateWithSchema(getFile("/my-test-connector.json"));
        assertNotNull(jsonNode);
    }

    @Test
    public void unformedFileAgainstSchema() throws Exception {
        GenerateConnectorInspectionsMojo mojo = new GenerateConnectorInspectionsMojo();

        String unformedConnectorFile = "my-unformed-connector.json";
        assertThatExceptionOfType(MojoExecutionException.class)
            .isThrownBy(() -> {
                mojo.validateWithSchema(getFile("/" + unformedConnectorFile));
            })
            .withCauseInstanceOf(JsonParseException.class)
            .withStackTraceContaining("line: 9, column: 12");
    }

    @Test
    public void invalidFileAgainstSchema() throws Exception {
        GenerateConnectorInspectionsMojo mojo = new GenerateConnectorInspectionsMojo();
        String invalidConnectorFile = "my-invalid-connector.json";

        assertThatExceptionOfType(MojoExecutionException.class)
            .isThrownBy(() -> {
                mojo.validateWithSchema(getFile("/" + invalidConnectorFile));
            })
            .withMessageContaining("Validation of json file " + invalidConnectorFile + " failed, see previous logs");
    }

    // ******************
    //
    // Support
    //
    // ******************

    public static class MyShape {
    }
    public static class MyShapeVariant {
    }
}
