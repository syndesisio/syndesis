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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

import io.syndesis.common.model.action.StepAction;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Flow.FlowType;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Scheduler;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.util.Properties;
import io.syndesis.common.util.Json;
import io.syndesis.common.util.KeyGenerator;
import io.syndesis.common.util.Resources;
import io.syndesis.integration.runtime.capture.OutMessageCaptureProcessor;
import io.syndesis.integration.runtime.logging.IntegrationLoggingConstants;
import org.apache.camel.CamelContext;
import org.apache.camel.Processor;
import org.apache.camel.builder.DefaultErrorHandlerBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ExpressionNode;
import org.apache.camel.model.LogDefinition;
import org.apache.camel.model.ModelHelper;
import org.apache.camel.model.OnExceptionDefinition;
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
    private final SourceProvider sourceProvider;
    private final List<IntegrationStepHandler> stepHandlerList;
    private final Set<String> resources;
    private final List<ActivityTrackingPolicyFactory> activityTrackingPolicyFactories;


    public IntegrationRouteBuilder(String configurationUri) {
        this(configurationUri, Resources.loadServices(IntegrationStepHandler.class));
    }

    public IntegrationRouteBuilder(String configurationUri, Collection<IntegrationStepHandler> handlers) {
        this(configurationUri, handlers, Collections.emptyList());
    }

    public IntegrationRouteBuilder(String configurationUri, Collection<IntegrationStepHandler> handlers, List<ActivityTrackingPolicyFactory> activityTrackingPolicyFactories) {
        this.configurationUri = configurationUri;
        this.sourceProvider = null;
        this.resources = new HashSet<>();

        this.stepHandlerList = new ArrayList<>();
        this.stepHandlerList.addAll(handlers);

        this.activityTrackingPolicyFactories = activityTrackingPolicyFactories;
    }

    public IntegrationRouteBuilder(SourceProvider sourceProvider, Collection<IntegrationStepHandler> handlers, List<ActivityTrackingPolicyFactory> activityTrackingPolicyFactories) {
        this.configurationUri = null;
        this.sourceProvider = sourceProvider;
        this.resources = new HashSet<>();

        this.stepHandlerList = new ArrayList<>();
        this.stepHandlerList.addAll(handlers);

        this.activityTrackingPolicyFactories = activityTrackingPolicyFactories;
    }

    protected Integration loadIntegration() throws IOException {
        final Integration integration;

        try (InputStream is = createIntegrationInputStream()) {
            if (is != null) {
                integration = Json.reader().forType(Integration.class).readValue(is);
            } else {
                throw new IllegalStateException("Unable to load deployment: " + configurationUri);
            }
        }

        return integration;
    }

    protected InputStream createIntegrationInputStream() throws IOException {
        if (sourceProvider != null) {
            try {
                return sourceProvider.getSource(getContext());
            } catch (IOException | RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw ObjectHelper.wrapRuntimeCamelException(e);
            }
        }
        LOGGER.info("Loading integration from: {}", configurationUri);
        return ResourceHelper.resolveResourceAsInputStream(getContext().getClassResolver(), configurationUri);
    }

    @Override
    public void configure() throws Exception {
        final Integration integration = loadIntegration();
        final List<Flow> flows = integration.getFlows();

        for (int f = 0; f < flows.size(); f++) {
            configureFlow(flows.get(f), String.valueOf(f));
        }
    }

    private void configureFlow(Flow flow, final String flowIndex) throws URISyntaxException {
        final List<Step> steps = flow.getSteps();
        final String flowId = flow.getId().orElseGet(KeyGenerator::createKey);

        if (steps.isEmpty()) {
            return;
        }

        ProcessorDefinition<?> parent = configureRouteScheduler(flow);
        final Deque<String> splitStack = new ArrayDeque<>();
        for (int stepIndex = 0; stepIndex < steps.size(); stepIndex++) {
            final Step step = steps.get(stepIndex);
            final Optional<Step> nextStep = stepIndex < steps.size() -1 ? Optional.of(steps.get(stepIndex + 1)) : Optional.empty();
            final String stepId = step.getId().orElseGet(KeyGenerator::createKey);
            final IntegrationStepHandler handler = findHandler(step);

            // Load route fragments eventually defined by extensions.
            loadFragments(step);

            if (parent == null) {
                if (!(handler instanceof IntegrationStepHandler.Consumer)) {
                    throw new IllegalStateException("The handler for step kind " + step.getKind() + " is not a consumer");
                }

                Optional<ProcessorDefinition<?>> definition = handler.handle(step, null, this, flowIndex, String.valueOf(stepIndex));
                if (definition.isPresent()) {
                    parent = definition.get();
                    parent = configureRouteDefinition(parent, flow, flowId, stepId);
                    parent = createPipeline(parent, stepId);
                    parent = parent.setHeader(IntegrationLoggingConstants.FLOW_ID, constant(flowId));
                    parent = parent.end();

                    if (nextStep.map(Step::getStepKind)
                                .map(kind -> kind.equals(StepKind.split)).orElse(false)) {
                        parent = findHandler(nextStep.get()).handle(nextStep.get(), parent, this, flowIndex, String.valueOf(stepIndex)).orElse(parent);
                        String splitStepId = nextStep.get().getId().orElseGet(KeyGenerator::createKey);
                        splitStack.push(splitStepId);
                        parent.id(getStepId(splitStepId));
                        parent = captureOutMessage(parent, splitStepId);
                        stepIndex++;
                    } else {
                        parent = captureOutMessage(parent, stepId);
                    }
                }
            } else {
                parent = configureRouteDefinition(parent, flow, flowId, stepId);

                if (StepKind.aggregate.equals(step.getStepKind())) {
                    if (!splitStack.isEmpty()) {
                        String splitStepId = splitStack.pop();
                        while (!getStepId(splitStepId).equals(parent.getId())) {
                            if (parent instanceof ExpressionNode) {
                                parent = parent.end();
                                parent = parent.endParent();
                            } else {
                                parent = parent.getParent();
                            }
                        }

                        if (parent instanceof ExpressionNode) {
                            parent = parent.end();
                            parent = parent.endParent();
                        }

                        parent = handler.handle(step, parent, this, flowIndex, String.valueOf(stepIndex)).orElse(parent);
                        parent = captureOutMessage(parent, stepId);
                    }
                } else {
                    if (stepIndex == 0) {
                        // If parent is not null and this is the first step, a scheduler
                        // has been created as route initiator so now its is time to set the flow id
                        parent = createPipeline(parent, stepId);
                        parent = parent.setHeader(IntegrationLoggingConstants.FLOW_ID, constant(flowId));
                        parent = parent.end();
                    } else {
                        parent = createPipeline(parent, stepId);
                    }

                    parent = handler.handle(step, parent, this, flowIndex, String.valueOf(stepIndex)).orElse(parent);

                    if (nextStep.map(Step::getStepKind)
                                .map(kind -> kind.equals(StepKind.split)).orElse(false)) {
                        if (stepIndex > 0) {
                            parent = closeParent(parent, stepId, (processorDefinition, id) -> processorDefinition);
                        }

                        parent = findHandler(nextStep.get()).handle(nextStep.get(), parent, this, flowIndex, String.valueOf(stepIndex)).orElse(parent);
                        String splitStepId = nextStep.get().getId().orElseGet(KeyGenerator::createKey);
                        splitStack.push(splitStepId);
                        parent.id(getStepId(splitStepId));
                        parent = captureOutMessage(parent, splitStepId);
                        stepIndex++;
                    } else {
                        parent = closeParent(parent, stepId, this::captureOutMessage);
                    }
                }
            }
        }
    }

    private ProcessorDefinition<?> closeParent(ProcessorDefinition<?> parent, String stepId,
                                               BiFunction<ProcessorDefinition<?>, String, ProcessorDefinition<?>> fallback) {
        if (parent instanceof PipelineDefinition) {
            parent = captureOutMessage(parent, stepId);
            parent = parent.end();
        } else if (parent instanceof ExpressionNode) {
            parent = captureOutMessage(parent, stepId);
            parent = parent.endParent();
        } else {
            parent = fallback.apply(parent, stepId);
        }

        return parent;
    }

    /**
     * Adds out message capture message processor to save current message to memory for later usage.
     * @param parent
     * @param stepId
     * @return
     */
    private ProcessorDefinition<?> captureOutMessage(ProcessorDefinition<?> parent, String stepId) {
        if (parent instanceof PipelineDefinition &&
                ObjectHelper.isNotEmpty(parent.getOutputs())) {
            ProcessorDefinition<?> lastInPipeline = parent.getOutputs().get(parent.getOutputs().size() - 1);
            if (lastInPipeline instanceof LogDefinition) {
                // skip our message capture for log steps
                return parent;
            }
        } else {
            // not in a pipeline so set the step id header to be sure it is there
            parent = parent.setHeader(IntegrationLoggingConstants.STEP_ID, constant(stepId));
        }

        parent = parent.process(OutMessageCaptureProcessor.INSTANCE)
                        .id(String.format("capture-out:%s", stepId));
        return parent;
    }

    private ProcessorDefinition<?> configureRouteDefinition(ProcessorDefinition<?> definition, Flow flow, String flowId, String stepId) {
        if (definition instanceof RouteDefinition) {
            final RouteDefinition rd = (RouteDefinition)definition;

            if (containsConfiguredActivityTrackingPolicies(rd)) {
                // Route has already been configured so no need to go ahead
                return definition;
            }

            if (ObjectHelper.isNotEmpty(flow.getName())) {
                rd.routeDescription(flow.getName());
            }

            rd.routeId(flowId);
            for (ActivityTrackingPolicyFactory factory : activityTrackingPolicyFactories) {
                if (factory.appliesTo(flow)) {
                    rd.routePolicy(factory.createRoutePolicy(flowId));
                }
            }
            rd.getInputs().get(0).id(stepId);

            //Adding an errorHandler on API-Provider integrations
            if (FlowType.API_PROVIDER.equals(flow.getType())) {
                final OnExceptionDefinition onException = new OnExceptionDefinition(Throwable.class)
                        .handled(true)
                        .maximumRedeliveries(0);

                final Processor errorHandler = (Processor) mandatoryLoadResource(
                        this.getContext(), "class:io.syndesis.connector.apiprovider.ApiProviderOnExceptionHandler");
                final Step endStep = flow.getSteps().get(flow.getSteps().size()-1);
                ((Properties) errorHandler).setProperties(endStep.getConfiguredProperties());

                final DefaultErrorHandlerBuilder builder = new DefaultErrorHandlerBuilder();
                builder.setExceptionPolicyStrategy((exceptionPolicies, exchange, exception) -> onException);
                builder.setOnExceptionOccurred(errorHandler);

                rd.setErrorHandlerBuilder(builder);
            }
        }

        return definition;
    }

    /**
     * Checks if given route definition has already been configured with activity tracking policies.
     * @param routeDefinition the route definition to evaluate.
     * @return true if activity tracking policies have already been configured on given route definition.
     */
    private boolean containsConfiguredActivityTrackingPolicies(RouteDefinition routeDefinition) {
        List<RoutePolicy> routePolicies = routeDefinition.getRoutePolicies();
        if (ObjectHelper.isEmpty(routePolicies)) {
            return false;
        }

        return activityTrackingPolicyFactories.stream().anyMatch(policyFactory -> routePolicies.stream().anyMatch(policyFactory::isInstance));
    }

    private ProcessorDefinition<PipelineDefinition> createPipeline(ProcessorDefinition<?> parent, String stepId) {
        return parent.pipeline()
            .id(getStepId(stepId))
            .setHeader(IntegrationLoggingConstants.STEP_ID, constant(stepId));
    }

    private String getStepId(String stepId) {
        return String.format("step:%s", stepId);
    }

    /**
     * If the integration has a scheduler, start the route with a timer or quartz2
     * endpoint.
     */
    private ProcessorDefinition<?> configureRouteScheduler(final Flow flow) throws URISyntaxException {
        if (flow.getScheduler().isPresent()) {
            Scheduler scheduler = flow.getScheduler().get();

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
                flow.getId().ifPresent(route::setId);

                return route;
            } else {
                throw new IllegalArgumentException("Unsupported scheduler type: " + scheduler.getType());
            }
        }

        return null;
    }

    private void loadFragments(Step step) {
        if (StepKind.extension != step.getStepKind()) {
            return;
        }

        final StepAction action = step.getActionAs(StepAction.class)
                                      .orElseThrow(() -> new IllegalArgumentException(
                                              String.format("Missing step action on step: %s - %s", step.getId(), step.getName())));

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
        for (IntegrationStepHandler handler : stepHandlerList) {
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
