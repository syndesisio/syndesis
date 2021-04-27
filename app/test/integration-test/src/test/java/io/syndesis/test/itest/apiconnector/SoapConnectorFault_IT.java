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

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.endpoint.CitrusEndpoints;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.ws.server.WebServiceServer;
import io.syndesis.test.SyndesisTestEnvironment;
import io.syndesis.test.container.integration.SyndesisIntegrationRuntimeContainer;
import io.syndesis.test.itest.SyndesisIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.util.SocketUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.CoreMatchers.is;

@Testcontainers
public class SoapConnectorFault_IT extends SyndesisIntegrationTestSupport {

    private static final int SOAP_SERVER_PORT = SocketUtils.findAvailableTcpPort();

    static {
        org.testcontainers.Testcontainers.exposeHostPorts(SOAP_SERVER_PORT);
    }

    private static final WebServiceServer SOAP_SERVER = startup(soapServer());

    private static final String REQUEST_PAYLOAD =
        "<ns1:sayHi xmlns:ns1=\"http://camel.apache.org/cxf/wsrm\">" +
            "<arg0>Testing Errors</arg0>" +
            "</ns1:sayHi>";
    private static final String THE_TEST_FAILED_MISERABLY = "Fault";
    /**
     * Integration uses api connector to send SOAP client requests to a REST endpoint. The client API connector was generated
     * from SOAP WSDL1.1 specification.
     * <p>
     * The integration invokes following sequence of client requests on the test server
     * Invoke operation sayHi.
     */
    @Container
    public static final SyndesisIntegrationRuntimeContainer INTEGRATION_CONTAINER = new SyndesisIntegrationRuntimeContainer.Builder()
        .name("soap-fault")
        .fromExport(SoapConnectorFault_IT.class.getResource("SOAPFault-export"))
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
            .sendFault()
                .faultCode("{http://www.consol.com/citrus/samples/errorcodes}CITRUS:999")
                .faultString(THE_TEST_FAILED_MISERABLY)
                .faultActor(THE_TEST_FAILED_MISERABLY));

        runner.repeatOnError()
            .index("retries")
            .autoSleep(1000L)
            .until(is(60))
            .actions(runner.query(builder -> builder.dataSource(sampleDb())
                .statement("select count(*) as found_records from contact where first_name='CITRUS:999'")
                .validate("FOUND_RECORDS", "1")));
    }

    private static WebServiceServer soapServer() {
        return CitrusEndpoints.soap()
            .server()
            .port(SOAP_SERVER_PORT)
            .autoStart(true)
            .timeout(600000L)
            .build();
    }
}
