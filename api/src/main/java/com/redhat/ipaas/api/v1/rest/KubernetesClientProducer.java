package com.redhat.ipaas.api.v1.rest;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

@ApplicationScoped
public class KubernetesClientProducer {

    @Produces
    public KubernetesClient create() {
        return new DefaultKubernetesClient();
    }
}
