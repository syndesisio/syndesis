package io.syndesis.connector.salesforce.springboot;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Generated("org.apache.camel.maven.connector.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "salesforce-upsert-sobject")
public class SalesforceUpsertSObjectConnectorConfiguration
        extends
            SalesforceUpsertSObjectConnectorConfigurationCommon {

    /**
     * Define additional configuration definitions
     */
    private Map<String, SalesforceUpsertSObjectConnectorConfigurationCommon> configurations = new HashMap<>();

    public Map<String, SalesforceUpsertSObjectConnectorConfigurationCommon> getConfigurations() {
        return configurations;
    }
}