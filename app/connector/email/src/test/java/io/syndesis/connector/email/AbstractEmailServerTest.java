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
package io.syndesis.connector.email;

import java.util.HashMap;
import java.util.Map;
import org.junit.BeforeClass;
import io.syndesis.connector.email.server.EMailTestServer;
import io.syndesis.connector.email.server.EMailTestServer.Options;

public abstract class AbstractEmailServerTest extends AbstractEmailTest {

    protected static final String TEST_HOST_NAME = "localhost";
    protected static final String TEST_ADDRESS = TEST_USER_NAME + AT + TEST_HOST_NAME;
    protected static final String TEST_FOLDER = "testFolder";

    private static Options[] serverTypes = {
            Options.IMAP,
            Options.IMAPS,
            Options.POP3,
            Options.POP3S,
            Options.SMTP,
            Options.SMTPS
            };

    private static Map<Options, EMailTestServer> mailServers = new HashMap<>();

    @BeforeClass
    public static void scaffold() throws Exception {
        for (Options option : serverTypes) {
            if (! mailServers.containsKey(option)) {
                EMailTestServer server = new EMailTestServer(TEST_HOST_NAME, option);
                server.createUser(TEST_ADDRESS, TEST_PASSWORD);
                server.start();
                refresh(server);
                mailServers.put(option, server);
            }
        }
    }

    protected static void refresh(EMailTestServer server) throws Exception {
        if (server == null) {
            return;
        }

        server.clear();
        if (! server.isSmtp()) {
            server.generateMail(TEST_ADDRESS, TEST_PASSWORD);
        }
    }

    protected static EMailTestServer server(Options option) {
        return mailServers.get(option);
    }

    protected static EMailTestServer imapServer() {
        return server(Options.IMAP);
    }

    protected static EMailTestServer imapsServer() {
        return server(Options.IMAPS);
    }

    protected static EMailTestServer pop3Server() {
        return server(Options.POP3);
    }

    protected static EMailTestServer pop3sServer() {
        return server(Options.POP3S);
    }

    protected static EMailTestServer smtpServer() {
        return server(Options.SMTP);
    }

    protected static EMailTestServer smtpsServer() {
        return server(Options.SMTPS);
    }

}
