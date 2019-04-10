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
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Christoph Deppisch
 */
public class DataShapeBuilderTest {

    private String specification = "some uncompressed data";
    private String compressedSpecification = "H4sIAAAAAAAAACvOz01VKM1Lzs8tKEotLk5NUUhJLEkEALZP9NQWAAAA";
    private String variantSpecification = "some uncompressed variant data";
    private String compressedVariantSpecification = "H4sIAAAAAAAAACvOz01VKM1Lzs8tKEotLk5NUShLLMpMzCtRSEksSQQAQv1meR4AAAA=";

    @Test
    public void shouldNotCompressBasedOnMetadata() {
        DataShape.Builder shape = new DataShape.Builder()
                .putMetadata(DataShapeMetaData.SHOULD_COMPRESS, "false")
                .specification(specification);

        Assert.assertEquals(specification, shape.build().getSpecification());
        Assert.assertEquals(specification, shape.compress().build().getSpecification());
    }

    @Test
    public void shouldCompressBasedOnMetadata() throws IOException {
        DataShape.Builder shape = new DataShape.Builder()
                .putMetadata(DataShapeMetaData.SHOULD_COMPRESS, "true")
                .specification(specification);

        Assert.assertEquals(specification, shape.build().getSpecification());
        assertCompressedSpecification(specification, shape.compress().build().getSpecification());
        Assert.assertEquals(specification, shape.compress().decompress().build().getSpecification());
    }

    @Test
    public void shouldDecompressBasedOnMetadata() {
        DataShape.Builder shape = new DataShape.Builder()
                .putMetadata(DataShapeMetaData.SHOULD_COMPRESS, "true")
                .specification(compressedSpecification);

        Assert.assertEquals(specification, shape.decompress().build().getSpecification());
    }

    @Test
    public void shouldNotCompressMultipleTimes() throws IOException {
        DataShape.Builder shape = new DataShape.Builder()
                .putMetadata(DataShapeMetaData.SHOULD_COMPRESS, "true")
                .specification(specification);

        assertCompressedSpecification(specification, shape.compress().compress().build().getSpecification());
    }

    @Test
    public void shouldNotDecompressMultipleTimes() {
        DataShape.Builder shape = new DataShape.Builder()
                .putMetadata(DataShapeMetaData.SHOULD_COMPRESS, "true")
                .specification(compressedSpecification);

        Assert.assertEquals(specification, shape.decompress().decompress().build().getSpecification());
    }

    @Test
    public void shouldCompressVariants() throws IOException {
        DataShape.Builder shape = new DataShape.Builder()
                .putMetadata(DataShapeMetaData.SHOULD_COMPRESS, "true")
                .specification(specification)
                .addVariant(new DataShape.Builder()
                                .putMetadata(DataShapeMetaData.SHOULD_COMPRESS, "true")
                                .specification(variantSpecification)
                                .build());

        Assert.assertEquals(specification, shape.build().getSpecification());
        assertCompressedSpecification(specification, shape.compress().build().getSpecification());
        Assert.assertEquals(specification, shape.compress().decompress().build().getSpecification());
        Assert.assertEquals(variantSpecification, shape.build().getVariants().get(0).getSpecification());
        assertCompressedSpecification(variantSpecification, shape.compress().build().getVariants().get(0).getSpecification());
        Assert.assertEquals(variantSpecification, shape.compress().decompress().build().getVariants().get(0).getSpecification());
    }

    @Test
    public void shouldDecompressVariants() {
        DataShape.Builder shape = new DataShape.Builder()
                .putMetadata(DataShapeMetaData.SHOULD_COMPRESS, "true")
                .specification(compressedSpecification)
                .addVariant(new DataShape.Builder()
                                .putMetadata(DataShapeMetaData.SHOULD_COMPRESS, "true")
                                .specification(compressedVariantSpecification)
                                .build());

        Assert.assertEquals(specification, shape.decompress().build().getSpecification());
        Assert.assertEquals(variantSpecification, shape.decompress().build().getVariants().get(0).getSpecification());
    }

    @Test
    public void shouldNotCompressVariantsBasedOnMetadata() throws IOException {
        DataShape.Builder shape = new DataShape.Builder()
                .putMetadata(DataShapeMetaData.SHOULD_COMPRESS, "true")
                .specification(specification)
                .addVariant(new DataShape.Builder()
                                .putMetadata(DataShapeMetaData.SHOULD_COMPRESS, "false")
                                .specification(variantSpecification)
                                .build());

        Assert.assertEquals(specification, shape.build().getSpecification());
        assertCompressedSpecification(specification, shape.compress().build().getSpecification());
        Assert.assertEquals(specification, shape.compress().decompress().build().getSpecification());
        Assert.assertEquals(variantSpecification, shape.build().getVariants().get(0).getSpecification());
        Assert.assertEquals(variantSpecification, shape.compress().build().getVariants().get(0).getSpecification());
    }

    @Test
    public void shouldCompressShapesBasedOnMetadata() throws IOException {
        DataShape.Builder shape = new DataShape.Builder()
                .specification(specification)
                .addVariant(new DataShape.Builder()
                                .putMetadata(DataShapeMetaData.SHOULD_COMPRESS, "true")
                                .specification(variantSpecification)
                                .build())
                .addVariant(new DataShape.Builder()
                                .specification(variantSpecification)
                                .build());

        Assert.assertEquals(specification, shape.build().getSpecification());
        Assert.assertEquals(specification, shape.compress().build().getSpecification());
        Assert.assertEquals(specification, shape.compress().decompress().build().getSpecification());
        Assert.assertEquals(variantSpecification, shape.build().getVariants().get(0).getSpecification());
        assertCompressedSpecification(variantSpecification, shape.compress().build().getVariants().get(0).getSpecification());
        Assert.assertEquals(variantSpecification, shape.build().getVariants().get(1).getSpecification());
        Assert.assertEquals(variantSpecification, shape.compress().build().getVariants().get(1).getSpecification());
    }

    @Test
    public void shouldNotCompressAlreadyCompressedVariants() throws IOException {
        DataShape.Builder shape = new DataShape.Builder()
                .putMetadata(DataShapeMetaData.SHOULD_COMPRESS, "true")
                .specification(specification)
                .addVariant(new DataShape.Builder()
                        .putMetadata(DataShapeMetaData.SHOULD_COMPRESS, "true")
                        .specification(variantSpecification)
                        .compress()
                        .build());

        assertCompressedSpecification(specification, shape.compress().compress().build().getSpecification());
        assertCompressedSpecification(variantSpecification, shape.compress().compress().build().getVariants().get(0).getSpecification());
    }

    @Test
    public void shouldNotDecompressAlreadyDecompressedVariants() {
        DataShape.Builder shape = new DataShape.Builder()
                .putMetadata(DataShapeMetaData.SHOULD_COMPRESS, "true")
                .specification(compressedSpecification)
                .addVariant(new DataShape.Builder()
                        .putMetadata(DataShapeMetaData.SHOULD_COMPRESS, "true")
                        .specification(variantSpecification)
                        .compress()
                        .build());

        Assert.assertEquals(specification, shape.decompress().decompress().build().getSpecification());
        Assert.assertEquals(variantSpecification, shape.decompress().decompress().build().getVariants().get(0).getSpecification());
    }

    private void assertCompressedSpecification(String expected, String compressed) throws IOException {
        byte[] data = Base64.getDecoder().decode(compressed);

        try (InputStream is = new GZIPInputStream(new ByteArrayInputStream(data))) {
            Assert.assertEquals(expected, IOStreams.readText(is));
        }
    }

}