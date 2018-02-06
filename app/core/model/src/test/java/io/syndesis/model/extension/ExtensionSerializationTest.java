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
package io.syndesis.model.extension;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.syndesis.core.Json;
import org.junit.Test;

import static junit.framework.TestCase.fail;

public class ExtensionSerializationTest {
    @Test
    public void testSerializeDeserialize() throws IOException {
        final ObjectReader reader = Json.reader();
        final ObjectWriter writer = Json.writer();

        try (InputStream source = getClass().getResourceAsStream("syndesis-extension-definition.json")) {
            try {
                reader.forType(Extension.class).readValue(
                    writer.writeValueAsString(
                        new Extension.Builder()
                            .createFrom(reader.forType(Extension.class).readValue(source))
                            .build()
                    )

                );
            } catch (IOException e) {
                fail("Should not throw an exception");
            }
        }
    }
}
