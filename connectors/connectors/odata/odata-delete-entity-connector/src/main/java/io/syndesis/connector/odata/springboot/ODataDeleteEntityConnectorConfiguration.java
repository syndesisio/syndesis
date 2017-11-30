package io.syndesis.connector.odata.springboot;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Generated("org.apache.camel.maven.connector.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "odata-delete-entity")
public class ODataDeleteEntityConnectorConfiguration
        extends
            ODataDeleteEntityConnectorConfigurationCommon {

    /**
     * Define additional configuration definitions
     */
    private Map<String, ODataDeleteEntityConnectorConfigurationCommon> configurations = new HashMap<>();

    public Map<String, ODataDeleteEntityConnectorConfigurationCommon> getConfigurations() {
        return configurations;
    }
}