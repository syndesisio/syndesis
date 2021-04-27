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
package io.syndesis.connector.odata2.verifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.syndesis.connector.odata2.AbstractODataTest;
import io.syndesis.connector.odata2.server.Certificates;
import io.syndesis.connector.odata2.server.ODataTestServer;
import io.syndesis.connector.support.verifier.api.Verifier;
import io.syndesis.connector.support.verifier.api.VerifierResponse;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.filter;

public class ODataVerifierTest extends AbstractODataTest {

    @BeforeEach
    public void setup() throws Exception {
        context = new DefaultCamelContext();
        context.disableJMX();
        context.start();
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (context != null) {
            context.stop();
            context = null;
        }
    }

    @Test
    public void testVerifyWithServer() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(SERVICE_URI, odataTestServer.getServiceUri());

        Verifier verifier = new ODataVerifier();
        List<VerifierResponse> responses = verifier.verify(context, "odata-v2", parameters);

        assertThat(responses).hasSize(2);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.CONNECTIVITY);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.PARAMETERS);
        assertThat(responses).allMatch(response -> response.getStatus() == Verifier.Status.OK);
    }

    @Test
    public void testVerifyWithServerWithEndSlashesServiceURI() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        StringBuilder builder = new StringBuilder(odataTestServer.getServiceUri());
        for (int i = 0; i < 10; ++i) {
            builder.append(FORWARD_SLASH);
        }
        parameters.put(SERVICE_URI, builder.toString());

        Verifier verifier = new ODataVerifier();
        List<VerifierResponse> responses = verifier.verify(context, "odata-v2", parameters);

        assertThat(responses).hasSize(2);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.CONNECTIVITY);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.PARAMETERS);
        assertThat(responses).allMatch(response -> response.getStatus() == Verifier.Status.OK);
    }

    @Test
    public void testVerifyWithServerNoServiceURI() throws Exception {
        Map<String, Object> parameters = new HashMap<>();

        Verifier verifier = new ODataVerifier();
        List<VerifierResponse> responses = verifier.verify(context, "odata-v2", parameters);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0)).hasFieldOrPropertyWithValue("scope", Verifier.Scope.PARAMETERS);
        assertThat(responses.get(0)).hasFieldOrPropertyWithValue("status", Verifier.Status.ERROR);

        assertThat(responses.get(0).getErrors()).hasSize(1);
        assertThat(responses.get(0).getErrors()).allMatch(error -> error.getCode().equals("MISSING_PARAMETER"));
        assertThat(responses.get(0).getErrors()).allMatch(error -> error.getDescription().equals("serviceUri should be set"));
    }

    @Test
    public void testVerifyWithBasicAuthenticatedServerNoPassword() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(SERVICE_URI, authTestServer.getServiceUri());
        parameters.put(BASIC_USER_NAME, ODataTestServer.USER);

        Verifier verifier = new ODataVerifier();
        List<VerifierResponse> responses = verifier.verify(context, "odata-v2", parameters);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0)).hasFieldOrPropertyWithValue("scope", Verifier.Scope.PARAMETERS);
        assertThat(responses.get(0)).hasFieldOrPropertyWithValue("status", Verifier.Status.ERROR);

        assertThat(responses.get(0).getErrors()).hasSize(1);
        assertThat(responses.get(0).getErrors()).allMatch(error -> error.getCode().equals("MISSING_PARAMETER"));
        assertThat(responses.get(0).getErrors()).allMatch(error -> error.getDescription().equals("Basic authentication requires both a user name and password"));
    }

    @Test
    public void testVerifyWithBasicAuthenticatedServer() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(SERVICE_URI, authTestServer.getServiceUri());
        parameters.put(BASIC_USER_NAME, ODataTestServer.USER);
        parameters.put(BASIC_PASSWORD, ODataTestServer.USER_PASSWORD);

        Verifier verifier = new ODataVerifier();
        List<VerifierResponse> responses = verifier.verify(context, "odata-v2", parameters);

        assertThat(responses).hasSize(2);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.PARAMETERS);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.CONNECTIVITY);
        assertThat(responses).allMatch(response -> response.getStatus() == Verifier.Status.OK);
    }

    @Test
    public void testVerifyWithBasicAuthenticatedServerWrongPassword() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(SERVICE_URI, authTestServer.getServiceUri());
        parameters.put(BASIC_USER_NAME, ODataTestServer.USER);
        parameters.put(BASIC_PASSWORD, "WrongPassword");

        Verifier verifier = new ODataVerifier();
        List<VerifierResponse> responses = verifier.verify(context, "odata-v2", parameters);

        assertThat(responses).hasSize(2);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.PARAMETERS);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.CONNECTIVITY);
        assertThat(
                   filter(responses).with("scope").equalsTo(Verifier.Scope.PARAMETERS).get())
                        .allMatch(response -> response.getStatus() == Verifier.Status.OK);

        assertThat(
                   filter(responses).with("scope").equalsTo(Verifier.Scope.CONNECTIVITY).get())
                        .allMatch(response -> response.getStatus() == Verifier.Status.ERROR);
    }

    /**
     * Needs to supply server certificate since the server is unknown to the default
     * certificate authorities that is loaded into the keystore by default
     */
    @Test
    public void testVerifyWithSSLServer() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(SERVICE_URI, sslTestServer.getSecuredServiceUri());
        parameters.put(SERVER_CERTIFICATE, Certificates.TEST_SERVICE.get());

        Verifier verifier = new ODataVerifier();
        List<VerifierResponse> responses = verifier.verify(context, "odata-v2", parameters);

        assertThat(responses).hasSize(2);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.PARAMETERS);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.CONNECTIVITY);
        assertThat(responses).allMatch(response -> response.getStatus() == Verifier.Status.OK);
    }

    @Test
    public void testVerifyWithSSLServerFailsInvalidCertificate() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(SERVICE_URI, sslTestServer.getSecuredServiceUri());
        parameters.put(SERVER_CERTIFICATE, Certificates.INVALID.get());

        Verifier verifier = new ODataVerifier();
        List<VerifierResponse> responses = verifier.verify(context, "odata-v2", parameters);

        assertThat(responses).hasSize(2);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.PARAMETERS);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.CONNECTIVITY);

        assertThat(
                   filter(responses).with("scope").equalsTo(Verifier.Scope.PARAMETERS).get())
                        .allMatch(response -> response.getStatus() == Verifier.Status.OK);

        assertThat(
                   filter(responses).with("scope").equalsTo(Verifier.Scope.CONNECTIVITY).get())
                        .allMatch(response -> response.getStatus() == Verifier.Status.ERROR);
    }

    /**
     * Needs to supply server certificate since the server is unknown to the default
     * certificate authorities that is loaded into the keystore by default
     */
    @Test
    public void testVerifyWithSSLServerAndBasicAuthentication() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(SERVICE_URI, sslAuthTestServer.getSecuredServiceUri());
        parameters.put(SERVER_CERTIFICATE, Certificates.TEST_SERVICE.get());
        parameters.put(BASIC_USER_NAME, ODataTestServer.USER);
        parameters.put(BASIC_PASSWORD, ODataTestServer.USER_PASSWORD);

        Verifier verifier = new ODataVerifier();
        List<VerifierResponse> responses = verifier.verify(context, "odata-v2", parameters);

        assertThat(responses).hasSize(2);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.PARAMETERS);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.CONNECTIVITY);
        assertThat(responses).allMatch(response -> response.getStatus() == Verifier.Status.OK);
    }

    @Test
    public void testVerifyWithSSLServerAndBasicAuthenticationWrongPassword() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(SERVICE_URI, sslAuthTestServer.getSecuredServiceUri());
        parameters.put(SERVER_CERTIFICATE, Certificates.TEST_SERVICE.get());
        parameters.put(BASIC_USER_NAME, ODataTestServer.USER);
        parameters.put(BASIC_PASSWORD, "WrongPassword");

        Verifier verifier = new ODataVerifier();
        List<VerifierResponse> responses = verifier.verify(context, "odata-v2", parameters);

        assertThat(responses).hasSize(2);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.PARAMETERS);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.CONNECTIVITY);
        assertThat(
            filter(responses).with("scope").equalsTo(Verifier.Scope.PARAMETERS).get())
            .allMatch(response -> response.getStatus() == Verifier.Status.OK);

        assertThat(
            filter(responses).with("scope").equalsTo(Verifier.Scope.CONNECTIVITY).get())
            .allMatch(response -> response.getStatus() == Verifier.Status.ERROR);
    }
}
