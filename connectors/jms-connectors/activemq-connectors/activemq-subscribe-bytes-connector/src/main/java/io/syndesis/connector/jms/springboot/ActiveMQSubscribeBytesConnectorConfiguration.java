package io.syndesis.connector.jms.springboot;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Generated("org.apache.camel.maven.connector.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "activemq-subscribe-bytes")
public class ActiveMQSubscribeBytesConnectorConfiguration
        extends
            ActiveMQSubscribeBytesConnectorConfigurationCommon {

    /**
     * Define additional configuration definitions
     */
    private Map<String, ActiveMQSubscribeBytesConnectorConfigurationCommon> configurations = new HashMap<>();

    public Map<String, ActiveMQSubscribeBytesConnectorConfigurationCommon> getConfigurations() {
        return configurations;
    }
}