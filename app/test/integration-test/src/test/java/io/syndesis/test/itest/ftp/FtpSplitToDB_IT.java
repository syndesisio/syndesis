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

import io.syndesis.test.container.integration.SyndesisIntegrationRuntimeContainer;

import org.apache.commons.net.ftp.FTPCmd;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.container.IteratingConditionExpression;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.ftp.message.FtpMessage;

@Testcontainers
public class FtpSplitToDB_IT extends FtpTestSupport {

    /**
     * Integration periodically retrieves tasks as FTP file transfer and maps those to the database.
     * The integration uses a split step to pass entries one by one to the database.
     */
    @Container
    public static final SyndesisIntegrationRuntimeContainer INTEGRATION_CONTAINER = new SyndesisIntegrationRuntimeContainer.Builder()
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
    public void testFtpSplitToDB(@CitrusResource TestRunner runner) {
        runner.receive(receiveMessageBuilder -> receiveMessageBuilder
                .endpoint(ftpTestServer)
                .timeout(60000L)
                .message(FtpMessage.command(FTPCmd.RETR).arguments("todo.json")));

        runner.send(sendMessageBuilder -> sendMessageBuilder
                .endpoint(ftpTestServer)
                .message(FtpMessage.success()));

        runner.repeatOnError()
                .startsWith(1)
                .autoSleep(1000L)
                .until(new IteratingConditionExpression() {
                    @Override
                    public boolean evaluate(int index, TestContext context) {
                        return index > 10;
                    }
                })
                .actions(runner.query(builder -> builder.dataSource(sampleDb())
                        .statement("select count(*) as found_records from todo")
                        .validate("found_records", String.valueOf(3))));

        runner.query(builder -> builder.dataSource(sampleDb())
                .statement("select task, completed from todo")
                .validate("task", "FTP task #1", "FTP task #2", "FTP task #3")
                .validate("completed", "0", "1", "0"));
    }

    @Override
    protected SyndesisIntegrationRuntimeContainer integrationContainer() {
        return INTEGRATION_CONTAINER;
    }

}
