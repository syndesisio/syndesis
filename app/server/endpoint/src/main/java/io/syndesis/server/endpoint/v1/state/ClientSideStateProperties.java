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

import java.security.SecureRandom;
import java.util.Base64;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("client.state")
public class ClientSideStateProperties {

    public static final String DEFAULT_AUTHENTICATION_ALGORITHM = "HmacSHA1";

    public static final String DEFAULT_ENCRYPTION_ALGORITHM = "AES/CBC/PKCS5Padding";

    private static final SecureRandom RANDOM = new SecureRandom();

    private String authenticationAlgorithm;

    private String authenticationKey;

    private String encryptionAlgorithm;

    private String encryptionKey;

    private Long tid;

    public boolean areSet() {
        return tid != null && !StringUtils.isEmpty(authenticationAlgorithm) && !StringUtils.isEmpty(authenticationKey)
            && !StringUtils.isEmpty(encryptionAlgorithm) && !StringUtils.isEmpty(encryptionKey);
    }

    public String getAuthenticationAlgorithm() {
        return value(authenticationAlgorithm, this::setAuthenticationAlgorithm, () -> DEFAULT_AUTHENTICATION_ALGORITHM);
    }

    public String getAuthenticationKey() {
        return value(authenticationKey, this::setAuthenticationKey, ClientSideStateProperties::generateKey);
    }

    public String getEncryptionAlgorithm() {
        return value(encryptionAlgorithm, this::setEncryptionAlgorithm, () -> DEFAULT_ENCRYPTION_ALGORITHM);
    }

    public String getEncryptionKey() {
        return value(encryptionKey, this::setEncryptionKey, ClientSideStateProperties::generateKey);
    }

    public long getTid() {
        return value(tid, this::setTid, () -> Long.valueOf(RANDOM.nextLong()));
    }

    public void setAuthenticationAlgorithm(final String authenticationAlgorithm) {
        this.authenticationAlgorithm = authenticationAlgorithm;
    }

    public void setAuthenticationKey(final String authenticationKey) {
        this.authenticationKey = authenticationKey;
    }

    public void setEncryptionAlgorithm(final String encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
    }

    public void setEncryptionKey(final String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public void setTid(final Long tid) {
        this.tid = tid;
    }

    private static String generateKey() {
        final byte[] key = new byte[32];
        RANDOM.nextBytes(key);

        return Base64.getEncoder().encodeToString(key);
    }

    private static <T> T value(final T given, final Consumer<T> setter, final Supplier<T> generator) {
        if (given != null && !StringUtils.isEmpty(given.toString())) {
            return given;
        }

        final T newValue = generator.get();

        setter.accept(newValue);

        return newValue;
    }
}
