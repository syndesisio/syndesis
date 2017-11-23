package io.syndesis.connector.jms.springboot;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Generated("org.apache.camel.maven.connector.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "activemq-publish")
public class ActiveMQPublishConnectorConfiguration
        extends
            ActiveMQPublishConnectorConfigurationCommon {

    /**
     * Define additional configuration definitions
     */
    private Map<String, ActiveMQPublishConnectorConfigurationCommon> configurations = new HashMap<>();

    public Map<String, ActiveMQPublishConnectorConfigurationCommon> getConfigurations() {
        return configurations;
    }
}