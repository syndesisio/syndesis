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
package io.syndesis.server.api.generator.openapi.v3;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

import static io.syndesis.server.api.generator.openapi.DataShapeGenerator.APPLICATION_JSON;
import static io.syndesis.server.api.generator.openapi.DataShapeGenerator.APPLICATION_XML;
import static io.syndesis.server.api.generator.openapi.v3.UnifiedDataShapeGenerator.supports;

import static org.assertj.core.api.Assertions.assertThat;

public class UnifiedDataShapeGeneratorTest {

    @ParameterizedTest
    @MethodSource("parameters")
    public void shouldDetermineSupportedMimes(final String mime, final Set<String> mimes, final boolean outcome) {
        assertThat(supports(mime, mimes)).isEqualTo(outcome);
    }

    static Stream<Arguments> parameters() {
        return Stream.of(
            Arguments.of(APPLICATION_XML, null, false),
            Arguments.of(APPLICATION_JSON, null, false),
            Arguments.of(APPLICATION_XML, emptySet(), false),
            Arguments.of(APPLICATION_JSON, emptySet(), false),
            Arguments.of(APPLICATION_XML, singleton(APPLICATION_JSON), false),
            Arguments.of(APPLICATION_JSON, singleton(APPLICATION_JSON), true),
            Arguments.of(APPLICATION_XML, singleton(APPLICATION_XML), true),
            Arguments.of(APPLICATION_JSON, singleton(APPLICATION_XML), false),
            Arguments.of(APPLICATION_XML, Stream.of(APPLICATION_JSON, APPLICATION_XML).collect(Collectors.toSet()), true),
            Arguments.of(APPLICATION_JSON, Stream.of(APPLICATION_JSON, APPLICATION_XML).collect(Collectors.toSet()), true));
    }
}
