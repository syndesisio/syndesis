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

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import io.syndesis.dao.manager.DataManager;
import io.syndesis.model.integration.Integration;
import io.syndesis.model.metrics.IntegrationDeploymentMetrics;
import io.syndesis.model.metrics.IntegrationMetricsSummary;
import io.syndesis.rest.metrics.MetricsProvider;

@Component
@ConditionalOnProperty(value = "metrics.kind", havingValue = "prometheus")
public class PrometheusMetricsProviderImpl implements MetricsProvider {

    private static final String METRIC_TOTAL = "org_apache_camel_ExchangesTotal";
    private static final String METRIC_FAILED = "org_apache_camel_ExchangesFailed";
    private static final String METRIC_START_TIMESTAMP = "io_syndesis_camel_StartTimestamp";
    private static final String METRIC_COMPLETED_TIMESTAMP = "io_syndesis_camel_LastExchangeCompletedTimestamp";
    private static final String METRIC_FAILURE_TIMESTAMP = "io_syndesis_camel_LastExchangeFailureTimestamp";

    private static final String FUNCTION_MAX_OVER_TIME = "max_over_time";
    private static final String VALUE_CONTEXT = "context";
    private static final String LABEL_TYPE = "type";
    private static final String VALUE_INTEGRATION = "integration";
    private static final String LABEL_COMPONENT = "component";

    private static final BinaryOperator<Long> SUM_LONGS = (aLong, aLong2) -> aLong == null ? aLong2 :
        aLong2 == null ? aLong : aLong + aLong2;
    private static final BinaryOperator<Date> MAX_DATE = (date1, date2) -> date1 == null ? date2 :
        date2 == null ? date1 : date1.after(date2) ? date1 : date2;
    private static final BinaryOperator<Date> MIN_DATE = (date1, date2) -> date1 == null ? date2 :
        date2 == null ? date1 : date1.before(date2) ? date1 : date2;

    private final HttpClient httpClient = new HttpClient();
    private final DataManager dataManager;

    private final String serviceName;
    private final String integrationIdLabel;
    private final String deploymentVersionLabel;
    private final String metricsHistoryRange;

    protected PrometheusMetricsProviderImpl(PrometheusConfigurationProperties config, DataManager dataManager) {
        this.serviceName = config.getService();
        this.integrationIdLabel = config.getIntegrationIdLabel();
        this.deploymentVersionLabel = config.getDeploymentVersionLabel();
        this.metricsHistoryRange = config.getMetricsHistoryRange();
        this.dataManager = dataManager;
    }

