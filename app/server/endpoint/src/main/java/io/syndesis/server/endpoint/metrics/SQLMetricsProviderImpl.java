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
package io.syndesis.server.endpoint.metrics;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.metrics.IntegrationMetricsSummary;
import io.syndesis.server.dao.manager.DataManager;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "metrics.kind", havingValue = "sql")
public class SQLMetricsProviderImpl implements MetricsProvider {

    static final Map<String,String> LABELS = new HashMap<>();
    static {
        LABELS.put("app", "syndesis");
        LABELS.put("component", "syndesis-server");
    }
    private static final LabelSelector SELECTOR = new LabelSelector(null, LABELS);

    private final DataManager dataMgr;

    private final NamespacedOpenShiftClient openShiftClient;

    protected SQLMetricsProviderImpl(DataManager dataMgr, NamespacedOpenShiftClient openShiftClient) {
        this.dataMgr = dataMgr;
        this.openShiftClient = openShiftClient;
    }

    @Override
    public IntegrationMetricsSummary getIntegrationMetricsSummary(String integrationId) {
        return dataMgr.fetch(IntegrationMetricsSummary.class, integrationId);
    }

    @Override
    public IntegrationMetricsSummary getTotalIntegrationMetricsSummary() {
        ListResult<IntegrationMetricsSummary> integrationMetricsList = dataMgr.fetchAll(
                IntegrationMetricsSummary.class);
        return rollup(integrationMetricsList.getItems());
    }

    private IntegrationMetricsSummary rollup(List<IntegrationMetricsSummary> metricsSummaryList) {
        Long totalMessages = 0L;
        Long totalErrors = 0L;
        Optional<Instant> totalLastProcessed = Optional.empty();
        Optional<Instant> totalStart = Optional.empty();
        for (IntegrationMetricsSummary summary : metricsSummaryList) {
            totalMessages += summary.getMessages();
            totalErrors += summary.getErrors();
            if (totalLastProcessed.isPresent()) {
                totalLastProcessed = summary.getLastProcessed().isPresent() &&
                        totalLastProcessed.get().isBefore(summary.getLastProcessed().get()) ?
                                totalLastProcessed : summary.getLastProcessed();
            } else {
                totalLastProcessed = summary.getLastProcessed();
            }

            totalStart = Optional.of(Instant.parse(
                openShiftClient.pods().withLabelSelector(SELECTOR).list().getItems()
                    .get(0)
                    .getStatus()
                    .getStartTime()));
        }
        return new IntegrationMetricsSummary.Builder()
                .metricsProvider("sql")
                .messages(totalMessages)
                .errors(totalErrors)
                .lastProcessed(totalLastProcessed)
                .uptimeDuration(totalStart.map(date -> Duration.between(date, Instant.now()).toMillis()).orElse(0L))
                .start(totalStart)
                .build();
    }
}
