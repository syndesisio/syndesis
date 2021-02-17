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
package io.syndesis.connector.soap.cxf;

import org.apache.wss4j.common.ConfigurationConstants;

import io.syndesis.common.model.connection.Connection;
import io.syndesis.connector.soap.cxf.auth.AuthenticationType;
import io.syndesis.connector.soap.cxf.auth.PasswordType;

import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static io.syndesis.connector.soap.cxf.ComponentProperties.ADDRESS;
import static io.syndesis.connector.soap.cxf.ComponentProperties.ADD_TIMESTAMP;
import static io.syndesis.connector.soap.cxf.ComponentProperties.AUTHENTICATION_TYPE;
import static io.syndesis.connector.soap.cxf.ComponentProperties.PASSWORD;
import static io.syndesis.connector.soap.cxf.ComponentProperties.SOAP_VERSION;
import static io.syndesis.connector.soap.cxf.ComponentProperties.SPECIFICATION;
import static io.syndesis.connector.soap.cxf.ComponentProperties.USERNAME;

public class BasicAuthIntegrationTest extends IntegrationTestBase {

    @Override
    protected String requestEnvelopePattern(String body) {
        return  ("<soap:Envelope xmlns:soap=\"" + getSoapNamespace() + "\">" +
                    "<soap:Body>" +
                        body +
                    "</soap:Body>" +
                "</soap:Envelope>").replaceAll("/", "\\\\/").replaceAll("([^\\\\])\\.", "$1\\\\.");
    }

    protected String getSoapVersion() {
        return "1.1";
    }

    @Override
    protected void createConnection() {
        connection = new Connection.Builder()
            .putConfiguredProperty(ADDRESS, "http://localhost:" + wiremock.port())
            .putConfiguredProperty(SPECIFICATION, readSpecification())
            .putConfiguredProperty(SOAP_VERSION, getSoapVersion())
            .putConfiguredProperty(AUTHENTICATION_TYPE, AuthenticationType.BASIC.getValue())
            .putConfiguredProperty(USERNAME, TEST_USER)
            .putConfiguredProperty(PASSWORD, TEST_PASSWORD)
            .putConfiguredProperty(ConfigurationConstants.PASSWORD_TYPE, PasswordType.DIGEST.getValue())
            .putConfiguredProperty(ADD_TIMESTAMP, "true")
            .putConfiguredProperty(ConfigurationConstants.ADD_USERNAMETOKEN_NONCE, "true")
            .putConfiguredProperty(ConfigurationConstants.ADD_USERNAMETOKEN_CREATED, "true")
            .connector(SOAP_CXF_CONNECTOR)
            .build();
    }

    @Override
    protected void verifyWireMock(WireMockRule wiremock) {
        wiremock.verify(postRequestedFor(urlEqualTo("/"))
            .withBasicAuth(new BasicCredentials(TEST_USER, TEST_PASSWORD)));
    }
}
