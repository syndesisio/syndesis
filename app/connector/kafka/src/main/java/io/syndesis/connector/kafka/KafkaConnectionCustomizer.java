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
package io.syndesis.connector.kafka;

import java.util.Map;

import io.syndesis.connector.support.util.ConnectorOptions;
import io.syndesis.connector.support.util.KeyStoreHelper;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.component.kafka.KafkaConfiguration;
import org.apache.camel.util.jsse.KeyManagersParameters;
import org.apache.camel.util.jsse.KeyStoreParameters;
import org.apache.camel.util.jsse.SSLContextParameters;
import org.apache.camel.util.jsse.TrustManagersParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaConnectionCustomizer implements ComponentProxyCustomizer {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaConnectionCustomizer.class);
    private static final String CERTIFICATE_OPTION = "brokerCertificate";

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        if (ConnectorOptions.extractOption(options, CERTIFICATE_OPTION) != null) {
            LOG.info("Setting SSLContextParameters configuration as a self-signed certificate was provided");
            SSLContextParameters sslContextParameters = createSSLContextParameters(
                ConnectorOptions.extractOption(options, CERTIFICATE_OPTION));
            KafkaConfiguration configuration = new KafkaConfiguration();
            configuration.setSslContextParameters(sslContextParameters);
            configuration.setSecurityProtocol("SSL");
            // If present, Kafka client 2.0 is using this parameter to verify host
            // we must set to blank to skip host verification
            configuration.setSslEndpointAlgorithm("");
            options.put("configuration", configuration);
        }
    }

    private static SSLContextParameters createSSLContextParameters(String certificate) {
        KeyStoreHelper brokerKeyStoreHelper = new KeyStoreHelper(certificate, "brokerCertificate").store();

        KeyStoreParameters keyStore = createKeyStore(brokerKeyStoreHelper);
        KeyStoreParameters brokerStore = createKeyStore(brokerKeyStoreHelper);
        KeyManagersParameters kmp = createKeyManagerParameters(keyStore);
        TrustManagersParameters tmp = createTrustManagerParameters(brokerStore);

        SSLContextParameters scp = new SSLContextParameters();
        scp.setKeyManagers(kmp);
        scp.setTrustManagers(tmp);

        return scp;
    }

    private static KeyStoreParameters createKeyStore(KeyStoreHelper helper) {
        KeyStoreParameters keyStoreParams = new KeyStoreParameters();
        keyStoreParams.setResource(helper.getKeyStorePath());
        keyStoreParams.setPassword(helper.getPassword());
        return keyStoreParams;
    }

    private static KeyManagersParameters createKeyManagerParameters(KeyStoreParameters keyStore) {
        KeyManagersParameters keyManagersParams = new KeyManagersParameters();
        keyManagersParams.setKeyStore(keyStore);
        keyManagersParams.setKeyPassword(keyStore.getPassword());
        return keyManagersParams;
    }

    private static TrustManagersParameters createTrustManagerParameters(KeyStoreParameters keystore) {
        TrustManagersParameters trustManagersParams = new TrustManagersParameters();
        trustManagersParams.setKeyStore(keystore);
        return trustManagersParams;
    }
}
