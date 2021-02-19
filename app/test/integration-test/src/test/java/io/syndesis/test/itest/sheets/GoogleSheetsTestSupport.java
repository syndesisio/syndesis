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

import javax.servlet.Filter;
import javax.sql.DataSource;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import com.consol.citrus.TestCaseRunner;
import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.http.server.HttpServerBuilder;
import com.consol.citrus.http.servlet.RequestCachingServletFilter;
import io.syndesis.test.SyndesisTestEnvironment;
import io.syndesis.test.itest.SyndesisIntegrationTestSupport;
import io.syndesis.test.itest.sheets.util.GzipServletFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.SocketUtils;
import org.testcontainers.Testcontainers;

import static com.consol.citrus.actions.ExecuteSQLAction.Builder.sql;
import static com.consol.citrus.actions.PurgeEndpointAction.Builder.purgeEndpoints;

/**
 * @author Christoph Deppisch
 */
@ContextConfiguration(classes = GoogleSheetsTestSupport.EndpointConfig.class)
public class GoogleSheetsTestSupport extends SyndesisIntegrationTestSupport {

    static final int GOOGLE_SHEETS_SERVER_PORT = SocketUtils.findAvailableTcpPort();
    static {
        Testcontainers.exposeHostPorts(GOOGLE_SHEETS_SERVER_PORT);
    }

    @Autowired
    protected DataSource sampleDb;

    @Autowired
    protected HttpServer googleSheetsApiServer;

    @Configuration
    public static class EndpointConfig {

        @Bean
        public HttpServer googleSheetsApiServer() {
            Map<String, Filter> filterMap = new LinkedHashMap<>();
            filterMap.put("request-caching-filter", new RequestCachingServletFilter());
            filterMap.put("gzip-filter", new GzipServletFilter());

            return new HttpServerBuilder()
                    .port(GOOGLE_SHEETS_SERVER_PORT)
                    .autoStart(true)
                    .timeout(Duration.ofSeconds(SyndesisTestEnvironment.getDefaultTimeout()).toMillis())
                    .filters(filterMap)
                    .build();
        }
    }

    protected void cleanupDatabase(TestCaseRunner runner) {
        runner.given(purgeEndpoints()
                    .endpoint(googleSheetsApiServer));

        runner.given(sql(sampleDb)
            .statement("delete from contact"));
    }
}
