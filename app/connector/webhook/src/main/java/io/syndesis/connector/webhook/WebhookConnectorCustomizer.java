package io.syndesis.connector.webhook;

import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.Exchange;

import java.util.Map;

public class WebhookConnectorCustomizer implements ComponentProxyCustomizer {

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        // Unconditionally we remove output in 7.1 release
        component.setAfterConsumer(this::removeOutput);
    }

    public void removeOutput(final Exchange exchange) {
        exchange.getOut().setBody("");
        exchange.getOut().removeHeaders("*");

        if (exchange.getException() == null) {
            // In case of exception, we leave the error code as is
            exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, 204);
            exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_TEXT, "No Content");
        }
    }
}
