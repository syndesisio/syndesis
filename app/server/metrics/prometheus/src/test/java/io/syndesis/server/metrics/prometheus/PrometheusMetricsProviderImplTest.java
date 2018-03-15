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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import io.syndesis.common.model.metrics.IntegrationDeploymentMetrics;
import io.syndesis.common.model.metrics.IntegrationMetricsSummary;

/**
 * @author dhirajsb
 */
@Ignore ("requires an instrumented pod and prometheus service name")
public class PrometheusMetricsProviderImplTest {

    public static final String TEST_INTEGRATION_ID = "-L5SIZLIuUOBiQou7QTw";
    private PrometheusMetricsProviderImpl metricsProvider;

    @Before
    public void setUp() {
        final PrometheusConfigurationProperties config = new PrometheusConfigurationProperties();
        config.setService("syndesis-prometheus-syndesis.192.168.64.22.nip.io");
        NamespacedOpenShiftClient openshiftClient = mock(NamespacedOpenShiftClient.class);
        when(openshiftClient.pods().withName("syndesis-server")
                .get()
                .getStatus()
                .getStartTime()).thenReturn("2018-03-14T23:34:09Z");

        metricsProvider = new PrometheusMetricsProviderImpl(config, openshiftClient);
    }

    @Test
    public void testDateConverter() throws Exception {
        final Date value = Utils.getObjectReader().forType(Date.class).readValue("\"1518048434686\"");
        assertThat(value).isNotNull();
    }

    @Test
    public void testGetIntegrationMetricsSummary() throws Exception {
        final IntegrationMetricsSummary summary = metricsProvider.getIntegrationMetricsSummary(TEST_INTEGRATION_ID);
        assertThat(summary.getMessages()).isNotNull();
        final Optional<List<IntegrationDeploymentMetrics>> deploymentMetrics = summary
            .getIntegrationDeploymentMetrics();
        assertThat(deploymentMetrics).isNotEmpty().map(List::isEmpty).hasValue(false);
    }

    @Test
    public void testGetTotalIntegrationMetricsSummary() throws Exception {
        final IntegrationMetricsSummary summary = metricsProvider.getTotalIntegrationMetricsSummary();
        assertThat(summary.getMessages()).isNotNull();
        final Optional<List<IntegrationDeploymentMetrics>> deploymentMetrics = summary
            .getIntegrationDeploymentMetrics();
        assertThat(deploymentMetrics).isEmpty();
    }
}
