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
package io.syndesis.server.logging.jsondb.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.fasterxml.jackson.databind.JsonNode;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.PodListBuilder;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.syndesis.common.util.Json;
import io.syndesis.common.util.KeyGenerator;
import io.syndesis.server.jsondb.GetOptions;
import io.syndesis.server.jsondb.JsonDB;
import io.syndesis.server.jsondb.impl.SqlJsonDB;
import io.syndesis.server.openshift.OpenShiftService;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Used to unit test the LogsController implementation.
 */
public class ActivityTrackingControllerTest {

    private static final class StubbedController extends ActivityTrackingController {
        private final InputStream podLogs;

        private long time;

        private StubbedController(JsonDB jsondb, DBI dbi, KubernetesClient client, InputStream podLogs, long time) {
            super(jsondb, dbi, client);
            this.podLogs = podLogs;
            this.time = time;
        }

        @Override
        protected PodList listPods() {
            return new PodListBuilder()
                .addNewItem()
                .withNewMetadata()
                .withName("test-pod-x23x")
                .addToLabels(OpenShiftService.COMPONENT_LABEL, "integration")
                .addToLabels(OpenShiftService.DEPLOYMENT_VERSION_LABEL, "3")
                .addToLabels(OpenShiftService.INTEGRATION_ID_LABEL, "my-integration")
                .endMetadata()
                .withNewStatus()
                .withPhase("Running")
                .endStatus()
                .endItem().build();
        }

        @Override
        protected boolean isPodRunning(String name) {
            return true;
        }

        @Override
        protected void watchLog(String podName, Consumer<InputStream> handler, String sinceTime) throws IOException {
            execute("test", ()->{
                handler.accept(podLogs);
            });
        }

        @Override
        protected PodLogMonitor createLogMonitor(Pod pod) {
            return new PodLogMonitor(this, pod) {
                @Override
                long now() {
                    return time;
                }
            };
        }
    }

    private SqlJsonDB jsondb;
    private DBI dbi;

    private KubernetesClient client;

    @Before
    public void before() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:t;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
        this.dbi = new DBI(ds);
        this.jsondb = new SqlJsonDB(dbi, null);
        this.jsondb.createTables();

        client = mock(KubernetesClient.class);
        when(client.getConfiguration()).thenReturn(new ConfigBuilder().withMasterUrl("http://master").build());
    }

    @After
    public void destroyEverything() {
        jsondb.dropTables();
    }

    @Test
    public void testLogsController() throws IOException {
        final String expectedDBState = resource("logs-controller-db.json").trim();
        final String podLogs = resource("test-pod-x23x.txt");
        final InputStream podLogsStream = new ByteArrayInputStream(podLogs.getBytes(StandardCharsets.UTF_8));

        try (ActivityTrackingController controller = new StubbedController(jsondb, dbi, client, podLogsStream, ZonedDateTime.parse("2018-01-12T21:22:02.068338027Z").toInstant().toEpochMilli())) {

            controller.setStartupDelay("0 seconds");
            controller.setRetention(Integer.MAX_VALUE);
            controller.open();

            // Eventually all the log data should make it into the jsondb
            given().await()
                .atMost(20, SECONDS)
                .pollInterval(1, SECONDS)
                .untilAsserted(() -> {
                    String db = jsondb.getAsString("/", new GetOptions().prettyPrint(true));
                    assertThat(db).isEqualTo(expectedDBState);
                });
        }

    }

    @Test
    public void shouldRetainLastRetainActivityLogs() throws IOException {
        try (InputStream podLogsStream = logStream(1100);
            ActivityTrackingController controller = new StubbedController(jsondb, dbi, client, podLogsStream, System.currentTimeMillis())) {

            controller.setStartupDelay("0 seconds");
            controller.setCleanUpInterval("15 minutes");
            controller.open();

            // Eventually all the log data should make it into the jsondb
            given().await()
                .atMost(20, SECONDS)
                .pollInterval(1, SECONDS)
                .untilAsserted(() -> {
                    final String json = jsondb.getAsString("/", new GetOptions().prettyPrint(true));
                    assertThat(json).isNotNull();
                    final JsonNode tree = Json.reader().readTree(json);
                    assertThat(tree.get("activity").get("exchanges").get("my-integration").size()).isGreaterThan(1000);
                });

            controller.cleanupLogs();
            final String json = jsondb.getAsString("/", new GetOptions().prettyPrint(true));
            assertThat(json).isNotNull();
            final JsonNode tree = Json.reader().readTree(json);
            assertThat(tree.get("activity").get("exchanges").get("my-integration").size()).isLessThanOrEqualTo(controller.getRetention());
        }
    }

    private static String timestamp() {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC));
    }

    @SuppressWarnings("InputStreamSlowMultibyteRead")
    private static InputStream logStream(final int totalExchanges) {
        final InputStream podLogsStream = new InputStream() {
            int exchanges;

            int idx;

            byte[] buffy;

            String exchange;

            final Supplier<byte[]> lineSupplier = () ->
            (timestamp() + " {\"exchange\":\"" + exchange + "\",\"status\":\"begin\"}\n"
             + timestamp() + " {\"exchange\":\"" + exchange + "\",\"step\":\"s2\",\"id\":\"i-L38cZ5Jd1L876xV4vEHz\",\"message\":\"Hello World\"}\n"
             + timestamp() + " {\"exchange\":\"" + exchange + "\",\"step\":\"s2\",\"id\":\"i-L38cZ5Jd1L876xV4vEGz\",\"duration\":582977}\n"
             + timestamp() + " {\"exchange\":\"" + exchange + "\",\"step\":\"s3\",\"id\":\"-L38cZ5Nd1L876xV4vEI\",\"duration\":18087}\n"
             + timestamp() + " {\"exchange\":\"" + exchange + "\",\"step\":\"s4\",\"id\":\"i-L38cZ5Od1L876xV4vEJz\",\"duration\":494949}\n"
             + timestamp() + " {\"exchange\":\"" + exchange + "\",\"status\":\"done\",\"failed\":false}\n"
            ).getBytes(StandardCharsets.UTF_8);

            @Override
            public int read() throws IOException {
                if (buffy == null || buffy.length == idx) {
                    if (exchanges++ > totalExchanges) {
                        return -1;
                    }

                    exchange = KeyGenerator.createKey();
                    buffy = lineSupplier.get();
                    idx = 0;
                }

                return buffy[idx++];
            }
        };
        return podLogsStream;
    }

    private static String resource(String file) throws IOException {
        try (InputStream is = requireNonNull(ActivityTrackingControllerTest.class.getClassLoader().getResourceAsStream(file)) ) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            copy(is, os);
            return new String(os.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    private static void copy(InputStream is, ByteArrayOutputStream os) throws IOException {
        int c;
        while( (c=is.read())>=0 ) {
            os.write(c);
        }
    }

}
