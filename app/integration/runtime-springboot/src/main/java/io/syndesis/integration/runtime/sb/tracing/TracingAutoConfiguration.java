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
package io.syndesis.integration.runtime.sb.tracing;

import org.apache.camel.spi.InterceptStrategy;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.opentracing.Tracer;
import io.syndesis.integration.runtime.ActivityTrackingPolicyFactory;
import io.syndesis.integration.runtime.tracing.TracingActivityTrackingPolicyFactory;
import io.syndesis.integration.runtime.tracing.TracingInterceptStrategy;

@Configuration
@AutoConfigureAfter(CamelAutoConfiguration.class)
@ConditionalOnClass(CamelAutoConfiguration.class)
@ConditionalOnProperty(prefix = "syndesis.integration.runtime.tracing", name = "enabled", matchIfMissing = false)
@EnableConfigurationProperties(TracingConfiguration.class)
public class TracingAutoConfiguration {

    @Bean
    public CamelContextConfiguration integrationLoggingContextConfiguration(Tracer tracer) {
        return new TracingCamelContextConfiguration(tracer);
    }

    @Bean
    public InterceptStrategy integrationLoggingInterceptStrategy(Tracer tracer) {
        return new TracingInterceptStrategy(tracer);
    }

    @Bean
    public ActivityTrackingPolicyFactory activityTrackingPolicyFactory(Tracer tracer) {
        return new TracingActivityTrackingPolicyFactory(tracer);
    }

}

