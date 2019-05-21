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

import static org.junit.Assume.assumeThat;
import java.util.Map;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.hamcrest.CoreMatchers;
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
import io.syndesis.connector.email.AbstractEmailTest;
import io.syndesis.connector.email.RouteUtils;
import io.syndesis.connector.email.component.EMailComponentFactory;
import io.syndesis.connector.email.customizer.EMailReceiveCustomizer;
import io.syndesis.connector.email.model.EMailMessageModel;
import io.syndesis.connector.support.util.PropertyBuilder;

@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {
        EMailReadWithTLSRouteTest.TestConfiguration.class
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

public class EMailReadWithTLSRouteTest extends AbstractEmailTest implements RouteUtils.ConnectorActionFactory {

    @Override
    public ConnectorAction createConnectorAction(Map<String, String> configuredProperties) {
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

    private static final String HOSTNAME = NO_HOST;
    private static final int PORT_NO = 143;
    private static final String TEST_USER = "<to be changed";
    private static final String TEST_PASSWORD = "<to be changed>";

    /**
     * This test must be manually run since it requires a StartTLS enabled imap server.
     * No such test server is available (GreenMail doesn't support StartTLS)
     *
     * Change the credentials in the fields above then execute.
     */
    @Test
    public void testImapEMailRouteWithStartTLS() throws Exception {
        assumeThat(HOSTNAME, CoreMatchers.is(CoreMatchers.not(NO_HOST)));

        Protocol protocol = Protocol.IMAP;

        Connector mailConnector = RouteUtils.createEMailConnector(protocol.componentSchema(),
                                                       new PropertyBuilder<String>()
                                                               .property(PROTOCOL, protocol.id())
                                                               .property(SECURE_TYPE, SecureType.STARTTLS.id())
                                                               .property(HOST, HOSTNAME)
                                                               .property(PORT, Integer.toString(PORT_NO))
                                                               .property(USER, TEST_USER)
                                                               .property(PASSWORD, TEST_PASSWORD));

        Step mailStep = RouteUtils.createEMailStep(mailConnector, this::createConnectorAction);
        Integration mailIntegration = RouteUtils.createIntegrationWithMock(mailStep);

        RouteBuilder routes = RouteUtils.newIntegrationRouteBuilder(mailIntegration);
        context.addRoutes(routes);

        MockEndpoint result = RouteUtils.initMockEndpoint(context);
        result.setMinimumExpectedMessageCount(1);

        context.start();
        result.assertIsSatisfied();
    }
}
