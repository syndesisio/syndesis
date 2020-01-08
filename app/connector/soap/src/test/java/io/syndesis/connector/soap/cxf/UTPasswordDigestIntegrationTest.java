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

import static io.syndesis.connector.soap.cxf.ComponentProperties.ADDRESS;
import static io.syndesis.connector.soap.cxf.ComponentProperties.ADD_TIMESTAMP;
import static io.syndesis.connector.soap.cxf.ComponentProperties.AUTHENTICATION_TYPE;
import static io.syndesis.connector.soap.cxf.ComponentProperties.PASSWORD;
import static io.syndesis.connector.soap.cxf.ComponentProperties.SOAP_VERSION;
import static io.syndesis.connector.soap.cxf.ComponentProperties.SPECIFICATION;
import static io.syndesis.connector.soap.cxf.ComponentProperties.USERNAME;

public class UTPasswordDigestIntegrationTest extends IntegrationTestBase {

    @Override
    protected String requestEnvelopePattern(String body) {
        return  ("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"  +
                    "<soap:Header>"  +
                        "<wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" soap:mustUnderstand=\"1\">"  +
                            "<wsu:Timestamp wsu:Id=\"[0-9a-zA-Z\\-]+\">"  +
                                "<wsu:Created>[0-9\\-:\\.TZ]+</wsu:Created>"  +
                                "<wsu:Expires>[0-9\\-:\\.TZ]+</wsu:Expires>"  +
                            "</wsu:Timestamp>"  +
                            "<wsse:UsernameToken wsu:Id=\"UsernameToken-[a-z0-9\\-]+\">"  +
                                "<wsse:Username>TestUser</wsse:Username>"  +
                                "<wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest\">[^<]+</wsse:Password>"  +
                                "<wsse:Nonce EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\">[^<]+</wsse:Nonce>"  +
                                "<wsu:Created>[0-9\\-:\\.TZ]+</wsu:Created>"  +
                            "</wsse:UsernameToken>"  +
                        "</wsse:Security>"  +
                    "</soap:Header>"  +
                    "<soap:Body>" +
                        body +
                    "</soap:Body>" +
                "</soap:Envelope>").replaceAll("/", "\\\\/").replaceAll("([^\\\\])\\.", "$1\\\\.");
    }

    @Override
    protected void createConnection() {
        connection = new Connection.Builder()
            .putConfiguredProperty(ADDRESS, "http://localhost:" + wiremock.port())
            .putConfiguredProperty(SPECIFICATION, readSpecification())
            .putConfiguredProperty(SOAP_VERSION, "1.1")
            .putConfiguredProperty(AUTHENTICATION_TYPE, AuthenticationType.WSSE_UT.getValue())
            .putConfiguredProperty(USERNAME, TEST_USER)
            .putConfiguredProperty(PASSWORD, TEST_PASSWORD)
            .putConfiguredProperty(ConfigurationConstants.PASSWORD_TYPE, PasswordType.DIGEST.getValue())
            .putConfiguredProperty(ADD_TIMESTAMP, "true")
            .putConfiguredProperty(ConfigurationConstants.ADD_USERNAMETOKEN_NONCE, "true")
            .putConfiguredProperty(ConfigurationConstants.ADD_USERNAMETOKEN_CREATED, "true")
            .connector(SOAP_CXF_CONNECTOR)
            .build();
    }
}
