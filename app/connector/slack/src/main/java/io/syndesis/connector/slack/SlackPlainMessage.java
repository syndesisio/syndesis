package io.syndesis.connector.slack;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author roland
 * @since 30.04.18
 */
public class SlackPlainMessage {

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
