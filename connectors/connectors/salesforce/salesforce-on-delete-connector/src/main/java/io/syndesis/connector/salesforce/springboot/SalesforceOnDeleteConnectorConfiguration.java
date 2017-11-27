package io.syndesis.connector.salesforce.springboot;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Generated("org.apache.camel.maven.connector.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "salesforce-on-delete")
public class SalesforceOnDeleteConnectorConfiguration
        extends
            SalesforceOnDeleteConnectorConfigurationCommon {

    /**
     * Define additional configuration definitions
     */
    private Map<String, SalesforceOnDeleteConnectorConfigurationCommon> configurations = new HashMap<>();

    public Map<String, SalesforceOnDeleteConnectorConfigurationCommon> getConfigurations() {
        return configurations;
    }
}