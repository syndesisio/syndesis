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
package io.syndesis.integration.runtime.sb.jmx;

import org.apache.camel.CamelContext;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.apache.camel.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.syndesis.integration.runtime.jmx.CamelContextMetadataMBean;
import io.syndesis.integration.runtime.sb.logging.IntegrationLoggingConfiguration;

@Configuration
@AutoConfigureAfter(CamelAutoConfiguration.class)
@ConditionalOnClass(CamelAutoConfiguration.class)
@ConditionalOnProperty(prefix = "syndesis.integration.runtime.metadata", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(IntegrationLoggingConfiguration.class)
public class IntegrationMetadataAutoConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationMetadataAutoConfiguration.class);

    @Bean
    public CamelContextConfiguration integrationContextMetadataConfiguration() {
        return new CamelContextConfiguration() {

            @Override
            public void beforeApplicationStart(CamelContext camelContext) {
                try {
                    // register custom mbean
                    camelContext.addService(new CamelContextMetadataMBean());
                    LOGGER.info("Added Syndesis MBean Service");
                } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception e) {
                    ObjectHelper.wrapRuntimeCamelException(e);
                }
            }

            @Override
            public void afterApplicationStart(CamelContext camelContext) {
                // nop
            }
        };
    }
}
