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

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.syndesis.connector.kafka.service.KafkaBrokerService;
import io.syndesis.connector.support.util.ConnectorOptions;
import io.syndesis.connector.support.util.KeyStoreHelper;
import io.syndesis.integration.component.proxy.ComponentDefinition;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import org.apache.camel.Component;
import org.apache.camel.component.kafka.KafkaComponent;
import org.apache.camel.component.kafka.KafkaConfiguration;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.jsse.KeyManagersParameters;
import org.apache.camel.util.jsse.KeyStoreParameters;
import org.apache.camel.util.jsse.SSLContextParameters;
import org.apache.camel.util.jsse.TrustManagersParameters;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.kafka.common.config.SaslConfigs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static io.syndesis.connector.kafka.service.KafkaBrokerService.OAUTHBEARER;
import static io.syndesis.connector.kafka.service.KafkaBrokerService.PLAIN;

public class KafkaConnector extends ComponentProxyComponent {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaConnector.class);
    public static final ObjectMapper MAPPER = new ObjectMapper();

    // this field is injected by reflection with the unencrypted value
    private String password;

    public KafkaConnector(String componentId, String componentScheme) {
        super(componentId, componentScheme);
    }

    @Override
    protected void configureDelegateComponent(ComponentDefinition definition, Component component, Map<String, Object> options) {
        super.configureDelegateComponent(definition, component, options);
        if (!(component instanceof KafkaComponent)) {
            return;
        }
        KafkaConfiguration configuration = new KafkaConfiguration();
        String certificate = ConnectorOptions.extractOption(options, KafkaBrokerService.BROKER_CERTIFICATE);
        String transportProtocol = ConnectorOptions.extractOption(options, KafkaBrokerService.TRANSPORT_PROTOCOL);
        String saslMechanism = ConnectorOptions.extractOption(options, KafkaBrokerService.SASL_MECHANISM);
        String saslLoginCallbackHandlerClass = ConnectorOptions.extractOption(options, KafkaBrokerService.SASL_LOGIN_CALLBACK_HANDLER_CLASS);
        String username = ConnectorOptions.extractOption(options, KafkaBrokerService.USERNAME);
        String passwd = getPassword();
        String oauthTokenEndpointURI = ConnectorOptions.extractOption(options, KafkaBrokerService.OAUTH_TOKEN_ENDPOINT_URI);

        if (ObjectHelper.isNotEmpty(username) && ObjectHelper.isNotEmpty(passwd)) {
            configuration.setSecurityProtocol(transportProtocol);
            configuration.setSaslMechanism(saslMechanism);
            if (OAUTHBEARER.equals(saslMechanism)) {
                LOG.info("Using kafka connection with SASL and OAuthBearerLoginModule");
                String template = "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required " +
                    "oauth.client.id=\"%s\" " +
                    "oauth.client.secret=\"%s\" " +
                    "oauth.token.endpoint.uri=\"%s\" ;";
                String config = String.format(template, username, passwd, oauthTokenEndpointURI);
                configuration.setSaslJaasConfig(config);
                Map<String, Object> additionalProps = new HashMap<>();
                additionalProps.put(SaslConfigs.SASL_LOGIN_CALLBACK_HANDLER_CLASS, saslLoginCallbackHandlerClass);
                configuration.setAdditionalProperties(additionalProps);
            } else if (PLAIN.equals(saslMechanism)) {
                LOG.info("Using kafka connection with SASL and PlainLoginModule");
                String template = "org.apache.kafka.common.security.plain.PlainLoginModule required " +
                    "username=\"%s\" " +
                    "password=\"%s\" ;";
                String config = String.format(template, username, passwd);
                configuration.setSaslJaasConfig(config);
            }
        } else if (certificate != null) {
            LOG.info("Setting SSLContextParameters configuration as a self-signed certificate was provided");
            SSLContextParameters sslContextParameters = createSSLContextParameters(certificate);
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
                if (!key.isEmpty()) {
                    final String value = attribute.get("value");
                    final String property = getCanonicalPropertyName(key);
                    try {
                        //make sure we don't have a specific attribute for this
                        if (PropertyUtils.isWriteable(configuration, property)) {
                            Class<?> c = PropertyUtils.getPropertyType(configuration, property);
                            if(c == Integer.class) {
                                PropertyUtils.setSimpleProperty(configuration, property, Integer.valueOf(value.trim()));
                            } else if(c == Boolean.class) {
                                PropertyUtils.setSimpleProperty(configuration, property, Boolean.valueOf(value.trim()));
                            } else {
                                PropertyUtils.setSimpleProperty(configuration, property, c.cast(value));
                            }
                        }
                    } catch (Exception e) {
                        LOG.error("Couldn't assign Additional Property " + key + " with value '" + value + "'", e);
                    }
                }
            }
        } catch (JsonProcessingException e) {
            LOG.error(e.getMessage(), e);
        }
        options.put("configuration", configuration);
        ((KafkaComponent) component).setConfiguration(configuration);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String passwd) {
        this.password = passwd;
    }

    private static String getCanonicalPropertyName(final String name) {
        //If there are "." it means we have to convert to "Camel-alike" properties
        String property = name;
        while (property.contains(".")) {
            int i = property.indexOf('.');
            property = property.substring(0, i)
                + property.substring(i + 1, i + 2).toUpperCase(Locale.ENGLISH)
                + property.substring(i + 2);
        }
        return property;
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
