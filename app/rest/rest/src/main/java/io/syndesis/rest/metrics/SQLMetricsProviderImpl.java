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

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import io.syndesis.dao.manager.DataManager;
import io.syndesis.model.ListResult;
import io.syndesis.model.metrics.IntegrationMetricsSummary;

@Component
@ConditionalOnProperty(value = "metrics.kind", havingValue = "sql")
public class SQLMetricsProviderImpl implements MetricsProvider {

    private final DataManager dataMgr;

    protected SQLMetricsProviderImpl(DataManager dataMgr) {
        this.dataMgr = dataMgr;
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
            //grab longest living integration pod
            if (totalStart.isPresent()) {
                totalStart = summary.getStart().isPresent() &&
                        summary.getStart().get().before(totalStart.get()) ?
                                summary.getStart() : totalStart;
            } else {
                totalStart = summary.getStart();
            }
        }
        return new IntegrationMetricsSummary.Builder()
                .messages(totalMessages)
                .errors(totalErrors)
                .lastProcessed(totalLastProcessed)
                .start(totalStart)
                .build();
    }
}
