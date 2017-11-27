package io.syndesis.connector.tradeinsight.springboot;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Generated("org.apache.camel.maven.connector.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "trade-insight-sell")
public class TradeInsightSellConnectorConfiguration
        extends
            TradeInsightSellConnectorConfigurationCommon {

    /**
     * Define additional configuration definitions
     */
    private Map<String, TradeInsightSellConnectorConfigurationCommon> configurations = new HashMap<>();

    public Map<String, TradeInsightSellConnectorConfigurationCommon> getConfigurations() {
        return configurations;
    }
}