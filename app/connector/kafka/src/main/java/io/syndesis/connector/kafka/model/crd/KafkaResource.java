package io.syndesis.connector.kafka.model.crd;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.fabric8.kubernetes.client.CustomResource;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KafkaResource extends CustomResource {

    private Map<String, Object>  status;

    public Map<String, Object>  getStatus() {
        return status;
    }

    public void setStatus(Map<String, Object>  status) {
        this.status = status;
    }

}
