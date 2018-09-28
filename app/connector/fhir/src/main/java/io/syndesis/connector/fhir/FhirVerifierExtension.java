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
package io.syndesis.connector.fhir;

import java.util.Map;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.rest.client.api.IGenericClient;
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

    protected FhirVerifierExtension(String defaultScheme, CamelContext context) {
        super(defaultScheme, context);
    }


    // *********************************
    // Parameters validation
    // *********************************

    @Override
    protected Result verifyParameters(Map<String, Object> parameters) {
        ResultBuilder builder = ResultBuilder.withStatusAndScope(Result.Status.OK, Scope.PARAMETERS).error(ResultErrorHelper.requiresOption("serverUrl", parameters))
            .error(ResultErrorHelper.requiresOption("fhirVersion", parameters));

        return builder.build();
    }

    // *********************************
    // Connectivity validation
    // *********************************

    @Override
    protected Result verifyConnectivity(Map<String, Object> parameters) {
        return ResultBuilder.withStatusAndScope(Result.Status.OK, Scope.CONNECTIVITY)
            .error(parameters, this::verifyFhirVersion)
            .error(parameters, this::verifyConnection)
            .build();
    }

    private void verifyFhirVersion(ResultBuilder builder, Map<String, Object> parameters) {
        final String fhirVersion = (String) parameters.get("fhirVersion");
        try {
            FhirVersionEnum fhirVersionEnum = FhirVersionEnum.valueOf(fhirVersion);
            parameters.put("fhirVersion", fhirVersionEnum);
        } catch (IllegalArgumentException e) {
            builder.error(
                ResultErrorBuilder.withCodeAndDescription(VerificationError.StandardCode.ILLEGAL_PARAMETER_VALUE, "Invalid FHIR version")
                    .parameterKey("fhirVersion")
                    .build());
        }
    }

    private void verifyConnection(ResultBuilder builder, Map<String, Object> parameters) {
        if (!builder.build().getErrors().isEmpty()) {
            return;
        }
        final String serverUrl = (String) parameters.get("serverUrl");
        final FhirVersionEnum fhirVersion = (FhirVersionEnum) parameters.get("fhirVersion");

        LOG.debug("Validating FHIR connection to {} with FHIR version {}", serverUrl, fhirVersion);

        if (ObjectHelper.isNotEmpty(serverUrl)) {
            try {
                FhirContext fhirContext = new FhirContext(fhirVersion);
                IGenericClient iGenericClient = fhirContext.newRestfulGenericClient(serverUrl);
                iGenericClient.forceConformanceCheck();
            } catch (Exception e) {
                builder.error(
                    ResultErrorBuilder.withCodeAndDescription(VerificationError.StandardCode.ILLEGAL_PARAMETER_GROUP_COMBINATION, "Unable to connect to FHIR server")
                        .detail(VerificationError.ExceptionAttribute.EXCEPTION_INSTANCE, e)
                        .parameterKey("serverUrl")
                        .parameterKey("fhirVersion")
                        .build()
                );
            }
        } else {
            builder.error(
                ResultErrorBuilder.withCodeAndDescription(VerificationError.StandardCode.ILLEGAL_PARAMETER_VALUE, "Invalid blank FHIR server URL")
                    .parameterKey("serverUrl")
                    .build()
            );
        }
    }
}
