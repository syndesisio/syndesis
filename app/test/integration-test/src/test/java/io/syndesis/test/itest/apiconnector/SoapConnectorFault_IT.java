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
import com.consol.citrus.dsl.runner.TestRunnerBeforeTestSupport;
import com.consol.citrus.ws.server.WebServiceServer;
import io.syndesis.test.SyndesisTestEnvironment;
import io.syndesis.test.container.integration.SyndesisIntegrationRuntimeContainer;
import io.syndesis.test.itest.SyndesisIntegrationTestSupport;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.SocketUtils;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;

import javax.sql.DataSource;

import static org.hamcrest.CoreMatchers.is;

/**
 * @author delawen
 */
@ContextConfiguration(classes = SoapConnectorFault_IT.EndpointConfig.class)
public class SoapConnectorFault_IT extends SyndesisIntegrationTestSupport {

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
            "<arg0 xmlns=\"http://camel.apache.org/cxf/wsrm\">Testing Errors</arg0>" +
            "</ns1:sayHi>";
    private static final String THE_TEST_FAILED_MISERABLY = "Fault";
    /**
     * Integration uses api connector to send SOAP client requests to a REST endpoint. The client API connector was generated
     * from SOAP WSDL1.1 specification.
     * <p>
     * The integration invokes following sequence of client requests on the test server
     * Invoke operation sayHi.
     */
    @ClassRule
    public static SyndesisIntegrationRuntimeContainer integrationContainer = new SyndesisIntegrationRuntimeContainer.Builder()
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
        runner.echo("SayHi operation");

        runner.soap(builder -> builder.server(soapServer)
            .receive()
            .payload(REQUEST_PAYLOAD));

        runner.soap(builder -> builder.server(soapServer)
            .sendFault()
                .faultCode("{http://www.consol.com/citrus/samples/errorcodes}CITRUS:999")
                .faultString(THE_TEST_FAILED_MISERABLY)
                .faultActor(THE_TEST_FAILED_MISERABLY));

        runner.repeatOnError()
            .index("retries")
            .autoSleep(1000L)
            .until(is(6))
            .actions(runner.query(builder -> builder.dataSource(sampleDb)
                .statement("select count(*) as found_records from contact where first_name like '" +
                    THE_TEST_FAILED_MISERABLY + "'")
                .validateScript("assert rows.get(0).get(\"found_records\") > 0", "groovy")));

    }

    @Configuration
    /**
     * Configure citrus with a basic authentication security
     */
    public static class EndpointConfig {

        @Bean
        public WebServiceServer soapServer() throws Exception {

            return CitrusEndpoints.soap()
                .server()
                .port(SOAP_SERVER_PORT)
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
