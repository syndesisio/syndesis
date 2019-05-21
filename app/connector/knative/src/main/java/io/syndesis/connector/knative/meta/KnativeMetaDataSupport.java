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
package io.syndesis.connector.knative.meta;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionBuilder;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionStatusBuilder;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import io.syndesis.connector.knative.meta.crd.KnativeResource;
import io.syndesis.connector.knative.meta.crd.KnativeResourceDoneable;
import io.syndesis.connector.knative.meta.crd.KnativeResourceList;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class KnativeMetaDataSupport {

    private static final CustomResourceDefinition KNATIVE_CHANNEL_CRD = new CustomResourceDefinitionBuilder()
        .withApiVersion("eventing.knative.dev/v1alpha1")
        .withKind("Channel")
        .withNewMetadata()
            .withName("channels.eventing.knative.dev")
        .endMetadata()
        .withNewSpec()
            .withGroup("eventing.knative.dev")
            .withScope("Namespaced")
            .withVersion("v1alpha1")
            .withNewNames()
                .withKind("Channel")
                .withListKind("ChannelList")
                .withPlural("channels")
                .withSingular("channel")
            .endNames()
        .endSpec()
        .withStatus(new CustomResourceDefinitionStatusBuilder().build())
        .build();

    private static final CustomResourceDefinition KNATIVE_SERVICE_CRD = new CustomResourceDefinitionBuilder()
        .withApiVersion("serving.knative.dev/v1alpha1")
            .withKind("Service")
        .withNewMetadata()
            .withName("services.serving.knative.dev")
        .endMetadata()
        .withNewSpec()
            .withGroup("serving.knative.dev")
            .withScope("Namespaced")
            .withVersion("v1alpha1")
            .withNewNames()
                .withKind("Service")
                .withListKind("ServiceList")
                .withPlural("services")
                .withSingular("service")
            .endNames()
        .endSpec()
        .withStatus(new CustomResourceDefinitionStatusBuilder().build())
        .build();

    private KnativeMetaDataSupport() {
    }

    public static List<String> listChannels() {
        return listResources(KNATIVE_CHANNEL_CRD);
    }

    public static List<String> listServices() {
        return listResources(KNATIVE_SERVICE_CRD);
    }

    private static List<String> listResources(CustomResourceDefinition crd) {
        try (OpenShiftClient client = new DefaultOpenShiftClient()) {
            return client.customResources(crd, KnativeResource.class, KnativeResourceList.class, KnativeResourceDoneable.class)
                .inNamespace(getTargetNamespace())
                .list()
                .getItems()
                .stream()
                .map(KnativeResource::getMetadata)
                .map(ObjectMeta::getName)
                .collect(Collectors.toList());
        }
    }

    private static String getTargetNamespace() {
        return Optional.ofNullable(System.getenv("NAMESPACE")).orElse("");
    }

}
