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
package io.syndesis.connector.meta;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.LocalHostUriTemplateHandler;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.main.banner-mode = off",
        "debug = true"
    })
public class MetaIT {

    private final TestRestTemplate api;

    private final TestRestTemplate management;

    @Autowired
    public MetaIT(final Environment environment, final TestRestTemplate api, final RestTemplateBuilder builder) {
        this.api = api;
        management = new TestRestTemplate(builder.uriTemplateHandler(new LocalHostUriTemplateHandler(environment) {
            @Override
            public String getRootUri() {
                final String port = environment.getProperty("local.management.port");
                return "http://localhost:" + port;
            }
        }));
    }

    @Test
    void shouldExportPrometheusMetrics() {
        final String prom = management.getForObject("/metrics", String.class);

        assertThat(prom).contains("process_uptime_seconds");
    }

    @Test
    void shouldServeHealthEndpoint() {
        final String health = management.getForObject("/health", String.class);

        assertThat(health).isEqualTo("{\"status\":\"UP\"}");
    }

    @Test
    void shouldServeOpenAPI() {
        final String openapi = api.getForObject("/api/v1/openapi.json", String.class);

        assertThat(openapi).contains("Syndesis Meta Service");
    }
}
