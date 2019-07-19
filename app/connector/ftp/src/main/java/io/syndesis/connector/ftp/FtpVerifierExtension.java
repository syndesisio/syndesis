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
package io.syndesis.connector.ftp;

import java.io.IOException;
import java.util.Map;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.verifier.DefaultComponentVerifierExtension;
import org.apache.camel.component.extension.verifier.ResultBuilder;
import org.apache.camel.component.extension.verifier.ResultErrorBuilder;
import org.apache.camel.component.extension.verifier.ResultErrorHelper;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import io.syndesis.connector.support.util.ConnectorOptions;

public class FtpVerifierExtension extends DefaultComponentVerifierExtension {

    protected FtpVerifierExtension(String defaultScheme, CamelContext context) {
        super(defaultScheme, context);
    }

    // *********************************
    // Parameters validation
    //
    @Override
    protected Result verifyParameters(Map<String, Object> parameters) {
        ResultBuilder builder = ResultBuilder.withStatusAndScope(Result.Status.OK, Scope.PARAMETERS)
                .error(ResultErrorHelper.requiresOption("host", parameters))
                .error(ResultErrorHelper.requiresOption("port", parameters));
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
                .error(parameters, this::verifyCredentials).build();
    }

    @SuppressWarnings("PMD.NPathComplexity")
    private void verifyCredentials(ResultBuilder builder, Map<String, Object> parameters) {

        final String host = ConnectorOptions.extractOption(parameters, "host");
        final Integer port = ConnectorOptions.extractOptionAndMap(parameters, "port", Integer::parseInt, 21);
        final String userName = ConnectorOptions.extractOption(parameters, "username", "anonymous");

        String password = "";
        if (! "anonymous".equals(userName)) {
            password = ConnectorOptions.extractOption(parameters, "password", password);
        }

        int reply;
        FTPClient ftp = new FTPClient();

        String illegalParametersMessage = "Unable to connect to the FTP server";
        boolean hasValidParameters = false;

        try {
            ftp.connect(host, port);
            reply = ftp.getReplyCode();
            hasValidParameters = FTPReply.isPositiveCompletion(reply);
        } catch (IOException e) {
            illegalParametersMessage = e.getMessage();
        }

        if (!hasValidParameters) {
            builder.error(
                    ResultErrorBuilder.withCodeAndDescription(VerificationError.StandardCode.ILLEGAL_PARAMETER_VALUE,
                            illegalParametersMessage).parameterKey("host").parameterKey("port").build());

        } else {

            boolean isAuthenticated = false;
            String authentionErrorMessage = "Authentication failed";

            try {
                isAuthenticated = ftp.login(userName, password);
            } catch (IOException ioe) {
                authentionErrorMessage = ioe.getMessage();
            }
            if (!isAuthenticated) {

                builder.error(ResultErrorBuilder
                        .withCodeAndDescription(VerificationError.StandardCode.AUTHENTICATION, authentionErrorMessage)
                        .parameterKey("username").parameterKey("password").build());

            } else {
                try {
                    ftp.logout();
                    ftp.disconnect();
                } catch (IOException ignored) {
                    // ignore
                }

            }
        }

    }

}
