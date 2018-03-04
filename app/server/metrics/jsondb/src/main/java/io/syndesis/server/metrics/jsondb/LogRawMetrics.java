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

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogRawMetrics implements RawMetricsHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogRawMetrics.class);

    @Override
    public void persist(RawMetrics rawMetrics) {
        LOGGER.info("Persist rawMetrics for integrationId: {}. Version: {}. Pod: {}. Messages: {}. Errors {}. ResetDate:{}",
                rawMetrics.getIntegrationId(), "1", rawMetrics.getPod(),
                rawMetrics.getMessages(), rawMetrics.getErrors(), rawMetrics.getResetDate());
    }

    @Override
    public Map<String, RawMetrics> getRawMetrics(String integrationId) throws IOException {
        LOGGER.info("GetRawMetrics for integrationId {}", integrationId);
        return null;
    }

    @Override
    public void curate(String integrationId, Map<String, RawMetrics> metrics, Set<String> livePodIds)
            throws IOException {
        LOGGER.info("Curate DeadPodMetrics for integrationId {}", integrationId);
    }

    @Override
    public void curate(Set<String> activeIntegrationIds) {
        LOGGER.info("Curate DeletedIntegrationMetrics");
    }
}
