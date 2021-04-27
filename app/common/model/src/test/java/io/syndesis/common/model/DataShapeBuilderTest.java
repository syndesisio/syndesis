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

package io.syndesis.common.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.zip.GZIPInputStream;

import io.syndesis.common.util.IOStreams;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DataShapeBuilderTest {

    private static final String SPECIFICATION = "some uncompressed data";
    private static final String COMPRESSED_SPECIFICATION = "H4sIAAAAAAAAACvOz01VKM1Lzs8tKEotLk5NUUhJLEkEALZP9NQWAAAA";
    private static final String VARIANT_SPECIFICATION = "some uncompressed variant data";
    private static final String COMPRESSED_VARIANT_SPECIFICATION = "H4sIAAAAAAAAACvOz01VKM1Lzs8tKEotLk5NUShLLMpMzCtRSEksSQQAQv1meR4AAAA=";

    @Test
    public void shouldNotCompressBasedOnMetadata() {
        DataShape.Builder shape = new DataShape.Builder()
                .putMetadata(DataShapeMetaData.SHOULD_COMPRESS, "false")
                .specification(SPECIFICATION);

        Assertions.assertEquals(SPECIFICATION, shape.build().getSpecification());
        Assertions.assertEquals(SPECIFICATION, shape.compress().build().getSpecification());
    }

    @Test
    public void shouldCompressBasedOnMetadata() throws IOException {
        DataShape.Builder shape = new DataShape.Builder()
                .putMetadata(DataShapeMetaData.SHOULD_COMPRESS, "true")
                .specification(SPECIFICATION);

        Assertions.assertEquals(SPECIFICATION, shape.build().getSpecification());
        assertCompressedSpecification(SPECIFICATION, shape.compress().build().getSpecification());
        Assertions.assertEquals(SPECIFICATION, shape.compress().decompress().build().getSpecification());
    }

    @Test
    public void shouldDecompressBasedOnMetadata() {
        DataShape.Builder shape = new DataShape.Builder()
                .putMetadata(DataShapeMetaData.SHOULD_COMPRESS, "true")
                .specification(COMPRESSED_SPECIFICATION);

        Assertions.assertEquals(SPECIFICATION, shape.decompress().build().getSpecification());
    }

    @Test
    public void shouldNotCompressMultipleTimes() throws IOException {
        DataShape.Builder shape = new DataShape.Builder()
                .putMetadata(DataShapeMetaData.SHOULD_COMPRESS, "true")
                .specification(SPECIFICATION);

        assertCompressedSpecification(SPECIFICATION, shape.compress().compress().build().getSpecification());
    }

    @Test
    public void shouldNotDecompressMultipleTimes() {
        DataShape.Builder shape = new DataShape.Builder()
                .putMetadata(DataShapeMetaData.SHOULD_COMPRESS, "true")
                .specification(COMPRESSED_SPECIFICATION);

        Assertions.assertEquals(SPECIFICATION, shape.decompress().decompress().build().getSpecification());
    }

    @Test
    public void shouldCompressVariants() throws IOException {
        DataShape.Builder shape = new DataShape.Builder()
                .putMetadata(DataShapeMetaData.SHOULD_COMPRESS, "true")
                .specification(SPECIFICATION)
                .addVariant(new DataShape.Builder()
                                .putMetadata(DataShapeMetaData.SHOULD_COMPRESS, "true")
                                .specification(VARIANT_SPECIFICATION)
                                .build());

        Assertions.assertEquals(SPECIFICATION, shape.build().getSpecification());
        assertCompressedSpecification(SPECIFICATION, shape.compress().build().getSpecification());
        Assertions.assertEquals(SPECIFICATION, shape.compress().decompress().build().getSpecification());
        Assertions.assertEquals(VARIANT_SPECIFICATION, shape.build().getVariants().get(0).getSpecification());
        assertCompressedSpecification(VARIANT_SPECIFICATION, shape.compress().build().getVariants().get(0).getSpecification());
        Assertions.assertEquals(VARIANT_SPECIFICATION, shape.compress().decompress().build().getVariants().get(0).getSpecification());
    }

    @Test
    public void shouldDecompressVariants() {
        DataShape.Builder shape = new DataShape.Builder()
                .putMetadata(DataShapeMetaData.SHOULD_COMPRESS, "true")
                .specification(COMPRESSED_SPECIFICATION)
                .addVariant(new DataShape.Builder()
                                .putMetadata(DataShapeMetaData.SHOULD_COMPRESS, "true")
                                .specification(COMPRESSED_VARIANT_SPECIFICATION)
                                .build());

        Assertions.assertEquals(SPECIFICATION, shape.decompress().build().getSpecification());
        Assertions.assertEquals(VARIANT_SPECIFICATION, shape.decompress().build().getVariants().get(0).getSpecification());
    }

    @Test
    public void shouldNotCompressVariantsBasedOnMetadata() throws IOException {
        DataShape.Builder shape = new DataShape.Builder()
                .putMetadata(DataShapeMetaData.SHOULD_COMPRESS, "true")
                .specification(SPECIFICATION)
                .addVariant(new DataShape.Builder()
                                .putMetadata(DataShapeMetaData.SHOULD_COMPRESS, "false")
                                .specification(VARIANT_SPECIFICATION)
                                .build());

        Assertions.assertEquals(SPECIFICATION, shape.build().getSpecification());
        assertCompressedSpecification(SPECIFICATION, shape.compress().build().getSpecification());
        Assertions.assertEquals(SPECIFICATION, shape.compress().decompress().build().getSpecification());
        Assertions.assertEquals(VARIANT_SPECIFICATION, shape.build().getVariants().get(0).getSpecification());
        Assertions.assertEquals(VARIANT_SPECIFICATION, shape.compress().build().getVariants().get(0).getSpecification());
    }

    @Test
    public void shouldCompressShapesBasedOnMetadata() throws IOException {
        DataShape.Builder shape = new DataShape.Builder()
                .specification(SPECIFICATION)
                .addVariant(new DataShape.Builder()
                                .putMetadata(DataShapeMetaData.SHOULD_COMPRESS, "true")
                                .specification(VARIANT_SPECIFICATION)
                                .build())
                .addVariant(new DataShape.Builder()
                                .specification(VARIANT_SPECIFICATION)
                                .build());

        Assertions.assertEquals(SPECIFICATION, shape.build().getSpecification());
        Assertions.assertEquals(SPECIFICATION, shape.compress().build().getSpecification());
        Assertions.assertEquals(SPECIFICATION, shape.compress().decompress().build().getSpecification());
        Assertions.assertEquals(VARIANT_SPECIFICATION, shape.build().getVariants().get(0).getSpecification());
        assertCompressedSpecification(VARIANT_SPECIFICATION, shape.compress().build().getVariants().get(0).getSpecification());
        Assertions.assertEquals(VARIANT_SPECIFICATION, shape.build().getVariants().get(1).getSpecification());
        Assertions.assertEquals(VARIANT_SPECIFICATION, shape.compress().build().getVariants().get(1).getSpecification());
    }

    @Test
    public void shouldNotCompressAlreadyCompressedVariants() throws IOException {
        DataShape.Builder shape = new DataShape.Builder()
                .putMetadata(DataShapeMetaData.SHOULD_COMPRESS, "true")
                .specification(SPECIFICATION)
                .addVariant(new DataShape.Builder()
                        .putMetadata(DataShapeMetaData.SHOULD_COMPRESS, "true")
                        .specification(VARIANT_SPECIFICATION)
                        .compress()
                        .build());

        assertCompressedSpecification(SPECIFICATION, shape.compress().compress().build().getSpecification());
        assertCompressedSpecification(VARIANT_SPECIFICATION, shape.compress().compress().build().getVariants().get(0).getSpecification());
    }

    @Test
    public void shouldNotDecompressAlreadyDecompressedVariants() {
        DataShape.Builder shape = new DataShape.Builder()
                .putMetadata(DataShapeMetaData.SHOULD_COMPRESS, "true")
                .specification(COMPRESSED_SPECIFICATION)
                .addVariant(new DataShape.Builder()
                        .putMetadata(DataShapeMetaData.SHOULD_COMPRESS, "true")
                        .specification(VARIANT_SPECIFICATION)
                        .compress()
                        .build());

        Assertions.assertEquals(SPECIFICATION, shape.decompress().decompress().build().getSpecification());
        Assertions.assertEquals(VARIANT_SPECIFICATION, shape.decompress().decompress().build().getVariants().get(0).getSpecification());
    }

    private static void assertCompressedSpecification(String expected, String compressed) throws IOException {
        byte[] data = Base64.getDecoder().decode(compressed);

        try (InputStream is = new GZIPInputStream(new ByteArrayInputStream(data))) {
            Assertions.assertEquals(expected, IOStreams.readText(is));
        }
    }

}