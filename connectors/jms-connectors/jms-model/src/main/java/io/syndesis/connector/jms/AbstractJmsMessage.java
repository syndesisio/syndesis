package io.syndesis.connector.jms;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Base class for JMS Wrapper Messages.
 */
public abstract class AbstractJmsMessage {

    @JsonProperty("Headers")
    private Map<String, Object> headers;

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, Object> headers) {
        this.headers = headers;
    }
}
