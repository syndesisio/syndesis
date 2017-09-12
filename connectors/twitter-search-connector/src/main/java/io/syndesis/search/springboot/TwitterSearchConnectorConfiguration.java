package io.syndesis.search.springboot;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Generated("org.apache.camel.maven.connector.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "twitter-search-connector")
public class TwitterSearchConnectorConfiguration
        extends
            TwitterSearchConnectorConfigurationCommon {

    /**
     * Define additional configuration definitions
     */
    private Map<String, TwitterSearchConnectorConfigurationCommon> configurations = new HashMap<>();

    public Map<String, TwitterSearchConnectorConfigurationCommon> getConfigurations() {
        return configurations;
    }
}