package io.syndesis.connector.jms.springboot;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Generated("org.apache.camel.maven.connector.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "activemq-publish-text")
public class ActiveMQPublishTextConnectorConfiguration
        extends
            ActiveMQPublishTextConnectorConfigurationCommon {

    /**
     * Define additional configuration definitions
     */
    private Map<String, ActiveMQPublishTextConnectorConfigurationCommon> configurations = new HashMap<>();

    public Map<String, ActiveMQPublishTextConnectorConfigurationCommon> getConfigurations() {
        return configurations;
    }
}