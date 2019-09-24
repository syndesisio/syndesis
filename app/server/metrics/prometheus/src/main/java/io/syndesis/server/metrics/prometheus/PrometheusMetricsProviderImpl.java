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
package io.syndesis.server.metrics.prometheus;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import io.syndesis.common.model.metrics.IntegrationDeploymentMetrics;
import io.syndesis.common.model.metrics.IntegrationMetricsSummary;
import io.syndesis.common.util.CollectionsUtils;
import io.syndesis.common.util.SyndesisServerException;
import io.syndesis.server.endpoint.metrics.MetricsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "metrics.kind", havingValue = "prometheus")
public class PrometheusMetricsProviderImpl implements MetricsProvider {
    private static final Logger LOG = LoggerFactory.getLogger(PrometheusMetricsProviderImpl.class);

    private static final String METRIC_TOTAL = "org_apache_camel_ExchangesTotal";
    private static final String METRIC_FAILED = "org_apache_camel_ExchangesFailed";
    private static final String METRIC_START_TIMESTAMP = "io_syndesis_camel_StartTimestamp";
    private static final String METRIC_COMPLETED_TIMESTAMP = "io_syndesis_camel_LastExchangeCompletedTimestamp";
    private static final String METRIC_FAILURE_TIMESTAMP = "io_syndesis_camel_LastExchangeFailureTimestamp";

    private static final String FUNCTION_MAX_OVER_TIME = "max_over_time";
    private static final String VALUE_CONTEXT = "context";
    private static final String VALUE_INTEGRATION = "integration";

    private static final BinaryOperator<Long> SUM_LONGS = (aLong, aLong2) -> aLong == null
        ? aLong2
        : aLong2 == null ? aLong : aLong + aLong2;
    private static final BinaryOperator<Date> MAX_DATE = (date1, date2) -> date1 == null
        ? date2
        : date2 == null ? date1 : date1.after(date2) ? date1 : date2;
    public static final String OPERATOR_TOPK = "topk";

    private final String serviceName;
    private final String integrationIdLabel;
    private final String deploymentVersionLabel;
    private final String componentLabel;
    private final String typeLabel;
    private final String metricsHistoryRange;
    private final int topIntegrationsCount;

