package com.redhat.ipaas.openshift;

import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OpenShiftConfigurationProperties.class)
public class OpenShiftConfiguration {

    @Bean
    @ConditionalOnProperty(value = "openshift.enabled", matchIfMissing = true, havingValue = "true")
    public NamespacedOpenShiftClient openShiftClient(OpenShiftConfigurationProperties openShiftConfigurationProperties) {
        return new DefaultOpenShiftClient(openShiftConfigurationProperties.getOpenShiftClientConfiguration());
    }

    @Bean
    @ConditionalOnProperty(value = "openshift.enabled", matchIfMissing = true, havingValue = "true")
    public OpenShiftService openShiftService(NamespacedOpenShiftClient openShiftClient, OpenShiftConfigurationProperties openShiftConfigurationProperties) {
        return new OpenShiftServiceImpl(openShiftClient, openShiftConfigurationProperties.getBuilderImage());
    }

    @Bean
    @ConditionalOnProperty(value = "openshift.enabled", havingValue = "false")
    public OpenShiftService openNoOpShiftService() {
        return new OpenShiftServiceNoOp();
    }

}
