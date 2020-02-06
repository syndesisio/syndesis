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
package io.syndesis.connector.sheets;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.sheets.v4.Sheets;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.verifier.ResultBuilder;
import org.apache.camel.component.extension.verifier.ResultErrorBuilder;
import org.apache.camel.component.google.sheets.BatchGoogleSheetsClientFactory;
import org.apache.camel.component.google.sheets.GoogleSheetsClientFactory;
import org.apache.camel.component.google.sheets.GoogleSheetsConfiguration;


/**
 * FIXME Remove this class when Camel dependency gets updated
 **/
public class CustomGoogleSheetsVerifierExtension
    extends org.apache.camel.component.google.sheets.GoogleSheetsVerifierExtension {
    public CustomGoogleSheetsVerifierExtension(String defaultScheme) {
        super(defaultScheme);
    }

    public CustomGoogleSheetsVerifierExtension(String defaultScheme, CamelContext context) {
        super(defaultScheme, context);
    }

    @Override
    protected Result verifyConnectivity(Map<String, Object> parameters) {
        ResultBuilder builder = ResultBuilder.withStatusAndScope(Result.Status.OK, Scope.CONNECTIVITY);

        try {
            GoogleSheetsConfiguration configuration =
                this.setProperties(new GoogleSheetsConfiguration(), parameters);
            GoogleSheetsClientFactory clientFactory = new BatchGoogleSheetsClientFactory();
            Sheets client = clientFactory.makeClient(configuration.getClientId(), configuration.getClientSecret(),
                configuration.getApplicationName(), configuration.getRefreshToken(),
                configuration.getAccessToken());
            client.spreadsheets().get(Optional.ofNullable(parameters.get("spreadsheetId")).map(Object::toString).orElse(UUID.randomUUID().toString())).execute();
        } catch (GoogleJsonResponseException e) {
            Integer code = e.getStatusCode();
            //We may have a "404 spreadsheet no found" which is fine
            //What we don't want is a bad credential error or similar
            if (code != 404) {
                buildError(builder, e);
            }
        } catch (Exception e) {
            buildError(builder, e);
        }

        return builder.build();
    }

    private static void buildError(ResultBuilder builder, Exception var6) {
        ResultErrorBuilder errorBuilder =
            ResultErrorBuilder.withCodeAndDescription(VerificationError.StandardCode.AUTHENTICATION,
                var6.getMessage()).detail("google_sheets_exception_message",
                var6.getMessage()).detail(VerificationError.ExceptionAttribute.EXCEPTION_CLASS,
                var6.getClass().getName()).detail(VerificationError.ExceptionAttribute.EXCEPTION_INSTANCE, var6);
        builder.error(errorBuilder.build());
    }
}
