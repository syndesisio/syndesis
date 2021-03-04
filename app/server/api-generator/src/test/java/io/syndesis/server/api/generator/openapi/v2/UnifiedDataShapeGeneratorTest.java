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
package io.syndesis.server.api.generator.openapi.v2;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import static io.syndesis.server.api.generator.openapi.DataShapeGenerator.APPLICATION_JSON;
import static io.syndesis.server.api.generator.openapi.DataShapeGenerator.APPLICATION_XML;
import static io.syndesis.server.api.generator.openapi.v2.UnifiedDataShapeGenerator.supports;

import static org.assertj.core.api.Assertions.assertThat;

public class UnifiedDataShapeGeneratorTest {

    @ParameterizedTest
    @MethodSource("parameters")
    public void shouldDetermineSupportedMimes(final String mime, final List<String> defaultMimes, final List<String> mimes, final boolean outcome) {
        assertThat(supports(mime, defaultMimes, mimes)).isEqualTo(outcome);
    }

    static Stream<Arguments> parameters() {
        return Stream.of(
            Arguments.of(APPLICATION_XML, null, null, false),
            Arguments.of(APPLICATION_JSON, null, null, false),
            Arguments.of(APPLICATION_XML, emptyList(), null, false),
            Arguments.of(APPLICATION_JSON, emptyList(), null, false),
            Arguments.of(APPLICATION_XML, null, emptyList(), false),
            Arguments.of(APPLICATION_JSON, null, emptyList(), false),
            Arguments.of(APPLICATION_XML, emptyList(), emptyList(), false),
            Arguments.of(APPLICATION_JSON, emptyList(), emptyList(), false),
            Arguments.of(APPLICATION_XML, emptyList(), singletonList(APPLICATION_JSON), false),
            Arguments.of(APPLICATION_JSON, emptyList(), singletonList(APPLICATION_JSON), true),
            Arguments.of(APPLICATION_XML, emptyList(), singletonList(APPLICATION_XML), true),
            Arguments.of(APPLICATION_JSON, emptyList(), singletonList(APPLICATION_XML), false),
            Arguments.of(APPLICATION_XML, singletonList(APPLICATION_JSON), emptyList(), false),
            Arguments.of(APPLICATION_JSON, singletonList(APPLICATION_JSON), emptyList(), true),
            Arguments.of(APPLICATION_XML, singletonList(APPLICATION_XML), emptyList(), true),
            Arguments.of(APPLICATION_JSON, singletonList(APPLICATION_XML), emptyList(), false),
            Arguments.of(APPLICATION_XML, emptyList(), asList(APPLICATION_JSON, APPLICATION_XML), true),
            Arguments.of(APPLICATION_JSON, emptyList(), asList(APPLICATION_JSON, APPLICATION_XML), true),
            Arguments.of(APPLICATION_JSON, emptyList(), asList(APPLICATION_JSON, APPLICATION_XML), true));
    }
}
