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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.sun.net.httpserver.HttpServer;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.NamespacedKubernetesClient;

import org.junit.After;
import org.junit.AssumptionViolatedException;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitStrategyTarget;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import eu.rekawek.toxiproxy.Proxy;
import eu.rekawek.toxiproxy.ToxiproxyClient;
import eu.rekawek.toxiproxy.model.ToxicDirection;

public class KubernetesSupportIT {

    private static final int TOXIPROXY_API_PORT = 8474;

    private static final class CheckSinglePort extends HostPortWaitStrategy {
        private final int port;

        public CheckSinglePort(final int port) {
            this.port = port;
        }

        @Override
        protected Set<Integer> getLivenessCheckPorts() {
            final WaitStrategyTarget defaultTarget = waitStrategyTarget;
            waitStrategyTarget = new WaitStrategyTarget() {

                @Override
                public String getContainerId() {
                    return defaultTarget.getContainerId();
                }

                @Override
                public InspectContainerResponse getContainerInfo() {
                    return defaultTarget.getContainerInfo();
                }

                @Override
                public List<Integer> getExposedPorts() {
                    return Collections.singletonList(port);
                }
            };
            return Collections.singleton(toxiproxy.getMappedPort(port));
        }
    }

    @ClassRule
    public static GenericContainer<?> toxiproxy = new GenericContainer<>("shopify/toxiproxy:2.1.3").withExposedPorts(TOXIPROXY_API_PORT, 80).withNetwork(Network.SHARED)
        .waitingFor(new CheckSinglePort(TOXIPROXY_API_PORT));

    final AtomicInteger count = new AtomicInteger(0);

    final AtomicBoolean running = new AtomicBoolean(false);

    HttpServer server;

    private Proxy proxy;

    @Before
    public void setup() throws IOException {
        server = HttpServer.create(new InetSocketAddress("0.0.0.0", 0), 0);
        server.start();

        running.set(true);
        server.createContext("/api/v1/namespaces/syndesis/pods/pod/log", exchange -> {
            exchange.sendResponseHeaders(200, 0);
            try (PrintStream body = new PrintStream(exchange.getResponseBody())) {
                while (running.get()) {
                    body.println(count.incrementAndGet());
                    body.flush();
                    Thread.sleep(1000);
                }
            } catch (final InterruptedException ignored) {
            }
        });

        final int port = server.getAddress().getPort();
        final InetAddress addr = determineAccessibleAddress(port);
        final String upstream = addr.getHostAddress() + ":" + port;
        proxy = new ToxiproxyClient("localhost", toxiproxy.getMappedPort(TOXIPROXY_API_PORT)).createProxy("http", "0.0.0.0:80", upstream);
    }

    @Test
    public void shouldTolerateNetworkTimeouts() throws IOException {
        final String masterUrl = "http://" + toxiproxy.getContainerIpAddress() + ":" + toxiproxy.getMappedPort(80);
        final Config config = new ConfigBuilder().withMasterUrl(masterUrl).withNamespace("syndesis").build();

        final Thread outer = Thread.currentThread();
        final AtomicReference<AssertionError> error = new AtomicReference<>();
        final AtomicInteger counter = new AtomicInteger(-1);

        try (final NamespacedKubernetesClient client = new DefaultKubernetesClient(config)) {
            final KubernetesSupport kubernetesSupport = new KubernetesSupport(client);

            final Consumer<InputStream> handler = stream -> {
                try {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            final int number = Integer.parseInt(line);
                            if (counter.get() == -1) {
                                counter.set(number);
                            } else {
                                assertThat(counter.compareAndSet(number - 1, number)).as("Skipped over line").isTrue();
                            }
                            if (number > 5) {
                                proxy.toxics().timeout("half open", ToxicDirection.DOWNSTREAM, 0);
                            }
                        }
                    } catch (final IOException e) {
                        fail("Unable to read log lines", e);
                    }
                } catch (final AssertionError e) {
                    error.set(e);
                    outer.interrupt();
                }
            };

            final ExecutorService threads = Executors.newFixedThreadPool(2);
            threads.submit(() -> {
                try {
                    kubernetesSupport.watchLog("pod", handler, null, threads);
                } catch (IOException e) {
                    error.set(new AssertionError(e));
                }
            });
        }

        try {
            Thread.sleep(15 * 1000);
        } catch (final InterruptedException ignored) {
            final AssertionError assertionError = error.get();
            if (assertionError != null) {
                throw assertionError;
            }
        }

        assertThat(counter.get()).isGreaterThan(10);
    }

    @After
    public void stopServer() throws IOException {
        running.compareAndSet(true, false);
        server.stop(0);

        // testcontainers rule get's interrupted by JUnit runner and
        // subsequently fails the test, this is why we manually stop the
        // container here so we can handle the interrupted exception
        final ExecutorService shutdown = Executors.newSingleThreadExecutor();
        shutdown.submit(toxiproxy::stop);
        try {
            shutdown.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
        }
    }

    private static InetAddress determineAccessibleAddress(final int port) throws IOException {
        for (final NetworkInterface inf : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            for (final InetAddress addr : Collections.list(inf.getInetAddresses())) {
                if (!addr.isLoopbackAddress()) {
                    try {
                        final ExecResult result = toxiproxy.execInContainer("/usr/bin/nc", "-zv", addr.getHostAddress() + ":" + port);
                        if (result.getStderr().contains("open")) {
                            return addr;
                        }
                    } catch (UnsupportedOperationException | InterruptedException ignored) {
                        continue;
                    }
                }
            }
        }

        throw new AssumptionViolatedException("Server is bound to a unreachable address from the toxiproxy container");
    }
}
