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

import io.syndesis.integration.component.proxy.ComponentDefinition;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import org.apache.camel.Component;
import org.apache.camel.component.google.sheets.GoogleSheetsClientFactory;
import org.apache.camel.component.google.sheets.GoogleSheetsComponent;
import org.apache.camel.component.google.sheets.stream.GoogleSheetsStreamComponent;

/**
 * @author Christoph Deppisch
 */
public class GoogleSheetsConnector extends ComponentProxyComponent {

    private String rootUrl;
    private String serverCertificate;
    private boolean validateCertificates = true;

    public GoogleSheetsConnector(String componentId, String componentScheme) {
        super(componentId, componentScheme);
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    @Override
    protected Optional<Component> createDelegateComponent(ComponentDefinition definition, Map<String, Object> options) throws Exception {
        final GoogleSheetsClientFactory clientFactory = GoogleSheetsConnectorHelper.createClientFactory(rootUrl, serverCertificate, validateCertificates);

        switch (getComponentScheme()) {
            case "google-sheets-stream": {
                GoogleSheetsStreamComponent component = new GoogleSheetsStreamComponent();
                component.setClientFactory(clientFactory);
                return Optional.of(component);
            }
            case "google-sheets": {
                GoogleSheetsComponent component = new GoogleSheetsComponent();
                component.setClientFactory(clientFactory);
                return Optional.of(component);
            }
            default:
                throw new IllegalArgumentException("Invalid component scheme for google sheets connector: " + getComponentScheme());
        }
    }

    public String getRootUrl() {
        return rootUrl;
    }

    /**
     * Specifies the rootUrl.
     *
     * @param rootUrl
     */
    public void setRootUrl(String rootUrl) {
        this.rootUrl = rootUrl;
    }

    public String getServerCertificate() {
        return serverCertificate;
    }

    /**
     * Specifies the serverCertificate.
     *
     * @param serverCertificate
     */
    public void setServerCertificate(String serverCertificate) {
        this.serverCertificate = serverCertificate;
    }

    public boolean isValidateCertificates() {
        return validateCertificates;
    }

    /**
     * Specifies the validateCertificates.
     *
     * @param validateCertificates
     */
    public void setValidateCertificates(boolean validateCertificates) {
        this.validateCertificates = validateCertificates;
    }
}
