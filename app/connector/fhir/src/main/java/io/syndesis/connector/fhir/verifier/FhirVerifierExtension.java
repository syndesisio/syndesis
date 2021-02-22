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
package io.syndesis.connector.fhir.verifier;

import java.util.Map;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import io.syndesis.connector.support.util.ConnectorOptions;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.verifier.DefaultComponentVerifierExtension;
import org.apache.camel.component.extension.verifier.ResultBuilder;
import org.apache.camel.component.extension.verifier.ResultErrorBuilder;
import org.apache.camel.component.extension.verifier.ResultErrorHelper;
import org.apache.camel.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FhirVerifierExtension extends DefaultComponentVerifierExtension {

    private static final Logger LOG = LoggerFactory.getLogger(FhirVerifierExtension.class);
    public static final String FHIR_VERSION = "fhirVersion";
    public static final String SERVER_URL = "serverUrl";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String ACCESS_TOKEN = "accessToken";

    protected FhirVerifierExtension(String defaultScheme, CamelContext context) {
        super(defaultScheme, context);
    }


    // *********************************
    // Parameters validation
    // *********************************

    @Override
    protected Result verifyParameters(Map<String, Object> parameters) {
        ResultBuilder builder = ResultBuilder.withStatusAndScope(Result.Status.OK, Scope.PARAMETERS).error(ResultErrorHelper.requiresOption(SERVER_URL, parameters))
            .error(ResultErrorHelper.requiresOption(FHIR_VERSION, parameters));

        return builder.build();
    }

    // *********************************
    // Connectivity validation
    // *********************************

    @Override
    protected Result verifyConnectivity(Map<String, Object> parameters) {
        return ResultBuilder.withStatusAndScope(Result.Status.OK, Scope.CONNECTIVITY)
            .error(parameters, FhirVerifierExtension::verifyFhirVersion)
            .error(parameters, FhirVerifierExtension::verifyConnection)
            .build();
    }

    private static void verifyFhirVersion(ResultBuilder builder, Map<String, Object> parameters) {
        try {
            final FhirVersionEnum fhirVersionEnum = ConnectorOptions.extractOptionAndMap(
                parameters, FHIR_VERSION, FhirVersionEnum::valueOf);
            parameters.put(FHIR_VERSION, fhirVersionEnum);
        } catch (Exception e) {
            builder.error(
                ResultErrorBuilder.withCodeAndDescription(VerificationError.StandardCode.ILLEGAL_PARAMETER_VALUE, "Invalid FHIR version")
                    .parameterKey(FHIR_VERSION)
                    .build());
        }
    }

    private static void verifyConnection(ResultBuilder builder, Map<String, Object> parameters) {
        if (!builder.build().getErrors().isEmpty()) {
            return;
        }
        final String serverUrl = ConnectorOptions.extractOption(parameters, SERVER_URL);
        final FhirVersionEnum fhirVersion = ConnectorOptions.extractOptionAsType(
            parameters, FHIR_VERSION, FhirVersionEnum.class);
        final String username = ConnectorOptions.extractOption(parameters, USERNAME);
        final String password = ConnectorOptions.extractOption(parameters, PASSWORD);
        final String accessToken = ConnectorOptions.extractOption(parameters, ACCESS_TOKEN);

        LOG.debug("Validating FHIR connection to {} with FHIR version {}", serverUrl, fhirVersion);

        if (ObjectHelper.isNotEmpty(serverUrl)) {
            try {
                FhirContext fhirContext = new FhirContext(fhirVersion);
                IGenericClient iGenericClient = fhirContext.newRestfulGenericClient(serverUrl);

                if (ObjectHelper.isNotEmpty(username) || ObjectHelper.isNotEmpty(password)) {
                    if (ObjectHelper.isEmpty(username) || ObjectHelper.isEmpty(password)) {
                        builder.error(
                            ResultErrorBuilder.withCodeAndDescription(VerificationError.StandardCode.ILLEGAL_PARAMETER_GROUP_COMBINATION,
                                "Both username and password must be provided to enable basic authentication")
                                .parameterKey(USERNAME)
                                .parameterKey(PASSWORD)
                                .build());
                    } else if (ObjectHelper.isNotEmpty(accessToken)) {
                        builder.error(
                            ResultErrorBuilder.withCodeAndDescription(VerificationError.StandardCode.ILLEGAL_PARAMETER_GROUP_COMBINATION,
                                "You must provide either username and password or bearer token to enable authentication")
                                .parameterKey(ACCESS_TOKEN)
                                .build());
                    } else {
                        iGenericClient.registerInterceptor(new BasicAuthInterceptor(username, password));
                    }
                } else if (ObjectHelper.isNotEmpty(accessToken)) {
                    iGenericClient.registerInterceptor(new BearerTokenAuthInterceptor(accessToken));
                }
                iGenericClient.forceConformanceCheck();
            } catch (Exception e) {
                builder.error(
                    ResultErrorBuilder.withCodeAndDescription(VerificationError.StandardCode.ILLEGAL_PARAMETER_GROUP_COMBINATION, "Unable to connect to FHIR server")
                        .detail(VerificationError.ExceptionAttribute.EXCEPTION_INSTANCE, e)
                        .parameterKey(SERVER_URL)
                        .parameterKey(FHIR_VERSION)
                        .build()
                );
            }
        } else {
            builder.error(
                ResultErrorBuilder.withCodeAndDescription(VerificationError.StandardCode.ILLEGAL_PARAMETER_VALUE, "Invalid blank FHIR server URL")
                    .parameterKey(SERVER_URL)
                    .build()
            );
        }
    }
}
