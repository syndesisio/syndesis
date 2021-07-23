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

import java.util.Map;

import io.syndesis.connector.kafka.service.KafkaBrokerService;
import io.syndesis.connector.kafka.service.KafkaBrokerServiceException;
import io.syndesis.connector.kafka.service.KafkaBrokerServiceImpl;
import io.syndesis.connector.support.util.ConnectorOptions;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.verifier.DefaultComponentVerifierExtension;
import org.apache.camel.component.extension.verifier.ResultBuilder;
import org.apache.camel.component.extension.verifier.ResultErrorBuilder;
import org.apache.camel.component.extension.verifier.ResultErrorHelper;
import org.apache.camel.util.ObjectHelper;

/**
 * Component verifier for Kafka Connector.
 */
public class KafkaVerifierExtension extends DefaultComponentVerifierExtension {

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
        final String brokers = ConnectorOptions.extractOption(parameters, KafkaBrokerService.BROKERS);
        final String certificate = ConnectorOptions.extractOption(parameters, KafkaBrokerService.BROKER_CERTIFICATE);
        final String transportProtocol = ConnectorOptions.extractOption(parameters, KafkaBrokerService.TRANSPORT_PROTOCOL);
        // TODO: there is a defaultValue in kafka.json but it doesn't work, it should be fixed.
        final String saslMechanism = ConnectorOptions.extractOption(parameters, KafkaBrokerService.SASL_MECHANISM, "PLAIN");
        final String saslLoginCallbackHandlerClass = ConnectorOptions.extractOption(parameters, KafkaBrokerService.SASL_LOGIN_CALLBACK_HANDLER_CLASS);
        final String username = ConnectorOptions.extractOption(parameters, KafkaBrokerService.USERNAME);
        final String password = ConnectorOptions.extractOption(parameters, KafkaBrokerService.PASSWORD);
        final String oauthTokenEndpointURI = ConnectorOptions.extractOption(parameters, KafkaBrokerService.OAUTH_TOKEN_ENDPOINT_URI);
        if (ObjectHelper.isNotEmpty(brokers)) {
            boolean validParameters = true;
            if (KafkaBrokerService.SASL_SSL.equals(transportProtocol)) {
                if (ObjectHelper.isEmpty(username)) {
                    builder.error(
                        ResultErrorBuilder.withCodeAndDescription(VerificationError.StandardCode.MISSING_PARAMETER, "Missing Username")
                            .parameterKey(KafkaBrokerService.USERNAME)
                            .build()
                    );
                    validParameters = false;
                }
                if (ObjectHelper.isEmpty(password)) {
                    builder.error(
                        ResultErrorBuilder.withCodeAndDescription(VerificationError.StandardCode.MISSING_PARAMETER, "Missing Password")
                            .parameterKey(KafkaBrokerService.PASSWORD)
                            .build()
                    );
                    validParameters = false;
                }
            }
            if (KafkaBrokerService.OAUTHBEARER.equals(saslMechanism) &&  ObjectHelper.isEmpty(oauthTokenEndpointURI)) {
                builder.error(
                    ResultErrorBuilder.withCodeAndDescription(VerificationError.StandardCode.MISSING_PARAMETER, "Missing OAuth Token Endpoint URI")
                        .parameterKey(KafkaBrokerService.OAUTH_TOKEN_ENDPOINT_URI)
                        .build()
                );
                validParameters = false;
            }

            if (validParameters) {
                KafkaBrokerService kafkaBrokerService = new KafkaBrokerServiceImpl(brokers, transportProtocol, certificate);
                if (username != null) {
                    kafkaBrokerService.setUsername(username);
                    kafkaBrokerService.setPassword(password);
                    kafkaBrokerService.setSaslMechanism(saslMechanism);
                    kafkaBrokerService.setSaslLoginCallbackHandlerClass(saslLoginCallbackHandlerClass);
                    kafkaBrokerService.setOauthTokenEndpointURI(oauthTokenEndpointURI);
                }
                try {
                    kafkaBrokerService.ping();
                } catch (KafkaBrokerServiceException e) {
                    builder.error(
                        ResultErrorBuilder.withCodeAndDescription(VerificationError.StandardCode.ILLEGAL_PARAMETER_VALUE, "Unable to connect to Kafka broker: " + e.getMessage())
                            .parameterKey(KafkaBrokerService.BROKERS)
                            .detail(VerificationError.ExceptionAttribute.EXCEPTION_INSTANCE, e)
                            .detail(VerificationError.ExceptionAttribute.EXCEPTION_CLASS, e.getClass().getName())
                            .build()
                    );
                }
            }
        } else {
            builder.error(
                ResultErrorBuilder.withCodeAndDescription(VerificationError.StandardCode.ILLEGAL_PARAMETER_VALUE, "Invalid blank Kafka brokers ")
                    .parameterKey(KafkaBrokerService.BROKERS)
                    .build()
            );
        }
    }
}