    @Override
    public IntegrationMetricsSummary getIntegrationMetricsSummary(String integrationId) {

        // aggregate values across versions
        final Map<String, Long> totalMessagesMap = getMetricValues(integrationId, METRIC_TOTAL,
            deploymentVersionLabel, Long.class, SUM_LONGS);
        final Map<String, Long> failedMessagesMap = getMetricValues(integrationId, METRIC_FAILED, deploymentVersionLabel, Long.class, SUM_LONGS);

        final Map<String, Date> startTimeMap = getMetricValues(integrationId, METRIC_START_TIMESTAMP,
            deploymentVersionLabel, Date.class, MAX_DATE);

        // compute last processed time from lastCompleted and lastFailure times
        final Map<String, Date> lastCompletedTimeMap = getMetricValues(integrationId, METRIC_COMPLETED_TIMESTAMP, deploymentVersionLabel, Date.class, MAX_DATE);
        final Map<String, Date> lastFailedTimeMap = getMetricValues(integrationId, METRIC_FAILURE_TIMESTAMP, deploymentVersionLabel, Date.class, MAX_DATE);
        final Map<String, Date> lastProcessedTimeMap = Stream.concat(lastCompletedTimeMap.entrySet().stream(), lastFailedTimeMap.entrySet().stream())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, MAX_DATE));

        final Optional<Date> startTime = getCurrentMetricValue(integrationId, METRIC_START_TIMESTAMP, Date.class, MIN_DATE);

        final Optional<Date> lastCompletedTime = getCurrentMetricValue(integrationId, METRIC_COMPLETED_TIMESTAMP, Date.class, MAX_DATE);
        final Optional<Date> lastFailureTime = getCurrentMetricValue(integrationId, METRIC_FAILURE_TIMESTAMP, Date.class, MAX_DATE);
        final Date lastProcessedTime = MAX_DATE.apply(lastCompletedTime.orElse(null), lastFailureTime.orElse(null));

        return createIntegrationMetricsSummary(totalMessagesMap, failedMessagesMap,
            startTimeMap, lastProcessedTimeMap, startTime,Optional.ofNullable(lastProcessedTime));
    }

    private IntegrationMetricsSummary createIntegrationMetricsSummary(Map<String, Long> totalMessagesMap, Map<String, Long> failedMessagesMap,
                                                                      Map<String, Date> startTimeMap, Map<String, Date> lastProcessingTimeMap,
                                                                      Optional<Date> startTime, Optional<Date> lastProcessingTime) {

        final long[] totalMessages = {0L};
        final long[] totalErrors = {0L};

        final List<IntegrationDeploymentMetrics> deploymentMetrics = totalMessagesMap.entrySet().stream().map(entry -> {
            final String version = entry.getKey();
            final Long messages = entry.getValue();

            final Long errors = failedMessagesMap.get(version);
            final Date start = startTimeMap.get(version);
            final Date lastProcessed = lastProcessingTimeMap.get(version);

            // aggregate values while we are at it
            totalMessages[0] = SUM_LONGS.apply(totalMessages[0], messages);
            totalErrors[0] = SUM_LONGS.apply(totalErrors[0], errors);

            return new IntegrationDeploymentMetrics.Builder()
                .version(version)
                .messages(messages)
                .errors(errors)
                .start(Optional.ofNullable(start))
                .lastProcessed(Optional.ofNullable(lastProcessed))
                .build();
        }).sorted(Comparator.comparing(IntegrationDeploymentMetrics::getVersion)).collect(Collectors.toList());

        return new IntegrationMetricsSummary.Builder()
                .integrationDeploymentMetrics(deploymentMetrics)
                .start(startTime)
                .lastProcessed(lastProcessingTime)
                .messages(totalMessages[0])
                .errors(totalErrors[0])
                .build();
    }

    @Override
    public IntegrationMetricsSummary getTotalIntegrationMetricsSummary() {
        // get list of current integration ids, since Prometheus db has deleted integrations too
        final Set<String> currentIds = dataManager.fetchIds(Integration.class);

        final Map<String, Long> totalMessagesMap = getMetricValues(METRIC_TOTAL, integrationIdLabel, Long.class, SUM_LONGS);
        final Map<String, Long> failedMessagesMap = getMetricValues(METRIC_FAILED, integrationIdLabel, Long.class, SUM_LONGS);

        final Optional<Date> startTime = getCurrentMetricValue(METRIC_START_TIMESTAMP, Date.class, MIN_DATE);

        // compute last processed time
        final Optional<Date> lastCompletedTime = getCurrentMetricValue(METRIC_COMPLETED_TIMESTAMP, Date.class, MAX_DATE);
        final Optional<Date> lastFailureTime = getCurrentMetricValue(METRIC_FAILURE_TIMESTAMP, Date.class, MAX_DATE);
        final Date lastProcessedTime = MAX_DATE.apply(lastCompletedTime.orElse(null), lastFailureTime.orElse(null));

        return new IntegrationMetricsSummary.Builder()
            .start(startTime)
            .lastProcessed(Optional.ofNullable(lastProcessedTime))
            .messages(totalMessagesMap.entrySet().stream().filter(entry -> currentIds.contains(entry.getKey())).mapToLong(Map.Entry::getValue).sum())
            .errors(failedMessagesMap.entrySet().stream().filter(entry -> currentIds.contains(entry.getKey())).mapToLong(Map.Entry::getValue).sum())
            .build();
    }

    private <T> Map<String, T> getMetricValues(String integrationId, String metric, String label, Class<? extends T> clazz, BinaryOperator<T> mergeFunction) {
        HttpQuery queryTotalMessages = createSummaryHttpQuery(integrationId, metric);
        QueryResult response = httpClient.queryPrometheus(queryTotalMessages);
        validateResponse(response);
        return QueryResult.getValueMap(response, label, clazz, mergeFunction);
    }

    private <T> Map<String, T> getMetricValues(String metric, String label, Class<? extends T> clazz, BinaryOperator<T> mergeFunction) {
        HttpQuery queryTotalMessages = createSummaryHttpQuery(metric);
        QueryResult response = httpClient.queryPrometheus(queryTotalMessages);
        validateResponse(response);
        return QueryResult.getValueMap(response, label, clazz, mergeFunction);
    }

    private <T> Optional<T> getCurrentMetricValue(String integrationId, String metric, Class<? extends T> clazz, BinaryOperator<T> mergeFunction) {
        HttpQuery queryTotalMessages = createInstantHttpQuery(integrationId, metric);
        QueryResult response = httpClient.queryPrometheus(queryTotalMessages);
        validateResponse(response);
        return QueryResult.getMergedValue(response, clazz, mergeFunction);
    }

    private <T> Optional<T> getCurrentMetricValue(String metric, Class<? extends T> clazz, BinaryOperator<T> mergeFunction) {
        HttpQuery queryTotalMessages = createInstantHttpQuery(metric);
        QueryResult response = httpClient.queryPrometheus(queryTotalMessages);
        validateResponse(response);
        return QueryResult.getMergedValue(response, clazz, mergeFunction);
    }

    private HttpQuery createSummaryHttpQuery(String integrationId, String metric) {
        return new HttpQuery.Builder()
                .host(serviceName)
                .function(FUNCTION_MAX_OVER_TIME)
                .metric(metric)
                .addLabelValues(HttpQuery.LabelValue.Builder.of(integrationId, this.integrationIdLabel))
                .addLabelValues(HttpQuery.LabelValue.Builder.of(VALUE_CONTEXT, LABEL_TYPE))
                .range(metricsHistoryRange)
                .build();
    }

    private HttpQuery createSummaryHttpQuery(String metric) {
        return new HttpQuery.Builder()
            .host(serviceName)
            .function(FUNCTION_MAX_OVER_TIME)
            .metric(metric)
            .addLabelValues(HttpQuery.LabelValue.Builder.of(VALUE_INTEGRATION, LABEL_COMPONENT))
            .addLabelValues(HttpQuery.LabelValue.Builder.of(VALUE_CONTEXT, LABEL_TYPE))
            .range(metricsHistoryRange)
            .build();
    }

    private HttpQuery createInstantHttpQuery(String integrationId, String metric) {
        return new HttpQuery.Builder()
                .host(serviceName)
                .metric(metric)
                .addLabelValues(HttpQuery.LabelValue.Builder.of(integrationId, this.integrationIdLabel))
                .addLabelValues(HttpQuery.LabelValue.Builder.of(VALUE_CONTEXT, LABEL_TYPE))
                .build();
    }

    private HttpQuery createInstantHttpQuery(String metric) {
        return new HttpQuery.Builder()
                .host(serviceName)
                .metric(metric)
                .addLabelValues(HttpQuery.LabelValue.Builder.of(VALUE_INTEGRATION, LABEL_COMPONENT))
                .addLabelValues(HttpQuery.LabelValue.Builder.of(VALUE_CONTEXT, LABEL_TYPE))
                .build();
    }

    private static void validateResponse(QueryResult response) {
        if (response.isError()) {
            throw new IllegalArgumentException(
                String.format("Error Type: %s, Error: %s", response.getErrorType(), response.getError()));
        }
    }

}
