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

package io.syndesis.test.itest.sheets;

import javax.sql.DataSource;
import java.time.Duration;
import java.util.Arrays;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.http.server.HttpServer;
import io.syndesis.test.SyndesisTestEnvironment;
import io.syndesis.test.container.integration.SyndesisIntegrationRuntimeContainer;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

/**
 * @author Christoph Deppisch
 */
public class DBSplitToSheets_IT extends GoogleSheetsTestSupport {

    @Autowired
    private DataSource sampleDb;

    @Autowired
    private HttpServer googleSheetsApiServer;

    /**
     * Integration periodically retrieves all contacts from the database and maps the entries (first_name, last_name, company) to a spreadsheet on a Google Sheets account.
     * The integration uses a split step to pass entries one by one to the Google Sheets API.
     */
    @ClassRule
    public static SyndesisIntegrationRuntimeContainer integrationContainer = new SyndesisIntegrationRuntimeContainer.Builder()
            .name("db-split-to-sheets")
            .fromExport(DBSplitToSheets_IT.class.getResource("DBSplitToSheets-export"))
            .customize("$..configuredProperties.schedulerExpression", "5000")
            .customize("$..rootUrl.defaultValue",
                        String.format("http://%s:%s", GenericContainer.INTERNAL_HOST_HOSTNAME, googleSheetsServerPort))
            .build()
            .withNetwork(getSyndesisDb().getNetwork())
            .waitingFor(Wait.defaultWaitStrategy().withStartupTimeout(Duration.ofSeconds(SyndesisTestEnvironment.getContainerStartupTimeout())));

    @Test
    @CitrusTest
    public void testDBSplitToSheets(@CitrusResource TestRunner runner) {
        runner.sql(builder -> builder.dataSource(sampleDb)
                .statements(Arrays.asList("insert into contact (first_name, last_name, company, lead_source) values ('Joe','Jackson','Red Hat','google-sheets')",
                                          "insert into contact (first_name, last_name, company, lead_source) values ('Joanne','Jackson','Red Hat','google-sheets')")));

        runner.http(builder -> builder.server(googleSheetsApiServer)
                        .receive()
                        .put()
                        .payload("{\"majorDimension\":\"ROWS\",\"values\":[[\"Joe\",\"Jackson\",\"Red Hat\"]]}"));

        runner.http(builder -> builder.server(googleSheetsApiServer)
                        .send()
                        .response(HttpStatus.OK));

        runner.http(builder -> builder.server(googleSheetsApiServer)
                .receive()
                .put()
                .payload("{\"majorDimension\":\"ROWS\",\"values\":[[\"Joanne\",\"Jackson\",\"Red Hat\"]]}"));

        runner.http(builder -> builder.server(googleSheetsApiServer)
                .send()
                .response(HttpStatus.OK));
    }
}
