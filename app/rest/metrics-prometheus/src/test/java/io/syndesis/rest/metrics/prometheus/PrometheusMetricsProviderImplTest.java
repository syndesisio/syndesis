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
package io.syndesis.rest.metrics.prometheus;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.syndesis.model.metrics.IntegrationMetricsSummary;
import io.syndesis.rest.metrics.MetricsProvider;

import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author dhirajsb
 */
@Ignore ("requires an instrumented pod and prometheus service name")
public class PrometheusMetricsProviderImplTest {

    @Test
    public void getIntegrationMetricsSummary() throws Exception {
        final PrometheusConfigurationProperties config = new PrometheusConfigurationProperties();
        config.setService("syndesis-prometheus-syndesis.192.168.64.20.nip.io");
        MetricsProvider impl = new PrometheusMetricsProviderImpl(config, new DefaultKubernetesClient());
        final IntegrationMetricsSummary summary = impl.getIntegrationMetricsSummary("-L4cW1YAyODR3qQ_6o7v");
        assertThat(summary.getMessages()).isNotNull();
    }

}
