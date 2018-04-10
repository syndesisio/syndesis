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
package io.syndesis.integration.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

import io.syndesis.integration.runtime.logging.IntegrationLoggingConfiguration;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "syndesis.integration.runtime", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties({IntegrationRuntimeConfiguration.class, IntegrationLoggingConfiguration.class})
public class IntegrationRuntimeAutoConfiguration {
    @Autowired
    private IntegrationRuntimeConfiguration configuration;

    @Autowired
    private IntegrationLoggingConfiguration loggingConfiguration;

    @SuppressWarnings("PMD.ImmutableField")
    @Autowired(required = false)
    private List<IntegrationStepHandler> integrationStepHandlers = Collections.emptyList();

    @Bean
    public CamelContextConfiguration integrationContextRuntimeConfiguration() {
        return new CamelContextConfiguration() {
            @Override
            public void beforeApplicationStart(CamelContext camelContext) {
                final String location = configuration.getConfigurationLocation();
                final List<IntegrationStepHandler> handlers = new ArrayList<>();

                // register handlers discovered from application context
                handlers.addAll(integrationStepHandlers);

                // register handlers discovered using service loader
                for (IntegrationStepHandler handler : ServiceLoader.load(IntegrationStepHandler.class, Thread.currentThread().getContextClassLoader())) {
                    handlers.add(handler);
                }

                // IntegrationRouteBuilder automatically add known handlers to
                // the list of provided ones, know handlers have priority
                final RouteBuilder routeBuilder = new IntegrationRouteBuilder(location, handlers, loggingConfiguration.isEnabled());

                try {
                    // Register routes to the camel context
                    camelContext.addRoutes(routeBuilder);
                } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception e) {
                    throw new IllegalArgumentException(e);
                }
            }

            @Override
            public void afterApplicationStart(CamelContext camelContext) {
                // noop
            }
        };
    }
}
