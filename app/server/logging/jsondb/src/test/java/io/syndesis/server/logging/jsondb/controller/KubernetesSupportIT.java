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

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.sun.net.httpserver.HttpServer;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.NamespacedKubernetesClient;
import io.syndesis.server.jsondb.JsonDB;
import io.syndesis.server.openshift.OpenShiftService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class KubernetesSupportIT {

    final AtomicInteger count = new AtomicInteger(0);

    final AtomicBoolean running = new AtomicBoolean(false);

    HttpServer server;

    @Before
    public void setup() throws IOException {
        server = HttpServer.create(new InetSocketAddress("0.0.0.0", 0), 0);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        running.set(true);
        server.createContext("/api/v1/namespaces/syndesis/pods/pod/log", exchange -> {
            exchange.sendResponseHeaders(200, 0);
            try (PrintStream body = new PrintStream(exchange.getResponseBody())) {
                while (running.get()) {
                    int current = count.incrementAndGet();
                    body.println(current);
                    body.flush();

                    if (current > 5) {
                        // provoke a timeout after 5 lines sent, with the sleep
                        // below it waits for 1.6 sec
                        Thread.sleep(600);
                    }
                    Thread.sleep(1000);
                }
            } catch (final InterruptedException ignored) {
                return;
            } finally {
                exchange.close();
            }
        });
    }

    @Test
    public void shouldTolerateNetworkTimeouts() throws IOException, InterruptedException, ExecutionException {
        final String masterUrl = "http://0.0.0.0:" + server.getAddress().getPort();
        final Config config = new ConfigBuilder().withMasterUrl(masterUrl).withNamespace("syndesis").build();

        final AtomicInteger counter = new AtomicInteger(-1);

        try (final NamespacedKubernetesClient client = new DefaultKubernetesClient(config)) {
            final Phaser phaser = new Phaser(2);

            final Pod pod = new PodBuilder()//
                .withNewMetadata()//
                .withName("pod")//
                .addToLabels(OpenShiftService.INTEGRATION_ID_LABEL, "id")//
                .addToLabels(OpenShiftService.DEPLOYMENT_VERSION_LABEL, "1")//
                .endMetadata()//
                .withNewStatus()//
                .withPhase("Running")//
                .endStatus()//
                .build();

            final JsonDB jsondb = mock(JsonDB.class);
            final DBI dbi = mock(DBI.class);

            // we're using the mock HTTP server to emulate K8S API server, but
            // only for the log endpoint, so we need to stub out some of the
            // calls that would go to the live server and replace the
            // PodLogMonitor with test version
            final ActivityTrackingController controller = new ActivityTrackingController(jsondb, dbi, client) {
                @Override
                protected boolean isPodRunning(String name) {
                    return true;
                }

                @Override
                protected PodList listPods() {
                    final PodList podList = new PodList();
                    podList.setItems(Collections.singletonList(pod));

                    return podList;
                }

                @Override
                protected PodLogMonitor createLogMonitor(Pod pod) {
                    return new TestPodLogMonitor(this, pod, counter, phaser);
                }
            };
            // we change the timeout to 1.5 sec so we don't have to wait for the
            // default 35min, above we add a timeout after 5 lines of 1.6 sec
            controller.kubernetesSupport.setReadTimeout(Duration.ofMillis(1500));
            controller.open();

            // we should have received > 5 log lines
            phaser.arriveAndAwaitAdvance();
            assertThat(counter.get()).isGreaterThanOrEqualTo(5);

            // after 5 sec delay controller reschedules
            // KubernetesSupport::watchLog, some lines are skipped during this,
            // but for this test we don't care about that
            phaser.arriveAndAwaitAdvance();
            assertThat(counter.get()).isGreaterThan(10);
        }
    }

    @After
    public void stopServer() throws IOException {
        running.compareAndSet(true, false);
        server.stop(0);
    }

}
