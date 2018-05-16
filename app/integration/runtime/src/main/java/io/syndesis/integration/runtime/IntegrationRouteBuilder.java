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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import io.syndesis.common.model.Split;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.action.StepAction;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Scheduler;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.util.Json;
import io.syndesis.common.util.KeyGenerator;
import io.syndesis.integration.runtime.capture.OutMessageCaptureProcessor;
import io.syndesis.integration.runtime.handlers.ConnectorStepHandler;
import io.syndesis.integration.runtime.handlers.DataMapperStepHandler;
import io.syndesis.integration.runtime.handlers.EndpointStepHandler;
import io.syndesis.integration.runtime.handlers.ExpressionFilterStepHandler;
import io.syndesis.integration.runtime.handlers.ExtensionStepHandler;
import io.syndesis.integration.runtime.handlers.LogStepHandler;
import io.syndesis.integration.runtime.handlers.RuleFilterStepHandler;
import io.syndesis.integration.runtime.handlers.SimpleEndpointStepHandler;
import io.syndesis.integration.runtime.handlers.SplitStepHandler;
import io.syndesis.integration.runtime.logging.ActivityTracker;
import io.syndesis.integration.runtime.logging.ActivityTrackingPolicy;
import io.syndesis.integration.runtime.logging.IntegrationLoggingConstants;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ExpressionNode;
import org.apache.camel.model.ModelHelper;
import org.apache.camel.model.PipelineDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.RoutesDefinition;
import org.apache.camel.runtimecatalog.RuntimeCamelCatalog;
import org.apache.camel.spi.RoutePolicy;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.ResourceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Camel {@link RouteBuilder} which maps an Integration to Camel routes
 */
