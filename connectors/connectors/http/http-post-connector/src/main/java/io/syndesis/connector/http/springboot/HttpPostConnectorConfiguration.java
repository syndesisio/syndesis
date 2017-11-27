package io.syndesis.connector.http.springboot;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Generated("org.apache.camel.maven.connector.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "http-post-connector")
public class HttpPostConnectorConfiguration
        extends
            HttpPostConnectorConfigurationCommon {

    /**
     * Define additional configuration definitions
     */
    private Map<String, HttpPostConnectorConfigurationCommon> configurations = new HashMap<>();

    public Map<String, HttpPostConnectorConfigurationCommon> getConfigurations() {
        return configurations;
    }
}