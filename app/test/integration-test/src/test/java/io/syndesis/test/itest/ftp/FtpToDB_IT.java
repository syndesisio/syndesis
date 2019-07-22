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

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.ftp.message.FtpMessage;
import io.syndesis.test.container.integration.SyndesisIntegrationRuntimeContainer;
import org.apache.commons.net.ftp.FTPCmd;
import org.apache.ftpserver.DataConnectionConfiguration;
import org.apache.ftpserver.DataConnectionConfigurationFactory;
import org.hamcrest.Matchers;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.GenericContainer;

/**
 * @author Christoph Deppisch
 */
@ContextConfiguration(classes = FtpToDB_IT.EndpointConfig.class)
@DirtiesContext
public class FtpToDB_IT extends FtpTestSupport {

    /**
     * Integration periodically retrieves tasks as FTP file transfer and maps those to the database.
     * The integration uses data mapper topmost collection support to map all entries in batch insert to the database.
     */
    @ClassRule
    public static SyndesisIntegrationRuntimeContainer integrationContainer = new SyndesisIntegrationRuntimeContainer.Builder()
            .name("ftp-to-db")
            .fromExport(FtpToDB_IT.class.getResource("FtpToDB-export"))
            .customize("$..configuredProperties.delay", "60000")
            .customize("$..configuredProperties.directoryName", "public")
            .customize("$..configuredProperties.fileName", "todo.json")
            .customize("$..configuredProperties.host", GenericContainer.INTERNAL_HOST_HOSTNAME)
            .customize("$..configuredProperties.port", ftpTestServerPort)
            .build()
            .withNetwork(getSyndesisDb().getNetwork());

    @Test
    @CitrusTest
    public void testFtpToDB(@CitrusResource TestRunner runner) {
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
                .until(Matchers.greaterThan(10))
                .actions(runner.query(builder -> builder.dataSource(sampleDb)
                        .statement("select count(*) as found_records from todo")
                        .validate("found_records", String.valueOf(3))));

        runner.query(builder -> builder.dataSource(sampleDb)
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
            dataConnectionFactory.setPassivePorts(String.valueOf(passivePort));
            return dataConnectionFactory.createDataConnectionConfiguration();
        }
    }
}
