package io.syndesis.connector.jms.springboot;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Generated("org.apache.camel.maven.connector.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "activemq-subscribe")
public class ActiveMQSubscribeConnectorConfiguration
        extends
            ActiveMQSubscribeConnectorConfigurationCommon {

    /**
     * Define additional configuration definitions
     */
    private Map<String, ActiveMQSubscribeConnectorConfigurationCommon> configurations = new HashMap<>();

    public Map<String, ActiveMQSubscribeConnectorConfigurationCommon> getConfigurations() {
        return configurations;
    }
}