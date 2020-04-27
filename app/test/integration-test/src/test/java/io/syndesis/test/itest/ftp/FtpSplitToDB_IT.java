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

import java.time.Duration;

import com.consol.citrus.TestCaseRunner;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.container.IteratingConditionExpression;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.ftp.message.FtpMessage;
import io.syndesis.test.SyndesisTestEnvironment;
import io.syndesis.test.container.integration.SyndesisIntegrationRuntimeContainer;
import org.apache.commons.net.ftp.FTPCmd;
import org.apache.ftpserver.DataConnectionConfiguration;
import org.apache.ftpserver.DataConnectionConfigurationFactory;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.GenericContainer;

import static com.consol.citrus.actions.ExecuteSQLQueryAction.Builder.query;
import static com.consol.citrus.actions.ReceiveMessageAction.Builder.receive;
import static com.consol.citrus.actions.SendMessageAction.Builder.send;
import static com.consol.citrus.container.RepeatOnErrorUntilTrue.Builder.repeatOnError;

/**
 * @author Christoph Deppisch
 */
@ContextConfiguration(classes = FtpSplitToDB_IT.EndpointConfig.class)
@DirtiesContext
public class FtpSplitToDB_IT extends FtpTestSupport {

    /**
     * Integration periodically retrieves tasks as FTP file transfer and maps those to the database.
     * The integration uses a split step to pass entries one by one to the database.
     */
    @ClassRule
    public static SyndesisIntegrationRuntimeContainer integrationContainer = new SyndesisIntegrationRuntimeContainer.Builder()
            .name("ftp-split-to-db")
            .fromExport(FtpSplitToDB_IT.class.getResource("FtpSplitToDB-export"))
            .customize("$..configuredProperties.delay", "60000")
            .customize("$..configuredProperties.directoryName", "public")
            .customize("$..configuredProperties.fileName", "todo.json")
            .customize("$..configuredProperties.host", GenericContainer.INTERNAL_HOST_HOSTNAME)
            .customize("$..configuredProperties.port", FTP_TEST_SERVER_PORT)
            .build()
            .withNetwork(getSyndesisDb().getNetwork());

    @Test
    @CitrusTest
    public void testFtpSplitToDB(@CitrusResource TestCaseRunner runner) {
        cleanupDatabase(runner);

        runner.given(receive()
                .endpoint(ftpTestServer)
                .timeout(Duration.ofSeconds(SyndesisTestEnvironment.getDefaultTimeout()).toMillis())
                .message(FtpMessage.command(FTPCmd.RETR).arguments("todo.json")));

        runner.when(send()
                .endpoint(ftpTestServer)
                .message(FtpMessage.success()));

        runner.then(repeatOnError()
                .startsWith(1)
                .autoSleep(1000L)
                .until(new IteratingConditionExpression() {
                    @Override
                    public boolean evaluate(int index, TestContext context) {
                        return index > 10;
                    }
                })
                .actions(query(sampleDb)
                        .statement("select count(*) as found_records from todo")
                        .validate("found_records", String.valueOf(3))));

        runner.then(query(sampleDb)
                .statement("select task, completed from todo")
                .validate("task", "FTP task #1", "FTP task #2", "FTP task #3")
                .validate("completed", "0", "1", "0"));
    }

    @Configuration
    public static class EndpointConfig {

        @Bean
        public DataConnectionConfiguration dataConnectionConfiguration() {
            DataConnectionConfigurationFactory dataConnectionFactory = new DataConnectionConfigurationFactory();
            dataConnectionFactory.setPassiveExternalAddress(integrationContainer.getInternalHostIp());
            dataConnectionFactory.setPassivePorts(String.valueOf(PASSIVE_PORT));
            return dataConnectionFactory.createDataConnectionConfiguration();
        }
    }
}
