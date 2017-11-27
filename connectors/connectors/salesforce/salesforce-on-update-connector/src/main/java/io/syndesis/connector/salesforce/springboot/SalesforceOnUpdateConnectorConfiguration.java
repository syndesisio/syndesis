package io.syndesis.connector.salesforce.springboot;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Generated("org.apache.camel.maven.connector.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "salesforce-on-update")
public class SalesforceOnUpdateConnectorConfiguration
        extends
            SalesforceOnUpdateConnectorConfigurationCommon {

    /**
     * Define additional configuration definitions
     */
    private Map<String, SalesforceOnUpdateConnectorConfigurationCommon> configurations = new HashMap<>();

    public Map<String, SalesforceOnUpdateConnectorConfigurationCommon> getConfigurations() {
        return configurations;
    }
}