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

package io.syndesis.test.itest.ftp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import io.syndesis.test.SyndesisTestEnvironment;
import io.syndesis.test.container.integration.SyndesisIntegrationRuntimeContainer;

import org.apache.commons.net.ftp.FTPCmd;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.endpoint.CitrusEndpoints;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.ftp.message.FtpMessage;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.validation.json.JsonMessageValidationContext;
import com.consol.citrus.validation.json.JsonTextMessageValidator;

@Testcontainers
public class WebHookToFtp_IT extends FtpTestSupport {

    private final HttpClient webHookClient = CitrusEndpoints.http().client()
        .requestUrl(String.format("http://localhost:%s/webhook/contact", INTEGRATION_CONTAINER.getServerPort()))
        .build();;

    /**
     * Integration receives a WebHook trigger with contact information as Json object. The contact is transformed to a csv like
     * comma delimited String and pushed to Ftp server as new file.
     */
    @Container
    public static final SyndesisIntegrationRuntimeContainer INTEGRATION_CONTAINER = new SyndesisIntegrationRuntimeContainer.Builder()
            .name("webhook-to-ftp")
            .fromExport(WebHookToFtp_IT.class.getResource("WebhookToFtp-export"))
            .customize("$..configuredProperties.contextPath", "contact")
            .customize("$..configuredProperties.directoryName", "public")
            .customize("$..configuredProperties.host", GenericContainer.INTERNAL_HOST_HOSTNAME)
            .customize("$..configuredProperties.port", FTP_TEST_SERVER_PORT)
            .build()
            .withExposedPorts(SyndesisTestEnvironment.getServerPort());

    @Test
    @CitrusTest
    public void testWebHookToFtp(@CitrusResource TestRunner runner) {
        runner.variable("first_name", "Joanne");
        runner.variable("company", "Red Hat");
        runner.variable("email", "joanne@syndesis.org");

        runner.http(builder -> builder.client(webHookClient)
                .send()
                .post()
                .fork(true)
                .payload("{\"first_name\":\"${first_name}\",\"company\":\"${company}\",\"mail\":\"${email}\"}"));

        runner.receive(receiveMessageBuilder -> receiveMessageBuilder
                .endpoint(ftpTestServer)
                .message(FtpMessage.command(FTPCmd.STOR).arguments("tmp_contacts.csv")));

        runner.send(sendMessageBuilder -> sendMessageBuilder
                .endpoint(ftpTestServer)
                .message(FtpMessage.success()));

        runner.receive(receiveMessageBuilder -> receiveMessageBuilder
                .endpoint(ftpTestServer)
                .message(FtpMessage.command(FTPCmd.RNFR).arguments("public/tmp_contacts.csv")));

        runner.send(sendMessageBuilder -> sendMessageBuilder
                .endpoint(ftpTestServer)
                .message(FtpMessage.success()));

        runner.http(builder -> builder.client(webHookClient)
                .receive()
                .response(HttpStatus.OK));

        runner.run(new VerifyFtpUploadTestAction());
    }
    
    @Override
    protected SyndesisIntegrationRuntimeContainer integrationContainer() {
        return INTEGRATION_CONTAINER;
    }

    /*
     * Helper test action ready the uploaded ftp file from user home directory and verifies
     * the content with Json message validator.
     */
    private static class VerifyFtpUploadTestAction extends AbstractTestAction {

        private static final String UPLOAD_FILENAME = "contacts.csv";

        @Override
        public void doExecute(TestContext testContext) {
            Path publicUserDir = getFtpUserHome().resolve("public");
            Assertions.assertTrue(publicUserDir.toFile().exists(), "Missing ftp user home directory");

            File ftpUploadFile = publicUserDir.resolve(UPLOAD_FILENAME).toFile();
            Assertions.assertTrue(ftpUploadFile.exists(), String.format("Missing ftp upload file '%s'", UPLOAD_FILENAME));
            try {
                JsonTextMessageValidator validator = new JsonTextMessageValidator();
                validator.validateMessage(new DefaultMessage(FileUtils.readToString(ftpUploadFile)),
                                            new DefaultMessage("{\"message\" : \"${first_name},${company},${email}\"}"),
                                            testContext,
                                            new JsonMessageValidationContext());
            } catch (IOException e) {
                throw new CitrusRuntimeException(String.format("Failed to verify ftp upload file '%s'", UPLOAD_FILENAME), e);
            }
        }
    }
}
