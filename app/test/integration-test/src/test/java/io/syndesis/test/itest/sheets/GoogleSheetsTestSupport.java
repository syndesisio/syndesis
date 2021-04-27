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

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.Filter;

import io.syndesis.test.itest.SyndesisIntegrationTestSupport;
import io.syndesis.test.itest.sheets.util.GzipServletFilter;

import org.springframework.util.SocketUtils;
import org.testcontainers.Testcontainers;

import com.consol.citrus.dsl.endpoint.CitrusEndpoints;
import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.http.servlet.RequestCachingServletFilter;

public class GoogleSheetsTestSupport extends SyndesisIntegrationTestSupport {

    static final int GOOGLE_SHEETS_SERVER_PORT = SocketUtils.findAvailableTcpPort();
    static {
        Testcontainers.exposeHostPorts(GOOGLE_SHEETS_SERVER_PORT);
    }

    protected HttpServer googleSheetsApiServer = startup(googleSheetsApiServer());

    private static final HttpServer googleSheetsApiServer() {
        Map<String, Filter> filterMap = new LinkedHashMap<>();
        filterMap.put("request-caching-filter", new RequestCachingServletFilter());
        filterMap.put("gzip-filter", new GzipServletFilter());

        final HttpServer server = CitrusEndpoints.http()
                .server()
                .port(GOOGLE_SHEETS_SERVER_PORT)
                .autoStart(true)
                .timeout(60000L)
                .filters(filterMap)
                .build();

        return server;
    }

}
