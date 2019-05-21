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
package io.syndesis.connector.email.verifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeThat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import io.syndesis.connector.email.AbstractEmailServerTest;
import io.syndesis.connector.support.verifier.api.Verifier;
import io.syndesis.connector.support.verifier.api.VerifierResponse;

public class EMailVerifierTest extends AbstractEmailServerTest {

    @Test
    public void testVerifyWithServer() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(PROTOCOL, "imap");
        parameters.put(HOST, imapServer().getHost());
        parameters.put(PORT, imapServer().getPort());
        parameters.put(USER, TEST_ADDRESS);
        parameters.put(PASSWORD, TEST_PASSWORD);

        Verifier verifier = new EMailVerifier();
        List<VerifierResponse> responses = verifier.verify(context, "email", parameters);

        assertThat(responses).hasSize(2);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.CONNECTIVITY);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.PARAMETERS);
        assertThat(responses).allMatch(response -> response.getStatus() == Verifier.Status.OK);
    }

    @Test
    public void testVerifyWithSSLServer() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(PROTOCOL, "imap");
        parameters.put(SECURE_TYPE, SecureType.SSL_TLS.id());
        parameters.put(HOST, imapsServer().getHost());
        parameters.put(PORT, imapsServer().getPort());
        parameters.put(USER, TEST_ADDRESS);
        parameters.put(PASSWORD, TEST_PASSWORD);
        parameters.put(SERVER_CERTIFICATE, imapsServer().getCertificate());

        Verifier verifier = new EMailVerifier();
        List<VerifierResponse> responses = verifier.verify(context, "email", parameters);

        assertThat(responses).hasSize(2);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.CONNECTIVITY);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.PARAMETERS);
        assertThat(responses).allMatch(response -> response.getStatus() == Verifier.Status.OK);
    }

    private static final String TLS_HOSTNAME = NO_HOST;
    private static final int TLS_PORT = 143;
    private static final String TEST_TLS_USER = "<to be changed>";
    private static final String TEST_TLS_PASSWORD = "<to be changed>";

    /**
     * This test must be manually run since it requires a StartTLS enabled imap server.
     * No such test server is available (GreenMail doesn't support StartTLS)
     *
     * Change the credentials in the fields above then execute.
     */
    @Test
    public void testVerifyWithStartTLSServer() throws Exception {
        assumeThat(TLS_HOSTNAME, CoreMatchers.is(CoreMatchers.not(NO_HOST)));

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(PROTOCOL, Protocol.IMAP.id());
        parameters.put(SECURE_TYPE, SecureType.STARTTLS.id());
        parameters.put(HOST, TLS_HOSTNAME);
        parameters.put(PORT, TLS_PORT);
        parameters.put(USER, TEST_TLS_USER);
        parameters.put(PASSWORD, TEST_TLS_PASSWORD);

        Verifier verifier = new EMailVerifier();
        List<VerifierResponse> responses = verifier.verify(context, "email", parameters);

        assertThat(responses).hasSize(2);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.CONNECTIVITY);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.PARAMETERS);
        assertThat(responses).allMatch(response -> response.getStatus() == Verifier.Status.OK);
    }
}
