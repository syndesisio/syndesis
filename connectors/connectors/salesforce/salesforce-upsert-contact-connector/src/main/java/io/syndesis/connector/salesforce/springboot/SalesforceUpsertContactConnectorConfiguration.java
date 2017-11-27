package io.syndesis.connector.salesforce.springboot;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Generated("org.apache.camel.maven.connector.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "salesforce-upsert-contact-connector")
public class SalesforceUpsertContactConnectorConfiguration
        extends
            SalesforceUpsertContactConnectorConfigurationCommon {

    /**
     * Define additional configuration definitions
     */
    private Map<String, SalesforceUpsertContactConnectorConfigurationCommon> configurations = new HashMap<>();

    public Map<String, SalesforceUpsertContactConnectorConfigurationCommon> getConfigurations() {
        return configurations;
    }
}