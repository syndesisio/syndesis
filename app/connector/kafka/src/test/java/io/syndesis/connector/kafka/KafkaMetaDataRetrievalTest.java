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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionList;
import io.fabric8.kubernetes.api.model.apiextensions.v1.DoneableCustomResourceDefinition;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.V1ApiextensionAPIGroupDSL;
import io.fabric8.kubernetes.client.V1beta1ApiextensionAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.ApiextensionsAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.syndesis.connector.kafka.model.crd.Kafka;
import io.syndesis.connector.kafka.model.crd.KafkaResourceDoneable;
import io.syndesis.connector.kafka.model.crd.KafkaResourceList;
import io.syndesis.connector.support.verifier.api.PropertyPair;
import io.syndesis.connector.support.verifier.api.SyndesisMetadataProperties;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KafkaMetaDataRetrievalTest {

    private static final KafkaResourceList KAFKAS = loadYaml("kafkas.yaml", KafkaResourceList.class);

    @Test
    public void dontLoadKafkaBrokersFromCR() {
        final KafkaMetaDataRetrieval metaDataRetrieval = new KafkaMetaDataRetrieval() {
            @Override
            KubernetesClient createKubernetesClient() {
                return client(new KafkaResourceList());
            }
        };

        final SyndesisMetadataProperties properties = metaDataRetrieval.fetchProperties(null, null, null);
        assertThat(properties.getProperties().get("brokers")).isEmpty();
    }

    @Test
    public void shouldFetchPropertiesFromKafkaCustomResources() {
        final KafkaMetaDataRetrieval metaDataRetrieval = new KafkaMetaDataRetrieval() {
            @Override
            KubernetesClient createKubernetesClient() {
                return client(KAFKAS);
            }
        };

        final SyndesisMetadataProperties properties = metaDataRetrieval.fetchProperties(null, null, null);

        final Map<String, List<PropertyPair>> expected = new HashMap<>();
        expected.put("brokers", Arrays.asList(
            new PropertyPair("my-cluster-kafka-bootstrap.zregvart.svc:9092", "zregvart::my-cluster (plain)"),
            new PropertyPair("my-cluster-kafka-bootstrap.zregvart.svc:9093", "zregvart::my-cluster (tls)"),
            new PropertyPair("zorans-cluster-kafka-bootstrap.zregvart.svc:9092", "zregvart::zorans-cluster (plain)"),
            new PropertyPair("zorans-cluster-kafka-bootstrap.zregvart.svc:9093", "zregvart::zorans-cluster (tls)")));

        assertThat(properties.getProperties()).containsExactlyEntriesOf(expected);
    }

    @SuppressWarnings("resource")
    public static KubernetesClient client(final KafkaResourceList kafkas) {
        final KubernetesClient client = mock(KubernetesClient.class);

        final ApiextensionsAPIGroupDSL apiextensions = mock(ApiextensionsAPIGroupDSL.class);
        when(client.apiextensions()).thenReturn(apiextensions);

        // v1
        final V1ApiextensionAPIGroupDSL v1 = mock(V1ApiextensionAPIGroupDSL.class);
        when(apiextensions.v1()).thenReturn(v1);
        @SuppressWarnings("unchecked")
        final MixedOperation<CustomResourceDefinition, CustomResourceDefinitionList, DoneableCustomResourceDefinition, Resource<CustomResourceDefinition, DoneableCustomResourceDefinition>> operationv1 = mock(
            MixedOperation.class);
        when(v1.customResourceDefinitions()).thenReturn(operationv1);

        @SuppressWarnings("unchecked")
        final Resource<CustomResourceDefinition, DoneableCustomResourceDefinition> resourceNamev1 = mock(Resource.class);
        when(operationv1.withName("kafkas.kafka.strimzi.io")).thenReturn(resourceNamev1);
        when(resourceNamev1.get()).thenReturn(new CustomResourceDefinition());

        // v1beta1
        final V1beta1ApiextensionAPIGroupDSL v1beta1 = mock(V1beta1ApiextensionAPIGroupDSL.class);
        when(apiextensions.v1beta1()).thenReturn(v1beta1);
        @SuppressWarnings("unchecked")
        final MixedOperation<io.fabric8.kubernetes.api.model.apiextensions.v1beta1.CustomResourceDefinition, io.fabric8.kubernetes.api.model.apiextensions.v1beta1.CustomResourceDefinitionList, io.fabric8.kubernetes.api.model.apiextensions.v1beta1.DoneableCustomResourceDefinition, Resource<io.fabric8.kubernetes.api.model.apiextensions.v1beta1.CustomResourceDefinition, io.fabric8.kubernetes.api.model.apiextensions.v1beta1.DoneableCustomResourceDefinition>> operation1beta1 = mock(
            MixedOperation.class);
        when(v1beta1.customResourceDefinitions()).thenReturn(operation1beta1);

        @SuppressWarnings("unchecked")
        final Resource<io.fabric8.kubernetes.api.model.apiextensions.v1beta1.CustomResourceDefinition, io.fabric8.kubernetes.api.model.apiextensions.v1beta1.DoneableCustomResourceDefinition> resourceNamev1beta1 = mock(
            Resource.class);
        when(operation1beta1.withName("kafkas.kafka.strimzi.io")).thenReturn(resourceNamev1beta1);

        @SuppressWarnings("unchecked")
        final MixedOperation<Kafka, KafkaResourceList, KafkaResourceDoneable, Resource<Kafka, KafkaResourceDoneable>> operation = mock(
            MixedOperation.class);

        // test with latest
        when(client.customResources(KafkaMetaDataRetrieval.KAFKA_CRDS[0],
            Kafka.class,
            KafkaResourceList.class,
            KafkaResourceDoneable.class)).thenReturn(operation);
        when(operation.inAnyNamespace()).thenReturn(operation);

        when(operation.list()).thenReturn(kafkas);

        return client;
    }

    static <T> T loadYaml(final String resource, final Class<T> type) {
        try (InputStream myCluster = KafkaMetaDataRetrievalTest.class.getResourceAsStream(resource)) {
            return new YAMLMapper().readValue(myCluster, type);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
