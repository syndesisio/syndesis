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

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.internal.PodOperationsImpl;
import io.fabric8.kubernetes.client.utils.HttpClientUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Provides some enriched operations against a KubernetesClient.
 */
public class KubernetesSupport {

    private static final Set<String> BLACKLISTED_CONTAINERS = Collections.singleton("istio-proxy");

    private static final Logger LOG = LoggerFactory.getLogger(KubernetesSupport.class);

    private final KubernetesClient client;
    private final OkHttpClient okHttpClient;
    /** Read timeout for the HTTP client, we expect to receive at least one log line in 30 mins. */
    private Duration readTimeout = Duration.ofMinutes(35);

    public KubernetesSupport(KubernetesClient client) {
        this.client = client;
        this.okHttpClient = HttpClientUtils.createHttpClient(this.client.getConfiguration());
    }


    /*
     * Feeds the controller of the given podName to the callback handler for processing.
     *
     * We do this instead of using the watchLog() feature of the k8s client lib because it really sucks due to:
     *  1. You can't configure the timestamps option or the sinceTime option.  Need to resume log downloads.
     *  2. It seems to need extra threads..
     *  3. It might be hiding some of the http failure conditions.
     *
     */
    protected void watchLog(String podName, Consumer<InputStream> handler, String sinceTime, Executor executor) throws IOException {
        try {
            PodOperationsImpl pod = (PodOperationsImpl) client.pods().withName(podName);

            List<Container> containers = pod.get().getSpec().getContainers();
            String containerFilter = getSpecificUserContainer(containers)
                .map(n -> "&container=" + n)
                .orElse("");

            StringBuilder url = new StringBuilder()
                .append(pod.getResourceUrl().toString())
                .append("/log?pretty=false&follow=true&timestamps=true")
                .append(containerFilter);

            if (sinceTime != null) {
                url.append("&sinceTime=").append(sinceTime);
            }
            String podLogUrl = url.toString();

            Thread.currentThread().setName("Logs Controller [running], request: " + podLogUrl);
            Request request = new Request.Builder().url(new URL(podLogUrl)).get().tag("log-watcher").build();
            OkHttpClient clone = okHttpClient.newBuilder()
                .readTimeout(readTimeout.toMillis(), TimeUnit.MILLISECONDS)
                .build();
            clone.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    LOG.info("Failure occurred getting  controller for pod: {},", podName, e);
                    handler.accept(null);
                }

                @Override
                public void onResponse(final Call call, final Response response) throws IOException {
                    executor.execute(() -> {
                        Thread.currentThread().setName("Logs Controller [running], streaming: " + podLogUrl);
                        try {
                            if (response.code() == 200) {
                                handler.accept(response.body().byteStream());
                            } else {
                                LOG.info("Failure occurred while processing controller for pod: {}, http status: {}, details: {}", podName, response.code(), response.body().string());
                                handler.accept(null);
                            }
                        } catch (SocketTimeoutException timeout) {
                            LOG.warn("Timed out reading the log stream");
                            LOG.debug("Timed out reading the log stream", timeout);
                        } catch (IOException e) {
                            LOG.error("Unexpected Error", e);
                        } finally {
                            Thread.currentThread().setName(ActivityTrackingController.IDLE_THREAD_NAME);
                        }
                    });
                }
            });
        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") RuntimeException t) {
            throw new IOException("Unexpected Error", t);
        } finally {
            Thread.currentThread().setName(ActivityTrackingController.IDLE_THREAD_NAME);
        }
    }

    protected Optional<String> getSpecificUserContainer(List<Container> containers) {
        if (containers.size() <= 1) {
            // implicit
            return Optional.empty();
        }
        return containers.stream().map(Container::getName)
            .filter(n -> !BLACKLISTED_CONTAINERS.contains(n))
            .findFirst();
    }

    public void setReadTimeout(final Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    void cancelAllRequests() {
        final Dispatcher dispatcher = okHttpClient.dispatcher();
        dispatcher.cancelAll();
    }
}
