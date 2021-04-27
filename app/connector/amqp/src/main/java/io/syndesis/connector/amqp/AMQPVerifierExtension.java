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
package io.syndesis.connector.amqp;

import java.util.HashMap;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.JMSException;

import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.verifier.DefaultComponentVerifierExtension;
import org.apache.camel.component.extension.verifier.ResultBuilder;
import org.apache.camel.component.extension.verifier.ResultErrorBuilder;
import org.apache.camel.component.extension.verifier.ResultErrorHelper;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.syndesis.connector.support.util.ConnectorOptions;

/**
 * Component verifier for AMQP Connector.
 */
public class AMQPVerifierExtension extends DefaultComponentVerifierExtension {

    private static final Logger LOG = LoggerFactory.getLogger(AMQPVerifierExtension.class);
    public static final String CONNECTION_URI = "connectionUri";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String BROKER_CERTIFICATE = "brokerCertificate";
    public static final String CLIENT_CERTIFICATE = "clientCertificate";

    protected AMQPVerifierExtension(String defaultScheme, CamelContext context) {
        super(defaultScheme, context);
    }

    // *********************************
    // Parameters validation
    // *********************************

    @Override
    protected Result verifyParameters(Map<String, Object> parameters) {
        ResultBuilder builder = ResultBuilder.withStatusAndScope(Result.Status.OK, Scope.PARAMETERS)
                .error(ResultErrorHelper.requiresOption(CONNECTION_URI, parameters));

        if (builder.build().getErrors().isEmpty()) {
            verifyCredentials(builder, parameters);
        }
        return builder.build();
    }

    // *********************************
    // Connectivity validation
    // *********************************
    @Override
    protected Result verifyConnectivity(Map<String, Object> parameters) {
        return ResultBuilder.withStatusAndScope(Result.Status.OK, Scope.CONNECTIVITY)
                .error(parameters, AMQPVerifierExtension::verifyCredentials)
                .build();
    }

    private static void verifyCredentials(ResultBuilder builder, Map<String, Object> parameters) {

        final String connectionUri = ConnectorOptions.extractOption(parameters, CONNECTION_URI);
        final String username = ConnectorOptions.extractOption(parameters, USERNAME);
        final String password = ConnectorOptions.extractOption(parameters, PASSWORD);
        final boolean skipCertificateCheck = ConnectorOptions.extractOptionAndMap(parameters,
            "skipCertificateCheck", Boolean::parseBoolean, false);
        final String brokerCertificate = ConnectorOptions.extractOption(parameters, BROKER_CERTIFICATE);
        final String clientCertificate = ConnectorOptions.extractOption(parameters, CLIENT_CERTIFICATE);

        LOG.debug("Validating AMQP connection to {}", connectionUri);
        final AMQPUtil.ConnectionParameters connectionParameters = new AMQPUtil.ConnectionParameters(connectionUri,
                username, password, brokerCertificate, clientCertificate, skipCertificateCheck);
        JmsConnectionFactory connectionFactory = AMQPUtil.createConnectionFactory(connectionParameters);
        Connection connection = null;
        try {
            // try to create and start the JMS connection
            connection = connectionFactory.createConnection();
            connection.start();
        } catch (JMSException e) {
            final Map<String, Object> redacted = new HashMap<>(parameters);
            redacted.replace(PASSWORD, "********");
            LOG.warn("Unable to connect to AMQP Broker with parameters {}, Message: {}, error code: {}",
                    redacted, e.getMessage(), e.getErrorCode(), e);
            builder.error(ResultErrorBuilder.withCodeAndDescription(
                    VerificationError.StandardCode.ILLEGAL_PARAMETER_VALUE, e.getMessage())
                    .parameterKey(CONNECTION_URI)
                    .parameterKey(USERNAME)
                    .parameterKey(PASSWORD)
                    .build());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException ignored) {
                    // ignore
                }
            }
        }
    }
}
