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
package io.syndesis.rest.metrics.prometheus;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import io.syndesis.model.metrics.IntegrationMetricsSummary;
import io.syndesis.rest.metrics.MetricsProvider;

@Component
@ConditionalOnProperty(value = "metrics.kind", havingValue = "prometheus")
public class PrometheusMetricsProviderImpl implements MetricsProvider {

    private final String serviceName;

    private final HttpClient httpClient = new HttpClient();

    protected PrometheusMetricsProviderImpl(PrometheusConfigurationProperties config) {
        this.serviceName = config.getService();
    }

    @Override
    public IntegrationMetricsSummary getIntegrationMetricsSummary(String integrationId) {

        // aggregate values across versions
        final Optional<Map<String, Long>> totalMessagesMap = getMetricValues(integrationId,"org_apache_camel_ExchangesTotal", "syndesis_io_deployment_id", Long.class);
        final Optional<Map<String, Long>> failedMessagesMap = getMetricValues(integrationId, "org_apache_camel_ExchangesFailed", "syndesis_io_deployment_id", Long.class);
        final Optional<Map<String, Date>> startTimeMap = getMetricValues(integrationId, "org_apache_camel_StartTimestamp", "syndesis_io_deployment_id", Date.class);
        final Optional<Map<String, Date>> lastProcessingTimeMap = getMetricValues(integrationId, "org_apache_camel_LastExchangeCompletedTimestamp", "syndesis_io_deployment_id", Date.class);

        Optional<Long> totalMessages = totalMessagesMap.map(Map::values).flatMap(
            longs -> Optional.of(longs.stream().mapToLong(Long::longValue).sum()));
        Optional<Long> failedMessages = failedMessagesMap.map(Map::values).flatMap(
            longs -> Optional.of(longs.stream().mapToLong(Long::longValue).sum()));
        Optional<Date> startTime = startTimeMap.map(Map::values).flatMap(
            dates -> dates.stream().max(Date::compareTo));
        Optional<Date> lastProcessingTime = lastProcessingTimeMap.map(Map::values).flatMap(
            dates -> dates.stream().max(Date::compareTo));

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

    private <T> Optional<Map<String, T>> getMetricValues(String integrationId, String metric, String label, Class<? extends T> clazz) {
        HttpQuery queryTotalMessages = createHttpQuery(integrationId, metric);
        QueryResult response = httpClient.queryPrometheus(queryTotalMessages);
        validateResponse(response);
        return QueryResult.getValueMap(response, label, clazz);
    }

    private <T> Optional<T> getMetricValue(String integrationId, String metric, Class<? extends T> clazz) {
        HttpQuery queryTotalMessages = createHttpQuery(integrationId, metric);
        QueryResult response = httpClient.queryPrometheus(queryTotalMessages);
        validateResponse(response);
        return QueryResult.getFirstValue(response, clazz);
    }

    private HttpQuery createHttpQuery(String integrationId, String metric) {
        return new HttpQuery.Builder()
                .host(serviceName)
                .metric(metric)
                .addLabelValues(HttpQuery.LabelValue.Builder.of(integrationId, "syndesis_io_integration_id"))
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
