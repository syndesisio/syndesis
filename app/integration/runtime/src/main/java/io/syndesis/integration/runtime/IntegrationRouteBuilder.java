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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import io.syndesis.core.Json;
import io.syndesis.integration.runtime.handlers.ConnectorStepHandler;
import io.syndesis.integration.runtime.handlers.DataMapperStepHandler;
import io.syndesis.integration.runtime.handlers.EndpointStepHandler;
import io.syndesis.integration.runtime.handlers.ExtensionStepHandler;
import io.syndesis.integration.runtime.handlers.FilterStepHandler;
import io.syndesis.integration.runtime.handlers.SimpleEndpointStepHandler;
import io.syndesis.integration.runtime.handlers.SplitStepHandler;
import io.syndesis.model.integration.IntegrationDeployment;
import io.syndesis.model.integration.Step;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.util.ResourceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Camel {@link RouteBuilder} which maps an Integration to Camel routes
 */
public class IntegrationRouteBuilder extends RouteBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationRouteBuilder.class);

    private final String configurationUri;
    private final List<IntegrationStepHandler> stepHandlerList;

    public IntegrationRouteBuilder(String configurationUri, Collection<IntegrationStepHandler> handlers) {
        this.configurationUri = configurationUri;

        this.stepHandlerList = new ArrayList<>();
        this.stepHandlerList.add(new ConnectorStepHandler());
        this.stepHandlerList.add(new EndpointStepHandler());
        this.stepHandlerList.add(new SimpleEndpointStepHandler());
        this.stepHandlerList.add(new DataMapperStepHandler());
        this.stepHandlerList.add(new FilterStepHandler());
        this.stepHandlerList.add(new ExtensionStepHandler());
        this.stepHandlerList.add(new SplitStepHandler());
        this.stepHandlerList.addAll(handlers);
    }

    protected IntegrationDeployment loadDeployment() throws IOException {
        final IntegrationDeployment deployment;

        try (InputStream is = ResourceHelper.resolveResourceAsInputStream(getContext().getClassResolver(), configurationUri)) {
            if (is != null) {
                LOGGER.info("Loading integration from: {}", configurationUri);
                deployment = Json.mapper().readValue(is, IntegrationDeployment.class);
            } else {
                throw new IllegalStateException("Unable to load deployment: " + configurationUri);
            }
        }

        return deployment;
    }

    @Override
    public void configure() throws Exception {
        SyndesisLoggingSupport.install(getContext());

        final IntegrationDeployment deployment = loadDeployment();
        final List<? extends Step> steps = deployment.getSpec().getSteps();

        ProcessorDefinition route = null;

        for (int i = 0; i< steps.size(); i++) {
            final Step step = steps.get(i);

            if (i == 0 && !"endpoint".equals(step.getStepKind())) {
                throw new IllegalStateException("No connector found as first step (found: " + step.getKind() + ")");
            }

            final IntegrationStepHandler handler = findHandler(step);
            final Optional<ProcessorDefinition> definition = handler.handle(step, route, this);

            if (route == null && definition.isPresent()) {
                definition.filter(RouteDefinition.class::isInstance)
                    .map(RouteDefinition.class::cast)
                    .map(rd -> rd.getInputs().get(0))
                    .ifPresent(rd -> {
                        step.getId().ifPresent(rd::id);
                    });

                route = definition.get();
                deployment.getIntegrationId().ifPresent(route::setId);
            } else {
                route = definition.map(rd -> {
                    step.getId().ifPresent(rd::id);
                    return rd;
                }).orElse(route);
            }
        }
    }

    // Visibility changed for test purpose.
    protected IntegrationStepHandler findHandler(Step step) {
        for (int i = 0; i < stepHandlerList.size(); i++) {
            IntegrationStepHandler handler = stepHandlerList.get(i);

            if (handler.canHandle(step)) {
                LOGGER.debug("Step kind: {}, handler: {}", step.getStepKind(), handler.getClass().getName());
                return handler;
            }
        }

        throw new IllegalStateException("Unsupported step kind: " + step.getStepKind());
    }
}
