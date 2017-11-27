package io.syndesis.connector.salesforce.springboot;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Generated("org.apache.camel.maven.connector.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "salesforce-delete-sobject")
public class SalesforceDeleteSObjectConnectorConfiguration
        extends
            SalesforceDeleteSObjectConnectorConfigurationCommon {

    /**
     * Define additional configuration definitions
     */
    private Map<String, SalesforceDeleteSObjectConnectorConfigurationCommon> configurations = new HashMap<>();

    public Map<String, SalesforceDeleteSObjectConnectorConfigurationCommon> getConfigurations() {
        return configurations;
    }
}