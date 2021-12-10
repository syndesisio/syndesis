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

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import io.syndesis.common.model.metrics.IntegrationDeploymentMetrics;
import io.syndesis.common.model.metrics.IntegrationMetricsSummary;
import io.syndesis.common.util.CollectionsUtils;
import io.syndesis.common.util.KeyGenerator;
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

    public static final String OPERATOR_TOPK = "topk";

    private final String serviceName;
    private final String integrationIdLabel;
    private final String deploymentVersionLabel;
    private final String componentLabel;
    private final String typeLabel;
    private final String metricsHistoryRange;
    private final int topIntegrationsCount;

    private final NamespacedOpenShiftClient openShiftClient;

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
        if (!KeyGenerator.resemblesAKey(integrationId)) {
            throw new IllegalArgumentException("Did not privide an valid integration ID: " + integrationId);
        }

        // aggregate values across versions
        final Map<String, Long> totalMessagesMap = getMetricValues(integrationId, METRIC_TOTAL, deploymentVersionLabel, Long.class, PrometheusMetricsProviderImpl::sum);
        final Map<String, Long> failedMessagesMap = getMetricValues(integrationId, METRIC_FAILED, deploymentVersionLabel, Long.class, PrometheusMetricsProviderImpl::sum);

        final Map<String, Instant> startTimeMap = getMetricValues(integrationId, METRIC_START_TIMESTAMP, deploymentVersionLabel, Instant.class, PrometheusMetricsProviderImpl::max);

        // compute last processed time from lastCompleted and lastFailure times
        final Map<String, Instant> lastCompletedTimeMap = getMetricValues(integrationId, METRIC_COMPLETED_TIMESTAMP, deploymentVersionLabel, Instant.class, PrometheusMetricsProviderImpl::max);
        final Map<String, Instant> lastFailedTimeMap = getMetricValues(integrationId, METRIC_FAILURE_TIMESTAMP, deploymentVersionLabel, Instant.class, PrometheusMetricsProviderImpl::max);
        final Map<String, Instant> lastProcessedTimeMap = Stream.concat(lastCompletedTimeMap.entrySet().stream(), lastFailedTimeMap.entrySet().stream())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, PrometheusMetricsProviderImpl::max));

        final Optional<Instant> startTime = getAggregateMetricValue(integrationId, METRIC_START_TIMESTAMP, Instant.class, "min");

        final Optional<Instant> lastCompletedTime = getAggregateMetricValue(integrationId, METRIC_COMPLETED_TIMESTAMP, Instant.class, "max");
        final Optional<Instant> lastFailureTime = getAggregateMetricValue(integrationId, METRIC_FAILURE_TIMESTAMP, Instant.class, "max");
        final Instant lastProcessedTime = max(lastCompletedTime.orElse(null), lastFailureTime.orElse(null));

        return createIntegrationMetricsSummary(totalMessagesMap, failedMessagesMap,
            startTimeMap, lastProcessedTimeMap, startTime, Optional.ofNullable(lastProcessedTime));
    }

    private static IntegrationMetricsSummary createIntegrationMetricsSummary(Map<String, Long> totalMessagesMap, Map<String, Long> failedMessagesMap,
                                                                      Map<String, Instant> startTimeMap, Map<String, Instant> lastProcessingTimeMap,
                                                                      Optional<Instant> startTime, Optional<Instant> lastProcessedTime) {

        final long[] totalMessages = {0L};
        final long[] totalErrors = {0L};

        final List<IntegrationDeploymentMetrics> deploymentMetrics = totalMessagesMap.entrySet().stream().map(entry -> {
            final String version = entry.getKey();
            final Long messages = entry.getValue();

            final Long errors = failedMessagesMap.get(version);
            final Optional<Instant> start = Optional.ofNullable(startTimeMap.get(version));
            final Optional<Instant> lastProcessed = Optional.ofNullable(lastProcessingTimeMap.get(version));

            // aggregate values while we are at it
            totalMessages[0] = sum(totalMessages[0], messages);
            totalErrors[0] = sum(totalErrors[0], errors);

            return new IntegrationDeploymentMetrics.Builder()
                .version(version)
                .messages(messages)
                .errors(errors)
                // set as long values instead of long and nano parts as the UI don't need to handle the nano part
                .start(start.map(st -> Instant.ofEpochMilli(st.toEpochMilli() * 1000)))
                .lastProcessed(lastProcessed.map(lp -> Instant.ofEpochMilli(lp.toEpochMilli() * 1000)))
                .uptimeDuration(start.map(date -> Duration.between(date, Instant.now()).toMillis()).orElse(0L))
                .build();
        }).sorted(Comparator.comparing(IntegrationDeploymentMetrics::getVersion)).collect(Collectors.toList());

        return new IntegrationMetricsSummary.Builder()
                .metricsProvider("prometheus")
                .integrationDeploymentMetrics(deploymentMetrics)
                // set as long values instead of long and nano parts as the UI don't need to handle the nano part
                .start(startTime.map(st -> Instant.ofEpochMilli(st.toEpochMilli() * 1000)))
                .lastProcessed(lastProcessedTime.map(lp -> Instant.ofEpochMilli(lp.toEpochMilli() * 1000)))
                .uptimeDuration(startTime.map(date -> Duration.between(date, Instant.now()).toMillis()).orElse(0L))
                .messages(totalMessages[0])
                .errors(totalErrors[0])
                .build();
    }

    @Override
    public IntegrationMetricsSummary getTotalIntegrationMetricsSummary() {
        final Optional<Long> totalMessages = getSummaryMetricValue(METRIC_TOTAL, Long.class, "sum");
        final Optional<Long> failedMessages = getSummaryMetricValue(METRIC_FAILED, Long.class, "sum");

        final List<Pod> serverList = openShiftClient.pods().withLabelSelector(SELECTOR).list().getItems();
        final Optional<Instant> startTime;
        if (!serverList.isEmpty()) {
            startTime = Optional.of(Instant.parse(serverList.get(0).getStatus().getStartTime()));
        } else {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Missing syndesis-server pod in lookup with selector " + LABELS);
            }
            startTime = Optional.empty();
        }

        // compute last processed time
        final Optional<Instant> lastCompletedTime = getAggregateMetricValue(METRIC_COMPLETED_TIMESTAMP, Instant.class, "max");
        final Optional<Instant> lastFailureTime = getAggregateMetricValue(METRIC_FAILURE_TIMESTAMP, Instant.class, "max");
        final Optional<Instant> lastProcessedTime = Optional.ofNullable(max(lastCompletedTime.orElse(null), lastFailureTime.orElse(null)));

        // get top 5 integrations by total messages
        return new IntegrationMetricsSummary.Builder()
            .metricsProvider("prometheus")
            // set as long values instead of long and nano parts as the UI don't need to handle the nano part
            .start(startTime.map(st -> Instant.ofEpochMilli(st.toEpochMilli() * 1000)))
            .lastProcessed(lastProcessedTime.map(lp -> Instant.ofEpochMilli(lp.toEpochMilli() * 1000)))
            .uptimeDuration(startTime.map(date -> Duration.between(date, Instant.now()).toMillis()).orElse(0L))
            .messages(totalMessages.orElse(0L))
            .errors(failedMessages.orElse(0L))
            .topIntegrations(getTopIntegrations())
            .build();
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

    private Map<String, Long> getTopIntegrations() {
        HttpQuery queryTotalMessages = new HttpQuery.Builder().createFrom(createInstantHttpQuery(METRIC_TOTAL, OPERATOR_TOPK))
                .addAggregationOperatorParameters(Integer.toString(topIntegrationsCount))
                .addByLabels(integrationIdLabel)
                .build();
        QueryResult response = httpClient.queryPrometheus(queryTotalMessages);
        validateResponse(response);
        return QueryResult.getValueMap(response, integrationIdLabel, Long.class, PrometheusMetricsProviderImpl::sum);
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

    private static long sum(final Long a, final Long b) {
        if (a == null && b == null) {
            return 0;
        }

        if (a == null) {
            return b;
        }

        if (b == null) {
            return a;
        }

        return a + b;
    }

    /**
     * @return more recent of the two dates or null if both are null
     */
    private static Instant max(final Instant a, final Instant b) {
        if (a == null) {
            return b;
        }

        if (b == null) {
            return a;
        }

        return a.compareTo(b) > 0 ? a : b;
    }
}
