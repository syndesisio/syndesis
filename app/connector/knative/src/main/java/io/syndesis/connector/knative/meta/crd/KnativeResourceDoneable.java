package io.syndesis.connector.knative.meta.crd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KnativeResourceDoneable extends CustomResourceDoneable<KnativeResource> {

    public KnativeResourceDoneable(KnativeResource resource, Function<KnativeResource, KnativeResource> function) {
        super(resource, function);
    }

}
