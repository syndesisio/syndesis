package io.syndesis.connector.jms.springboot;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Generated("org.apache.camel.maven.connector.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "activemq-request-map")
public class ActiveMQRequestMapConnectorConfiguration
        extends
            ActiveMQRequestMapConnectorConfigurationCommon {

    /**
     * Define additional configuration definitions
     */
    private Map<String, ActiveMQRequestMapConnectorConfigurationCommon> configurations = new HashMap<>();

    public Map<String, ActiveMQRequestMapConnectorConfigurationCommon> getConfigurations() {
        return configurations;
    }
}