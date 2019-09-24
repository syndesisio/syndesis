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
package io.syndesis.server.metrics.jsondb;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import io.syndesis.common.model.metrics.IntegrationDeploymentMetrics;
import io.syndesis.common.model.metrics.IntegrationMetricsSummary;
import io.syndesis.server.dao.manager.DataManager;
import lombok.Data;

public class IntegrationMetricsHandler {

    private final DataManager dataManager;

    IntegrationMetricsHandler (DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public void persist(IntegrationMetricsSummary currentSummary) {

        IntegrationMetricsSummary existingSummary =
                dataManager.fetch(IntegrationMetricsSummary.class, currentSummary.getId().get());
        if (existingSummary == null) {
            dataManager.create(currentSummary);
        } else if (! existingSummary.equals(currentSummary)) {
            //only write to the DB when the new metrics differs to unnecessary
            //and expensive writes to the DB
            dataManager.update(currentSummary);
        }

    }

    /**
     * Deletes metrics from delete integrations
     *
     * @param activeIntegrationIds
     */
    public void curate(Set<String> activeIntegrationIds) {
        Set<String> summaryIds = dataManager.fetchIds(IntegrationMetricsSummary.class);
        for (String summaryId : summaryIds) {
            if (! activeIntegrationIds.contains(summaryId)) {
                dataManager.delete(IntegrationMetricsSummary.class, summaryId);
            }
        }
    }

    /**
     * Computes the IntegrationMetricsSummary from the RawMetrics available for the
     * current integration.
     *
     * @param integrationId
     * @param metrics
     * @param livePodIds
     * @return
     */
    public IntegrationMetricsSummary compute(
            String integrationId,
            Map<String,RawMetrics> metrics,
            Set<String> livePodIds) {

        Map<String, Metrics> m = new HashMap<>();

        Metrics tm = new Metrics();
        for (RawMetrics raw:metrics.values()) {
            String version = raw.getVersion();
            Metrics mh = m.containsKey(version) ? m.get(version) : new Metrics();
            mh.add(livePodIds, raw);
            m.put(version, mh);
            tm = tm.add(livePodIds, raw);
        }

        List<IntegrationDeploymentMetrics> dmList = new ArrayList<>();
        for (Metrics mh : m.values()) {
            IntegrationDeploymentMetrics dm = new IntegrationDeploymentMetrics.Builder()
                    .version(mh.getVersion())
                    .messages(mh.getMessages())
                    .errors(mh.getErrors())
                    .start(mh.getStartDate())
                    .lastProcessed(mh.getLastProcessed())
                    .uptimeDuration(mh.getStartDate().map(date -> System.currentTimeMillis() - date.getTime()).orElse(0L))
                    .build();
            dmList.add(dm);
        }
        return new IntegrationMetricsSummary.Builder()
                .id(integrationId)
                .messages(tm.getMessages())
                .errors(tm.getErrors())
                .start(tm.getStartDate())
                .lastProcessed(tm.getLastProcessed())
                .uptimeDuration(tm.getStartDate().map(date -> System.currentTimeMillis() - date.getTime()).orElse(0L))
                .integrationDeploymentMetrics(dmList)
                .build();
    }

    @Data
    static class Metrics {

        private Long messages = 0L;
        private Long errors = 0L;
        private Optional<Date> lastProcessed = Optional.empty();
        private Optional<Date> startDate = Optional.empty(); //we may have no more live pods for this integration
        private String version;

        public Metrics add(Set<String> livePodIds, RawMetrics raw) {

            this.version = raw.getVersion();
            this.messages += raw.getMessages();
            this.errors += raw.getErrors();
            //Let's simply grab the oldest living pod, we will need to revisit when doing rolling upgrades etc
            if (livePodIds.contains(raw.getPod())) {
                if (this.startDate.isPresent()) {
                    if (raw.getStartDate().get().before(this.startDate.get())) {
                        this.setStartDate(raw.getStartDate());
                    }
                } else {
                    this.setStartDate(raw.getStartDate());
                }
            }
            if (raw.getLastProcessed().isPresent()) {
                if (this.getLastProcessed().isPresent()) {
                    this.setLastProcessed(raw.getLastProcessed().get().after(this.getLastProcessed().get()) ? raw.getLastProcessed() : this.getLastProcessed());
                } else {
                    this.setLastProcessed(raw.getLastProcessed());
                }
            }
            return this;
        }
    }
}
