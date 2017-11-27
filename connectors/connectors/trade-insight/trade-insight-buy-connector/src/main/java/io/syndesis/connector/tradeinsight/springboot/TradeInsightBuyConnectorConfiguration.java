package io.syndesis.connector.tradeinsight.springboot;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Generated("org.apache.camel.maven.connector.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "trade-insight-buy")
public class TradeInsightBuyConnectorConfiguration
        extends
            TradeInsightBuyConnectorConfigurationCommon {

    /**
     * Define additional configuration definitions
     */
    private Map<String, TradeInsightBuyConnectorConfigurationCommon> configurations = new HashMap<>();

    public Map<String, TradeInsightBuyConnectorConfigurationCommon> getConfigurations() {
        return configurations;
    }
}