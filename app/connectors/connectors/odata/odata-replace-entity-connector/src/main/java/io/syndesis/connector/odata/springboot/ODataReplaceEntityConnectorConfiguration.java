package io.syndesis.connector.odata.springboot;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Generated("org.apache.camel.maven.connector.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "odata-replace-entity")
public class ODataReplaceEntityConnectorConfiguration
        extends
            ODataReplaceEntityConnectorConfigurationCommon {

    /**
     * Define additional configuration definitions
     */
    private Map<String, ODataReplaceEntityConnectorConfigurationCommon> configurations = new HashMap<>();

    public Map<String, ODataReplaceEntityConnectorConfigurationCommon> getConfigurations() {
        return configurations;
    }
}