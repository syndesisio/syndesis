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

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.util.Arrays;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.endpoint.CitrusEndpoints;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.ws.interceptor.LoggingEndpointInterceptor;
import com.consol.citrus.ws.server.WebServiceServer;
import io.syndesis.test.SyndesisTestEnvironment;
import io.syndesis.test.container.integration.SyndesisIntegrationRuntimeContainer;
import io.syndesis.test.itest.SyndesisIntegrationTestSupport;
import org.apache.wss4j.common.WSS4JConstants;
import org.apache.wss4j.common.ext.WSPasswordCallback;
import org.junit.jupiter.api.Test;
import org.springframework.util.SocketUtils;
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class SoapConnector_IT extends SyndesisIntegrationTestSupport {

    private static final int SOAP_SERVER_PORT = SocketUtils.findAvailableTcpPort();
    static {
        org.testcontainers.Testcontainers.exposeHostPorts(SOAP_SERVER_PORT);
    }

    private static final WebServiceServer SOAP_SERVER = startup(soapServer());

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
     * The integration invokes following sequence of client requests on the test server
     *  Invoke operation sayHi.
     */
    @Container
    public static final SyndesisIntegrationRuntimeContainer INTEGRATION_CONTAINER = new SyndesisIntegrationRuntimeContainer.Builder()
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
        runner.sql(builder -> builder.dataSource(sampleDb())
            .statement("delete from contact"));

        runner.echo("SayHi operation");

        runner.soap(builder -> builder.server(SOAP_SERVER)
            .receive()
            .payload(REQUEST_PAYLOAD));

        runner.soap(builder -> builder.server(SOAP_SERVER)
            .send()
            .payload(RESPONSE_PAYLOAD));

        runner.repeatOnError()
            .index("retries")
            .autoSleep(1000L)
            .until((index, rules) -> index <= 6)
            .actions(runner.query(builder -> builder.dataSource(sampleDb())
                .statement("select count(*) as found_records from contact where first_name like 'Hello Hello!'")
                .validateScript("assert rows.get(0).get(\"found_records\") > 0", "groovy")));
    }

    private static WebServiceServer soapServer() {
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

}
