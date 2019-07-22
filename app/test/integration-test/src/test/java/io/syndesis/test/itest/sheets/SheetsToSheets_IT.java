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

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.http.server.HttpServer;
import io.syndesis.test.container.integration.SyndesisIntegrationRuntimeContainer;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.testcontainers.containers.GenericContainer;

/**
 * @author Christoph Deppisch
 */
public class SheetsToSheets_IT extends GoogleSheetsTestSupport {

    @Autowired
    private HttpServer googleSheetsApiServer;

    /**
     * Integration periodically retrieves all values from Google Sheets spreadsheet range A:E and maps the column names to custom names. The obtained
     * values are updated to the same spreadsheet using different custom column name mappings.
     */
    public SyndesisIntegrationRuntimeContainer.Builder integrationContainer = new SyndesisIntegrationRuntimeContainer.Builder()
            .name("named-columns")
            .fromExport(SheetsToSheets_IT.class.getResource("SheetsToSheets-export"))
            .customize("$..rootUrl.defaultValue",
                        String.format("http://%s:%s", GenericContainer.INTERNAL_HOST_HOSTNAME, googleSheetsServerPort))
            .customize("$..configuredProperties.spreadsheetId", "testSheetId")
            .customize("$..configuredProperties.schedulerExpression", "5000");

    @Test
    @CitrusTest
    public void testNamedColumns(@CitrusResource TestRunner runner) {
        try (SyndesisIntegrationRuntimeContainer container = integrationContainer.build()) {
            container.start();

            runner.http(builder -> builder.server(googleSheetsApiServer)
                    .receive()
                    .get("/v4/spreadsheets/testSheetId/values:batchGet")
                    .queryParam("ranges", "Drinks!A:E")
                    .queryParam("majorDimension", "ROWS"));

            runner.http(builder -> builder.server(googleSheetsApiServer)
                    .send()
                    .response(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .payload("{" +
                                "\"spreadsheetId\": \"testSheetId\"," +
                                "\"valueRanges\": [" +
                                    "{" +
                                        "\"range\": \"Drinks!A:E\"," +
                                        "\"majorDimension\": \"ROWS\"," +
                                        "\"values\": [[\"a1\", \"b1\", \"c1\", \"d1\", \"e1\"]]" +
                                    "}" +
                                "]" +
                            "}"));

            runner.http(builder -> builder.server(googleSheetsApiServer)
                    .receive()
                    .put("/v4/spreadsheets/testSheetId/values/Copy!A:E")
                    .payload("{\"majorDimension\":\"ROWS\",\"values\":[[\"a1\", \"b1\", \"c1\", \"d1\", \"e1\"]]}"));

            runner.http(builder -> builder.server(googleSheetsApiServer)
                    .send()
                    .response(HttpStatus.OK));
        }
    }
}
