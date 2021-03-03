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
package io.syndesis.test.itest.googlecalendar;

import io.syndesis.test.SyndesisTestEnvironment;
import io.syndesis.test.container.integration.SyndesisIntegrationRuntimeContainer;
import io.syndesis.test.itest.SyndesisIntegrationTestSupport;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.runner.TestRunner;

@Testcontainers
public class WebhookToGoogleCalendar_IT extends SyndesisIntegrationTestSupport {

    /**
     * Webhook receives POST request and creates a Google Calendar event.
     */
    @Container
    public static final SyndesisIntegrationRuntimeContainer INTEGRATION_CONTAINER = new SyndesisIntegrationRuntimeContainer.Builder()
        .name("webhook-to-googlecalendar")
        .fromExport(WebhookToGoogleCalendar_IT.class.getResource("webhook-to-google-calendar-export"))
        .build()
        .withExposedPorts(SyndesisTestEnvironment.getServerPort(),
            SyndesisTestEnvironment.getManagementPort())
        .withExtraHost("www.googleapis.com", "127.0.0.1");

    @Test
    @CitrusTest
    public void waitForIntegrationToBeHealthy(@CitrusResource final TestRunner runner) {
        runner.waitFor().http()
            .method(HttpMethod.GET)
            .seconds(10L)
            .status(HttpStatus.OK)
            .url(String.format("http://localhost:%s/actuator/health", INTEGRATION_CONTAINER.getManagementPort()));
    }
}
