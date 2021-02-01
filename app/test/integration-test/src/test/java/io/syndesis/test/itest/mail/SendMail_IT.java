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
import java.time.Duration;

import com.consol.citrus.TestCaseRunner;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.http.client.HttpClientBuilder;
import com.consol.citrus.mail.message.CitrusMailMessageHeaders;
import com.consol.citrus.mail.message.MailMessage;
import com.consol.citrus.mail.server.MailServer;
import com.consol.citrus.mail.server.MailServerBuilder;
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

import static com.consol.citrus.actions.ExecuteSQLAction.Builder.sql;
import static com.consol.citrus.actions.ExecuteSQLQueryAction.Builder.query;
import static com.consol.citrus.actions.ReceiveMessageAction.Builder.receive;
import static com.consol.citrus.actions.SendMessageAction.Builder.send;
import static com.consol.citrus.http.actions.HttpActionBuilder.http;

/**
 * @author Christoph Deppisch
 */
@ContextConfiguration(classes = SendMail_IT.EndpointConfig.class)
public class SendMail_IT extends SyndesisIntegrationTestSupport {

    private static final int MAIL_SERVER_PORT = SocketUtils.findAvailableTcpPort();
    static {
        Testcontainers.exposeHostPorts(MAIL_SERVER_PORT);
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
                            .customize("$..configuredProperties.port", MAIL_SERVER_PORT)
                            .build()
                            .withNetwork(getSyndesisDb().getNetwork())
                            .withExposedPorts(SyndesisTestEnvironment.getServerPort());

    @Test
    @CitrusTest
    public void testSendMail(@CitrusResource TestCaseRunner runner) throws IOException {
        cleanupDatabase(runner);

        runner.variable("first_name", "John");
        runner.variable("company", "Red Hat");
        runner.variable("email", "john@syndesis.org");

        runner.given(http().client(webHookClient)
                .send()
                .post()
                .fork(true)
                .message()
                .body(getWebhookPayload()));

        String mailBody = FileUtils.readToString(new ClassPathResource("mail.txt", SendMail_IT.class));
        runner.when(receive().endpoint(mailServer)
                        .message(MailMessage.request()
                                .from("people-team@syndesis.org")
                                .to("${email}")
                                .cc("")
                                .bcc("")
                                .subject("Welcome!")
                                .body(mailBody, "text/plain; charset=UTF-8")));

        runner.then(send().endpoint(mailServer)
                        .message(MailMessage.response(250, "OK")));

        runner.then(http().client(webHookClient)
                .receive()
                .response(HttpStatus.OK));

        verifyRecordsInDb(runner, 1, "New hire for ${first_name} from ${company}");
    }

    @Test
    @CitrusTest
    public void testSendMailError(@CitrusResource TestCaseRunner runner) {
        cleanupDatabase(runner);

        runner.variable("first_name", "Joanne");
        runner.variable("company", "Red Hat");
        runner.variable("email", "joanne@syndesis.org");

        runner.given(http().client(webHookClient)
                .send()
                .post()
                .fork(true)
                .message()
                .body(getWebhookPayload()));

        runner.when(receive().endpoint(mailServer)
                        .message()
                        .header(CitrusMailMessageHeaders.MAIL_FROM, "people-team@syndesis.org")
                        .header(CitrusMailMessageHeaders.MAIL_TO, "${email}")
                        .header(CitrusMailMessageHeaders.MAIL_SUBJECT, "Welcome!"));

        runner.then(send().endpoint(mailServer)
                        .message(MailMessage.response(421, "Service not available, closing transmission channel")));

        runner.then(http().client(webHookClient)
                .receive()
                .response(HttpStatus.INTERNAL_SERVER_ERROR));

        verifyRecordsInDb(runner, 0, "New hire for ${first_name} from ${company}");
    }

    private static String getWebhookPayload() {
        return "{\"first_name\":\"${first_name}\",\"company\":\"${company}\",\"mail\":\"${email}\"}";
    }

    private void verifyRecordsInDb(TestCaseRunner runner, int numberOfRecords, String task) {
        runner.run(query(sampleDb)
                .statement("select count(*) as found_records from todo where task='" + task + "'")
                .validate("found_records", String.valueOf(numberOfRecords)));
    }

    @Configuration
    public static class EndpointConfig {
        @Bean
        public HttpClient webHookClient() {
            return new HttpClientBuilder()
                    .requestUrl(String.format("http://localhost:%s/webhook/test-webhook", integrationContainer.getServerPort()))
                    .build();
        }

        @Bean
        public MailServer mailServer() {
            return new MailServerBuilder()
                    .timeout(Duration.ofSeconds(SyndesisTestEnvironment.getDefaultTimeout()).toMillis())
                    .autoStart(true)
                    .autoAccept(true)
                    .port(MAIL_SERVER_PORT)
                    .build();
        }
    }

    private void cleanupDatabase(TestCaseRunner runner) {
        runner.given(sql(sampleDb)
            .statement("delete from todo"));
    }
}
