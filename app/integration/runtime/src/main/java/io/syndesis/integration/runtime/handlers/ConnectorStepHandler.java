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
package io.syndesis.integration.runtime.handlers;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import io.syndesis.common.model.action.Action.Pattern;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.util.CollectionsUtils;
import io.syndesis.common.util.Optionals;
import io.syndesis.common.util.Predicates;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyFactory;
import io.syndesis.integration.runtime.IntegrationRouteBuilder;
import io.syndesis.integration.runtime.IntegrationStepHandler;
import org.apache.camel.CamelContext;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.spi.ClassResolver;
import org.apache.camel.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.syndesis.common.model.InputDataShapeAware.trySetInputDataShape;
import static io.syndesis.common.model.OutputDataShapeAware.trySetOutputDataShape;
import static io.syndesis.integration.component.proxy.Processors.pollEnricher;

public class ConnectorStepHandler implements IntegrationStepHandler, IntegrationStepHandler.Consumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorStepHandler.class);

    @Override
    public boolean canHandle(Step step) {
        if (StepKind.endpoint != step.getStepKind() && StepKind.connector != step.getStepKind()) {
            return false;
        }
        if (!step.getConnection().isPresent()) {
            return false;
        }
        if (!step.getConnection().get().getConnector().isPresent()) {
            return false;
        }
        if (!step.getActionAs(ConnectorAction.class).isPresent()) {
            return false;
        }

        return Optionals.first(
            step.getActionAs(ConnectorAction.class).get().getDescriptor().getComponentScheme(),
            step.getConnection().get().getConnector().get().getComponentScheme()
        ).isPresent();
    }

    @Override
    public Optional<ProcessorDefinition<?>> handle(final Step step, final ProcessorDefinition<?> route, final IntegrationRouteBuilder builder, final String flowIndex, final String stepIndex) {
        // Model
        final Connection connection = step.getConnection().get();
        final Connector connector = connection.getConnector().get();
        final ConnectorAction action = step.getActionAs(ConnectorAction.class).get();
        final ConnectorDescriptor descriptor = action.getDescriptor();

        // Camel
        final String scheme = Optionals.first(descriptor.getComponentScheme(), connector.getComponentScheme()).get();
        final CamelContext context = builder.getContext();
        final String componentId = scheme + "-" + flowIndex + "-" + stepIndex;
        LOGGER.info("Resolving component: {} {} {} {}", componentId, scheme, connector, descriptor);
        final ComponentProxyComponent component = resolveComponent(componentId, scheme, context, connector, descriptor);
        LOGGER.info("Got component: {}, {}, {}", component, component.getComponentId(), component.getComponentScheme());
        final Map<String, String> properties = CollectionsUtils.aggregate(connection.getConfiguredProperties(), step.getConfiguredProperties());
        final Map<String, ConfigurationProperty> configurationProperties = CollectionsUtils.aggregate(connector.getProperties(), action.getProperties());

        // Add ConfigurationProperty's default value to the available properties.
        // Workaround for https://github.com/syndesisio/syndesis/issues/1713
        for (Map.Entry<String, ConfigurationProperty> entry: configurationProperties.entrySet()) {
            if (ObjectHelper.isNotEmpty(entry.getValue().getDefaultValue())) {
                properties.putIfAbsent(entry.getKey(), Objects.toString(entry.getValue().getDefaultValue(), null));
            }
        }

        // if the option is marked as secret use property placeholder as the
        // value is added to the integration secret.
        properties.entrySet()
            .stream()
            .filter(Predicates.or(connector::isSecret, action::isSecret))
            .forEach(e -> e.setValue(String.format("{{flow-%s.%s-%s.%s}}", flowIndex, scheme, stepIndex, e.getKey())));

        // raw values.
        properties.entrySet()
            .stream()
            .filter(Predicates.or(connector::isRaw, action::isRaw))
            .forEach(e -> e.setValue(String.format("RAW(%s)", e.getValue())));

        //Connector/Action properties have the precedence
        connector.getConfiguredProperties().forEach(properties::put);
        descriptor.getConfiguredProperties().forEach(properties::put);

        final Map<String, Object> proxyProperties = new HashMap<>(properties);

        // Set input/output data shape if the component proxy implements
        // Input/OutputDataShapeAware
        descriptor.getInputDataShape().ifPresent(ds -> trySetInputDataShape(component, ds));
        descriptor.getOutputDataShape().ifPresent(ds -> trySetOutputDataShape(component, ds));

        // Try to set properties to the component
        HandlerCustomizer.setProperties(context, component, proxyProperties);

        component.setCamelContext(context);

        HandlerCustomizer.customizeComponent(context, connector, descriptor, component, proxyProperties);

        component.setOptions(proxyProperties);

        // Remove component
        context.removeComponent(component.getComponentId());
        try {
            context.removeService(component);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to remove component `" + component.getComponentId() + "` from Camel managed services", e);
        }

        // Register component
        try {
            context.addService(component, true, true);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to add component `" + component.getComponentId() + "` to Camel managed services", e);
        }
        context.addComponent(component.getComponentId(), component);

        final ProcessorDefinition<?> definition;
        // Camel 3.2 makes mandatory to include a contextPath
        // Syndesis Component Proxy will take care to change that value with the real component context path needed
        String uri = formatUriWithPlaceholderContextPath(componentId);
        if (route == null) {
            // we're at step 0
            definition = builder.from(uri);
        } else {
            // route exists we passed step 0
            final Pattern pattern = action.getPattern().orElse(Pattern.To);
            switch (pattern) {
            case To:
            case Pipe:
            case From: // sql-start connector uses From
                definition = route.to(uri);
                break;
            case PollEnrich:
                definition = route.process(pollEnricher(uri, component));
                break;
            default:
                throw new UnsupportedOperationException("'" + pattern + "' pattern not supported");
            }
        }

        return Optional.ofNullable(definition);
    }

    private static String formatUriWithPlaceholderContextPath(String aliasScheme) {
        if (aliasScheme.indexOf(':') < 0) {
            LOGGER.warn("Appending a context path placeholder to original scheme {}. Required since Camel 3.2.", aliasScheme);
            return String.format("%s:%s", aliasScheme, "SyndesisContextPathPlaceholder");
        } else {
            return aliasScheme;
        }
    }

    // *************************
    // Helpers
    // *************************

    private static ComponentProxyComponent resolveComponent(String componentId, String componentScheme, CamelContext context, Connector connector, ConnectorDescriptor descriptor) {
        ComponentProxyFactory factory = ComponentProxyComponent::new;

        if (descriptor.getConnectorFactory().isPresent()) {
            factory = resolveComponentProxyFactory(context, descriptor.getConnectorFactory()).orElse(factory);
        } else if (connector.getConnectorFactory().isPresent()) {
            factory = resolveComponentProxyFactory(context, connector.getConnectorFactory()).orElse(factory);
        }

        return factory.newInstance(componentId, componentScheme);
    }

    private static Optional<ComponentProxyFactory> resolveComponentProxyFactory(CamelContext context, Optional<String> componentProxyFactory) {
        ComponentProxyFactory factory = null;

        if (componentProxyFactory.isPresent()) {
            final String factoryType = componentProxyFactory.get();
            final ClassResolver resolver = context.getClassResolver();

            Class<? extends ComponentProxyFactory> type = resolver.resolveClass(factoryType, ComponentProxyFactory.class);
            if (type == null) {
                throw new IllegalArgumentException("Unable to resolve a ComponentProxyFactory of type: " + factoryType);
            }

            factory = context.getInjector().newInstance(type);
            if (factory == null) {
                throw new IllegalArgumentException("Unable to instantiate a ComponentProxyFactory of type: " + factoryType);
            }
        }

        return Optional.ofNullable(factory);
    }

}
