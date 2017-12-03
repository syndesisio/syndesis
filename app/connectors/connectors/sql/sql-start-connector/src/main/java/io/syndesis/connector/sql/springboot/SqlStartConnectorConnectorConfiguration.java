package io.syndesis.connector.sql.springboot;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Generated("org.apache.camel.maven.connector.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "sql-start-connector")
public class SqlStartConnectorConnectorConfiguration
        extends
            SqlStartConnectorConnectorConfigurationCommon {

    /**
     * Define additional configuration definitions
     */
    private Map<String, SqlStartConnectorConnectorConfigurationCommon> configurations = new HashMap<>();

    public Map<String, SqlStartConnectorConnectorConfigurationCommon> getConfigurations() {
        return configurations;
    }
}