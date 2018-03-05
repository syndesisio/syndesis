package io.syndesis.connector.odata.springboot;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Generated("org.apache.camel.maven.connector.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "odata-retrieve-entity")
public class ODataRetrieveEntityConnectorConfiguration
        extends
            ODataRetrieveEntityConnectorConfigurationCommon {

    /**
     * Define additional configuration definitions
     */
    private Map<String, ODataRetrieveEntityConnectorConfigurationCommon> configurations = new HashMap<>();

    public Map<String, ODataRetrieveEntityConnectorConfigurationCommon> getConfigurations() {
        return configurations;
    }
}