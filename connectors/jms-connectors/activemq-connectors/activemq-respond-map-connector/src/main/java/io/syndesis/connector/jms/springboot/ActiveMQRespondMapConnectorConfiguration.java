package io.syndesis.connector.jms.springboot;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Generated("org.apache.camel.maven.connector.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "activemq-respond-map")
public class ActiveMQRespondMapConnectorConfiguration
        extends
            ActiveMQRespondMapConnectorConfigurationCommon {

    /**
     * Define additional configuration definitions
     */
    private Map<String, ActiveMQRespondMapConnectorConfigurationCommon> configurations = new HashMap<>();

    public Map<String, ActiveMQRespondMapConnectorConfigurationCommon> getConfigurations() {
        return configurations;
    }
}