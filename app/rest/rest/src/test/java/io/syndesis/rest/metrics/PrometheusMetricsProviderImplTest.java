package io.syndesis.rest.metrics;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.syndesis.model.metrics.IntegrationMetricsSummary;

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
