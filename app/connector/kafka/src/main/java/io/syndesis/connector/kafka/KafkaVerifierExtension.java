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
package io.syndesis.connector.kafka
    ;

import io.syndesis.connector.support.util.KeyStoreHelper;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.verifier.DefaultComponentVerifierExtension;
import org.apache.camel.component.extension.verifier.ResultBuilder;
import org.apache.camel.component.extension.verifier.ResultErrorBuilder;
import org.apache.camel.component.extension.verifier.ResultErrorHelper;
import org.apache.camel.util.ObjectHelper;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.syndesis.connector.support.util.ConnectorOptions;

import java.util.Map;
import java.util.Properties;

/**
 * Component verifier for Kafka Connector.
 *
 * @author valdar
 */
public class KafkaVerifierExtension extends DefaultComponentVerifierExtension {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaVerifierExtension.class);

    protected KafkaVerifierExtension(String scheme, CamelContext context) {
        super(scheme, context);
    }

    // *********************************
    // Parameters validation
    // *********************************

    @Override
    protected Result verifyParameters(Map<String, Object> parameters) {
        ResultBuilder builder = ResultBuilder.withStatusAndScope(Result.Status.OK, Scope.PARAMETERS)
            .error(ResultErrorHelper.requiresOption("brokers", parameters));

        return builder.build();
    }

    // *********************************
    // Connectivity validation
    // *********************************
    @Override
    protected Result verifyConnectivity(Map<String, Object> parameters) {
        return ResultBuilder.withStatusAndScope(Result.Status.OK, Scope.CONNECTIVITY)
            .error(parameters, KafkaVerifierExtension::verifyCredentials)
            .build();
    }

    private static void verifyCredentials(ResultBuilder builder, Map<String, Object> parameters) {
        final String brokers = ConnectorOptions.extractOption(parameters, "brokers");
        final String certificate = ConnectorOptions.extractOption(parameters, "brokerCertificate");
        final String transportProtocol = ConnectorOptions.extractOption(parameters, "transportProtocol");

        LOG.debug("Validating Kafka connection to {} with protocol {}", brokers, transportProtocol);
        if (ObjectHelper.isNotEmpty(brokers)) {
            // Use a key store helper if a self signed certificate is provided
            KeyStoreHelper brokerKeyStoreHelper = certificate != null ? new KeyStoreHelper(certificate, "brokerCertificate").store() : null;
            Properties properties = getKafkaAdminClientConfiguration(brokers, transportProtocol, brokerKeyStoreHelper);
            try (AdminClient client = KafkaAdminClient.create(properties)) {
                ListTopicsResult topics = client.listTopics();
                topics.names().get();
            } catch (Exception e) {
                builder.error(
                    ResultErrorBuilder.withCodeAndDescription(VerificationError.StandardCode.ILLEGAL_PARAMETER_VALUE, "Unable to connect to Kafka broker")
                        .parameterKey("brokers")
                        .detail(VerificationError.ExceptionAttribute.EXCEPTION_INSTANCE, e)
                        .detail(VerificationError.ExceptionAttribute.EXCEPTION_CLASS, e.getClass().getName())
                        .build()
                );
            } finally {
                if (brokerKeyStoreHelper != null) {
                    // Clean up temporary resources used by key store
                    boolean keystoreDeleted = brokerKeyStoreHelper.clean();
                    if (!keystoreDeleted) {
                        LOG.warn("Impossible to delete temporary keystore located at " + brokerKeyStoreHelper.getKeyStorePath());
                    }
                }
            }
        } else {
            builder.error(
                ResultErrorBuilder.withCodeAndDescription(VerificationError.StandardCode.ILLEGAL_PARAMETER_VALUE, "Invalid blank Kafka brokers ")
                    .parameterKey("brokers")
                    .build()
            );
        }
    }

    private static Properties getKafkaAdminClientConfiguration(String brokers, String transportProtocol, KeyStoreHelper brokerKeyStoreHelper) {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", brokers);
        properties.put("connections.max.idle.ms", 10000);
        properties.put("request.timeout.ms", 5000);
        if (!"PLAINTEXT".equals(transportProtocol)) {
            properties.put("security.protocol", "SSL");
            if (brokerKeyStoreHelper != null) {
                properties.put("ssl.endpoint.identification.algorithm", "");
                properties.put("ssl.keystore.location", brokerKeyStoreHelper.getKeyStorePath());
                properties.put("ssl.keystore.password", brokerKeyStoreHelper.getPassword());
                properties.put("ssl.key.password", brokerKeyStoreHelper.getPassword());
                properties.put("ssl.truststore.location", brokerKeyStoreHelper.getKeyStorePath());
                properties.put("ssl.truststore.password", brokerKeyStoreHelper.getPassword());

            }
        }
        return properties;
    }
}
