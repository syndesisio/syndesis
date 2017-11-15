package io.syndesis.connector.sql.stored.springboot;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Generated("org.apache.camel.maven.connector.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "sql-stored-start-connector")
public class SqlStoredStartConnectorConnectorConfiguration
        extends
            SqlStoredStartConnectorConnectorConfigurationCommon {

    /**
     * Define additional configuration definitions
     */
    private Map<String, SqlStoredStartConnectorConnectorConfigurationCommon> configurations = new HashMap<>();

    public Map<String, SqlStoredStartConnectorConnectorConfigurationCommon> getConfigurations() {
        return configurations;
    }
}