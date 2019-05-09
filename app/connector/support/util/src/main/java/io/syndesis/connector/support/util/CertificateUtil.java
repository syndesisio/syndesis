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
package io.syndesis.connector.support.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * Utility class for working with X.509 Certificates.
 *
 * @author dhirajsb
 */
public final class CertificateUtil {

    private CertificateUtil() {
        // utility class
    }

    public static KeyManager[] createKeyManagers(String clientCertificate, String alias) throws GeneralSecurityException, IOException {
        final KeyStore clientKs = createKeyStore(clientCertificate, alias);

        // create Key Manager
        KeyManagerFactory kmFactory = KeyManagerFactory.getInstance("PKIX");
        kmFactory.init(clientKs, null);
        return kmFactory.getKeyManagers();
    }

    public static TrustManager[] createTrustAllTrustManagers() {
        return new TrustManager[]{ new TrustAllTrustManager() };
    }

    public static TrustManager[] createTrustManagers(String brokerCertificate, String alias) throws GeneralSecurityException,
            IOException {
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("X509");
        trustManagerFactory.init(createKeyStore(brokerCertificate, alias));
        return trustManagerFactory.getTrustManagers();
    }

    public static KeyStore createKeyStore(String certificate, String alias) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        final KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, null);
        // create client key entry
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        final Certificate generated = factory.generateCertificate(new ByteArrayInputStream
                (getMultilineCertificate(certificate).getBytes("UTF-8")));
        keyStore.setCertificateEntry(alias, generated);
        return keyStore;
    }

    // X.509 PEM parser requires a newline after the header
    public static String getMultilineCertificate(String certificate) {
        // is this a multi line certificate?
        if (certificate.indexOf('\n') != -1) {
            return certificate;
        } else {
            // insert newline after header
            return certificate.replace("-----BEGIN CERTIFICATE-----", "-----BEGIN CERTIFICATE-----\n");
        }
    }
}
