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

import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;

import io.syndesis.common.util.Json;
import io.syndesis.common.model.integration.Integration;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the StringTrimmingConverters
 */
public class StringTrimmingConverterTest {

    @Test
    public void testTrimming() throws IOException {
        final SortedSet<String> tags = new TreeSet<>();
        tags.add("");
        tags.add(" tag");
        tags.add("\tTaggy McTagface\t");

        final Integration original = new Integration.Builder()
            .id("test")
            .name("  some-name\t").description("")
            .tags(tags)
            .build();

        final String source = Json.writer().writeValueAsString(original);
        final Integration created = Json.reader().forType(Integration.class).readValue(source);

        assertThat(created.getName()).isEqualTo("some-name");
        assertThat(created.getDescription()).isNotPresent();
        assertThat(created.getTags()).containsExactly("Taggy McTagface", "tag");
    }
}
