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
package io.syndesis.rest.metrics;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.internal.readiness.Readiness;
import io.syndesis.model.metrics.IntegrationMetricsSummary;
import io.syndesis.rest.metrics.prometheus.HttpClient;
import io.syndesis.rest.metrics.prometheus.HttpQuery;
import io.syndesis.rest.metrics.prometheus.QueryResult;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "metrics.kind", havingValue = "prometheus")
@EnableConfigurationProperties({PrometheusConfigurationProperties.class})
public class PrometheusMetricsProviderImpl implements MetricsProvider {

    private final String serviceName;
    private final KubernetesClient kubernetes;

    private final HttpClient httpClient = new HttpClient();

    protected PrometheusMetricsProviderImpl(PrometheusConfigurationProperties config, KubernetesClient kubernetes) {
        this.serviceName = config.getService();
        this.kubernetes = kubernetes;
    }

    @Override
    public IntegrationMetricsSummary getIntegrationMetricsSummary(String integrationId) {

        // map integration Id to pod name
        List<Pod> integrationPodList = kubernetes.pods().withLabel("integration").list().getItems();
        final List<Pod> pods = integrationPodList.stream()
            .filter(p -> Readiness.isReady(p)
                && integrationId.equals(p.getMetadata().getAnnotations().get("syndesis.io/integration-id")))
            .collect(Collectors.toList());

        if (pods.isEmpty()) {
            throw new IllegalArgumentException("Missing pod with integration id " + integrationId);
        }

        final String podName = pods.get(0).getMetadata().getName();
        final Optional<Long> totalMessages = getMetricValue(podName, "org_apache_camel_ExchangesTotal", Long.class);
        final Optional<Long> failedMessages = getMetricValue(podName, "org_apache_camel_ExchangesFailed", Long.class);
        final Optional<Date> startTime = getMetricValue(podName, "org_apache_camel_StartTimestamp", Date.class);
        final Optional<Date> lastProcessingTime = getMetricValue(podName, "org_apache_camel_LastExchangeCompletedTimestamp", Date.class);

        return new IntegrationMetricsSummary.Builder()
                .start(startTime)
                .lastProcessed(lastProcessingTime)
                .messages(totalMessages.orElse(0L))
                .errors(failedMessages.orElse(0L))
                .build();
    }

    @Override
    public IntegrationMetricsSummary getTotalIntegrationMetricsSummary() {
        throw new UnsupportedOperationException();
    }

    private <T> Optional<T> getMetricValue(String podName, String metric, Class<? extends T> clazz) {
        HttpQuery queryTotalMessages = createHttpQuery(podName, metric);
        QueryResult response = httpClient.queryPrometheus(queryTotalMessages);
        validateResponse(response);
        return QueryResult.getResponseValue(response, clazz);
    }

    private HttpQuery createHttpQuery(String podName, String metric) {
        return new HttpQuery.Builder()
                .host(serviceName)
                .metric(metric)
                .addLabelValues(HttpQuery.LabelValue.Builder.of(podName, "integration"))
                .addLabelValues(HttpQuery.LabelValue.Builder.of("context", "type"))
                .build();
    }

    private static void validateResponse(QueryResult response) {
        if (response.isError()) {
            throw new IllegalArgumentException(
                String.format("Error Type: %s, Error: %s", response.getErrorType(), response.getError()));
        }
    }

}
