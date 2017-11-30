package io.syndesis.connector.sql.springboot;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Generated("org.apache.camel.maven.connector.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "sql-connector")
public class SqlConnectorConnectorConfiguration
        extends
            SqlConnectorConnectorConfigurationCommon {

    /**
     * Define additional configuration definitions
     */
    private Map<String, SqlConnectorConnectorConfigurationCommon> configurations = new HashMap<>();

    public Map<String, SqlConnectorConnectorConfigurationCommon> getConfigurations() {
        return configurations;
    }
}