    private final NamespacedOpenShiftClient openShiftClient;
    private final DateFormat dateFormat = //2018-03-14T23:34:09Z
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);

    static final Map<String,String> LABELS = CollectionsUtils.immutableMapOf(
        "syndesis.io/app", "syndesis",
        "syndesis.io/component", "syndesis-server"
    );

    private static final LabelSelector SELECTOR = new LabelSelector(null, LABELS);

    private volatile HttpClient httpClient;

    protected PrometheusMetricsProviderImpl(PrometheusConfigurationProperties config, NamespacedOpenShiftClient openShiftClient) {
        this.serviceName = config.getService();
        this.integrationIdLabel = config.getIntegrationIdLabel();
        this.deploymentVersionLabel = config.getDeploymentVersionLabel();
        this.componentLabel = config.getComponentLabel();
        this.typeLabel = config.getTypeLabel();
        this.metricsHistoryRange = config.getMetricsHistoryRange();
        this.topIntegrationsCount = config.getTopIntegrationsCount();
        this.openShiftClient = openShiftClient;
    }

    @PostConstruct
    public void init() {
        if (this.httpClient == null) {
            this.httpClient = new HttpClient();
        }

    }

    @PreDestroy
    public void destroy() {
        if (this.httpClient != null) {
            this.httpClient.close();
            this.httpClient = null;
        }
    }

    @Override
    public IntegrationMetricsSummary getIntegrationMetricsSummary(String integrationId) {

        // aggregate values across versions
        final Map<String, Long> totalMessagesMap = getMetricValues(integrationId, METRIC_TOTAL, deploymentVersionLabel, Long.class, SUM_LONGS);
        final Map<String, Long> failedMessagesMap = getMetricValues(integrationId, METRIC_FAILED, deploymentVersionLabel, Long.class, SUM_LONGS);

        final Map<String, Date> startTimeMap = getMetricValues(integrationId, METRIC_START_TIMESTAMP, deploymentVersionLabel, Date.class, MAX_DATE);

        // compute last processed time from lastCompleted and lastFailure times
        final Map<String, Date> lastCompletedTimeMap = getMetricValues(integrationId, METRIC_COMPLETED_TIMESTAMP, deploymentVersionLabel, Date.class, MAX_DATE);
        final Map<String, Date> lastFailedTimeMap = getMetricValues(integrationId, METRIC_FAILURE_TIMESTAMP, deploymentVersionLabel, Date.class, MAX_DATE);
        final Map<String, Date> lastProcessedTimeMap = Stream.concat(lastCompletedTimeMap.entrySet().stream(), lastFailedTimeMap.entrySet().stream())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, MAX_DATE));

        final Optional<Date> startTime = getAggregateMetricValue(integrationId, METRIC_START_TIMESTAMP, Date.class, "min");

        final Optional<Date> lastCompletedTime = getAggregateMetricValue(integrationId, METRIC_COMPLETED_TIMESTAMP, Date.class, "max");
        final Optional<Date> lastFailureTime = getAggregateMetricValue(integrationId, METRIC_FAILURE_TIMESTAMP, Date.class, "max");
        final Date lastProcessedTime = MAX_DATE.apply(lastCompletedTime.orElse(null), lastFailureTime.orElse(null));

        return createIntegrationMetricsSummary(totalMessagesMap, failedMessagesMap,
            startTimeMap, lastProcessedTimeMap, startTime, Optional.ofNullable(lastProcessedTime));
    }

    private IntegrationMetricsSummary createIntegrationMetricsSummary(Map<String, Long> totalMessagesMap, Map<String, Long> failedMessagesMap,
                                                                      Map<String, Date> startTimeMap, Map<String, Date> lastProcessingTimeMap,
                                                                      Optional<Date> startTime, Optional<Date> lastProcessedTime) {

        final long[] totalMessages = {0L};
        final long[] totalErrors = {0L};

        final List<IntegrationDeploymentMetrics> deploymentMetrics = totalMessagesMap.entrySet().stream().map(entry -> {
            final String version = entry.getKey();
            final Long messages = entry.getValue();

            final Long errors = failedMessagesMap.get(version);
            final Optional<Date> start = Optional.ofNullable(startTimeMap.get(version));
            final Optional<Date> lastProcessed = Optional.ofNullable(lastProcessingTimeMap.get(version));

            // aggregate values while we are at it
            totalMessages[0] = SUM_LONGS.apply(totalMessages[0], messages);
            totalErrors[0] = SUM_LONGS.apply(totalErrors[0], errors);

            return new IntegrationDeploymentMetrics.Builder()
                .version(version)
                .messages(messages)
                .errors(errors)
                .start(start)
                .lastProcessed(lastProcessed)
                .uptimeDuration(start.map(date -> System.currentTimeMillis() - date.getTime()).orElse(0L))
                .build();
        }).sorted(Comparator.comparing(IntegrationDeploymentMetrics::getVersion)).collect(Collectors.toList());

        return new IntegrationMetricsSummary.Builder()
                .metricsProvider("prometheus")
                .integrationDeploymentMetrics(deploymentMetrics)
                .start(startTime)
                .lastProcessed(lastProcessedTime)
                .uptimeDuration(startTime.map(date -> System.currentTimeMillis() - date.getTime()).orElse(0L))
                .messages(totalMessages[0])
                .errors(totalErrors[0])
                .build();
    }

    @Override
    public IntegrationMetricsSummary getTotalIntegrationMetricsSummary() {
        final Optional<Long> totalMessages = getSummaryMetricValue(METRIC_TOTAL, Long.class, "sum");
        final Optional<Long> failedMessages = getSummaryMetricValue(METRIC_FAILED, Long.class, "sum");

         try {
             final List<Pod> serverList = openShiftClient.pods().withLabelSelector(SELECTOR).list().getItems();
             final Optional<Date> startTime;
             if (!serverList.isEmpty()) {
                 startTime = Optional.of(dateFormat.parse(serverList.get(0).getStatus().getStartTime()));
             } else {
                 if (LOG.isWarnEnabled()) {
                     LOG.warn("Missing syndesis-server pod in lookup with selector " + LABELS);
                 }
                 startTime = Optional.empty();
             }

            // compute last processed time
            final Optional<Date> lastCompletedTime = getAggregateMetricValue(METRIC_COMPLETED_TIMESTAMP, Date.class, "max");
            final Optional<Date> lastFailureTime = getAggregateMetricValue(METRIC_FAILURE_TIMESTAMP, Date.class, "max");
            final Optional<Date> lastProcessedTime = Optional.ofNullable(MAX_DATE.apply(lastCompletedTime.orElse(null), lastFailureTime.orElse(null)));

            // get top 5 integrations by total messages
            return new IntegrationMetricsSummary.Builder()
                .metricsProvider("prometheus")
                .start(startTime)
                .lastProcessed(lastProcessedTime)
                .uptimeDuration(startTime.map(date -> System.currentTimeMillis() - date.getTime()).orElse(0L))
                .messages(totalMessages.orElse(0L))
                .errors(failedMessages.orElse(0L))
                .topIntegrations(getTopIntegrations())
                .build();
         } catch (ParseException e) {
             throw new SyndesisServerException(e.getMessage(),e);
         }
    }

    private <T> Map<String, T> getMetricValues(String integrationId, String metric, String label, Class<? extends T> clazz, BinaryOperator<T> mergeFunction) {
        HttpQuery queryTotalMessages = createSummaryHttpQuery(integrationId, metric, null);
        QueryResult response = httpClient.queryPrometheus(queryTotalMessages);
        validateResponse(response);
        return QueryResult.getValueMap(response, label, clazz, mergeFunction);
    }

    private <T> Optional<T> getSummaryMetricValue(String metric, Class<? extends T> clazz, String aggregationOperator) {
        HttpQuery queryTotalMessages = createSummaryHttpQuery(metric, aggregationOperator);
        QueryResult response = httpClient.queryPrometheus(queryTotalMessages);
        validateResponse(response);
        return QueryResult.getFirstValue(response, clazz);
    }

    private <T> Optional<T> getAggregateMetricValue(String metric, Class<? extends T> clazz, String aggregationOperator) {
        HttpQuery queryTotalMessages = createInstantHttpQuery(metric, aggregationOperator);
        QueryResult response = httpClient.queryPrometheus(queryTotalMessages);
        validateResponse(response);
        return QueryResult.getFirstValue(response, clazz);
    }

    private <T> Optional<T> getAggregateMetricValue(String integrationId, String metric, Class<? extends T> clazz, String aggregationOperator) {
        HttpQuery queryTotalMessages = createInstantHttpQuery(integrationId, metric, aggregationOperator);
        QueryResult response = httpClient.queryPrometheus(queryTotalMessages);
        validateResponse(response);
        return QueryResult.getFirstValue(response, clazz);
    }

    @SuppressWarnings("unchecked")
    private <T> Map<String, Long> getTopIntegrations() {
        HttpQuery queryTotalMessages = new HttpQuery.Builder().createFrom(createInstantHttpQuery(METRIC_TOTAL, OPERATOR_TOPK))
                .addAggregationOperatorParameters(Integer.toString(topIntegrationsCount))
                .addByLabels(integrationIdLabel)
                .build();
        QueryResult response = httpClient.queryPrometheus(queryTotalMessages);
        validateResponse(response);
        return QueryResult.getValueMap(response, integrationIdLabel, Long.class, SUM_LONGS);
    }

    private HttpQuery createSummaryHttpQuery(String integrationId, String metric, String aggregationOperator) {
        return new HttpQuery.Builder().createFrom(createInstantHttpQuery(integrationId, metric, aggregationOperator))
                .function(FUNCTION_MAX_OVER_TIME)
                .range(metricsHistoryRange)
                .build();
    }

    private HttpQuery createSummaryHttpQuery(String metric, String aggregationOperator) {
        return new HttpQuery.Builder().createFrom(createInstantHttpQuery(metric, aggregationOperator))
                .function(FUNCTION_MAX_OVER_TIME)
                .range(metricsHistoryRange)
                .build();
    }

    private HttpQuery createInstantHttpQuery(String integrationId, String metric, String aggregationOperator) {
        return new HttpQuery.Builder().createFrom(createInstantHttpQuery(metric, aggregationOperator))
                .addLabelValues(integrationId, this.integrationIdLabel)
                .build();
    }

    private HttpQuery createInstantHttpQuery(String metric, String aggregationOperator) {
        return new HttpQuery.Builder()
                .host(serviceName)
                .metric(metric)
                .aggregationOperator(Optional.ofNullable(aggregationOperator))
                .addLabelValues(VALUE_INTEGRATION, this.componentLabel)
                .addLabelValues(VALUE_CONTEXT, this.typeLabel)
                .build();
    }

    private static void validateResponse(QueryResult response) {
        if (response.isError()) {
            throw new IllegalArgumentException(
                String.format("Error Type: %s, Error: %s", response.getErrorType(), response.getError()));
        }
    }
}
