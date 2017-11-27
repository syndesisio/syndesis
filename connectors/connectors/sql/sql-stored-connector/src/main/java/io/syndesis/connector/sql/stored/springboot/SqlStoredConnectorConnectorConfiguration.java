package io.syndesis.connector.sql.stored.springboot;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Generated("org.apache.camel.maven.connector.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "sql-stored-connector")
public class SqlStoredConnectorConnectorConfiguration
        extends
            SqlStoredConnectorConnectorConfigurationCommon {

    /**
     * Define additional configuration definitions
     */
    private Map<String, SqlStoredConnectorConnectorConfigurationCommon> configurations = new HashMap<>();

    public Map<String, SqlStoredConnectorConnectorConfigurationCommon> getConfigurations() {
        return configurations;
    }
}