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
package io.syndesis.connector.odata.verifier;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.filter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import io.syndesis.connector.odata.AbstractODataTest;
import io.syndesis.connector.odata.server.ODataTestServer;
import io.syndesis.connector.support.verifier.api.Verifier;
import io.syndesis.connector.support.verifier.api.VerifierResponse;

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

public class ODataVerifierTest extends AbstractODataTest {

    @Before
    public void setup() throws Exception {
        context = new DefaultCamelContext();
        context.disableJMX();
        context.start();
    }

    @After
    public void tearDown() throws Exception {
        if (context != null) {
            context.stop();
            context = null;
        }
    }

    @Test
    public void testVerifyWithServer() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(SERVICE_URI, defaultTestServer.servicePlainUri());

        Verifier verifier = new ODataVerifier();
        List<VerifierResponse> responses = verifier.verify(context, "odata", parameters);

        assertThat(responses).hasSize(2);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.CONNECTIVITY);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.PARAMETERS);
        assertThat(responses).allMatch(response -> response.getStatus() == Verifier.Status.OK);
    }

    @Test
    public void testVerifyWithServerWithEndSlashesServiceURI() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        StringBuilder builder = new StringBuilder(defaultTestServer.servicePlainUri());
        for (int i = 0; i < 10; ++i) {
            builder.append(FORWARD_SLASH);
        }
        parameters.put(SERVICE_URI, builder.toString());

        Verifier verifier = new ODataVerifier();
        List<VerifierResponse> responses = verifier.verify(context, "odata", parameters);

        assertThat(responses).hasSize(2);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.CONNECTIVITY);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.PARAMETERS);
        assertThat(responses).allMatch(response -> response.getStatus() == Verifier.Status.OK);
    }

    @Test
    public void testVerifyWithServerNoServiceURI() throws Exception {
        Map<String, Object> parameters = new HashMap<>();

        Verifier verifier = new ODataVerifier();
        List<VerifierResponse> responses = verifier.verify(context, "odata", parameters);

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
        parameters.put(SERVICE_URI, authTestServer.servicePlainUri());
        parameters.put(BASIC_USER_NAME, ODataTestServer.USER);

        Verifier verifier = new ODataVerifier();
        List<VerifierResponse> responses = verifier.verify(context, "odata", parameters);

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
        parameters.put(SERVICE_URI, authTestServer.servicePlainUri());
        parameters.put(BASIC_USER_NAME, ODataTestServer.USER);
        parameters.put(BASIC_PASSWORD, ODataTestServer.USER_PASSWORD);

        Verifier verifier = new ODataVerifier();
        List<VerifierResponse> responses = verifier.verify(context, "odata", parameters);

        assertThat(responses).hasSize(2);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.PARAMETERS);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.CONNECTIVITY);
        assertThat(responses).allMatch(response -> response.getStatus() == Verifier.Status.OK);
    }

    @Test
    public void testVerifyWithBasicAuthenticatedServerWrongPassword() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(SERVICE_URI, authTestServer.servicePlainUri());
        parameters.put(BASIC_USER_NAME, ODataTestServer.USER);
        parameters.put(BASIC_PASSWORD, "WrongPassword");

        Verifier verifier = new ODataVerifier();
        List<VerifierResponse> responses = verifier.verify(context, "odata", parameters);

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
        parameters.put(SERVICE_URI, sslTestServer.serviceSSLUri());
        parameters.put(SERVER_CERTIFICATE, ODataTestServer.serverCertificate());

        Verifier verifier = new ODataVerifier();
        List<VerifierResponse> responses = verifier.verify(context, "odata", parameters);

        assertThat(responses).hasSize(2);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.PARAMETERS);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.CONNECTIVITY);
        assertThat(responses).allMatch(response -> response.getStatus() == Verifier.Status.OK);
    }

    @Test
    public void testVerifyWithSSLServerFailsDifferentCertificate() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(SERVICE_URI, sslTestServer.serviceSSLUri());
        parameters.put(SERVER_CERTIFICATE, ODataTestServer.differentCertificate());

        Verifier verifier = new ODataVerifier();
        List<VerifierResponse> responses = verifier.verify(context, "odata", parameters);

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
        parameters.put(SERVICE_URI, sslTestServer.serviceSSLUri());
        parameters.put(SERVER_CERTIFICATE, ODataTestServer.serverCertificate());
        parameters.put(BASIC_USER_NAME, ODataTestServer.USER);
        parameters.put(BASIC_PASSWORD, ODataTestServer.USER_PASSWORD);

        Verifier verifier = new ODataVerifier();
        List<VerifierResponse> responses = verifier.verify(context, "odata", parameters);

        assertThat(responses).hasSize(2);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.PARAMETERS);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.CONNECTIVITY);
        assertThat(responses).allMatch(response -> response.getStatus() == Verifier.Status.OK);
    }
}
