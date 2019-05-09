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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import io.syndesis.connector.email.AbstractEMailTest;
import io.syndesis.connector.support.verifier.api.Verifier;
import io.syndesis.connector.support.verifier.api.VerifierResponse;

public class EMailVerifierTest extends AbstractEMailTest {

    @Test
    public void testVerifyWithServer() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(PROTOCOL, "imap");
        parameters.put(HOST, imapServer().getHost());
        parameters.put(PORT, imapServer().getPort());
        parameters.put(USER, TEST_USER_NAME);
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
        parameters.put(SECURE, Boolean.toString(true));
        parameters.put(HOST, imapsServer().getHost());
        parameters.put(PORT, imapsServer().getPort());
        parameters.put(USER, TEST_USER_NAME);
        parameters.put(PASSWORD, TEST_PASSWORD);
        parameters.put(SERVER_CERTIFICATE, imapsServer().getCertificate());

        Verifier verifier = new EMailVerifier();
        List<VerifierResponse> responses = verifier.verify(context, "email", parameters);

        assertThat(responses).hasSize(2);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.CONNECTIVITY);
        assertThat(responses).anyMatch(response -> response.getScope() == Verifier.Scope.PARAMETERS);
        assertThat(responses).allMatch(response -> response.getStatus() == Verifier.Status.OK);
    }
}
