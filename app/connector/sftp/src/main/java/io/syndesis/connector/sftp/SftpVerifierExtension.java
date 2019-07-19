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
package io.syndesis.connector.sftp;

import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.verifier.DefaultComponentVerifierExtension;
import org.apache.camel.component.extension.verifier.ResultBuilder;
import org.apache.camel.component.extension.verifier.ResultErrorBuilder;
import org.apache.camel.component.extension.verifier.ResultErrorHelper;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import io.syndesis.connector.support.util.ConnectorOptions;

public class SftpVerifierExtension extends DefaultComponentVerifierExtension {

    protected SftpVerifierExtension(String defaultScheme, CamelContext context) {
        super(defaultScheme, context);
    }

    // *********************************
    // Parameters validation
    //
    @Override
    protected Result verifyParameters(Map<String, Object> parameters) {
        ResultBuilder builder = ResultBuilder.withStatusAndScope(Result.Status.OK, Scope.PARAMETERS)
                .error(ResultErrorHelper.requiresOption("username", parameters))
                .error(ResultErrorHelper.requiresOption("host", parameters))
                .error(ResultErrorHelper.requiresOption("port", parameters))
                .error(ResultErrorHelper.requiresOption("password", parameters));
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

    private void verifyCredentials(ResultBuilder builder, Map<String, Object> parameters) {

        final String host = ConnectorOptions.extractOption(parameters, "host");
        final Integer port = ConnectorOptions.extractOptionAndMap(parameters, "port", Integer::parseInt, 22);
        final String userName = ConnectorOptions.extractOption(parameters, "username");
        final String password = ConnectorOptions.extractOption(parameters, "password", "");

        JSch jsch = new JSch();
        Session session = null;
        try {
            session = jsch.getSession(userName, host, port);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(password);
            session.connect();
        } catch (JSchException e) {
            builder.error(ResultErrorBuilder
                    .withCodeAndDescription(VerificationError.StandardCode.AUTHENTICATION, e.getMessage()).build());
        } finally {
            if (session != null) {
                session.disconnect();
                jsch = null;
            }

        }
    }

}
