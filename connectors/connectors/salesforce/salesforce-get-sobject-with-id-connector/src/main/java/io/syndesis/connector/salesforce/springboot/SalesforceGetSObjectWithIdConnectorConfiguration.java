package io.syndesis.connector.salesforce.springboot;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Generated("org.apache.camel.maven.connector.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "salesforce-get-sobject-with-id")
public class SalesforceGetSObjectWithIdConnectorConfiguration
        extends
            SalesforceGetSObjectWithIdConnectorConfigurationCommon {

    /**
     * Define additional configuration definitions
     */
    private Map<String, SalesforceGetSObjectWithIdConnectorConfigurationCommon> configurations = new HashMap<>();

    public Map<String, SalesforceGetSObjectWithIdConnectorConfigurationCommon> getConfigurations() {
        return configurations;
    }
}