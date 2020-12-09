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
import com.consol.citrus.http.security.BasicAuthConstraint;
import com.consol.citrus.http.security.SecurityHandlerFactory;
import com.consol.citrus.http.security.User;
import com.consol.citrus.ws.server.WebServiceServer;
import io.syndesis.test.SyndesisTestEnvironment;
import io.syndesis.test.container.integration.SyndesisIntegrationRuntimeContainer;
import io.syndesis.test.itest.SyndesisIntegrationTestSupport;
import org.eclipse.jetty.security.AbstractLoginService;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.IdentityService;
import org.eclipse.jetty.security.PropertyUserStore;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.util.security.Credential;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.SocketUtils;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;

import javax.security.auth.Subject;
import javax.sql.DataSource;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;

/**
 * @author delawen
 */
@ContextConfiguration(classes = SoapConnectorBasicAuth_IT.EndpointConfig.class)
public class SoapConnectorBasicAuth_IT extends SyndesisIntegrationTestSupport {

    private static final int SOAP_SERVER_PORT = SocketUtils.findAvailableTcpPort();
    public static final String USERNAME = "registered";
    public static final String PASSWORD = "passw0rd";
    public static final  List<User> USERS = new ArrayList<User>();
    public static final String[] ROLES = new String[]{USERNAME};
    public static final User USER = new User();

    static {
        Testcontainers.exposeHostPorts(SOAP_SERVER_PORT);
        USER.setName(USERNAME);
        USER.setRoles(ROLES);
        USER.setPassword(PASSWORD);
        USERS.add(USER);
    }

    @Autowired
    private WebServiceServer soapServer;

    @Autowired
    private DataSource sampleDb;

    private static final String REQUEST_PAYLOAD =
        "<ns1:sayHi xmlns:ns1=\"http://camel.apache.org/cxf/wsrm\">" +
            "<arg0 xmlns=\"http://camel.apache.org/cxf/wsrm\">BasicAuth</arg0>" +
            "</ns1:sayHi>";
    private static final String RESPONSE_PAYLOAD =
        "<ns1:sayHiResponse xmlns:ns1=\"http://camel.apache.org/cxf/wsrm\">" +
            "   <ns1:return xmlns=\"http://camel.apache.org/cxf/wsrm\">Hello BasicAuth!</ns1:return>" +
            "</ns1:sayHiResponse>";

    /**
     * Integration uses api connector to send SOAP client requests to a REST endpoint. The client API connector was generated
     * from SOAP WSDL1.1 specification.
     * <p>
     * The integration invokes following sequence of client requests on the test server
     * Invoke operation sayHi.
     */
    @ClassRule
    public static SyndesisIntegrationRuntimeContainer integrationContainer = new SyndesisIntegrationRuntimeContainer.Builder()
        .name("soap-basic-auth")
        .fromExport(SoapConnectorBasicAuth_IT.class.getResource("SOAPBasicAuthentication-export"))
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
                .statement("select count(*) as found_records from contact where first_name like 'Hello BasicAuth!'")
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
                .securityHandler(basicAuthSecurityHandler())
                .autoStart(true)
                .timeout(600000L)
                .build();
        }

        @Bean
        public SecurityHandler basicAuthSecurityHandler() throws Exception {
            return basicAuthSecurityHandlerFactoryBean().getObject();
        }

        @Bean
        public SecurityHandlerFactory basicAuthSecurityHandlerFactoryBean() {
            SecurityHandlerFactory securityHandlerFactory = new SecurityHandlerFactory();
            securityHandlerFactory.setUsers(USERS);
            securityHandlerFactory.setLoginService(basicAuthLoginService(basicAuthUserStore()));
            securityHandlerFactory.setConstraints(
                Collections.singletonMap("/*", new BasicAuthConstraint(ROLES)));

            return securityHandlerFactory;
        }

        @Bean
        public HashLoginService basicAuthLoginService(PropertyUserStore basicAuthUserStore) {
            return new HashLoginService() {
                @Override
                protected void doStart() throws Exception {
                    setUserStore(basicAuthUserStore);
                    basicAuthUserStore.start();

                    super.doStart();
                }
            };
        }

        @Bean
        public PropertyUserStore basicAuthUserStore() {
            return new PropertyUserStore() {
                @Override
                protected void loadUsers() throws IOException {
                    getKnownUserIdentities().clear();

                    for (User user : USERS) {
                        Credential credential = Credential.getCredential(user.getPassword());

                        Principal userPrincipal = new AbstractLoginService.UserPrincipal(user.getName(), credential);
                        Subject subject = new Subject();
                        subject.getPrincipals().add(userPrincipal);
                        subject.getPrivateCredentials().add(credential);

                        String[] roleArray = IdentityService.NO_ROLES;
                        if (user.getRoles() != null && user.getRoles().length > 0) {
                            roleArray = user.getRoles();
                        }

                        for (String role : roleArray) {
                            subject.getPrincipals().add(new AbstractLoginService.RolePrincipal(role));
                        }

                        subject.setReadOnly();

                        getKnownUserIdentities().put(user.getName(), getIdentityService().newUserIdentity(subject, userPrincipal, roleArray));
                    }
                }
            };
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
