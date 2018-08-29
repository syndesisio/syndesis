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
package io.syndesis.server.api.generator.swagger;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import static io.syndesis.server.api.generator.swagger.UnifiedDataShapeGenerator.supports;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class UnifiedDataShapeGeneratorTest {

    private static final String APPLICATION_JSON = "application/json";

    private static final String APPLICATION_XML = "application/xml";

    @Parameter(1)
    public List<String> defaultMimes;

    @Parameter(0)
    public String mime;

    @Parameter(2)
    public List<String> mimes;

    @Parameter(3)
    public boolean outcome;

    @Test
    public void shouldDetermineSupportedMimes() {
        assertThat(supports(mime, defaultMimes, mimes)).isEqualTo(outcome);
    }

    @Parameters
    public static Iterable<Object[]> parameters() {
        return asList(//
            new Object[] {APPLICATION_XML, null, null, false}, //
            new Object[] {APPLICATION_JSON, null, null, false}, //
            new Object[] {APPLICATION_XML, emptyList(), null, false}, //
            new Object[] {APPLICATION_JSON, emptyList(), null, false}, //
            new Object[] {APPLICATION_XML, null, emptyList(), false}, //
            new Object[] {APPLICATION_JSON, null, emptyList(), false}, //
            new Object[] {APPLICATION_XML, emptyList(), emptyList(), false}, //
            new Object[] {APPLICATION_XML, emptyList(), emptyList(), false}, //
            new Object[] {APPLICATION_JSON, emptyList(), emptyList(), false}, //
            new Object[] {APPLICATION_XML, emptyList(), singletonList(APPLICATION_JSON), false}, //
            new Object[] {APPLICATION_JSON, emptyList(), singletonList(APPLICATION_JSON), true}, //
            new Object[] {APPLICATION_XML, emptyList(), singletonList(APPLICATION_XML), true}, //
            new Object[] {APPLICATION_JSON, emptyList(), singletonList(APPLICATION_XML), false}, //
            new Object[] {APPLICATION_XML, singletonList(APPLICATION_JSON), emptyList(), false}, //
            new Object[] {APPLICATION_JSON, singletonList(APPLICATION_JSON), emptyList(), true}, //
            new Object[] {APPLICATION_XML, singletonList(APPLICATION_XML), emptyList(), true}, //
            new Object[] {APPLICATION_JSON, singletonList(APPLICATION_XML), emptyList(), false}, //
            new Object[] {APPLICATION_XML, emptyList(), asList(APPLICATION_JSON, APPLICATION_XML), true}, //
            new Object[] {APPLICATION_JSON, emptyList(), asList(APPLICATION_JSON, APPLICATION_XML), true}, //
            new Object[] {APPLICATION_JSON, emptyList(), asList(APPLICATION_JSON, APPLICATION_XML), true}//
        );
    }
}
