/*
 * Copyright 2016 Red Hat, Inc.
 * <p>
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */
package io.fabric8.funktion.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.fabric8.funktion.DataKeys;
import io.fabric8.funktion.Labels;
import io.fabric8.funktion.model.Funktion;
import io.fabric8.kubernetes.api.KubernetesHelper;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.ConfigMapList;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.utils.Objects;
import io.fabric8.utils.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static io.fabric8.funktion.ApplicationProperties.toPropertiesString;
import static io.fabric8.funktion.Labels.Kind.SUBSCRIPTION;
import static io.fabric8.funktion.support.YamlHelper.createYamlMapper;
import static io.fabric8.utils.Lists.notNullList;

/**
 */
public class Agent {
    private static final transient Logger LOG = LoggerFactory.getLogger(Agent.class);

    private KubernetesClient kubernetesClient = new DefaultKubernetesClient();

    public KubernetesClient getKubernetesClient() {
        return kubernetesClient;
    }

    public void setKubernetesClient(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    public SubscribeResponse subscribe(SubscribeRequest request) throws InternalException {
        String namespace = request.getNamespace();
        Objects.notNull(namespace, "namespace");

        ConfigMap configMap = createSubscriptionResource(request, namespace);
        kubernetesClient.configMaps().inNamespace(namespace).create(configMap);
        return new SubscribeResponse(namespace, KubernetesHelper.getName(configMap));
    }


    public void unsubscribe(String namespace, String name) {
        kubernetesClient.configMaps().inNamespace(namespace).withName(name).delete();
    }

    public String getCurrentNamespace() {
        String answer = kubernetesClient.getNamespace();
        if (Strings.isNullOrBlank(answer)) {
            answer = KubernetesHelper.defaultNamespace();
        }
        if (Strings.isNullOrBlank(answer)) {
            answer = System.getenv("KUBERNETES_NAMESPACE");
        }
        if (Strings.isNullOrBlank(answer)) {
            answer = "default";
        }
        return answer;
    }

    protected ConfigMap createSubscriptionResource(SubscribeRequest request, String namespace) throws InternalException {
        Map<String, String> annotations = new LinkedHashMap<>();
        Map<String, String> data = new LinkedHashMap<>();
        Map<String, String> labels = new HashMap<>();
        labels.put(Labels.KIND, SUBSCRIPTION);

        String connectorName = request.findConnectorName();
        String namePrefix = "design";
        if (Strings.isNotBlank(connectorName)) {
            namePrefix += "-" + connectorName;
            labels.put(Labels.CONNECTOR, connectorName);
        }
        String name = createSubscriptionName(request, namespace, namePrefix);

        Funktion funktion = request.getFunktion();
        Objects.notNull(funktion, "funktion");
        try {
            String yaml = createYamlMapper().writeValueAsString(funktion);
            data.put(DataKeys.Subscription.FUNKTION_YAML, yaml);
        } catch (JsonProcessingException e) {
            throw new InternalException("Failed to marshal Funktion " + funktion + ". " + e, e);
        }
        Map<String, String> applicationProperties = request.getApplicationProperties();
        if (applicationProperties != null) {
            String comments = null;
            String applicationPropertiesText = null;
            try {
                applicationPropertiesText = toPropertiesString(applicationProperties, comments);
            } catch (IOException e) {
                throw new InternalException("Failed to marshal applicationProperties " + applicationProperties + ". " + e, e);
            }
            if (Strings.isNotBlank(applicationPropertiesText)) {
                data.put(DataKeys.Subscription.APPLICATION_PROPERTIES, applicationPropertiesText);
            }
        }
        return new ConfigMapBuilder().
                withNewMetadata().withName(name).withAnnotations(annotations).withLabels(labels).endMetadata().
                withData(data).build();
    }

    private String createSubscriptionName(SubscribeRequest request, String namespace, String prefix) {
        Set<String> configMapNames = new TreeSet<>();
        ConfigMapList configMapList = kubernetesClient.configMaps().inNamespace(namespace).list();
        List<ConfigMap> list = notNullList(configMapList.getItems());
        for (ConfigMap configMap : list) {
            configMapNames.add(KubernetesHelper.getName(configMap));
        }
        int idx = 0;
        while (true) {
            String name = prefix + (++idx);
            if (!configMapNames.contains(name)) {
                return name;
            }
        }
    }

}
