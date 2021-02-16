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
package io.syndesis.connector.kafka.model.crd;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.fabric8.kubernetes.api.model.ObjectMeta;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import static org.assertj.core.api.Assertions.assertThat;

public class KafkaResourceListTest {

    @Test
    public void shouldDeserialize() throws IOException {
        final KafkaResourceList list = new YAMLMapper().readValue(KafkaResourceListTest.class.getResourceAsStream("/io/syndesis/connector/kafka/kafkas.yaml"),
            KafkaResourceList.class);

        assertThat(list).isNotNull();

        final List<Kafka> items = list.getItems();
        assertThat(items).hasSize(2);

        final Status myClusterStatus = new Status(Arrays.asList(
            new Listener("plain", Collections.singletonList(new Address("my-cluster-kafka-bootstrap.zregvart.svc", 9092))),
            new Listener("tls", Collections.singletonList(new Address("my-cluster-kafka-bootstrap.zregvart.svc", 9093)))));
        final Kafka myCluster = new Kafka(myClusterStatus);

        final ObjectMeta myClusterMetadata = myCluster.getMetadata();
        myClusterMetadata.setName("my-cluster");
        myClusterMetadata.setNamespace("zregvart");

        final Status zoransClusterStatus = new Status(Arrays.asList(
            new Listener("plain", Collections.singletonList(new Address("zorans-cluster-kafka-bootstrap.zregvart.svc", 9092))),
            new Listener("tls", Collections.singletonList(new Address("zorans-cluster-kafka-bootstrap.zregvart.svc", 9093)))));
        final Kafka zoransCluster = new Kafka(zoransClusterStatus);

        final ObjectMeta zoransClusterMetadata = zoransCluster.getMetadata();
        zoransClusterMetadata.setName("zorans-cluster");
        zoransClusterMetadata.setNamespace("zregvart");

        assertThat(items).contains(zoransCluster, zoransCluster);
    }
}
