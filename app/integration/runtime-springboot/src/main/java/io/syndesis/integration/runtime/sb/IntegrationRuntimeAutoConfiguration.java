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
package io.syndesis.integration.runtime.sb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

import javax.xml.bind.JAXBException;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ModelHelper;
import org.apache.camel.model.RoutesDefinition;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.syndesis.integration.runtime.ActivityTrackingPolicyFactory;
import io.syndesis.integration.runtime.IntegrationRouteBuilder;
import io.syndesis.integration.runtime.IntegrationStepHandler;
import io.syndesis.integration.runtime.sb.logging.IntegrationLoggingConfiguration;

@Configuration
@ConditionalOnProperty(prefix = "syndesis.integration.runtime", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties({IntegrationRuntimeConfiguration.class, IntegrationLoggingConfiguration.class})
public class IntegrationRuntimeAutoConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationRuntimeAutoConfiguration.class);

    @Autowired
    private IntegrationRuntimeConfiguration configuration;

    @Autowired(required = false)
    private List<ActivityTrackingPolicyFactory> activityTrackingPolicyFactories = Collections.emptyList();

    @SuppressWarnings("PMD.ImmutableField")
    @Autowired(required = false)
    private List<IntegrationStepHandler> integrationStepHandlers = Collections.emptyList();

    /**
     * This method is responsible to set up an {@link CamelContextConfiguration}
     * that crates a route builder from the integration.json and set-up a post
     * context start task that dumps the generated routes for debugging purpose.
     *
     * @return a {@link CamelContextConfiguration}
     */
    @Bean
    public CamelContextConfiguration integrationContextRuntimeConfiguration() {
        return new CamelContextConfiguration() {
            @Override
            public void beforeApplicationStart(CamelContext camelContext) {
                final String location = configuration.getConfigurationLocation();
                final List<IntegrationStepHandler> handlers = new ArrayList<>();

                // register handlers discovered from application context
                handlers.addAll(integrationStepHandlers);
                LOGGER.info("Autowired IntegrationStepHandlers found: {}", integrationStepHandlers.size());

                // register handlers discovered using service loader
                for (IntegrationStepHandler handler : ServiceLoader.load(IntegrationStepHandler.class, Thread.currentThread().getContextClassLoader())) {
                    handlers.add(handler);
                }

                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("ServiceLoader loaded IntegrationStepHandlers: {}", handlers.size()-integrationStepHandlers.size());
                }

                // IntegrationRouteBuilder automatically add known handlers to
                // the list of provided ones, know handlers have priority
                final RouteBuilder routeBuilder = new IntegrationRouteBuilder(location, handlers, activityTrackingPolicyFactories);

                try {
                    // Register routes to the camel context
                    camelContext.addRoutes(routeBuilder);
                } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception e) {
                    throw new IllegalArgumentException(e);
                }
            }

            @Override
            public void afterApplicationStart(CamelContext camelContext) {
                RoutesDefinition routes = new RoutesDefinition();
                routes.setRoutes(camelContext.getRouteDefinitions());

                try {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Routes: \n{}", ModelHelper.dumpModelAsXml(camelContext, routes));
                    }
                } catch (JAXBException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        };
    }
}
