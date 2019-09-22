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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import io.syndesis.server.dao.manager.DataManager;
import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.metrics.IntegrationMetricsSummary;
import io.syndesis.common.util.SyndesisServerException;

@Component
@ConditionalOnProperty(value = "metrics.kind", havingValue = "sql")
public class SQLMetricsProviderImpl implements MetricsProvider {

    private final DateFormat dateFormat = //2018-03-14T23:34:09Z
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.US);
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
        Optional<Date> totalLastProcessed = Optional.empty();
        Optional<Date> totalStart = Optional.empty();
        for (IntegrationMetricsSummary summary : metricsSummaryList) {
            totalMessages += summary.getMessages();
            totalErrors += summary.getErrors();
            if (totalLastProcessed.isPresent()) {
                totalLastProcessed = summary.getLastProcessed().isPresent() &&
                        totalLastProcessed.get().before(summary.getLastProcessed().get()) ?
                                totalLastProcessed : summary.getLastProcessed();
            } else {
                totalLastProcessed = summary.getLastProcessed();
            }
            try {
                totalStart = Optional.of(dateFormat.parse(
                    openShiftClient.pods().withLabelSelector(SELECTOR).list().getItems()
                        .get(0)
                        .getStatus()
                        .getStartTime()));

            } catch (ParseException e) {
                throw new SyndesisServerException(e.getMessage(),e);
            }
        }
        return new IntegrationMetricsSummary.Builder()
                .metricsProvider("sql")
                .messages(totalMessages)
                .errors(totalErrors)
                .lastProcessed(totalLastProcessed)
                .uptimeDuration(totalStart.map(date -> System.currentTimeMillis() - date.getTime()).orElse(0L))
                .start(totalStart)
                .build();
    }
}
