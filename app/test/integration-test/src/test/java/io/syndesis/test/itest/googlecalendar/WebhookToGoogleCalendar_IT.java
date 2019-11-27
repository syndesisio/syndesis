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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.Filter;

import io.syndesis.test.SyndesisTestEnvironment;
import io.syndesis.test.container.integration.SyndesisIntegrationRuntimeContainer;
import io.syndesis.test.itest.SyndesisIntegrationTestSupport;
import io.syndesis.test.itest.sheets.util.GzipServletFilter;

import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.SocketUtils;
import org.testcontainers.Testcontainers;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.endpoint.CitrusEndpoints;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.http.servlet.RequestCachingServletFilter;

@ContextConfiguration(classes = WebhookToGoogleCalendar_IT.EndpointConfig.class)
public class WebhookToGoogleCalendar_IT extends SyndesisIntegrationTestSupport {

    /**
     * Webhook receives POST request and creates a Google Calendar event.
     */
    @ClassRule
    public static SyndesisIntegrationRuntimeContainer integrationContainer = new SyndesisIntegrationRuntimeContainer.Builder()
        .name("webhook-to-googlecalendar")
        .fromExport(WebhookToGoogleCalendar_IT.class.getResource("webhook-to-google-calendar-export"))
        .build()
        .withExposedPorts(SyndesisTestEnvironment.getServerPort(),
            SyndesisTestEnvironment.getManagementPort())
        .withExtraHost("www.googleapis.com", "127.0.0.1");

    @Autowired
    private HttpClient webHookClient;

    @Autowired
    private HttpServer googleApiServer;

    @Configuration
    public static class EndpointConfig {

        static final int GOOGLE_API_SERVER_PORT = SocketUtils.findAvailableTcpPort();

        static {
            Testcontainers.exposeHostPorts(Collections.singletonMap(GOOGLE_API_SERVER_PORT, 443));
        }

        @Bean
        public HttpServer googleApiServer() {
            final Map<String, Filter> filterMap = new LinkedHashMap<>();
            filterMap.put("request-caching-filter", new RequestCachingServletFilter());
            filterMap.put("gzip-filter", new GzipServletFilter());

            final HttpConfiguration https = new HttpConfiguration();
            https.addCustomizer(new SecureRequestCustomizer());

            final SslContextFactory ssl = new SslContextFactory();
            ssl.setKeyStoreResource(Resource.newClassPathResource("www.googleapis.com.p12"));
            ssl.setKeyStorePassword("password");

            final ServerConnector sslConnector = new ServerConnector(new Server(),
                new SslConnectionFactory(ssl, "http/1.1"),
                new HttpConnectionFactory(https));
            sslConnector.setPort(GOOGLE_API_SERVER_PORT);

            return CitrusEndpoints.http()
                .server()
                .connector(sslConnector)
                .port(GOOGLE_API_SERVER_PORT)
                .autoStart(true)
                .timeout(60000L)
                .filters(filterMap)
                .build();
        }

        @Bean
        public HttpClient webHookClient() {
            return CitrusEndpoints.http().client()
                .requestUrl(String.format("http://localhost:%s/webhook/token", integrationContainer.getServerPort()))
                .build();
        }
    }

    @Test
    @CitrusTest
    public void shouldCreateGoogleCalendarEvent(@CitrusResource final TestRunner runner) {
        runner.http(builder -> builder.client(webHookClient)
            .send()
            .post()
            .payload("{\"name\":\"Event name\",\"date\":\"1997-08-29\",\"time\":\"02:14:00\"}"));

        runner.http(builder -> builder.client(webHookClient)
            .receive()
            .response(HttpStatus.NO_CONTENT));

        runner.http(builder -> builder.server(googleApiServer)
            .receive()
            .post("/calendar/v3/calendars/calendar-id/events")
            .payload(
                "{\"attendees\":[],\"end\":{\"date\":\"1997-08-29T02:14:00.000Z\"},\"start\":{\"date\":\"1997-08-29T02:14:00.000Z\"},\"summary\":\"Event name\"}"));

        runner.http(builder -> builder.server(googleApiServer)
            .send()
            .response(HttpStatus.OK));
    }

    @Test
    @CitrusTest
    public void waitForIntegrationToBeHealthy(@CitrusResource final TestRunner runner) {
        runner.waitFor().http()
            .method(HttpMethod.GET)
            .seconds(10L)
            .status(HttpStatus.OK)
            .url(String.format("http://localhost:%s/health", integrationContainer.getManagementPort()));
    }
}
