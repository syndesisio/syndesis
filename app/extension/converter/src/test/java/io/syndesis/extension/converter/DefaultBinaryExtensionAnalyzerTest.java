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
package io.syndesis.extension.converter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import io.syndesis.common.model.extension.Extension;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultBinaryExtensionAnalyzerTest {

    static final byte[] ICON_BYTES = "<?xml version=\"1.0\" encoding=\"utf-8\"?><svg version=\"1.1\"></svg>".getBytes(StandardCharsets.US_ASCII);

    static final byte[] DESCRIPTOR_BYTES = "{\"icon\":\"an icon\"}".getBytes(StandardCharsets.US_ASCII);

    DefaultBinaryExtensionAnalyzer analyzer = new DefaultBinaryExtensionAnalyzer();

    @Test
    public void shouldReadExtension() throws IOException {
        try (InputStream extensionStream = createExtension()) {
            final Extension extension = analyzer.getExtension(extensionStream);

            assertThat(extension.getIcon()).isEqualTo("an icon");
        }
    }

    @Test
    public void shouldReadIconStream() throws IOException {
        try (InputStream extensionStream = createExtension()) {
            Optional<InputStream> icon = analyzer.getIcon(extensionStream, "icon.svg");

            assertThat(icon).hasValueSatisfying(s -> {
                try (InputStream stream = s) {
                    assertThat(stream).hasBinaryContent(ICON_BYTES);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
    }

    private static InputStream createExtension() {
        try (ByteArrayOutputStream buff = new ByteArrayOutputStream();
            ZipOutputStream zip = new ZipOutputStream(buff)) {

            zip.putNextEntry(new ZipEntry("META-INF/syndesis/icon.svg"));
            zip.write(ICON_BYTES);
            zip.closeEntry();

            zip.putNextEntry(new ZipEntry("META-INF/syndesis/syndesis-extension-definition.json"));
            zip.write(DESCRIPTOR_BYTES);
            zip.closeEntry();

            return new ByteArrayInputStream(buff.toByteArray());
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
    }
}
