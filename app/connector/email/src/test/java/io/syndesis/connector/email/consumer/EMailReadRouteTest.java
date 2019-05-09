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
package io.syndesis.connector.email.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.camel.Endpoint;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mail.MailEndpoint;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.connector.email.AbstractEMailRouteTest;
import io.syndesis.connector.email.component.EMailComponentFactory;
import io.syndesis.connector.email.customizer.EMailReceiveCustomizer;
import io.syndesis.connector.email.model.EMailMessageModel;
import io.syndesis.connector.email.server.EMailTestServer;
import io.syndesis.connector.support.util.PropertyBuilder;

@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {
        EMailReadRouteTest.TestConfiguration.class
    },
    properties = {
        "spring.main.banner-mode = off",
        "logging.level.io.syndesis.integration.runtime = DEBUG"
    }
)
@TestExecutionListeners(
    listeners = {
        DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class
    }
)

public class EMailReadRouteTest extends AbstractEMailRouteTest {

    private EMailTestServer server;

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        refresh(server);
    }

    @Override
    protected ConnectorAction createConnectorAction(Map<String, String> configuredProperties) throws Exception {
        ConnectorAction emailAction = new ConnectorAction.Builder()
            .description("Read email from the server")
             .id("io.syndesis:email-receive-connector")
             .name("Read Email")
             .descriptor(new ConnectorDescriptor.Builder()
                        .addConnectorCustomizer(EMailReceiveCustomizer.class.getName())
                        .connectorFactory(EMailComponentFactory.class.getName())
                        .outputDataShape(new DataShape.Builder()
                                         .kind(DataShapeKinds.JAVA)
                                         .type(EMailMessageModel.class.getCanonicalName())
                                         .build())
                        .putAllConfiguredProperties(configuredProperties)
                        .build())
            .build();
        return emailAction;
    }

    @Test
    public void testImapEMailRoute() throws Exception {
        server = imapServer();
        Connector mailConnector = createEMailConnector(componentScheme(server),
                                                       new PropertyBuilder<String>()
                                                               .property(PROTOCOL, Protocol.IMAP.id())
                                                               .property(HOST, server.getHost())
                                                               .property(PORT, Integer.toString(server.getPort()))
                                                               .property(USER, TEST_USER_NAME)
                                                               .property(PASSWORD, TEST_PASSWORD));

        Step mailStep = createEMailStep(mailConnector);
        Integration mailIntegration = createIntegration(mailStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(mailIntegration);
        context.addRoutes(routes);

        MockEndpoint result = initMockEndpoint();
        result.setMinimumExpectedMessageCount(server.getEmailCount());

        context.start();

        assertSatisfied(result);

        List<EMailMessageModel> emails = server.getEmails();
        for (int i = 0; i < emails.size(); ++i) {
            testResult(result, i, emails.get(i));
        }
    }

    @Test
    public void testImapEMailRouteWrongPassword() throws Exception {
        server = imapServer();
        Connector mailConnector = createEMailConnector(componentScheme(server),
                                                       new PropertyBuilder<String>()
                                                               .property(PROTOCOL, Protocol.IMAP.id())
                                                               .property(SECURE, Boolean.toString(true))
                                                               .property(HOST, server.getHost())
                                                               .property(PORT, Integer.toString(server.getPort()))
                                                               .property(USER, TEST_USER_NAME)
                                                               .property(PASSWORD, "ReallyWrongPassword"));

        Step mailStep = createEMailStep(mailConnector);
        Integration mailIntegration = createIntegration(mailStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(mailIntegration);
        context.addRoutes(routes);

        MockEndpoint result = initMockEndpoint();

        //
        // No messages returned due to wrong password
        //
        result.setAssertPeriod(2000L);
        result.setMinimumExpectedMessageCount(0);

        context.start();
        result.assertIsSatisfied();
    }

    @Test
    public void testPop3EMailRoute() throws Exception {
        server = pop3Server();
        Connector mailConnector = createEMailConnector(componentScheme(server),
                                                       new PropertyBuilder<String>()
                                                               .property(PROTOCOL, Protocol.POP3.id())
                                                               .property(HOST, server.getHost())
                                                               .property(PORT, Integer.toString(server.getPort()))
                                                               .property(USER, TEST_USER_NAME)
                                                               .property(PASSWORD, TEST_PASSWORD));

        Step mailStep = createEMailStep(mailConnector);
        Integration mailIntegration = createIntegration(mailStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(mailIntegration);
        context.addRoutes(routes);

        MockEndpoint result = initMockEndpoint();
        result.setMinimumExpectedMessageCount(server.getEmailCount());

        context.start();

        assertSatisfied(result);

        List<EMailMessageModel> emails = server.getEmails();
        for (int i = 0; i < emails.size(); ++i) {
            testResult(result, i, emails.get(i));
        }
    }

    @Test
    public void testImapsEMailRoute() throws Exception {
        server = imapsServer();
        Connector mailConnector = createEMailConnector(componentScheme(server),
                                                       new PropertyBuilder<String>()
                                                               .property(PROTOCOL, Protocol.IMAP.id())
                                                               .property(SECURE, Boolean.toString(true))
                                                               .property(HOST, server.getHost())
                                                               .property(PORT, Integer.toString(server.getPort()))
                                                               .property(USER, TEST_USER_NAME)
                                                               .property(PASSWORD, TEST_PASSWORD)
                                                               .property(SERVER_CERTIFICATE, server.getCertificate()));

        Step mailStep = createEMailStep(mailConnector);
        Integration mailIntegration = createIntegration(mailStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(mailIntegration);
        context.addRoutes(routes);

        MockEndpoint result = initMockEndpoint();
        result.setMinimumExpectedMessageCount(server.getEmailCount());

        context.start();

        assertSatisfied(result);

        List<EMailMessageModel> emails = server.getEmails();
        for (int i = 0; i < emails.size(); ++i) {
            testResult(result, i, emails.get(i));
        }
    }

    @Test
    public void testPop3sEMailRoute() throws Exception {
        server = pop3sServer();
        Connector mailConnector = createEMailConnector(componentScheme(server),
                                                       new PropertyBuilder<String>()
                                                               .property(PROTOCOL, Protocol.POP3.id())
                                                               .property(SECURE, Boolean.toString(true))
                                                               .property(HOST, server.getHost())
                                                               .property(PORT, Integer.toString(server.getPort()))
                                                               .property(USER, TEST_USER_NAME)
                                                               .property(PASSWORD, TEST_PASSWORD)
                                                               .property(SERVER_CERTIFICATE, server.getCertificate()));

        Step mailStep = createEMailStep(mailConnector);
        Integration mailIntegration = createIntegration(mailStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(mailIntegration);
        context.addRoutes(routes);

        MockEndpoint result = initMockEndpoint();
        result.setMinimumExpectedMessageCount(server.getEmailCount());

        context.start();

        assertSatisfied(result);

        List<EMailMessageModel> emails = server.getEmails();
        for (int i = 0; i < emails.size(); ++i) {
            testResult(result, i, emails.get(i));
        }
    }

    @Test
    public void testImapEMailRouteUnseenMailsOnly() throws Exception {
        server = imapServer();
        int emailCount = server.getEmailCount();
        int unseenCount = 2;

        //
        // Read all emails except unseenCount
        //
        int readCount = emailCount - unseenCount;
        server.readEmails(readCount);

        Connector mailConnector = createEMailConnector(componentScheme(server),
                                                       new PropertyBuilder<String>()
                                                               .property(PROTOCOL, Protocol.IMAP.id())
                                                               .property(HOST, server.getHost())
                                                               .property(PORT, Integer.toString(server.getPort()))
                                                               .property(USER, TEST_USER_NAME)
                                                               .property(PASSWORD, TEST_PASSWORD));

        PropertyBuilder<String> configuredProperties = new PropertyBuilder<>();
        configuredProperties.property(UNSEEN_ONLY, Boolean.toString(true));
        Step mailStep = createEMailStep(mailConnector, configuredProperties.build());
        Integration mailIntegration = createIntegration(mailStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(mailIntegration);
        context.addRoutes(routes);

        MockEndpoint result = initMockEndpoint();
        result.setResultWaitTime(10000L);
        result.setExpectedMessageCount(unseenCount);

        context.start();

        assertSatisfied(result);

        assertEquals(unseenCount, result.getExchanges().size());
        for (int i = readCount; i < emailCount; ++i) {
            testResult(result, (i - readCount), server.getEmails().get(i));
        }
    }

    @Test
    public void testImapEMailRouteMaxEmails() throws Exception {
        int filteredTotal = 2;
        server = imapServer();
        Connector mailConnector = createEMailConnector(componentScheme(server),
                                                       new PropertyBuilder<String>()
                                                               .property(PROTOCOL, Protocol.IMAP.id())
                                                               .property(HOST, server.getHost())
                                                               .property(PORT, Integer.toString(server.getPort()))
                                                               .property(USER, TEST_USER_NAME)
                                                               .property(PASSWORD, TEST_PASSWORD));

        PropertyBuilder<String> configuredProperties = new PropertyBuilder<>();
        configuredProperties.property(MAX_MESSAGES, Integer.toString(filteredTotal));
        Step mailStep = createEMailStep(mailConnector, configuredProperties.build());
        Integration mailIntegration = createIntegration(mailStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(mailIntegration);
        context.addRoutes(routes);

        MockEndpoint result = initMockEndpoint();
        result.setExpectedMessageCount(filteredTotal);

        context.start();

        assertSatisfied(result);

        assertEquals(filteredTotal, result.getExchanges().size());
        for (int i = 0; i < filteredTotal; ++i) {
            testResult(result, i, server.getEmails().get(i));
        }
    }

    @Test
    public void testImapRouteWithConsumerDelayProperties() throws Exception {
        String delayValue = "1000";

        server = imapServer();
        Connector mailConnector = createEMailConnector(componentScheme(server),
                                                       new PropertyBuilder<String>()
                                                               .property(PROTOCOL, Protocol.IMAP.id())
                                                               .property(HOST, server.getHost())
                                                               .property(PORT, Integer.toString(server.getPort()))
                                                               .property(USER, TEST_USER_NAME)
                                                               .property(PASSWORD, TEST_PASSWORD));

        PropertyBuilder<String> configuredProperties = new PropertyBuilder<>();
        configuredProperties.property(DELAY, delayValue);
        Step mailStep = createEMailStep(mailConnector, configuredProperties.build());
        Integration mailIntegration = createIntegration(mailStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(mailIntegration);
        context.addRoutes(routes);

        MockEndpoint result = initMockEndpoint();
        result.setMinimumExpectedMessageCount(server.getEmailCount());

        context.start();

        assertSatisfied(result);

        List<EMailMessageModel> emails = server.getEmails();
        for (int i = 0; i < emails.size(); ++i) {
            testResult(result, i, emails.get(i));
        }

        Collection<Endpoint> endpoints = context.getEndpoints();
        MailEndpoint mailEndpoint = null;
        for (Endpoint endpoint : endpoints) {
            if (endpoint instanceof MailEndpoint) {
                mailEndpoint = (MailEndpoint) endpoint;
            }
        }
        assertNotNull(mailEndpoint);

        Map<String, Object> consumerProperties = mailEndpoint.getConsumerProperties();
        assertNotNull(consumerProperties);
        assertTrue(consumerProperties.size() > 0);
        assertEquals(delayValue, consumerProperties.get(DELAY));
    }

    /**
     * Receive will parse and strip the html from the message
     * and just leave the plain text.
     *
     * @throws Exception
     */
    @Test
    public void testImapMessageHTMLToPlainText() throws Exception {
        server = imapServer();
        server.clear();

        String plainText = "Hi, how are you?";
        String body = "<html><body>Hi, <i>how are you?</i></body></html>";

        server.createMultipartMessage(TEST_USER_NAME, TEST_PASSWORD, "Ben1@test.com",
                                                                       "An HTML Message", TEXT_HTML, body);
        assertEquals(1, server.getEmailCount());

        Connector mailConnector = createEMailConnector(componentScheme(server),
                                                       new PropertyBuilder<String>()
                                                               .property(PROTOCOL, Protocol.IMAP.id())
                                                               .property(HOST, server.getHost())
                                                               .property(PORT, Integer.toString(server.getPort()))
                                                               .property(USER, TEST_USER_NAME)
                                                               .property(PASSWORD, TEST_PASSWORD));

        PropertyBuilder<String> configuredProperties = new PropertyBuilder<>();
        configuredProperties.property(TO_PLAIN_TEXT, Boolean.toString(true));
        Step mailStep = createEMailStep(mailConnector, configuredProperties.build());
        Integration mailIntegration = createIntegration(mailStep, mockStep);

        RouteBuilder routes = newIntegrationRouteBuilder(mailIntegration);
        context.addRoutes(routes);

        MockEndpoint result = initMockEndpoint();
        result.setMinimumExpectedMessageCount(server.getEmailCount());

        context.start();

        assertSatisfied(result);

        EMailMessageModel model = extractModelFromExchgMsg(result, 0);
        assertThat(model.getContent()).isInstanceOf(String.class);
        assertEquals(plainText, model.getContent().toString().trim());
    }
}
