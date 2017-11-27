package io.syndesis.connector.daytrade.springboot;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Generated("org.apache.camel.maven.connector.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "day-trade-place")
public class DayTradePlaceConnectorConfiguration
        extends
            DayTradePlaceConnectorConfigurationCommon {

    /**
     * Define additional configuration definitions
     */
    private Map<String, DayTradePlaceConnectorConfigurationCommon> configurations = new HashMap<>();

    public Map<String, DayTradePlaceConnectorConfigurationCommon> getConfigurations() {
        return configurations;
    }
}