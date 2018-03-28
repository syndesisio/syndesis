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
package io.syndesis.server.endpoint.v1.state;

import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang.RandomStringUtils;
import org.assertj.core.api.AbstractAssert;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StaticEditionTest {

    private static class KeySourceAssert extends AbstractAssert<KeySourceAssert, KeySource> {

        private KeySourceAssert(final KeySource actual) {
            super(actual, KeySourceAssert.class);
        }

        public void canBeUsedForCryptography() {
            final SecretKey authenticationKey = actual.authenticationKey();
            try {
                final Mac mac = Mac.getInstance(authenticationKey.getAlgorithm());
                mac.init(authenticationKey);
            } catch (final GeneralSecurityException e) {
                throw new AssertionError("Unable to utilize authentication key: " + authenticationKey, e);
            }

            final SecretKey encryptionKey = actual.encryptionKey();
            try {
                final Cipher cipher = Cipher.getInstance(encryptionKey.getAlgorithm());
                cipher.init(Cipher.ENCRYPT_MODE, encryptionKey);
            } catch (final GeneralSecurityException e) {
                throw new AssertionError("Unable to utilize encryption key: " + encryptionKey, e);
            }
        }

        public static KeySourceAssert assertThat(final KeySource actual) {
            return new KeySourceAssert(actual);
        }

    }

    @Test
    public void shouldCreateEditionFromProperties() {
        final ClientSideStateProperties properties = new ClientSideStateProperties();
        properties.setAuthenticationAlgorithm("HmacSHA1");
        properties.setAuthenticationKey("oID3dF6UovTkzMyr3a9dr0kgTnE=");
        properties.setEncryptionAlgorithm("AES/CBC/PKCS5Padding");
        properties.setEncryptionKey("T2NasjRXURA3dSL8dUQubQ==");
        properties.setTid(1L);

        final StaticEdition edition = new StaticEdition(properties);

        assertThat(edition.authenticationAlgorithm).isEqualTo("HmacSHA1");
        assertThat(edition.encryptionAlgorithm).isEqualTo("AES/CBC/PKCS5Padding");
        assertThat(edition.tid).isEqualTo(new byte[] {1});

        final KeySource keySource = edition.keySource();
        assertThat(keySource.authenticationKey())
            .isEqualTo(new SecretKeySpec(
                new byte[] {(byte) 0xa0, (byte) 0x80, (byte) 0xf7, 0x74, 0x5e, (byte) 0x94, (byte) 0xa2, (byte) 0xf4, (byte) 0xe4,
                    (byte) 0xcc, (byte) 0xcc, (byte) 0xab, (byte) 0xdd, (byte) 0xaf, 0x5d, (byte) 0xaf, 0x49, 0x20, 0x4e, 0x71},
                "HmacSHA1"));
        assertThat(keySource.encryptionKey()).isEqualTo(new SecretKeySpec(
            new byte[] {0x4f, 0x63, 0x5a, (byte) 0xb2, 0x34, 0x57, 0x51, 0x10, 0x37, 0x75, 0x22, (byte) 0xfc, 0x75, 0x44, 0x2e, 0x6d},
            "AES"));

        KeySourceAssert.assertThat(keySource).canBeUsedForCryptography();
    }

    @Test
    public void shouldCreateEditionWithNonBase64Passwords() {
        final ClientSideStateProperties properties = new ClientSideStateProperties();
        properties.setAuthenticationAlgorithm("HmacSHA1");
        properties.setAuthenticationKey(RandomStringUtils.random(32, true, true));
        properties.setEncryptionAlgorithm("AES/CBC/PKCS5Padding");
        properties.setEncryptionKey(RandomStringUtils.random(32, true, true));
        properties.setTid(1L);

        final StaticEdition edition = new StaticEdition(properties);

        final KeySource keySource = edition.keySource();

        KeySourceAssert.assertThat(keySource).canBeUsedForCryptography();
    }
}
