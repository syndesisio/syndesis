package io.syndesis.connector.odata.springboot;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Generated("org.apache.camel.maven.connector.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "odata-update-entity")
public class ODataUpdateEntityConnectorConfiguration
        extends
            ODataUpdateEntityConnectorConfigurationCommon {

    /**
     * Define additional configuration definitions
     */
    private Map<String, ODataUpdateEntityConnectorConfigurationCommon> configurations = new HashMap<>();

    public Map<String, ODataUpdateEntityConnectorConfigurationCommon> getConfigurations() {
        return configurations;
    }
}