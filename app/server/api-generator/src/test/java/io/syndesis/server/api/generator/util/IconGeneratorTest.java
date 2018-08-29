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
package io.syndesis.server.api.generator.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IconGeneratorTest {

    private static final String PREFIX = "data:image/svg+xml,%3Csvg";

    @Test
    @Ignore("Generates test.html for visual inspection")
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    public void generateTestHtml() throws IOException {
        try (PrintStream out = new PrintStream(new FileOutputStream("test.html"))) {
            out.println("<html><body>");

            for (int letter = 'A'; letter <= 'Z'; letter++) {
                final String letterString = String.valueOf((char) letter);
                final String icon = IconGenerator.generate("swagger-connector-template", letterString);

                out.println("<br/>");
                out.println("<img src=\"" + icon + "\" /> (" + icon.length() + ")");
            }
        }
    }

    @Test
    public void shouldGenerateIcon() {
        for (int letter = 'A'; letter <= 'Z'; letter++) {
            final String letterString = String.valueOf((char) letter);
            final String icon = IconGenerator.generate("swagger-connector-template", letterString);

            assertThat(icon).startsWith(PREFIX);
            assertThat(icon).matches(".*circle.*%20style%3D%22fill%3A%23fff.*");
            assertThat(icon).matches(".*path.*%20style%3D%22fill%3A%23fff.*");
        }
    }
}
