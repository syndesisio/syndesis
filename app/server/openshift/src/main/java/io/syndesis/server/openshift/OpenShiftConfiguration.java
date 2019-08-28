/*
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.server.openshift;

import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableConfigurationProperties(OpenShiftConfigurationProperties.class)
public class OpenShiftConfiguration {

    @Primary
    @Bean
    @ConditionalOnProperty(value = "openshift.enabled", matchIfMissing = true, havingValue = "true")
    public NamespacedOpenShiftClient openShiftClient(OpenShiftConfigurationProperties openShiftConfigurationProperties) {
        return new DefaultOpenShiftClient(openShiftConfigurationProperties.getOpenShiftClientConfiguration());
    }

    @Bean
    @ConditionalOnProperty(value = "openshift.enabled", matchIfMissing = true, havingValue = "true")
    public OpenShiftService openShiftService(NamespacedOpenShiftClient openShiftClient, OpenShiftConfigurationProperties openShiftConfigurationProperties) {
        return new OpenShiftServiceImpl(openShiftClient, openShiftConfigurationProperties);
    }

    @Bean
    @ConditionalOnProperty(value = "openshift.enabled", havingValue = "false")
    public OpenShiftService openNoOpShiftService() {
        return new OpenShiftServiceNoOp();
    }

    @Bean
    public ExposureHelper exposureHelper(OpenShiftConfigurationProperties openShiftConfigurationProperties) {
        return new ExposureHelper(openShiftConfigurationProperties);
    }

}
