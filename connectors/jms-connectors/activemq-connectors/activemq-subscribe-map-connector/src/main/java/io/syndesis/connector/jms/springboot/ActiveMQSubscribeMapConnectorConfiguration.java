package io.syndesis.connector.jms.springboot;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Generated("org.apache.camel.maven.connector.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "activemq-subscribe-map")
public class ActiveMQSubscribeMapConnectorConfiguration
        extends
            ActiveMQSubscribeMapConnectorConfigurationCommon {

    /**
     * Define additional configuration definitions
     */
    private Map<String, ActiveMQSubscribeMapConnectorConfigurationCommon> configurations = new HashMap<>();

    public Map<String, ActiveMQSubscribeMapConnectorConfigurationCommon> getConfigurations() {
        return configurations;
    }
}