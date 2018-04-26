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
package io.syndesis.integration.runtime.logging;

import io.syndesis.common.util.KeyGenerator;
import org.apache.camel.CamelContext;
import org.apache.camel.spi.InterceptStrategy;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter(CamelAutoConfiguration.class)
@ConditionalOnClass(CamelAutoConfiguration.class)
@ConditionalOnProperty(prefix = "syndesis.integration.runtime.logging", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(IntegrationLoggingConfiguration.class)
public class IntegrationLoggingAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(ActivityTracker.class)
    public ActivityTracker activityTracker() {
        return new ActivityTracker.SysOut();
    }

    @Bean
    public CamelContextConfiguration integrationLoggingContextConfiguration(ActivityTracker activityTracker) {
        return new CamelContextConfiguration() {
            @Override
            public void beforeApplicationStart(CamelContext camelContext) {
                // Lets generates always incrementing lexically sortable unique
                // uuids. These uuids are also more compact than the camel default
                // and contain an embedded timestamp.
                camelContext.setUuidGenerator(KeyGenerator::createKey);

                // Log listener
                camelContext.addLogListener(new IntegrationLoggingListener(activityTracker));
            }

            @Override
            public void afterApplicationStart(CamelContext camelContext) {
                // no ops
            }
        };
    }

    @Bean
    public InterceptStrategy integrationLoggingInterceptStrategy(ActivityTracker activityTracker) {
        return new ActivityTrackingInterceptStrategy(activityTracker);
    }
}
