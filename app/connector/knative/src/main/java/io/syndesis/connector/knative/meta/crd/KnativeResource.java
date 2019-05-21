package io.syndesis.connector.knative.meta.crd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.fabric8.kubernetes.client.CustomResource;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KnativeResource extends CustomResource {

}
