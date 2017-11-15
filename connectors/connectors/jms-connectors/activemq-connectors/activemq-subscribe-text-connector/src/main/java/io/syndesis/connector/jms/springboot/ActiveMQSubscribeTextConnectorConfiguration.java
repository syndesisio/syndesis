package io.syndesis.connector.jms.springboot;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Generated("org.apache.camel.maven.connector.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "activemq-subscribe-text")
public class ActiveMQSubscribeTextConnectorConfiguration
        extends
            ActiveMQSubscribeTextConnectorConfigurationCommon {

    /**
     * Define additional configuration definitions
     */
    private Map<String, ActiveMQSubscribeTextConnectorConfigurationCommon> configurations = new HashMap<>();

    public Map<String, ActiveMQSubscribeTextConnectorConfigurationCommon> getConfigurations() {
        return configurations;
    }
}