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
import org.apache.camel.component.extension.ComponentVerifierExtension.VerificationError.StandardCode;
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
import io.syndesis.connector.support.util.ConnectorOptions;

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

        Object userName = ConnectorOptions.extractOption(parameters, BASIC_USER_NAME);
        Object password = ConnectorOptions.extractOption(parameters, BASIC_PASSWORD);

        if (
                // Basic authentication requires both user name and password
                (ObjectHelper.isEmpty(userName) && ObjectHelper.isNotEmpty(password))
                ||
                (ObjectHelper.isNotEmpty(userName) && ObjectHelper.isEmpty(password)))
        {
            builder.error(ResultErrorBuilder.withCodeAndDescription(VerificationError.StandardCode.MISSING_PARAMETER,
                "Basic authentication requires both a user name and password").
                          parameterKey(BASIC_USER_NAME).parameterKey(BASIC_PASSWORD).build());
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
        String serviceUrl = ConnectorOptions.extractOption(parameters, SERVICE_URI);
        LOGGER.debug("Validating OData connection to {}", serviceUrl);

        if (ObjectHelper.isEmpty(serviceUrl)) {
            String msg = "Invalid blank OData service URL";
            LOGGER.error(msg);
            builder.error(
                ResultErrorBuilder.withCodeAndDescription(StandardCode.ILLEGAL_PARAMETER_VALUE, msg)
                    .parameterKey(SERVICE_URI)
                    .build()
            );
        }

        try (CloseableHttpClient httpClient = ODataUtil.createHttpClient(parameters)) {
            serviceUrl = ODataUtil.removeEndSlashes(serviceUrl);
            HttpGet httpGet = new HttpGet(serviceUrl + METADATA_ENDPOINT);
            CloseableHttpResponse response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == 401) {
                String msg = "Cannot authenticate to service URL";
                LOGGER.error(msg);
                builder
                    .error(ResultErrorBuilder.withCodeAndDescription(StandardCode.AUTHENTICATION, msg)
                           .parameterKey(SERVICE_URI).build());
            } else if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() > 299) {
                // 2xx is OK, anything else we regard as failure
                String msg = "Invalid service URL";
                LOGGER.error(msg);
                    builder
                        .error(ResultErrorBuilder.withCodeAndDescription(StandardCode.AUTHENTICATION, msg)
                           .parameterKey(SERVICE_URI).build());
                }

        } catch (CertificateException e) {
            String msg = "Invalid certificate: " + e.getMessage();
            LOGGER.error(msg, e);
            builder.error(ResultErrorBuilder.withCodeAndDescription(StandardCode.AUTHENTICATION, msg)
                          .detail(VerificationError.ExceptionAttribute.EXCEPTION_CLASS, e.getClass().getName())
                          .detail(VerificationError.ExceptionAttribute.EXCEPTION_INSTANCE, e)
                          .parameterKey(SERVER_CERTIFICATE)
                          .build());
        } catch (Exception e) {
            String msg = "Failure to communicate with service URL";
            LOGGER.error(msg, e);
            builder.error(ResultErrorBuilder.withCodeAndDescription(StandardCode.AUTHENTICATION, msg)
                          .detail(VerificationError.ExceptionAttribute.EXCEPTION_CLASS, e.getClass().getName())
                          .detail(VerificationError.ExceptionAttribute.EXCEPTION_INSTANCE, e)
                          .parameterKey(SERVICE_URI).parameterKey(SERVER_CERTIFICATE)
                          .build());
        }
    }
}
