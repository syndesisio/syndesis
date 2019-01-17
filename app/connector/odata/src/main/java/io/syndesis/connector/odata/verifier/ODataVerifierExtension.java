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
package io.syndesis.connector.odata.verifier;

import java.security.cert.CertificateException;
import java.util.Map;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.verifier.DefaultComponentVerifierExtension;
import org.apache.camel.component.extension.verifier.ResultBuilder;
import org.apache.camel.component.extension.verifier.ResultErrorBuilder;
import org.apache.camel.component.extension.verifier.ResultErrorHelper;
import org.apache.camel.util.ObjectHelper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.syndesis.connector.odata.ODataConstants;
import io.syndesis.connector.odata.ODataUtil;

public class ODataVerifierExtension extends DefaultComponentVerifierExtension implements ODataConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(ODataVerifierExtension.class);

    protected ODataVerifierExtension(String defaultScheme, CamelContext context) {
        super(defaultScheme, context);
    }

    // *********************************
    // Parameters validation
    //
    @Override
    protected Result verifyParameters(Map<String, Object> parameters) {
        ResultBuilder builder = ResultBuilder.withStatusAndScope(Result.Status.OK, Scope.PARAMETERS)
                .error(ResultErrorHelper.requiresOption(SERVICE_URI, parameters));

        Object userName = parameters.get(BASIC_USER_NAME);
        Object password = parameters.get(BASIC_PASSWORD);

        if (
                // Basic authentication requires both user name and password
                (ObjectHelper.isEmpty(userName) && ObjectHelper.isNotEmpty(password))
                ||
                (ObjectHelper.isNotEmpty(userName) && ObjectHelper.isEmpty(password)))
        {
            builder.error(ResultErrorBuilder.withCodeAndDescription(VerificationError.StandardCode.GENERIC,
                "Basic authentication requires both a user name and password").
                          parameterKey(BASIC_USER_NAME).parameterKey(BASIC_PASSWORD).build());
        }

        final String serviceUrl = (String) parameters.get(SERVICE_URI);
        if (ODataUtil.isServiceSSL(serviceUrl) && ObjectHelper.isEmpty(parameters.get(CLIENT_CERTIFICATE))) {
            builder.error(ResultErrorBuilder.withCodeAndDescription(VerificationError.StandardCode.GENERIC,
            "An https / ssl OData connection requires an ssl client certificate.").
                      parameterKey(CLIENT_CERTIFICATE).build());
        }

        return builder.build();
    }

    // *********************************
    // Connectivity validation
    // *********************************
    @Override
    protected Result verifyConnectivity(Map<String, Object> parameters) {
        return ResultBuilder.withStatusAndScope(Result.Status.OK, Scope.CONNECTIVITY)
                .error(parameters, this::verifyConnection).build();
    }

    private void verifyConnection(ResultBuilder builder, Map<String, Object> parameters) {
        if (!builder.build().getErrors().isEmpty()) {
            return;
        }
        final String serviceUrl = (String) parameters.get(SERVICE_URI);
        LOGGER.debug("Validating OData connection to {}", serviceUrl);

        if (ObjectHelper.isNotEmpty(serviceUrl)) {
            try (CloseableHttpClient httpClient = ODataUtil.createHttpClient(parameters)) {

                HttpGet httpGet = new HttpGet(serviceUrl + METADATA_ENDPOINT);
                CloseableHttpResponse response = httpClient.execute(httpGet);
                if (response.getStatusLine().getStatusCode() == 401) {
                    builder
                        .error(ResultErrorBuilder.withCodeAndDescription(VerificationError.StandardCode.AUTHENTICATION, "Cannot authenticate to serviceUrl").parameterKey(SERVICE_URI).build());
                } else if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() > 299) {
                    // 2xx is OK, anything else we regard as failure
                    builder
                        .error(ResultErrorBuilder.withCodeAndDescription(VerificationError.StandardCode.AUTHENTICATION, "Invalid serviceUrl").parameterKey(SERVICE_URI).build());
                }

            } catch (CertificateException e) {
                builder.error(ResultErrorBuilder.withCodeAndDescription(VerificationError.StandardCode.AUTHENTICATION, "Invalid certificate: " + e.getMessage()).
                              parameterKey(CLIENT_CERTIFICATE).
                              build());
            } catch (Exception e) {
                builder.error(ResultErrorBuilder.withCodeAndDescription(VerificationError.StandardCode.AUTHENTICATION, "Failure to communicate with serviceUrl: " + e.getMessage()).
                              parameterKey(SERVICE_URI).parameterKey(CLIENT_CERTIFICATE).
                              build());
            }

        } else {
            builder.error(
                ResultErrorBuilder.withCodeAndDescription(VerificationError.StandardCode.ILLEGAL_PARAMETER_VALUE, "Invalid blank OData service URL")
                    .parameterKey(SERVICE_URI)
                    .build()
            );
        }
    }
}
