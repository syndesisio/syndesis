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
package io.syndesis.connector.email;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Map;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.jsse.KeyManagersParameters;
import org.apache.camel.util.jsse.KeyStoreParameters;
import org.apache.camel.util.jsse.SSLContextParameters;
import org.apache.camel.util.jsse.TrustManagersParameters;
import io.syndesis.connector.support.util.ConnectorOptions;
import io.syndesis.connector.support.util.KeyStoreHelper;

public class EMailUtil implements EMailConstants {

    private static boolean isSecure(String protocol) {
        if (ObjectHelper.isEmpty(protocol)) {
            return false;
        }

        Protocol p = Protocol.getValueOf(protocol);
        return p != null && p.isSecure();
    }

    private static KeyStore createKeyStore(Map<String, Object> options)
        throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException {
        String certContent = ConnectorOptions.extractOption(options, SERVER_CERTIFICATE);
        if (ObjectHelper.isEmpty(certContent)) {
            return KeyStoreHelper.defaultKeyStore();
        }

        return KeyStoreHelper.createKeyStoreWithCustomCertificate("mail", certContent);
    }

    public static SSLContextParameters createSSLContextParameters(Map<String, Object> options) {
        String protocol = ConnectorOptions.extractOption(options, PROTOCOL);
        if (! isSecure(protocol)) {
            return null;
        }

        KeyStoreParameters keystoreParams = new KeyStoreParameters() {
            @Override
            public KeyStore createKeyStore() throws GeneralSecurityException, IOException {
                try {
                    return EMailUtil.createKeyStore(options);
                } catch (Exception e) {
                    throw new GeneralSecurityException(e);
                }
            }
        };

        KeyManagersParameters keyManagersParams = new KeyManagersParameters();
        keyManagersParams.setKeyStore(keystoreParams);

        TrustManagersParameters trustManagersParams = new TrustManagersParameters();
        trustManagersParams.setKeyStore(keystoreParams);

        SSLContextParameters sslContextParameters = new SSLContextParameters();
        sslContextParameters.setKeyManagers(keyManagersParams);
        sslContextParameters.setTrustManagers(trustManagersParams);
        return sslContextParameters;
    }
}
