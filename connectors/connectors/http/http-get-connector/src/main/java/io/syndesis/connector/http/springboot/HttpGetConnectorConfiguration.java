package io.syndesis.connector.http.springboot;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Generated("org.apache.camel.maven.connector.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "http-get-connector")
public class HttpGetConnectorConfiguration
        extends
            HttpGetConnectorConfigurationCommon {

    /**
     * Define additional configuration definitions
     */
    private Map<String, HttpGetConnectorConfigurationCommon> configurations = new HashMap<>();

    public Map<String, HttpGetConnectorConfigurationCommon> getConfigurations() {
        return configurations;
    }
}