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
import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import io.syndesis.connector.odata.ODataConstants;
import io.syndesis.connector.odata.server.ODataTestServer;
import io.syndesis.connector.odata.server.ODataTestServer.Options;
import io.syndesis.connector.odata.verifier.ODataVerifierAutoConfiguration;
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

public class ODataVerifierTest implements ODataConstants {

    private CamelContext context;

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
        ODataTestServer server = new ODataTestServer();
        server.start();

        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put(SERVICE_URI, server.serviceUrl());

            Verifier verifier = new ODataVerifierAutoConfiguration().odataVerifier();
            List<VerifierResponse> responses = verifier.verify(context, "odata", parameters);

            assertThat(responses).hasSize(2);
            assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.CONNECTIVITY);
            assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.PARAMETERS);
            assertThat(responses).allMatch(response -> response.getStatus() == Verifier.Status.OK);

        } finally {
            server.stop();
        }
    }

    @Test
    public void testVerifyWithBasicAuthenticatedServer() throws Exception {
        ODataTestServer server = new ODataTestServer(Options.AUTH_USER);
        server.start();

        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put(SERVICE_URI, server.serviceUrl());
            parameters.put(BASIC_USER_NAME, ODataTestServer.USER);
            parameters.put(BASIC_PASSWORD, ODataTestServer.USER_PASSWORD);

            Verifier verifier = new ODataVerifierAutoConfiguration().odataVerifier();
            List<VerifierResponse> responses = verifier.verify(context, "odata", parameters);

            assertThat(responses).hasSize(2);
            assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.PARAMETERS);
            assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.CONNECTIVITY);
            assertThat(responses).allMatch(response -> response.getStatus() == Verifier.Status.OK);

        } finally {
            server.stop();
        }
    }

    @Test
    public void testVerifyWithBasicAuthenticatedServerWrongPassword() throws Exception {
        ODataTestServer server = new ODataTestServer(Options.AUTH_USER);
        server.start();

        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put(SERVICE_URI, server.serviceUrl());
            parameters.put(BASIC_USER_NAME, ODataTestServer.USER);
            parameters.put(BASIC_PASSWORD, "WrongPassword");

            Verifier verifier = new ODataVerifierAutoConfiguration().odataVerifier();
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

        } finally {
            server.stop();
        }
    }

    @Test
    public void testVerifyWithSSLServer() throws Exception {
        ODataTestServer server = new ODataTestServer(Options.SSL);
        server.start();

        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put(SERVICE_URI, server.serviceUrl());
            parameters.put(SKIP_CERT_CHECK, false);
            parameters.put(CLIENT_CERTIFICATE, ODataTestServer.serverCertificate());

            Verifier verifier = new ODataVerifierAutoConfiguration().odataVerifier();
            List<VerifierResponse> responses = verifier.verify(context, "odata", parameters);

            assertThat(responses).hasSize(2);
            assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.PARAMETERS);
            assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.CONNECTIVITY);
            assertThat(responses).allMatch(response -> response.getStatus() == Verifier.Status.OK);

        } finally {
            server.stop();
        }
    }

    /**
     * Can use the different certificate and it will still be valid, ie. ignores certificate checking altogether
     * @throws Exception
     */
    @Test
    public void testVerifyWithSSLServerSkipCertificateCheck() throws Exception {
        ODataTestServer server = new ODataTestServer(Options.SSL);
        server.start();

        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put(SERVICE_URI, server.serviceUrl());
            // Turns off any certificate checking
            parameters.put(SKIP_CERT_CHECK, true);
            parameters.put(CLIENT_CERTIFICATE, ODataTestServer.differentCertificate());

            Verifier verifier = new ODataVerifierAutoConfiguration().odataVerifier();
            List<VerifierResponse> responses = verifier.verify(context, "odata", parameters);

            assertThat(responses).hasSize(2);
            assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.PARAMETERS);
            assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.CONNECTIVITY);
            assertThat(responses).allMatch(response -> response.getStatus() == Verifier.Status.OK);

        } finally {
            server.stop();
        }
    }

    @Test
    public void testVerifyWithSSLServerFailsDifferentCertificate() throws Exception {
        ODataTestServer server = new ODataTestServer(Options.SSL);
        server.start();

        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put(SERVICE_URI, server.serviceUrl());
            parameters.put(SKIP_CERT_CHECK, false);
            parameters.put(CLIENT_CERTIFICATE, ODataTestServer.differentCertificate());

            Verifier verifier = new ODataVerifierAutoConfiguration().odataVerifier();
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

        } finally {
            server.stop();
        }
    }

    @Test
    public void testVerifyWithSSLServerAndBasicAuthentication() throws Exception {
        ODataTestServer server = new ODataTestServer(Options.AUTH_USER, Options.SSL);
        server.start();

        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put(SERVICE_URI, server.serviceUrl());
            parameters.put(SKIP_CERT_CHECK, false);
            parameters.put(CLIENT_CERTIFICATE, ODataTestServer.serverCertificate());
            parameters.put(BASIC_USER_NAME, ODataTestServer.USER);
            parameters.put(BASIC_PASSWORD, ODataTestServer.USER_PASSWORD);

            Verifier verifier = new ODataVerifierAutoConfiguration().odataVerifier();
            List<VerifierResponse> responses = verifier.verify(context, "odata", parameters);

            assertThat(responses).hasSize(2);
            assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.PARAMETERS);
            assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.CONNECTIVITY);
            assertThat(responses).allMatch(response -> response.getStatus() == Verifier.Status.OK);

        } finally {
            server.stop();
        }
    }
}
