package io.syndesis.connector.jms.springboot;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Generated("org.apache.camel.maven.connector.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "activemq-respond")
public class ActiveMQRespondConnectorConfiguration
        extends
            ActiveMQRespondConnectorConfigurationCommon {

    /**
     * Define additional configuration definitions
     */
    private Map<String, ActiveMQRespondConnectorConfigurationCommon> configurations = new HashMap<>();

    public Map<String, ActiveMQRespondConnectorConfigurationCommon> getConfigurations() {
        return configurations;
    }
}