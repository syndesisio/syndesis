package io.syndesis.connector.jms.springboot;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Generated("org.apache.camel.maven.connector.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "activemq-request-text")
public class ActiveMQRequestTextConnectorConfiguration
        extends
            ActiveMQRequestTextConnectorConfigurationCommon {

    /**
     * Define additional configuration definitions
     */
    private Map<String, ActiveMQRequestTextConnectorConfigurationCommon> configurations = new HashMap<>();

    public Map<String, ActiveMQRequestTextConnectorConfigurationCommon> getConfigurations() {
        return configurations;
    }
}