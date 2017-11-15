package io.syndesis.connector.jms.springboot;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Generated("org.apache.camel.maven.connector.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "activemq-respond-text")
public class ActiveMQRespondTextConnectorConfiguration
        extends
            ActiveMQRespondTextConnectorConfigurationCommon {

    /**
     * Define additional configuration definitions
     */
    private Map<String, ActiveMQRespondTextConnectorConfigurationCommon> configurations = new HashMap<>();

    public Map<String, ActiveMQRespondTextConnectorConfigurationCommon> getConfigurations() {
        return configurations;
    }
}