@SuppressWarnings("PMD")
public class IntegrationRouteBuilder extends RouteBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationRouteBuilder.class);

    private final String configurationUri;
    private final List<IntegrationStepHandler> stepHandlerList;
    private final Set<String> resources;
    private final ActivityTracker activityTracker;

    public IntegrationRouteBuilder(String configurationUri, Collection<IntegrationStepHandler> handlers) {
        this(configurationUri, handlers, null);
    }

    public IntegrationRouteBuilder(String configurationUri, Collection<IntegrationStepHandler> handlers, ActivityTracker activityTracker) {
        this.configurationUri = configurationUri;
        this.resources = new HashSet<>();

        this.stepHandlerList = new ArrayList<>();
        this.stepHandlerList.add(new ConnectorStepHandler());
        this.stepHandlerList.add(new EndpointStepHandler());
        this.stepHandlerList.add(new SimpleEndpointStepHandler());
        this.stepHandlerList.add(new DataMapperStepHandler());
        this.stepHandlerList.add(new ExpressionFilterStepHandler());
        this.stepHandlerList.add(new RuleFilterStepHandler());
        this.stepHandlerList.add(new ExtensionStepHandler());
        this.stepHandlerList.add(new SplitStepHandler());
        this.stepHandlerList.add(new LogStepHandler());
        this.stepHandlerList.addAll(handlers);

        this.activityTracker = activityTracker;
    }

    protected Integration loadIntegration() throws IOException {
        final Integration integration;

        try (InputStream is = ResourceHelper.resolveResourceAsInputStream(getContext().getClassResolver(), configurationUri)) {
            if (is != null) {
                LOGGER.info("Loading integration from: {}", configurationUri);
                integration = Json.reader().forType(Integration.class).readValue(is);
            } else {
                throw new IllegalStateException("Unable to load deployment: " + configurationUri);
            }
        }

        return integration;
    }

    @Override
    public void configure() throws Exception {
        final Integration integration = loadIntegration();
        final List<Step> steps = Collections.unmodifiableList(integration.getSteps());

        if (steps.isEmpty()) {
            return;
        }

        ProcessorDefinition<?> parent = configureRouteScheduler(integration);

        for (int i = 0; i < steps.size(); i++) {
            final Step step = steps.get(i);
            final String stepIndex = Integer.toString(i + 1);
            final String stepId = step.getId().orElseGet(KeyGenerator::createKey);
            final IntegrationStepHandler handler = findHandler(step);

            // Load route fragments eventually defined by extensions.
            loadFragments(step);

            if (parent == null) {
                if (!(handler instanceof IntegrationStepHandler.Consumer)) {
                    throw new IllegalStateException("The handler for step kind " + step.getKind() + " is not a consumer");
                }

                Optional<ProcessorDefinition<?>> definition = handler.handle(step, null, this, stepIndex);
                if (definition.isPresent()) {
                    parent = definition.get();
                    parent = configureRouteDefinition(parent, stepId);
                    parent = parent.setHeader(IntegrationLoggingConstants.STEP_ID, constant(stepId));
                    parent = configureConnectorSplit(step, parent, stepIndex).orElse(parent);
                    parent = parent.process(OutMessageCaptureProcessor.INSTANCE);
                }
            } else {
                parent = configureRouteDefinition(parent, stepId);
                if (i > 0) {
                    // If parent is not null and this is the first step, a scheduler
                    // has been created as route initiator so d'ont include the
                    // first step in activity logging.
                    parent = createPipeline(parent, stepId);
                }

                parent = handler.handle(step, parent, this, stepIndex).orElse(parent);

                Optional<Step> splitStep = getConnectorSplitAsStep(step);
                if (splitStep.isPresent()) {
                    if (i > 0) {
                        if (parent instanceof PipelineDefinition) {
                            parent = parent.end();
                        } else if (parent instanceof ExpressionNode) {
                            parent = parent.endParent();
                        }
                    }

                    parent = new SplitStepHandler().handle(splitStep.get(), parent, this, stepIndex).orElse(parent);
                    parent = parent.setHeader(IntegrationLoggingConstants.STEP_ID, constant(stepId));
                    parent = parent.process(new OutMessageCaptureProcessor());
                } else {
                    if (parent instanceof PipelineDefinition) {
                        parent = parent.process(OutMessageCaptureProcessor.INSTANCE);
                        parent = parent.end();
                    } else if (parent instanceof ExpressionNode) {
                        parent = parent.process(OutMessageCaptureProcessor.INSTANCE);
                        parent = parent.endParent();
                    } else {
                        parent = parent.process(OutMessageCaptureProcessor.INSTANCE);
                    }
                }
            }
        }
    }

    private ProcessorDefinition<?> configureRouteDefinition(ProcessorDefinition<?> definition, String stepId) {
        if (definition instanceof RouteDefinition) {
            final RouteDefinition rd = (RouteDefinition)definition;
            final List<RoutePolicy> rp = rd.getRoutePolicies();

            if (rp != null && rp.stream().anyMatch(ActivityTrackingPolicy.class::isInstance)) {
                // Route has already been configured so no need to go ahead
                return definition;
            }

            rd.id(stepId);
            rd.routePolicy(new ActivityTrackingPolicy(activityTracker));
            rd.getInputs().get(0).id(stepId);
        }

        return definition;
    }

    private ProcessorDefinition<PipelineDefinition> createPipeline(ProcessorDefinition<?> parent, String stepId) {
        return parent.pipeline()
            .id(String.format("step:%s", stepId))
            .setHeader(IntegrationLoggingConstants.STEP_ID, constant(stepId));
    }

    /**
     * If the integration has a scheduler, start the route with a timer or quartz2
     * endpoint.
     */
    private ProcessorDefinition<?> configureRouteScheduler(Integration integration) throws URISyntaxException {
        if (integration.getScheduler().isPresent()) {
            Scheduler scheduler = integration.getScheduler().get();

            // We now support simple timer only, cron support will be supported
            // later on.
            if (scheduler.isTimer()) {
                Map<String, String> properties = new HashMap<>();
                properties.put("timerName", "integration");
                properties.put("period", scheduler.getExpression());

                final RuntimeCamelCatalog catalog = getContext().getRuntimeCamelCatalog();
                final String uri = catalog.asEndpointUri("timer", properties, false);

                RouteDefinition route = this.from(uri);
                route.getInputs().get(0).setId("integration-scheduler");
                integration.getId().ifPresent(route::setId);

                return route;
            } else {
                throw new IllegalArgumentException("Unsupported scheduler type: " + scheduler.getType());
            }
        }

        return null;
    }

    private Optional<Step> getConnectorSplitAsStep(Step step) {
        if (step.getAction().filter(ConnectorAction.class::isInstance).isPresent()) {
            final ConnectorAction action = step.getAction().filter(ConnectorAction.class::isInstance).map(ConnectorAction.class::cast).get();
            final ConnectorDescriptor descriptor = action.getDescriptor();

            if (descriptor.getSplit().isPresent()) {
                final Split split = descriptor.getSplit().get();
                final Step.Builder splitBuilder = new Step.Builder().stepKind(StepKind.split);

                split.getLanguage().ifPresent(s -> splitBuilder.putConfiguredProperty("language", s));
                split.getExpression().ifPresent(s -> splitBuilder.putConfiguredProperty("expression", s));

                return Optional.of(splitBuilder.build());
            }
        }

        return Optional.empty();
    }

    private Optional<ProcessorDefinition<?>> configureConnectorSplit(Step step, ProcessorDefinition<?> route, String index) {
        if (step.getAction().filter(ConnectorAction.class::isInstance).isPresent()) {
            final ConnectorAction action = step.getAction().filter(ConnectorAction.class::isInstance).map(ConnectorAction.class::cast).get();
            final ConnectorDescriptor descriptor = action.getDescriptor();

            if (descriptor.getSplit().isPresent()) {
                final Split split = descriptor.getSplit().get();
                final Step.Builder splitBuilder = new Step.Builder().stepKind(StepKind.split);

                split.getLanguage().ifPresent(s -> splitBuilder.putConfiguredProperty("language", s));
                split.getExpression().ifPresent(s -> splitBuilder.putConfiguredProperty("expression", s));

                return new SplitStepHandler().handle(
                    splitBuilder.build(),
                    route,
                    this,
                    index);
            }
        }

        return Optional.empty();
    }

    private void loadFragments(Step step) {
        if (StepKind.extension != step.getStepKind()) {
            return;
        }

        final StepAction action = step.getAction().filter(StepAction.class::isInstance).map(StepAction.class::cast).get();

        if (action.getDescriptor().getKind() == StepAction.Kind.ENDPOINT) {
            final CamelContext context = getContext();
            final String resource = action.getDescriptor().getResource();

            if (ObjectHelper.isNotEmpty(resource) && resources.add(resource)) {
                final Object instance = mandatoryLoadResource(context, resource);
                final RoutesDefinition definitions = mandatoryConvertToRoutesDefinition(resource, instance);

                LOGGER.debug("Resolved resource: {} as {}", resource, instance.getClass());

                try {
                    context.addRouteDefinitions(definitions.getRoutes());
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
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

    private Object mandatoryLoadResource(CamelContext context, String resource) {
        Object instance = null;

        if (resource.startsWith("classpath:")) {
            try (InputStream is = ResourceHelper.resolveMandatoryResourceAsInputStream(context, resource)) {
                instance = ModelHelper.loadRoutesDefinition(context, is);
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        } else if (resource.startsWith("class:")) {
            Class<?> type = context.getClassResolver().resolveClass(resource.substring("class:".length()));
            instance = context.getInjector().newInstance(type);
        } else if (resource.startsWith("bean:")) {
            instance = context.getRegistry().lookupByName(resource.substring("bean:".length()));
        }

        if (instance == null) {
            throw new IllegalArgumentException("Unable to resolve resource: " + resource);
        }

        return instance;
    }

    private RoutesDefinition mandatoryConvertToRoutesDefinition(String resource, Object instance)  {
        final RoutesDefinition definitions;

        if (instance instanceof RoutesDefinition) {
            definitions = (RoutesDefinition)instance;
        } else if (instance instanceof RouteDefinition) {
            definitions = new RoutesDefinition();
            definitions.route((RouteDefinition)instance);
        } else if (instance instanceof RouteBuilder) {
            RouteBuilder builder = (RouteBuilder)instance;
            try {
                builder.configure();
                definitions = builder.getRouteCollection();
            } catch (Exception e) {
                LOGGER.warn("Unable to configure resource: " + resource, e);

                throw ObjectHelper.wrapRuntimeCamelException(e);
            }
        } else {
            throw new IllegalArgumentException("Unable to convert instance: " + instance);
        }

        return definitions;
    }
}
