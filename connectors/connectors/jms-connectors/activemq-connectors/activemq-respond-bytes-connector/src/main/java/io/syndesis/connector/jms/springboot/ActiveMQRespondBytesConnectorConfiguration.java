package io.syndesis.connector.jms.springboot;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Generated("org.apache.camel.maven.connector.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "activemq-respond-bytes")
public class ActiveMQRespondBytesConnectorConfiguration
        extends
            ActiveMQRespondBytesConnectorConfigurationCommon {

    /**
     * Define additional configuration definitions
     */
    private Map<String, ActiveMQRespondBytesConnectorConfigurationCommon> configurations = new HashMap<>();

    public Map<String, ActiveMQRespondBytesConnectorConfigurationCommon> getConfigurations() {
        return configurations;
    }
}