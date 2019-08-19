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
package io.syndesis.server.logging.jaeger;

import io.syndesis.server.logging.jaeger.service.JaegerQueryAPI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * Configured in spring.factories so that this configuration is automatically picked
 * up when included in the classpath.
 */
@Configuration
@ComponentScan
@ConditionalOnProperty(value = "features.jaeger-activity-tracing.enabled", havingValue = "true")
public class JaegerActivityTrackingConfiguration {

    @Value("${jaeger.query.api.url:https://syndesis-jaeger-query:443/api}")
    String jaegerQueryAPIURL;

    @Lazy
    @Bean
    public JaegerQueryAPI api() {
        return new JaegerQueryAPI(jaegerQueryAPIURL);
    }

}
