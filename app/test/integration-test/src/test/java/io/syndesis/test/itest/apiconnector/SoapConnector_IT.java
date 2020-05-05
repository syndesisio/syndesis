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

package io.syndesis.test.itest.apiconnector;

import java.util.Arrays;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.sql.DataSource;

import org.apache.wss4j.common.WSS4JConstants;
import org.apache.wss4j.common.ext.WSPasswordCallback;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.SocketUtils;
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;

import io.syndesis.test.SyndesisTestEnvironment;
import io.syndesis.test.container.integration.SyndesisIntegrationRuntimeContainer;
import io.syndesis.test.itest.SyndesisIntegrationTestSupport;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.endpoint.CitrusEndpoints;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.dsl.runner.TestRunnerBeforeTestSupport;
import com.consol.citrus.ws.interceptor.LoggingEndpointInterceptor;
import com.consol.citrus.ws.server.WebServiceServer;

import static org.hamcrest.CoreMatchers.is;

/**
 * @author Dhiraj Bokde
 */
@ContextConfiguration(classes = SoapConnector_IT.EndpointConfig.class)
public class SoapConnector_IT extends SyndesisIntegrationTestSupport {

    private static final int SOAP_SERVER_PORT = SocketUtils.findAvailableTcpPort();
    static {
        Testcontainers.exposeHostPorts(SOAP_SERVER_PORT);
    }

    @Autowired
    private WebServiceServer soapServer;

    @Autowired
    private DataSource sampleDb;

    private static final String REQUEST_PAYLOAD =
        "<ns1:sayHi xmlns:ns1=\"http://camel.apache.org/cxf/wsrm\">" +
            "<arg0 xmlns=\"http://camel.apache.org/cxf/wsrm\">Hello</arg0>" +
            "</ns1:sayHi>";
    private static final String RESPONSE_PAYLOAD =
        "<ns1:sayHiResponse xmlns:ns1=\"http://camel.apache.org/cxf/wsrm\">" +
            "   <return xmlns=\"http://camel.apache.org/cxf/wsrm\">Hello Hello!</return>" +
            "</ns1:sayHiResponse>";

    /**
     * Integration uses api connector to send SOAP client requests to a REST endpoint. The client API connector was generated
     * from SOAP WSDL1.1 specification.
     *
     * The integration invokes following sequence of client requests on the test server
     *  Invoke operation sayHi.
     */
    @ClassRule
    public static SyndesisIntegrationRuntimeContainer integrationContainer = new SyndesisIntegrationRuntimeContainer.Builder()
                            .name("soap-helloworld-client")
                            .fromExport(SoapConnector_IT.class.getResource("HelloWorldSoapConnector-export"))
                            .customize("$..configuredProperties.period", "5000")
                            .customize("$..configuredProperties.address",
                                String.format("http://%s:%s/HelloWorld", GenericContainer.INTERNAL_HOST_HOSTNAME, SOAP_SERVER_PORT))
                            .build()
                            .withNetwork(getSyndesisDb().getNetwork())
                            .withExposedPorts(SyndesisTestEnvironment.getServerPort(),
                                              SyndesisTestEnvironment.getManagementPort());

    @Test
    @CitrusTest
    public void testSayHi(@CitrusResource TestRunner runner) {
        runner.echo("SayHi operation");

        runner.soap(builder -> builder.server(soapServer)
            .receive()
            .payload(REQUEST_PAYLOAD));

        runner.soap(builder -> builder.server(soapServer)
            .send()
            .payload(RESPONSE_PAYLOAD));

        runner.repeatOnError()
            .index("retries")
            .autoSleep(1000L)
            .until(is(6))
            .actions(runner.query(builder -> builder.dataSource(sampleDb)
                .statement("select count(*) as found_records from contact")
                .validate("found_records", String.valueOf(1))));

        runner.query(builder -> builder.dataSource(sampleDb)
            .statement("select * from contact")
            .validate("first_name", "Hello Hello!"));
    }

    @Configuration
    public static class EndpointConfig {

        @Bean
        public WebServiceServer soapServer() {
            // WS-Security validation
            final Wss4jSecurityInterceptor interceptor = new Wss4jSecurityInterceptor();
            interceptor.setValidationActions("UsernameToken Timestamp");
            interceptor.setSecurementPasswordType(WSS4JConstants.PW_TEXT);
            interceptor.setSecurementUsernameTokenCreated(true);
            interceptor.setSecurementUsernameTokenNonce(true);

            interceptor.setValidationCallbackHandler(callbacks -> {
                for (Callback callback : callbacks) {
                    if (callback instanceof WSPasswordCallback) {
                        final WSPasswordCallback passwordCallback = (WSPasswordCallback) callback;
                        if ("admin".equals(passwordCallback.getIdentifier())) {
                            passwordCallback.setPassword("secret");
                        }
                    } else {
                        throw new UnsupportedCallbackException(callback, "Unrecognized Callback");
                    }
                }
            });

            return CitrusEndpoints.soap()
                .server()
                .port(SOAP_SERVER_PORT)
                .interceptors(Arrays.asList(new LoggingEndpointInterceptor(), interceptor))
                .autoStart(true)
                .timeout(600000L)
                .build();
        }

        @Bean
        public TestRunnerBeforeTestSupport beforeTest(DataSource sampleDb) {
            return new TestRunnerBeforeTestSupport() {
                @Override
                public void beforeTest(TestRunner runner) {
                    runner.sql(builder -> builder.dataSource(sampleDb)
                        .statement("delete from contact"));
                }
            };
        }
    }
}
