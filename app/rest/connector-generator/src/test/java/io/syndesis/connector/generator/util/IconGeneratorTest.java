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
package io.syndesis.connector.generator.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IconGeneratorTest {

    private static final String PREFIX = "data:image/png;base64,";

    @Test
    public void shouldGenerateIcon() throws IOException {
        final String icon = IconGenerator.generate("swagger-connector-template", "Hello");

        assertThat(icon).startsWith(PREFIX);

        final byte[] png = Base64.getDecoder().decode(icon.substring(PREFIX.length()));

        final BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));

        assertThat(image.getHeight()).isEqualTo(200);
        assertThat(image.getWidth()).isEqualTo(200);
    }
}
