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
import static org.assertj.core.api.Assertions.entry;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeThat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import io.syndesis.connector.email.AbstractEmailServerTest;
import io.syndesis.connector.email.verifier.receive.ReceiveEMailVerifier;
import io.syndesis.connector.email.verifier.send.SendEMailVerifier;
import io.syndesis.connector.support.verifier.api.Verifier;
import io.syndesis.connector.support.verifier.api.VerifierResponse;

public class EMailVerifierTest extends AbstractEmailServerTest {

    @Test
    public void testVerifyWithImapServer() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(PROTOCOL, "imap");
        parameters.put(HOST, imapServer().getHost());
        parameters.put(PORT, imapServer().getPort());
        parameters.put(USER, TEST_ADDRESS);
        parameters.put(PASSWORD, TEST_PASSWORD);

        Verifier verifier = new ReceiveEMailVerifier();
        List<VerifierResponse> responses = verifier.verify(context, "email", parameters);

        assertThat(responses).hasSize(2);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.CONNECTIVITY);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.PARAMETERS);
        assertThat(responses).allMatch(response -> response.getStatus() == Verifier.Status.OK);
    }

    @Test
    public void testVerifyWithImapsServer() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(PROTOCOL, "imap");
        parameters.put(SECURE_TYPE, SecureType.SSL_TLS.id());
        parameters.put(HOST, imapsServer().getHost());
        parameters.put(PORT, imapsServer().getPort());
        parameters.put(USER, TEST_ADDRESS);
        parameters.put(PASSWORD, TEST_PASSWORD);
        parameters.put(SERVER_CERTIFICATE, imapsServer().getCertificate());

        Verifier verifier = new ReceiveEMailVerifier();
        List<VerifierResponse> responses = verifier.verify(context, "email", parameters);

        assertThat(responses).hasSize(2);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.CONNECTIVITY);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.PARAMETERS);
        assertThat(responses).allMatch(response -> response.getStatus() == Verifier.Status.OK);
    }

    @Test
    public void testFailureTimeoutForVerifyWithImapsServer() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(PROTOCOL, "imap");
        // No secure type set
        parameters.put(HOST, imapsServer().getHost());
        parameters.put(PORT, imapsServer().getPort());
        parameters.put(USER, TEST_ADDRESS);
        parameters.put(PASSWORD, TEST_PASSWORD);

        /*
         * With no secure type, the connection to the mail store will fail & timeout
         * However, we want it to fail fast & return fast.
         *
         * Add a timeout property to cut the default timeout (10secs) to a more
         * test-friendly time of 2secs.
         */
        long timeout = 2000L;
        parameters.put(CONNECTION_TIMEOUT, Long.toString(timeout));
        Verifier verifier = new ReceiveEMailVerifier();

        /*
         * Time to operation to ensure the timeout is working correctly.
         */
        long startTime = System.currentTimeMillis();
        List<VerifierResponse> responses = verifier.verify(context, "email", parameters);
        long stopTime = System.currentTimeMillis();

        // Time should be roughly equal to the timeout set
        assertTrue((stopTime - startTime) <= (timeout + 1000L)); // extra second for processing time

        assertThat(responses).hasSize(2);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.CONNECTIVITY);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.PARAMETERS);

        assertThat(responses.get(0).getStatus()).isEqualTo(Verifier.Status.OK);
        assertThat(responses.get(1).getStatus()).isEqualTo(Verifier.Status.ERROR);
    }

    @Test
    public void testFailureTimeoutForVerifyWithPop3sServer() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(PROTOCOL, "pop3");
        // No secure type set
        parameters.put(HOST, pop3sServer().getHost());
        parameters.put(PORT, pop3sServer().getPort());
        parameters.put(USER, TEST_ADDRESS);
        parameters.put(PASSWORD, TEST_PASSWORD);

        /*
         * With no secure type, the connection to the mail store will fail & timeout
         * However, we want it to fail fast & return fast.
         *
         * Add a timeout property to cut the default timeout (10secs) to a more
         * test-friendly time of 2secs.
         */
        long timeout = 2000L;
        parameters.put(CONNECTION_TIMEOUT, Long.toString(timeout));
        Verifier verifier = new ReceiveEMailVerifier();

        /*
         * Time to operation to ensure the timeout is working correctly.
         */
        long startTime = System.currentTimeMillis();
        List<VerifierResponse> responses = verifier.verify(context, "email", parameters);
        long stopTime = System.currentTimeMillis();

        // Time should be roughly equal to the timeout set
        assertTrue((stopTime - startTime) <= (timeout + 5000L)); // pop3 connect is quite slow so few extra seconds for processing time

        assertThat(responses).hasSize(2);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.CONNECTIVITY);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.PARAMETERS);

        assertThat(responses.get(0).getStatus()).isEqualTo(Verifier.Status.OK);
        assertThat(responses.get(1).getStatus()).isEqualTo(Verifier.Status.ERROR);
    }

    @Test
    public void testFailureTimeoutForVerifyWithSmtpsServer() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(PROTOCOL, "smtp");
        // No secure type set
        parameters.put(HOST, smtpsServer().getHost());
        parameters.put(PORT, smtpsServer().getPort());
        parameters.put(USER, TEST_ADDRESS);
        parameters.put(PASSWORD, TEST_PASSWORD);

        /*
         * With no secure type, the connection to the mail store will fail & timeout
         * However, we want it to fail fast & return fast.
         *
         * Add a timeout property to cut the default timeout (10secs) to a more
         * test-friendly time of 2secs.
         */
        long timeout = 2000L;
        parameters.put(CONNECTION_TIMEOUT, Long.toString(timeout));
        Verifier verifier = new SendEMailVerifier();

        /*
         * Time to operation to ensure the timeout is working correctly.
         */
        long startTime = System.currentTimeMillis();
        List<VerifierResponse> responses = verifier.verify(context, "email", parameters);
        long stopTime = System.currentTimeMillis();

        // Time should be roughly equal to the timeout set
        assertTrue((stopTime - startTime) <= (timeout + 3000L)); // pop3 connect is quite slow so few extra seconds for processing time

        assertThat(responses).hasSize(2);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.CONNECTIVITY);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.PARAMETERS);

        assertThat(responses.get(0).getStatus()).isEqualTo(Verifier.Status.OK);
        assertThat(responses.get(1).getStatus()).isEqualTo(Verifier.Status.ERROR);
    }

    @Test
    public void testVerifyWithImapServerWithNoUserNameOrPassword() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(PROTOCOL, "imap");
        parameters.put(HOST, imapServer().getHost());
        parameters.put(PORT, imapServer().getPort());

        Verifier verifier = new ReceiveEMailVerifier();
        List<VerifierResponse> responses = verifier.verify(context, "email", parameters);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0)).hasFieldOrPropertyWithValue("scope", Verifier.Scope.PARAMETERS);
        assertThat(responses.get(0)).hasFieldOrPropertyWithValue("status", Verifier.Status.ERROR);

        assertThat(responses.get(0).getErrors()).hasSize(2);
        assertThat(responses.get(0).getErrors()).allMatch(error -> error.getCode().equals("MISSING_PARAMETER"));
    }

    @Test
    public void testVerifyWithImapServerMadeUpProtocol() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(PROTOCOL, "MyOwnMadeUpProtocol");
        parameters.put(HOST, imapServer().getHost());
        parameters.put(PORT, imapServer().getPort());
        parameters.put(USER, TEST_ADDRESS);
        parameters.put(PASSWORD, TEST_PASSWORD);

        Verifier verifier = new ReceiveEMailVerifier();
        List<VerifierResponse> responses = verifier.verify(context, "email", parameters);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0)).hasFieldOrPropertyWithValue("scope", Verifier.Scope.PARAMETERS);

        assertThat(responses.get(0).getErrors()).hasSize(1);
        assertThat(responses.get(0).getErrors().get(0)).hasFieldOrPropertyWithValue("code", "ILLEGAL_PARAMETER_VALUE");
    }

    @Test
    public void testVerifyWithImapServerProducerProtocol() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(PROTOCOL, "smtp");
        parameters.put(HOST, imapServer().getHost());
        parameters.put(PORT, imapServer().getPort());
        parameters.put(USER, TEST_ADDRESS);
        parameters.put(PASSWORD, TEST_PASSWORD);

        Verifier verifier = new ReceiveEMailVerifier();
        List<VerifierResponse> responses = verifier.verify(context, "email", parameters);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0)).hasFieldOrPropertyWithValue("scope", Verifier.Scope.PARAMETERS);

        assertThat(responses.get(0).getErrors()).hasSize(1);
        assertThat(responses.get(0).getErrors().get(0)).hasFieldOrPropertyWithValue("code", "ILLEGAL_PARAMETER_VALUE");
    }

    @Test
    public void testVerifyWithSmtpServer() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(HOST, smtpServer().getHost());
        parameters.put(PORT, smtpServer().getPort());
        parameters.put(USER, TEST_ADDRESS);
        parameters.put(PASSWORD, TEST_PASSWORD);

        Verifier verifier = new SendEMailVerifier();
        List<VerifierResponse> responses = verifier.verify(context, "email", parameters);

        assertThat(parameters).contains(entry(PROTOCOL, "smtp"));

        assertThat(responses).hasSize(2);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.CONNECTIVITY);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.PARAMETERS);
        assertThat(responses).allMatch(response -> response.getStatus() == Verifier.Status.OK);
    }

    @Test
    public void testVerifyWithSmtpsServer() throws Exception {
        Map<String, Object> parameters = new HashMap<>();;
        parameters.put(SECURE_TYPE, SecureType.SSL_TLS.id());
        parameters.put(HOST, smtpsServer().getHost());
        parameters.put(PORT, smtpsServer().getPort());
        parameters.put(USER, TEST_ADDRESS);
        parameters.put(PASSWORD, TEST_PASSWORD);
        parameters.put(SERVER_CERTIFICATE, imapsServer().getCertificate());

        Verifier verifier = new SendEMailVerifier();
        List<VerifierResponse> responses = verifier.verify(context, "email", parameters);

        assertThat(parameters).contains(entry(PROTOCOL, "smtps"));

        assertThat(responses).hasSize(2);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.CONNECTIVITY);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.PARAMETERS);
        assertThat(responses).allMatch(response -> response.getStatus() == Verifier.Status.OK);
    }

    @Test
    public void testVerifyWithSmtpServerWithNoUserNameOrPassword() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(HOST, imapServer().getHost());
        parameters.put(PORT, imapServer().getPort());

        Verifier verifier = new SendEMailVerifier();
        List<VerifierResponse> responses = verifier.verify(context, "email", parameters);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0)).hasFieldOrPropertyWithValue("scope", Verifier.Scope.PARAMETERS);
        assertThat(responses.get(0)).hasFieldOrPropertyWithValue("status", Verifier.Status.ERROR);

        assertThat(responses.get(0).getErrors()).hasSize(2);
        assertThat(responses.get(0).getErrors()).allMatch(error -> error.getCode().equals("MISSING_PARAMETER"));
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

        Verifier verifier = new ReceiveEMailVerifier();
        List<VerifierResponse> responses = verifier.verify(context, "email", parameters);

        assertThat(responses).hasSize(2);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.CONNECTIVITY);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.PARAMETERS);
        assertThat(responses).allMatch(response -> response.getStatus() == Verifier.Status.OK);
    }
}
