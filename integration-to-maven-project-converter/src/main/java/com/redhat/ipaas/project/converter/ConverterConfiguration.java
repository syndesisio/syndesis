package com.redhat.ipaas.project.converter;

import com.redhat.ipaas.connector.catalog.ConnectorCatalog;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConverterConfiguration {

    @Bean
    public IntegrationToProjectConverter projectConverter(ConnectorCatalog connectorCatalog) {
        return new DefaultIntegrationToProjectConverter(connectorCatalog);
    }

}
