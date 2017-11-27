package io.syndesis.connector.twitter.springboot;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Generated("org.apache.camel.maven.connector.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "twitter-mention-connector")
public class TwitterMentionConnectorConfiguration
        extends
            TwitterMentionConnectorConfigurationCommon {

    /**
     * Define additional configuration definitions
     */
    private Map<String, TwitterMentionConnectorConfigurationCommon> configurations = new HashMap<>();

    public Map<String, TwitterMentionConnectorConfigurationCommon> getConfigurations() {
        return configurations;
    }
}