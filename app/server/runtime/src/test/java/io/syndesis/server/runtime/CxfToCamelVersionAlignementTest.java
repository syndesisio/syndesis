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
package io.syndesis.server.runtime;

import java.io.IOException;
import java.io.InputStream;

import org.apache.cxf.version.Version;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CxfToCamelVersionAlignementTest {

    @Test
    public void cxfVersionShouldBeAligned() throws IOException {
        String cxfVersion = Version.getCurrentVersion();

        try (InputStream stream = CxfToCamelVersionAlignementTest.class.getResourceAsStream("/cxf-version.txt")) {
            assertThat(stream).as("Version of CXF needs to be: " + cxfVersion
                + ". Update the `cxf.version` property in syndesis-parent POM")
                .hasContent(cxfVersion);
        }
    }
}
