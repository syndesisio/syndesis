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
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.SECONDS;

import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.PodListBuilder;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.syndesis.server.jsondb.GetOptions;
import io.syndesis.server.jsondb.impl.SqlJsonDB;
import io.syndesis.server.openshift.OpenShiftService;

import org.h2.jdbcx.JdbcDataSource;
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

    private SqlJsonDB jsondb;
    private DBI dbi;

    @Before
    public void before() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:t;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
        this.dbi = new DBI(ds);
        this.jsondb = new SqlJsonDB(dbi, null);
        this.jsondb.createTables();
    }

    @Test
    public void testLogsController() throws IOException {

        final String expectedDBState = resource("logs-controller-db.json").trim();
        final String podLogs = resource("test-pod-x23x.txt");

        final KubernetesClient client = mock(KubernetesClient.class);
        when(client.getConfiguration()).thenReturn(new ConfigBuilder().withMasterUrl("http://master").build());
        try (ActivityTrackingController controller = new ActivityTrackingController(jsondb, dbi, client) {

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
                    handler.accept(new ByteArrayInputStream(podLogs.getBytes(StandardCharsets.UTF_8)));
                });
            }

        }) {

            controller.setStartupDelay("0 seconds");
            controller.setRetention("1000000000 days");
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
