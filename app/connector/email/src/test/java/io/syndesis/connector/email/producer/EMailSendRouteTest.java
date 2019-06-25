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
package io.syndesis.connector.email.producer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.List;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.direct.DirectEndpoint;
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
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.util.SuppressFBWarnings;
import io.syndesis.connector.email.AbstractEmailServerTest;
import io.syndesis.connector.email.EMailConstants;
import io.syndesis.connector.email.RouteUtils;
import io.syndesis.connector.email.component.EMailComponentFactory;
import io.syndesis.connector.email.customizer.EMailSendCustomizer;
import io.syndesis.connector.email.model.EMailMessageModel;
import io.syndesis.connector.email.server.EMailTestServer;
import io.syndesis.connector.support.util.PropertyBuilder;


@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {
        EMailSendRouteTest.TestConfiguration.class
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
@SuppressFBWarnings
public class EMailSendRouteTest extends AbstractEmailServerTest implements RouteUtils.ConnectorActionFactory {

    private static EMailTestServer server;

    @After
    @Override
    public void tearDown() throws Exception {
        //
        // Clear smtp server
        //
        refresh(server);
    }

    public EMailSendRouteTest() throws Exception {
        super();
    }

    @Override
    public ConnectorAction createConnectorAction(Map<String, String> configuredProperties) {
        ConnectorAction emailAction = new ConnectorAction.Builder()
        .description("Send email to another server")
         .id("io.syndesis:email-send-connector")
         .name("Send Email")
         .descriptor(new ConnectorDescriptor.Builder()
                    .addConnectorCustomizer(EMailSendCustomizer.class.getName())
                    .connectorFactory(EMailComponentFactory.class.getName())
                    .inputDataShape(new DataShape.Builder()
                                     .kind(DataShapeKinds.JAVA)
                                     .type(EMailMessageModel.class.getCanonicalName())
                                     .build())
                    .putAllConfiguredProperties(configuredProperties)
                    .build())
        .build();
        return emailAction;
    }

    private Step createDirectStep() {
        Step directStep = new Step.Builder()
            .stepKind(StepKind.endpoint)
            .action(new ConnectorAction.Builder()
                    .descriptor(new ConnectorDescriptor.Builder()
                                .componentScheme("direct")
                                .putConfiguredProperty("name", "start")
                                .build())
                    .build())
            .build();
        return directStep;
    }

    @Test
    public void testSmtpSenderRoute() throws Exception {
        server = smtpServer();

        Step directStep = createDirectStep();

        Connector mailConnector = RouteUtils.createEMailConnector(server,
                                                       new PropertyBuilder<String>()
                                                               .property(PROTOCOL, Protocol.SMTP.id())
                                                               .property(HOST, server.getHost())
                                                               .property(PORT, Integer.toString(server.getPort()))
                                                               .property(USER, TEST_ADDRESS)
                                                               .property(PASSWORD, TEST_PASSWORD));

        Step mailStep = RouteUtils.createEMailStep(mailConnector, this::createConnectorAction);
        Integration mailIntegration = RouteUtils.createIntegrationWithMock(directStep, mailStep);

        RouteBuilder routes = RouteUtils.newIntegrationRouteBuilder(mailIntegration);
        context.addRoutes(routes);

        MockEndpoint result = RouteUtils.initMockEndpoint(context);
        result.setExpectedMessageCount(1);

        DirectEndpoint directEndpoint = context.getEndpoint("direct://start", DirectEndpoint.class);
        ProducerTemplate template = context.createProducerTemplate();

        context.start();

        EMailMessageModel msgModel = new EMailMessageModel();
        msgModel.setSubject("Test Email 1");
        msgModel.setFrom(TEST_ADDRESS);
        msgModel.setTo(TEST_ADDRESS);
        msgModel.setContent("Hello, I am sending emails to myself again!\r\n");

        template.sendBody(directEndpoint, msgModel);

        result.assertIsSatisfied();

        List<EMailMessageModel> emails = server.getEmails();
        assertEquals(1, emails.size());
        assertEquals(msgModel, emails.get(0));

        Exchange exchange = result.getReceivedExchanges().get(0);
        Map<String, Object> headers = exchange.getIn().getHeaders();
        assertNotNull(headers);
        Object contentType = headers.get(Exchange.CONTENT_TYPE);
        assertNotNull(contentType);
        assertEquals(EMailConstants.TEXT_PLAIN, contentType);
    }

    @Test
    public void testSmtpsSenderRoute() throws Exception {
        server = smtpsServer();

        Step directStep = createDirectStep();

        //
        // Even though protocol is set to 'smtp', it will be changed to 'smtps'
        // by the EMailComponent due to secure being true
        //
        Connector mailConnector = RouteUtils.createEMailConnector(server,
                                                       new PropertyBuilder<String>()
                                                               .property(PROTOCOL, Protocol.SMTP.id())
                                                               .property(SECURE_TYPE, SecureType.SSL_TLS.id())
                                                               .property(HOST, server.getHost())
                                                               .property(PORT, Integer.toString(server.getPort()))
                                                               .property(USER, TEST_ADDRESS)
                                                               .property(PASSWORD, TEST_PASSWORD)
                                                               .property(SERVER_CERTIFICATE, server.getCertificate()));

        Step mailStep = RouteUtils.createEMailStep(mailConnector, this::createConnectorAction);
        Integration mailIntegration = RouteUtils.createIntegrationWithMock(directStep, mailStep);

        RouteBuilder routes = RouteUtils.newIntegrationRouteBuilder(mailIntegration);
        context.addRoutes(routes);

        MockEndpoint result = RouteUtils.initMockEndpoint(context);
        result.setExpectedMessageCount(1);

        DirectEndpoint directEndpoint = context.getEndpoint("direct://start", DirectEndpoint.class);
        ProducerTemplate template = context.createProducerTemplate();

        context.start();

        EMailMessageModel msgModel = new EMailMessageModel();
        msgModel.setSubject("Test Email 1");
        msgModel.setFrom(TEST_ADDRESS);
        msgModel.setTo(TEST_ADDRESS);
        msgModel.setContent("Hello, I am sending emails to myself again!\r\n");

        template.sendBody(directEndpoint, msgModel);

        result.assertIsSatisfied();

        List<EMailMessageModel> emails = server.getEmails();
        assertEquals(1, emails.size());
        assertEquals(msgModel, emails.get(0));
    }

    @Test
    public void testPriorityOfValuesInjectedData() throws Exception {
        server = smtpServer();

        Step directStep = createDirectStep();

        Connector mailConnector = RouteUtils.createEMailConnector(server,
                                                       new PropertyBuilder<String>()
                                                               .property(PROTOCOL, Protocol.SMTP.id())
                                                               .property(HOST, server.getHost())
                                                               .property(PORT, Integer.toString(server.getPort()))
                                                               .property(USER, TEST_ADDRESS)
                                                               .property(PASSWORD, TEST_PASSWORD));

        EMailMessageModel injectedDataModel = new EMailMessageModel();
        injectedDataModel.setSubject("Injected Data Email 1");
        injectedDataModel.setFrom(TEST_ADDRESS);
        injectedDataModel.setTo(TEST_ADDRESS);
        injectedDataModel.setContent("Hello, this is an email from injected data!\r\n");

        String inputValueSubject = "Input Values Email 1";
        String inputValueText = "Hello, this is an email using inputted values!";

        PropertyBuilder<String> builder = new PropertyBuilder<>();
        builder.property(PRIORITY, Priority.CONSUMED_DATA.toString());
        builder.property(MAIL_SUBJECT, inputValueSubject);
        builder.property(MAIL_TEXT, inputValueText);

        Step mailStep = RouteUtils.createEMailStep(mailConnector, this::createConnectorAction, builder.build());
        Integration mailIntegration = RouteUtils.createIntegrationWithMock(directStep, mailStep);

        RouteBuilder routes = RouteUtils.newIntegrationRouteBuilder(mailIntegration);
        context.addRoutes(routes);

        MockEndpoint result = RouteUtils.initMockEndpoint(context);
        result.setExpectedMessageCount(1);

        DirectEndpoint directEndpoint = context.getEndpoint("direct://start", DirectEndpoint.class);
        ProducerTemplate template = context.createProducerTemplate();

        context.start();

        //
        // Sending through injected data model as if consumed from another connection
        //
        template.sendBody(directEndpoint, injectedDataModel);

        result.assertIsSatisfied();

        List<EMailMessageModel> emails = server.getEmails();
        assertEquals(1, emails.size());

        //
        // The email is consistent with the injected data and has overridden
        // the values set by the inputted values
        //
        assertEquals(injectedDataModel, emails.get(0));
    }

    @Test
    public void testPriorityOfValuesInputtedValues() throws Exception {
        server = smtpServer();

        Step directStep = createDirectStep();

        Connector mailConnector = RouteUtils.createEMailConnector(server,
                                                       new PropertyBuilder<String>()
                                                               .property(PROTOCOL, Protocol.SMTP.id())
                                                               .property(HOST, server.getHost())
                                                               .property(PORT, Integer.toString(server.getPort()))
                                                               .property(USER, TEST_ADDRESS)
                                                               .property(PASSWORD, TEST_PASSWORD));

        EMailMessageModel injectedDataModel = new EMailMessageModel();
        injectedDataModel.setSubject("Injected Data Email 1");
        injectedDataModel.setFrom(TEST_ADDRESS);
        injectedDataModel.setTo(TEST_ADDRESS);
        injectedDataModel.setContent("Hello, this is an email from injected data!\r\n");

        String inputValueSubject = "Input Values Email 1";
        String inputValueText = "Hello, this is an email using inputted values!\r\n";

        //
        // Change the integration's priority to prefer input values if present
        //
        PropertyBuilder<String> builder = new PropertyBuilder<>();
        builder.property(PRIORITY, Priority.INPUT_VALUES.toString());
        builder.property(MAIL_SUBJECT, inputValueSubject);
        builder.property(MAIL_TEXT, inputValueText);

        Step mailStep = RouteUtils.createEMailStep(mailConnector, this::createConnectorAction, builder.build());
        Integration mailIntegration = RouteUtils.createIntegrationWithMock(directStep, mailStep);

        RouteBuilder routes = RouteUtils.newIntegrationRouteBuilder(mailIntegration);
        context.addRoutes(routes);

        MockEndpoint result = RouteUtils.initMockEndpoint(context);
        result.setExpectedMessageCount(1);

        DirectEndpoint directEndpoint = context.getEndpoint("direct://start", DirectEndpoint.class);
        ProducerTemplate template = context.createProducerTemplate();

        context.start();

        //
        // Sending through injected data model as if consumed from another connection
        //
        template.sendBody(directEndpoint, injectedDataModel);

        result.assertIsSatisfied();

        List<EMailMessageModel> emails = server.getEmails();
        assertEquals(1, emails.size());

        //
        // The email looks like injectedData but retains
        // the subject & text specified by the input values
        //
        EMailMessageModel expectedModel = new EMailMessageModel();
        expectedModel.setSubject(inputValueSubject);
        expectedModel.setFrom(injectedDataModel.getFrom());
        expectedModel.setTo(injectedDataModel.getTo());
        expectedModel.setContent(inputValueText);

        assertEquals(expectedModel, emails.get(0));
    }

    @Test
    public void testSmtpHtmlContentType() throws Exception {
        server = smtpServer();

        Step directStep = createDirectStep();

        Connector mailConnector = RouteUtils.createEMailConnector(server,
                                                       new PropertyBuilder<String>()
                                                               .property(PROTOCOL, Protocol.SMTP.id())
                                                               .property(HOST, server.getHost())
                                                               .property(PORT, Integer.toString(server.getPort()))
                                                               .property(USER, TEST_ADDRESS)
                                                               .property(PASSWORD, TEST_PASSWORD));

        Step mailStep = RouteUtils.createEMailStep(mailConnector, this::createConnectorAction);
        Integration mailIntegration = RouteUtils.createIntegrationWithMock(directStep, mailStep);

        RouteBuilder routes = RouteUtils.newIntegrationRouteBuilder(mailIntegration);
        context.addRoutes(routes);

        MockEndpoint result = RouteUtils.initMockEndpoint(context);
        result.setExpectedMessageCount(1);

        DirectEndpoint directEndpoint = context.getEndpoint("direct://start", DirectEndpoint.class);
        ProducerTemplate template = context.createProducerTemplate();

        context.start();

        EMailMessageModel msgModel = new EMailMessageModel();
        msgModel.setSubject("Test Email 1");
        msgModel.setFrom(TEST_ADDRESS);
        msgModel.setTo(TEST_ADDRESS);
        msgModel.setContent("<p><b>Hello, I am sending emails to myself again!</b></p>\r\n");

        template.sendBody(directEndpoint, msgModel);

        result.assertIsSatisfied();

        List<EMailMessageModel> emails = server.getEmails();
        assertEquals(1, emails.size());
        assertEquals(msgModel, emails.get(0));

        Exchange exchange = result.getReceivedExchanges().get(0);
        Map<String, Object> headers = exchange.getIn().getHeaders();
        assertNotNull(headers);
        Object contentType = headers.get(Exchange.CONTENT_TYPE);
        assertNotNull(contentType);
        assertEquals(EMailConstants.TEXT_HTML, contentType);
    }
}
