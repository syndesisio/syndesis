package io.syndesis.connector.knative.meta.crd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.fabric8.kubernetes.client.CustomResourceList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KnativeResourceList extends CustomResourceList<KnativeResource> {

}
