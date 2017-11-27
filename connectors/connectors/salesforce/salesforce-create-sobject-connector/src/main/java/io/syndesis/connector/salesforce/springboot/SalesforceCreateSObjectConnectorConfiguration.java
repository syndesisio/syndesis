package io.syndesis.connector.salesforce.springboot;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Generated("org.apache.camel.maven.connector.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "salesforce-create-sobject")
public class SalesforceCreateSObjectConnectorConfiguration
        extends
            SalesforceCreateSObjectConnectorConfigurationCommon {

    /**
     * Define additional configuration definitions
     */
    private Map<String, SalesforceCreateSObjectConnectorConfigurationCommon> configurations = new HashMap<>();

    public Map<String, SalesforceCreateSObjectConnectorConfigurationCommon> getConfigurations() {
        return configurations;
    }
}