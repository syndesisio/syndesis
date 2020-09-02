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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.syndesis.connector.support.util.ConnectorOptions;
import io.syndesis.connector.support.util.KeyStoreHelper;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.component.kafka.KafkaConfiguration;
import org.apache.camel.support.jsse.KeyManagersParameters;
import org.apache.camel.support.jsse.KeyStoreParameters;
import org.apache.camel.support.jsse.SSLContextParameters;
import org.apache.camel.support.jsse.TrustManagersParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaConnectionCustomizer implements ComponentProxyCustomizer {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaConnectionCustomizer.class);
    private static final String CERTIFICATE_OPTION = "brokerCertificate";
    public static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        KafkaConfiguration configuration = new KafkaConfiguration();
        if (ConnectorOptions.extractOption(options, CERTIFICATE_OPTION) != null) {
            LOG.info("Setting SSLContextParameters configuration as a self-signed certificate was provided");
            SSLContextParameters sslContextParameters = createSSLContextParameters(
                ConnectorOptions.extractOption(options, CERTIFICATE_OPTION));
            configuration.setSslContextParameters(sslContextParameters);
            configuration.setSecurityProtocol("SSL");
            // If present, Kafka client 2.0 is using this parameter to verify host
            // we must set to blank to skip host verification
            configuration.setSslEndpointAlgorithm("");
        }

        String extraOptions = options.getOrDefault("extraOptions", "[]").toString();
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, String>> attributes = MAPPER.readValue(extraOptions, List.class);
            for (Map<String, String> attribute : attributes) {
                final String key = attribute.get("key").trim();
                if(!key.isEmpty()) {
                    final String value = attribute.get("value");
                    //make sure we don't have a specific option for this
                    Method method = findSetter(key);
                    if (method != null) {
                        try {
                            if(method.getParameterTypes()[0] == Integer.class) {
                                method.invoke(configuration, Integer.valueOf(value));
                            } else if(method.getParameterTypes()[0] == Boolean.class) {
                                method.invoke(configuration, Boolean.valueOf(value));
                            } else {
                                method.invoke(configuration, value);
                            }
                        } catch (IllegalAccessException e) {
                            LOG.error("This should never happen, we already checked it is a public method.", e);
                        } catch (InvocationTargetException e) {
                            LOG.error("Couldn't assign Additional Property " + key, e);
                            configuration.getAdditionalProperties().put("additionalProperties." + key,
                                value);
                        }
                    } else {
                        //default case, this is an additional property
                        configuration.getAdditionalProperties().put("additionalProperties." + key,
                            value);
                    }
                }
            }
        } catch (JsonProcessingException e) {
            LOG.error(e.getMessage(), e);
        }
        options.put("configuration", configuration);
    }

    private static Method findSetter(final String name) {
        //If there are "." it means we have to convert to "Camel-alike" properties
        String property = name;
        while(property.contains(".")) {
            int i = property.indexOf('.');
            property = property.substring(0, i)
                           + property.substring(i +1 , i + 2).toUpperCase(Locale.ENGLISH)
                           + property.substring(i + 2);
        }

        //We are looking for a setter for this property
        property = "set" + property.substring(0, 1).toUpperCase(Locale.ENGLISH) + property.substring(1);
        for (Method method : KafkaConfiguration.class.getDeclaredMethods()) {
            if (Modifier.isPublic(method.getModifiers()) &&
                    method.getReturnType().equals(void.class) &&
                    method.getParameterTypes().length == 1 &&
                    method.getName().equals(property)) {
                return method;
            }
        }
        return null;
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
