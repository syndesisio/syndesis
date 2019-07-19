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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Map;
import org.apache.camel.component.google.sheets.BatchGoogleSheetsClientFactory;
import org.apache.camel.component.google.sheets.GoogleSheetsClientFactory;
import org.apache.camel.util.ObjectHelper;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.sheets.v4.Sheets;
import io.syndesis.connector.support.util.ConnectorOptions;

/**
 * @author Christoph Deppisch
 */
public final class GoogleSheetsConnectorHelper {

    public static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
    public static final String END_CERT = "-----END CERTIFICATE-----";

    /**
     * Prevent instantiation of utility class.
     */
    private GoogleSheetsConnectorHelper() {
        super();
    }

    /**
     * Create Google sheets client factory with given root URL and server certificate.
     * @param rootUrl
     * @param serverCertificate
     * @param validateCertificates
     * @return
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public static GoogleSheetsClientFactory createClientFactory(String rootUrl, String serverCertificate, boolean validateCertificates) throws GeneralSecurityException, IOException {
        NetHttpTransport.Builder transport = new NetHttpTransport.Builder();

        if (validateCertificates && ObjectHelper.isNotEmpty(serverCertificate)) {
            byte [] decoded = Base64.getDecoder().decode(serverCertificate.replaceAll(BEGIN_CERT, "")
                    .replaceAll(END_CERT, ""));
            transport.trustCertificatesFromStream(new ByteArrayInputStream(decoded));
        } else {
            transport.doNotValidateCertificate();
        }

        return new BatchGoogleSheetsClientFactory(transport.build()) {
            @Override
            protected void configure(Sheets.Builder clientBuilder) {
                clientBuilder.setRootUrl(rootUrl);
            }
        };
    }

    /**
     * Create client from given client factory using properties or default values.
     * @param clientFactory
     * @param properties
     * @return
     */
    public static Sheets makeClient(GoogleSheetsClientFactory clientFactory, Map<String, Object> properties) {
        final String clientId = ConnectorOptions.extractOption(properties, "clientId");
        final String clientSecret = ConnectorOptions.extractOption(properties, "clientSecret");
        final String applicationName = ConnectorOptions.extractOption(properties, "applicationName");

        return clientFactory.makeClient(clientId, clientSecret, applicationName,
                                ConnectorOptions.extractOption(properties, "refreshToken", ""),
                                ConnectorOptions.extractOption(properties, "accessToken", ""));
    }
}
