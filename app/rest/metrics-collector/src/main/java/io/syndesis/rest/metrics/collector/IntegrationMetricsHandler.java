package io.syndesis.rest.metrics.collector;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import io.syndesis.dao.manager.DataManager;
import io.syndesis.model.metrics.IntegrationMetricsSummary;

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

        Long messages = 0L;
        Long errors = 0L;
        Optional<Date> lastProcessed = Optional.empty();
        Optional<Date> startDate = Optional.empty(); //we may have no more live pods for this integration

        for (RawMetrics raw:metrics.values()) {
            messages += raw.getMessages();
            errors += raw.getErrors();
            //Let's simply grab the oldest living pod, we will need to revisit when doing rolling upgrades etc
            if (livePodIds.contains(raw.getPod())) {
                if (startDate.isPresent()) {
                    if (raw.getStartDate().get().before(startDate.get())) {
                        startDate = raw.getStartDate();
                    }
                } else {
                    startDate = raw.getStartDate();
                }
            }
            if (raw.getLastProcessed().isPresent()) {
                if (lastProcessed.isPresent()) {
                    lastProcessed = raw.getLastProcessed().get().after(lastProcessed.get()) ? raw.getLastProcessed() : lastProcessed;
                } else {
                    lastProcessed = raw.getLastProcessed();
                }
            }
        }

        return new IntegrationMetricsSummary.Builder()
                .id(integrationId)
                .messages(messages)
                .errors(errors)
                .start(startDate)
                .lastProcessed(lastProcessed)
                .build();
    }
}
