package io.syndesis.connector.salesforce.springboot;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Generated("org.apache.camel.maven.connector.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "salesforce-on-create")
public class SalesforceOnCreateConnectorConfiguration
        extends
            SalesforceOnCreateConnectorConfigurationCommon {

    /**
     * Define additional configuration definitions
     */
    private Map<String, SalesforceOnCreateConnectorConfigurationCommon> configurations = new HashMap<>();

    public Map<String, SalesforceOnCreateConnectorConfigurationCommon> getConfigurations() {
        return configurations;
    }
}