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

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionList;
import io.fabric8.kubernetes.api.model.apiextensions.DoneableCustomResourceDefinition;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.syndesis.connector.kafka.model.crd.Kafka;
import io.syndesis.connector.kafka.model.crd.KafkaResourceDoneable;
import io.syndesis.connector.kafka.model.crd.KafkaResourceList;
import io.syndesis.connector.support.verifier.api.PropertyPair;
import io.syndesis.connector.support.verifier.api.SyndesisMetadataProperties;

import org.junit.Test;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KafkaMetaDataRetrievalTest {

    private static final YAMLMapper YAML_MAPPER = new YAMLMapper();

    private static final KafkaResourceList KAFKAS;

    private static final CustomResourceDefinition STRIMZI_KAFKA_CRD;

    private static final CustomResourceDefinition STRIMZI_KAFKA_TOPIC_CRD;

    static {
        STRIMZI_KAFKA_CRD = loadYaml("kafka-crd.yaml", CustomResourceDefinition.class);
        STRIMZI_KAFKA_TOPIC_CRD = loadYaml("kafka-topic-crd.yaml", CustomResourceDefinition.class);
        KAFKAS = loadYaml("kafkas.yaml", KafkaResourceList.class);
    }

    @Test
    public void shouldAssertIfACRDIsKafka() {
        assertThat(KafkaMetaDataRetrieval.isKafkaCustomResourceDefinition(STRIMZI_KAFKA_CRD)).isTrue();
        assertThat(KafkaMetaDataRetrieval.isKafkaCustomResourceDefinition(STRIMZI_KAFKA_TOPIC_CRD)).isFalse();
    }

    @Test
    public void shouldFetchPropertiesFromKafkaCustomResources() {
        final KubernetesClient client = mock(KubernetesClient.class);

        final KafkaMetaDataRetrieval metaDataRetrieval = new KafkaMetaDataRetrieval() {
            @Override
            KubernetesClient createKubernetesClient() {
                return client;
            }
        };

        @SuppressWarnings("unchecked")
        final NonNamespaceOperation<CustomResourceDefinition, CustomResourceDefinitionList, DoneableCustomResourceDefinition, Resource<CustomResourceDefinition, DoneableCustomResourceDefinition>> definitions = mock(
            NonNamespaceOperation.class);
        when(client.customResourceDefinitions()).thenReturn(definitions);
        final CustomResourceDefinitionList values = new CustomResourceDefinitionList();
        values.setItems(Collections.singletonList(STRIMZI_KAFKA_CRD));
        when(definitions.list()).thenReturn(values);

        @SuppressWarnings("unchecked")
        final MixedOperation<Kafka, KafkaResourceList, KafkaResourceDoneable, Resource<Kafka, KafkaResourceDoneable>> operation = mock(
            MixedOperation.class);
        when(client.customResources(STRIMZI_KAFKA_CRD,
            Kafka.class,
            KafkaResourceList.class,
            KafkaResourceDoneable.class)).thenReturn(operation);
        when(operation.inAnyNamespace()).thenReturn(operation);

        when(operation.list()).thenReturn(KAFKAS);

        final SyndesisMetadataProperties properties = metaDataRetrieval.fetchProperties(null, null, null);

        final Map<String, List<PropertyPair>> expected = new HashMap<>();
        expected.put("brokers", Arrays.asList(
            new PropertyPair("my-cluster-kafka-bootstrap.zregvart.svc:9092", "zregvart::my-cluster (plain)"),
            new PropertyPair("my-cluster-kafka-bootstrap.zregvart.svc:9093", "zregvart::my-cluster (tls)"),
            new PropertyPair("zorans-cluster-kafka-bootstrap.zregvart.svc:9092", "zregvart::zorans-cluster (plain)"),
            new PropertyPair("zorans-cluster-kafka-bootstrap.zregvart.svc:9093", "zregvart::zorans-cluster (tls)")
            ));

        assertThat(properties.getProperties()).containsExactlyEntriesOf(expected);
    }

    static <T> T loadYaml(final String resource, final Class<T> type) {
        try (InputStream myCluster = KafkaMetaDataRetrievalTest.class.getResourceAsStream(resource)) {
            return YAML_MAPPER.readValue(myCluster, type);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
