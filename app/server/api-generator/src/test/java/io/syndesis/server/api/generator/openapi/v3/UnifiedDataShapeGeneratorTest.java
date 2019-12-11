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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import static io.syndesis.server.api.generator.openapi.DataShapeGenerator.APPLICATION_JSON;
import static io.syndesis.server.api.generator.openapi.DataShapeGenerator.APPLICATION_XML;
import static io.syndesis.server.api.generator.openapi.v3.UnifiedDataShapeGenerator.supports;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class UnifiedDataShapeGeneratorTest {

    @Parameter(0)
    public String mime;

    @Parameter(1)
    public Set<String> mimes;

    @Parameter(2)
    public boolean outcome;

    @Test
    public void shouldDetermineSupportedMimes() {
        assertThat(supports(mime, mimes)).isEqualTo(outcome);
    }

    @Parameters
    public static Iterable<Object[]> parameters() {
        return asList(//
            new Object[] {APPLICATION_XML, null, false}, //
            new Object[] {APPLICATION_JSON, null, false}, //
            new Object[] {APPLICATION_XML, emptySet(), false}, //
            new Object[] {APPLICATION_JSON, emptySet(), false}, //
            new Object[] {APPLICATION_XML, singleton(APPLICATION_JSON), false}, //
            new Object[] {APPLICATION_JSON, singleton(APPLICATION_JSON), true}, //
            new Object[] {APPLICATION_XML, singleton(APPLICATION_XML), true}, //
            new Object[] {APPLICATION_JSON, singleton(APPLICATION_XML), false}, //
            new Object[] {APPLICATION_XML, Stream.of(APPLICATION_JSON, APPLICATION_XML).collect(Collectors.toSet()), true}, //
            new Object[] {APPLICATION_JSON, Stream.of(APPLICATION_JSON, APPLICATION_XML).collect(Collectors.toSet()), true}//
        );
    }
}
