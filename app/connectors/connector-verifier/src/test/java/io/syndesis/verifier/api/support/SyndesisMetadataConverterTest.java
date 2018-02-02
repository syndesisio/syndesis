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
package io.syndesis.verifier.api.support;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;

import io.syndesis.verifier.api.SyndesisMetadata;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

public class SyndesisMetadataConverterTest {

    @Test
    public void shouldContainAllFieldsOfSyndesisMetadata() {
        final SyndesisMetadataConverter converter = new SyndesisMetadataConverter();

        final PodamFactory podam = new PodamFactoryImpl();

        final SyndesisMetadata<?> metadata = podam.manufacturePojo(SyndesisMetadata.class, String.class);

        final Map<String, Object> converted = converter.convert(metadata);

        final String[] fields = Arrays.stream(SyndesisMetadata.class.getDeclaredFields()).map(Field::getName)
            .filter(field -> !"$jacocoData".equals(field)).toArray(String[]::new);

        assertThat(converted).as("The converted map doesn't contain exactly the fields that the SyndesisMetadata object contains")
            .containsOnlyKeys(fields);
    }
}
