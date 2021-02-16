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
package io.syndesis.common.util;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Unit tests for RandomValueGenerator
 */
public class RandomValueGeneratorTest {

    @Test
    public void testNegativeValue() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> RandomValueGenerator.generate("alphanum:-1"))
            .withMessage("Cannot generate a string of negative length");
    }

    @Test
    public void testPatternRespectedWithDefault() {
        for (int i = 0; i < 20; i++) {
            final String value = RandomValueGenerator.generate("alphanum");
            assertThat(value).matches("[A-Za-z0-9]{40}");
        }
    }

    @Test
    public void testPatternRespectedWithLength() {
        for (int i = 0; i < 20; i++) {
            final String value = RandomValueGenerator.generate("alphanum:12");
            assertThat(value).matches("[A-Za-z0-9]{12}");
        }
    }

    @Test
    public void testTrueRandom() {
        final Set<String> gen = new HashSet<>();
        for (int i = 0; i < 20; i++) {
            final String value = RandomValueGenerator.generate("alphanum:80");
            assertThat(value).matches("[A-Za-z0-9]{80}");
            gen.add(value);
        }

        assertThat(gen.size()).isGreaterThan(10);
    }

    @Test
    public void testUnknownGenerator() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> RandomValueGenerator.generate("alphanumx"))
            .withMessage("Unsupported generator scheme: alphanumx");
    }

    @Test
    public void testWrongValueType() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> RandomValueGenerator.generate("alphanum:aa"))
            .withMessage("Unexpected string after the alphanum scheme: expected length");
    }

    @Test
    public void testZeroValue() {
        final String value = RandomValueGenerator.generate("alphanum:0");
        assertThat(value).isEqualTo("");
    }

}
