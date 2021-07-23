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
package io.syndesis.connector.kafka;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.syndesis.connector.kafka.service.KafkaBrokerService;
import org.apache.camel.Endpoint;
import org.apache.camel.component.kafka.KafkaConfiguration;
import org.apache.camel.component.kafka.KafkaEndpoint;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class KafkaConnectorExtraOptionsTest extends KafkaConnectorTestSupport {

    @Test
    public void testExtraOptions() throws Exception {
        context().start();
        Optional<Endpoint> endpoint = context().getEndpoints().stream().filter(e -> e instanceof KafkaEndpoint).findFirst();
        Assertions.assertThat(endpoint.isPresent()).isTrue();
        KafkaConfiguration kafkaConfiguration = ((KafkaEndpoint) endpoint.get()).getConfiguration();
        assertThat(kafkaConfiguration.getAutoOffsetReset()).isEqualTo("earliest");
        assertThat(kafkaConfiguration.getCheckCrcs()).isEqualTo(false);
        assertThat(kafkaConfiguration.getAutoCommitIntervalMs()).isEqualTo(5);
        assertThat(kafkaConfiguration.getSessionTimeoutMs()).isEqualTo(60001);
    }

    @Override
    protected Map<String, String> connectorParameters() {
        Map<String, String> params = new HashMap<>();
        params.put(KafkaBrokerService.BROKERS, "test:9092");
        params.put("extraOptions", "[{\"key\":\"AAA\",\"value\":\"BBB\"}," +
            "{\"key\":\"autoOffsetReset\",\"value\":\"earliest\"}," +
            "{\"key\":\"sessionTimeoutMs\",\"value\":\"60001\"}," +
            "{\"key\":\"checkCrcs\",\"value\":\"false\"},  " +
            "{\"key\":\"auto.commit.interval.ms\",\"value\":\"5 \"}]");
        return params;
    }
}
