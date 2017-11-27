package io.syndesis.connector.tradeinsight.springboot;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Generated("org.apache.camel.maven.connector.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "trade-insight-top")
public class TradeInsightTopConnectorConfiguration
        extends
            TradeInsightTopConnectorConfigurationCommon {

    /**
     * Define additional configuration definitions
     */
    private Map<String, TradeInsightTopConnectorConfigurationCommon> configurations = new HashMap<>();

    public Map<String, TradeInsightTopConnectorConfigurationCommon> getConfigurations() {
        return configurations;
    }
}