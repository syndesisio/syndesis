package io.syndesis.connector.daytrade.springboot;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Generated("org.apache.camel.maven.connector.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "day-trade-get")
public class DayTradeGetConnectorConfiguration
        extends
            DayTradeGetConnectorConfigurationCommon {

    /**
     * Define additional configuration definitions
     */
    private Map<String, DayTradeGetConnectorConfigurationCommon> configurations = new HashMap<>();

    public Map<String, DayTradeGetConnectorConfigurationCommon> getConfigurations() {
        return configurations;
    }
}