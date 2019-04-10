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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Base64.Decoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class StaticEdition extends Edition {

    private final KeySource keySource;

    static final class StaticKeySource implements KeySource {
        private final SecretKey authenticationKey;

        private final SecretKey encryptionKey;

        StaticKeySource(final ClientSideStateProperties properties) {
            final String encryptionKeyAlgorithm = properties.getEncryptionAlgorithm().replaceFirst("/.*", "");

            encryptionKey = new SecretKeySpec(decode(properties.getEncryptionKey()), encryptionKeyAlgorithm);
            authenticationKey = new SecretKeySpec(decode(properties.getAuthenticationKey()), properties.getAuthenticationAlgorithm());
        }

        @Override
        public SecretKey authenticationKey() {
            return authenticationKey;
        }

        @Override
        public SecretKey encryptionKey() {
            return encryptionKey;
        }
    }

    public StaticEdition(final ClientSideStateProperties properties) {
        super(properties.getTid(), properties.getEncryptionAlgorithm(), properties.getAuthenticationAlgorithm());
        keySource = new StaticKeySource(properties);
    }

    @Override
    protected KeySource keySource() {
        return keySource;
    }

    static byte[] decode(final String given) {
        final Decoder decoder = Base64.getDecoder();

        try {
            return decoder.decode(given);
        } catch (final IllegalArgumentException ignored) {
            // given is not a base64 string
            return given.getBytes(StandardCharsets.US_ASCII);
        }
    }
}
