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
package io.syndesis.connector.gmail;

import java.util.Map;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.verifier.DefaultComponentVerifierExtension;
import org.apache.camel.component.extension.verifier.ResultBuilder;
import org.apache.camel.component.extension.verifier.ResultErrorBuilder;
import org.apache.camel.component.extension.verifier.ResultErrorHelper;
import org.apache.camel.component.google.mail.BatchGoogleMailClientFactory;
import org.apache.camel.component.google.mail.GoogleMailClientFactory;
import org.apache.camel.component.google.mail.GoogleMailConfiguration;
import org.apache.camel.util.ObjectHelper;
import com.google.api.services.gmail.Gmail;
import io.syndesis.connector.support.util.ConnectorOptions;

public class GmailVerifierExtension extends DefaultComponentVerifierExtension {

    protected GmailVerifierExtension(String defaultScheme, CamelContext context) {
        super(defaultScheme, context);
    }


    // *********************************
    // Parameters validation
    // *********************************

    @Override
    protected Result verifyParameters(Map<String, Object> parameters) {

        ResultBuilder builder = ResultBuilder.withStatusAndScope(Result.Status.OK, Scope.PARAMETERS).error(ResultErrorHelper.requiresOption("applicationName", parameters))
            .error(ResultErrorHelper.requiresOption("clientId", parameters)).error(ResultErrorHelper.requiresOption("clientSecret", parameters));

        return builder.build();
    }

    // *********************************
    // Connectivity validation
    // *********************************

    @Override
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    protected Result verifyConnectivity(Map<String, Object> parameters) {
        ResultBuilder builder = ResultBuilder.withStatusAndScope(Result.Status.OK, Scope.CONNECTIVITY);

        try {
            String profile = ConnectorOptions.extractOption(parameters, "userId");
            if (ObjectHelper.isEmpty(profile)) {
                throw new IllegalStateException("The profile parameter has not been not defined");
            }
            GoogleMailConfiguration configuration = setProperties(new GoogleMailConfiguration(), parameters);
            GoogleMailClientFactory clientFactory = new BatchGoogleMailClientFactory();
            Gmail client = clientFactory.makeClient(configuration.getClientId(), configuration.getClientSecret(),
                                                    configuration.getScopes(), configuration.getApplicationName(),
                                                    configuration.getRefreshToken(), configuration.getAccessToken());
            client.users().getProfile(profile).execute();
        } catch (Exception e) {
            ResultErrorBuilder errorBuilder = ResultErrorBuilder.withCodeAndDescription(VerificationError.StandardCode.AUTHENTICATION, e.getMessage())
                .detail("gmail_exception_message", e.getMessage()).detail(VerificationError.ExceptionAttribute.EXCEPTION_CLASS, e.getClass().getName())
                .detail(VerificationError.ExceptionAttribute.EXCEPTION_INSTANCE, e);

            builder.error(errorBuilder.build());
        }

        return builder.build();
    }
}
