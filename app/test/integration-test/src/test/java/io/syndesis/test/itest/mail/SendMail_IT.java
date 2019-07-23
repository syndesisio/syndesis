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

package io.syndesis.test.itest.mail;

import javax.sql.DataSource;
import java.io.IOException;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.endpoint.CitrusEndpoints;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.dsl.runner.TestRunnerBeforeTestSupport;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.mail.message.CitrusMailMessageHeaders;
import com.consol.citrus.mail.message.MailMessage;
import com.consol.citrus.mail.server.MailServer;
import com.consol.citrus.util.FileUtils;
import io.syndesis.test.SyndesisTestEnvironment;
import io.syndesis.test.container.integration.SyndesisIntegrationRuntimeContainer;
import io.syndesis.test.itest.SyndesisIntegrationTestSupport;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.SocketUtils;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;

/**
 * @author Christoph Deppisch
 */
@ContextConfiguration(classes = SendMail_IT.EndpointConfig.class)
public class SendMail_IT extends SyndesisIntegrationTestSupport {

    private static int mailServerPort = SocketUtils.findAvailableTcpPort();
    static {
        Testcontainers.exposeHostPorts(mailServerPort);
    }

    @Autowired
    private HttpClient webHookClient;

    @Autowired
    private MailServer mailServer;

    @Autowired
    private DataSource sampleDb;

    /**
     * This integration provides a webhook that expects a POST request with some contact Json object as payload. The
     * incoming contact (first_name, company, mail) triggers a send mail activity with a welcome message.
     *
     * After the mail is sent a new task entry for that contact is added to the sample database.
     */
    @ClassRule
    public static SyndesisIntegrationRuntimeContainer integrationContainer = new SyndesisIntegrationRuntimeContainer.Builder()
                            .name("send-mail")
                            .fromExport(SendMail_IT.class.getResource("SendMail-export"))
                            .customize("$..configuredProperties.host", GenericContainer.INTERNAL_HOST_HOSTNAME)
                            .customize("$..configuredProperties.port", mailServerPort)
                            .build()
                            .withNetwork(getSyndesisDb().getNetwork())
                            .withExposedPorts(SyndesisTestEnvironment.getServerPort());

    @Test
    @CitrusTest
    public void testSendMail(@CitrusResource TestRunner runner) throws IOException {
        runner.variable("first_name", "John");
        runner.variable("company", "Red Hat");
        runner.variable("email", "john@syndesis.org");

        runner.http(builder -> builder.client(webHookClient)
                .send()
                .post()
                .fork(true)
                .payload(getWebhookPayload()));

        String mailBody = FileUtils.readToString(new ClassPathResource("mail.txt", SendMail_IT.class));
        runner.receive(builder -> builder.endpoint(mailServer)
                        .message(MailMessage.request()
                                .from("people-team@syndesis.org")
                                .to("${email}")
                                .cc("")
                                .bcc("")
                                .subject("Welcome!")
                                .body(mailBody, "text/plain; charset=UTF-8")));

        runner.send(builder -> builder.endpoint(mailServer)
                        .message(MailMessage.response(250, "OK")));

        runner.http(builder -> builder.client(webHookClient)
                .receive()
                .response(HttpStatus.NO_CONTENT));

        verifyRecordsInDb(runner, 1, "New hire for ${first_name} from ${company}");
    }

    @Test
    @CitrusTest
    public void testSendMailError(@CitrusResource TestRunner runner) {
        runner.variable("first_name", "Joanne");
        runner.variable("company", "Red Hat");
        runner.variable("email", "joanne@syndesis.org");

        runner.http(builder -> builder.client(webHookClient)
                .send()
                .post()
                .fork(true)
                .payload(getWebhookPayload()));

        runner.receive(builder -> builder.endpoint(mailServer)
                        .header(CitrusMailMessageHeaders.MAIL_FROM, "people-team@syndesis.org")
                        .header(CitrusMailMessageHeaders.MAIL_TO, "${email}")
                        .header(CitrusMailMessageHeaders.MAIL_SUBJECT, "Welcome!"));

        runner.send(builder -> builder.endpoint(mailServer)
                        .message(MailMessage.response(421, "Service not available, closing transmission channel")));

        runner.http(builder -> builder.client(webHookClient)
                .receive()
                .response(HttpStatus.INTERNAL_SERVER_ERROR));

        verifyRecordsInDb(runner, 0, "New hire for ${first_name} from ${company}");
    }

    private String getWebhookPayload() {
        return "{\"first_name\":\"${first_name}\",\"company\":\"${company}\",\"mail\":\"${email}\"}";
    }

    private void verifyRecordsInDb(TestRunner runner, int numberOfRecords, String task) {
        runner.query(builder -> builder.dataSource(sampleDb)
                .statement("select count(*) as found_records from todo where task='" + task + "'")
                .validate("found_records", String.valueOf(numberOfRecords)));
    }

    @Configuration
    public static class EndpointConfig {
        @Bean
        public HttpClient webHookClient() {
            return CitrusEndpoints.http().client()
                    .requestUrl(String.format("http://localhost:%s/webhook/test-webhook", integrationContainer.getServerPort()))
                    .build();
        }

        @Bean
        public MailServer mailServer() {
            return CitrusEndpoints.mail().server()
                    .timeout(60000L)
                    .autoStart(true)
                    .autoAccept(true)
                    .port(mailServerPort)
                    .build();
        }

        @Bean
        public TestRunnerBeforeTestSupport beforeTest(DataSource sampleDb) {
            return new TestRunnerBeforeTestSupport() {
                @Override
                public void beforeTest(TestRunner runner) {
                    runner.sql(builder -> builder.dataSource(sampleDb)
                                                 .statement("delete from todo where task like 'New hire for%'"));
                }
            };
        }
    }
}
