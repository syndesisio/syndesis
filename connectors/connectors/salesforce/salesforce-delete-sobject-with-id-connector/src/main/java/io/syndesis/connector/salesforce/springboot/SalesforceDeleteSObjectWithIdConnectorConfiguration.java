package io.syndesis.connector.salesforce.springboot;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Generated("org.apache.camel.maven.connector.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "salesforce-delete-sobject-with-id")
public class SalesforceDeleteSObjectWithIdConnectorConfiguration
        extends
            SalesforceDeleteSObjectWithIdConnectorConfigurationCommon {

    /**
     * Define additional configuration definitions
     */
    private Map<String, SalesforceDeleteSObjectWithIdConnectorConfigurationCommon> configurations = new HashMap<>();

    public Map<String, SalesforceDeleteSObjectWithIdConnectorConfigurationCommon> getConfigurations() {
        return configurations;
    }
}