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
package io.syndesis.connector.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.stream.Collectors;

/**
 * Helper class to create KeyStores from Certificates.
 *
 * @author dhirajsb
 */
public class KeyStoreHelper {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final String certificate;
    private final String alias;

    private Path tempFile;
    private String password;

    public KeyStoreHelper(String certificate, String alias) {
        this.certificate = certificate;
        this.alias = alias;
    }

    public String getKeyStorePath() {
        return tempFile.toString();
    }

    public String getPassword() {
        return password;

    }

    public KeyStoreHelper store() {
        try {

            KeyStore keyStore = CertificateUtil.createKeyStore(certificate, alias);

            tempFile = Files.createTempFile(alias, ".ks", PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------")));
            password = generatePassword();

            try (OutputStream stream = new FileOutputStream(tempFile.toFile())) {
                keyStore.store(stream, password.toCharArray());
            }

        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new IllegalArgumentException(String.format("Error creating key store %s: %s", alias, e.getMessage()), e);
        }

        return this;
    }

    private static String generatePassword() {
        return SECURE_RANDOM.ints(16, 'A', 'Z' + 1).mapToObj(i -> String.valueOf((char)i)).collect(Collectors.joining());
    }

}
