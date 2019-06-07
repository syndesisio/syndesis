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
package io.syndesis.connector.box.verifier;

import java.util.Map;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxUser;
import org.apache.camel.CamelContext;
import org.apache.camel.component.box.BoxConfiguration;
import org.apache.camel.component.box.internal.BoxConnectionHelper;
import org.apache.camel.component.extension.verifier.DefaultComponentVerifierExtension;
import org.apache.camel.component.extension.verifier.ResultBuilder;
import org.apache.camel.component.extension.verifier.ResultErrorBuilder;
import org.apache.camel.component.extension.verifier.ResultErrorHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BoxVerifierExtension extends DefaultComponentVerifierExtension {

    private static final Logger LOG = LoggerFactory.getLogger(BoxVerifierExtension.class);

    protected BoxVerifierExtension(String defaultScheme, CamelContext context) {
        super(defaultScheme, context);
    }

    // *********************************
    // Parameters validation
    // *********************************

    @Override
    protected Result verifyParameters(Map<String, Object> parameters) {
        ResultBuilder builder = ResultBuilder.withStatusAndScope(Result.Status.OK, Scope.PARAMETERS)
            .error(ResultErrorHelper.requiresOption("userName", parameters))
            .error(ResultErrorHelper.requiresOption("userPassword", parameters))
            .error(ResultErrorHelper.requiresOption("clientId", parameters))
            .error(ResultErrorHelper.requiresOption("clientSecret", parameters));

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
        try {
            BoxConfiguration configuration = new BoxConfiguration();
            parameters.put("authenticationType", BoxConfiguration.STANDARD_AUTHENTICATION);
            setProperties(configuration, parameters);

            BoxAPIConnection connection = BoxConnectionHelper.createConnection(configuration);
            BoxUser.getCurrentUser(connection);
        } catch (Exception e) {
            LOG.error("Credentials verification faild", e);
            builder.error(ResultErrorBuilder
                .withCodeAndDescription(VerificationError.StandardCode.AUTHENTICATION, e.getMessage())
                .build());
        }
    }

}
