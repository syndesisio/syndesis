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

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import io.syndesis.connector.support.util.ConnectorOptions;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.component.google.sheets.GoogleSheetsClientFactory;
import org.apache.camel.util.ObjectHelper;

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

        return new ConfigurableGoogleSheetsClientFactory(transport.build()) {
            @Override
            protected void configure(Sheets.Builder clientBuilder) {
                clientBuilder.setRootUrl(rootUrl);
            }
        };
    }

    /**
     * Client factory allows to configure http transport and root URL for better testability.
     * TODO: Use the default client factory as soon as we are using latest Camel 3 bits in Syndesis.
     * TODO: Backport https://github.com/apache/camel/commit/47f173822d6730300e28d60117d513c89f78fa2b#diff-7200173bb3f32fad958e41b41828ba81 to 2.23.x.redhat-7-x
     */
    private static class ConfigurableGoogleSheetsClientFactory implements GoogleSheetsClientFactory {
        private final HttpTransport transport;
        private final JacksonFactory jsonFactory;

        ConfigurableGoogleSheetsClientFactory(HttpTransport httpTransport) {
            this(httpTransport, new JacksonFactory());
        }

        ConfigurableGoogleSheetsClientFactory(HttpTransport httpTransport, JacksonFactory jacksonFactory) {
            this.transport = httpTransport;
            this.jsonFactory = jacksonFactory;
        }

        @Override
        public Sheets makeClient(String clientId,
                                 String clientSecret,
                                 String applicationName,
                                 String refreshToken,
                                 String accessToken) {
            if (clientId == null || clientSecret == null) {
                throw new IllegalArgumentException("clientId and clientSecret are required to create Google Sheets client.");
            }

            try {
                Credential credential = authorize(clientId, clientSecret, refreshToken, accessToken);

                Sheets.Builder clientBuilder = new Sheets.Builder(transport, jsonFactory, credential)
                        .setApplicationName(applicationName);
                configure(clientBuilder);
                return clientBuilder.build();
            } catch (Exception e) {
                throw new RuntimeCamelException("Could not create Google Sheets client.", e);
            }
        }

        /**
         * Subclasses may add customized configuration to client builder.
         * @param clientBuilder
         */
        protected void configure(Sheets.Builder clientBuilder) {
            clientBuilder.setRootUrl(Sheets.DEFAULT_ROOT_URL);
        }

        // Authorizes the installed application to access user's protected data.
        private Credential authorize(String clientId, String clientSecret, String refreshToken, String accessToken) {
            // authorize
            Credential credential = new GoogleCredential.Builder()
                    .setJsonFactory(jsonFactory)
                    .setTransport(transport)
                    .setClientSecrets(clientId, clientSecret)
                    .build();

            if (ObjectHelper.isNotEmpty(refreshToken)) {
                credential.setRefreshToken(refreshToken);
            }

            if (ObjectHelper.isNotEmpty(accessToken)) {
                credential.setAccessToken(accessToken);
            }

            return credential;
        }
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
