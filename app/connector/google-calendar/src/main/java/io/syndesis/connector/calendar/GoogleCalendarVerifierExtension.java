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
package io.syndesis.connector.calendar;

import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.verifier.DefaultComponentVerifierExtension;
import org.apache.camel.component.extension.verifier.ResultBuilder;
import org.apache.camel.component.extension.verifier.ResultErrorBuilder;
import org.apache.camel.component.extension.verifier.ResultErrorHelper;
import org.apache.camel.component.google.calendar.BatchGoogleCalendarClientFactory;
import org.apache.camel.component.google.calendar.GoogleCalendarClientFactory;
import org.apache.camel.component.google.calendar.stream.GoogleCalendarStreamConfiguration;

import com.google.api.services.calendar.Calendar;

public class GoogleCalendarVerifierExtension extends DefaultComponentVerifierExtension {

    protected GoogleCalendarVerifierExtension(String defaultScheme, CamelContext context) {
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
            GoogleCalendarStreamConfiguration configuration = setProperties(new GoogleCalendarStreamConfiguration(), parameters);
            GoogleCalendarClientFactory clientFactory = new BatchGoogleCalendarClientFactory();
            Calendar client = clientFactory.makeClient(configuration.getClientId(), configuration.getClientSecret(), configuration.getScopes(), configuration.getApplicationName(),
                                                       configuration.getRefreshToken(), configuration.getAccessToken(), null, null, "me");
            client.calendarList().list().execute();
        } catch (Exception e) {
            ResultErrorBuilder errorBuilder = ResultErrorBuilder.withCodeAndDescription(VerificationError.StandardCode.AUTHENTICATION, e.getMessage())
                .detail("google_calendar_exception", e.getMessage()).detail(VerificationError.ExceptionAttribute.EXCEPTION_CLASS, e.getClass().getName())
                .detail(VerificationError.ExceptionAttribute.EXCEPTION_INSTANCE, e);

            builder.error(errorBuilder.build());
        }

        return builder.build();
    }
}
