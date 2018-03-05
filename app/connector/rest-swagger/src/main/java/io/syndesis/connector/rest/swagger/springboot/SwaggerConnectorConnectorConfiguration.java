package io.syndesis.connector.rest.swagger.springboot;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Generated("org.apache.camel.maven.connector.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "swagger-operation")
public class SwaggerConnectorConnectorConfiguration
        extends
            SwaggerConnectorConnectorConfigurationCommon {

    /**
     * Define additional configuration definitions
     */
    private Map<String, SwaggerConnectorConnectorConfigurationCommon> configurations = new HashMap<>();

    public Map<String, SwaggerConnectorConnectorConfigurationCommon> getConfigurations() {
        return configurations;
    }
}