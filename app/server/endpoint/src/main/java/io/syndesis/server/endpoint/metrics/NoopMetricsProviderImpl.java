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

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import io.syndesis.common.model.metrics.IntegrationMetricsSummary;

@Component
@ConditionalOnProperty(value = "metrics.kind", havingValue = "noop", matchIfMissing=true)
public class NoopMetricsProviderImpl implements MetricsProvider {

    @Override
    public IntegrationMetricsSummary getIntegrationMetricsSummary(String integrationId) {
        return null;
    }

    @Override
    public IntegrationMetricsSummary getTotalIntegrationMetricsSummary() {
        return new IntegrationMetricsSummary.Builder()
                .metricsProvider("noop")
                .messages(0L)
                .errors(0L)
                .build();
    }
}
