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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LabelsTest {

    @Test
    public void testValidValues() {
        assertThat(Labels.isValid("abcdefghijklmnopqrstyvwxyz0123456789")).isTrue();
        assertThat(Labels.isValid("012345678901234567890123456789012345678901234567890123456789123")).isTrue();
    }

    @Test
    public void testValidateValues() {
        assertThat(Labels.validate("abcdefghijklmnopqrstyvwxyz0123456789"))
            .isEqualTo("abcdefghijklmnopqrstyvwxyz0123456789");
        assertThat(Labels.validate("012345678901234567890123456789012345678901234567890123456789123"))
            .isEqualTo("012345678901234567890123456789012345678901234567890123456789123");
    }

    @Test
    public void testInvalidValues() {
        assertThat(Labels.isValid("-abcdefghijklmnopqrstyvwxyz0123456789")).isFalse();
        assertThat(Labels.isValid("abcdefghijklmnopqrstyvwxyz0123456789-")).isFalse();
        assertThat(Labels.isValid("01234567890123456789012345678901234567890123456789012345678912345")).isFalse();
        assertThat(Labels.isValid("syndesis.io/value")).isFalse();
    }

    @Test
    public void testValidateValueGeneratedKeys() {
        for (int i = 0; i < 1000; i++) {
            assertThat(Labels.isValid(KeyGenerator.createKey())).isTrue();
        }
    }

    @Test
    public void testValidKeys() {
        assertThat(Labels.isValidKey("key")).isTrue();
        assertThat(Labels.isValidKey("my-service.io/key")).isTrue();
        assertThat(Labels.isValidKey("syndesis.io-key")).isTrue();
    }

    @Test
    public void testValidateKeys() {
        assertThat(Labels.validateKey("key")).isEqualTo("key");
        assertThat(Labels.validateKey("my-service.io/key")).isEqualTo("my-service.io/key");
        assertThat(Labels.validateKey("syndesis.io-key")).isEqualTo("syndesis.io-key");
    }

    @Test
    public void testInvalidKeys() {
        assertThat(Labels.isValidKey("01234567890123456789012345678901234567890123456789012345" +
            "67891234501234567890123456789012345678901234567890123456789012345678912345012345678" +
            "90123456789012345678901234567890123456789012345678912345012345678901234567890123456" +
            "78901234567890123456789012345678912345012345678901234567890123456789012345678901234" +
            "56789012345678912345")).isFalse();
        assertThat(Labels.isValidKey("%wrong!")).isFalse();
        assertThat(Labels.isValidKey("syndesis.io/key")).isFalse();
        assertThat(Labels.isValidKey("k8s.io/key")).isFalse();
        assertThat(Labels.isValidKey("kubernetes.io/key")).isFalse();
    }
}
