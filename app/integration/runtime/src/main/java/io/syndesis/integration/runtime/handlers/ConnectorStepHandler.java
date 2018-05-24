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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import io.syndesis.integration.component.proxy.ComponentProxyFactory;
import io.syndesis.integration.runtime.IntegrationRouteBuilder;
import io.syndesis.integration.runtime.IntegrationStepHandler;
import org.apache.camel.CamelContext;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.spi.ClassResolver;
import org.apache.camel.util.IntrospectionSupport;
import org.apache.camel.util.ObjectHelper;

import static io.syndesis.common.model.InputDataShapeAware.trySetInputDataShape;
import static io.syndesis.common.model.OutputDataShapeAware.trySetOutputDataShape;

@SuppressWarnings("PMD.ExcessiveImports")
public class ConnectorStepHandler implements IntegrationStepHandler, IntegrationStepHandler.Consumer {
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

    @SuppressWarnings("PMD")
    @Override
    public Optional<ProcessorDefinition<?>> handle(Step step, ProcessorDefinition<?> route, IntegrationRouteBuilder builder, final String index) {
        // Model
        final Connection connection = step.getConnection().get();
        final Connector connector = connection.getConnector().get();
        final ConnectorAction action = step.getActionAs(ConnectorAction.class).get();
        final ConnectorDescriptor descriptor = action.getDescriptor();

        // Camel
        final String scheme = Optionals.first(descriptor.getComponentScheme(), connector.getComponentScheme()).get();
        final CamelContext context = builder.getContext();
        final String componentId = scheme + "-" + index;
        final ComponentProxyComponent component = resolveComponent(componentId, scheme, context, connector, descriptor);
        final List<String> customizers = CollectionsUtils.aggregate(ArrayList::new, connector.getConnectorCustomizers(), descriptor.getConnectorCustomizers());
        final Map<String, String> properties = CollectionsUtils.aggregate(connection.getConfiguredProperties(), step.getConfiguredProperties());
        final Map<String, ConfigurationProperty> configurationProperties = CollectionsUtils.aggregate(connector.getProperties(), action.getProperties());

        // Add ConfigurationProperty's default value to the available properties.
        // Workaround for https://github.com/syndesisio/syndesis/issues/1713
        for (Map.Entry<String, ConfigurationProperty> entry: configurationProperties.entrySet()) {
            if (ObjectHelper.isNotEmpty(entry.getValue().getDefaultValue())) {
                properties.putIfAbsent(entry.getKey(), entry.getValue().getDefaultValue());
            }
        }

        // if the option is marked as secret use property placeholder as the
        // value is added to the integration secret.
        properties.entrySet()
            .stream()
            .filter(Predicates.or(connector::isSecret, action::isSecret))
            .forEach(e -> e.setValue(String.format("{{%s-%s.%s}}", scheme, index, e.getKey())));

        // raw values.
        properties.entrySet()
            .stream()
            .filter(Predicates.or(connector::isRaw, action::isRaw))
            .forEach(e -> e.setValue(String.format("RAW(%s)", e.getValue())));

        //Connector/Action properties have the precedence
        connector.getConfiguredProperties().forEach(properties::put);
        descriptor.getConfiguredProperties().forEach(properties::put);

        try {
            final Map<String, Object> proxyProperties = new HashMap<>(properties);

            // Set input/output data shape if the component proxy implements
            // Input/OutputDataShapeAware
            descriptor.getInputDataShape().ifPresent(ds -> trySetInputDataShape(component, ds));
            descriptor.getOutputDataShape().ifPresent(ds -> trySetOutputDataShape(component, ds));

            // Try to set properties to the component
            setProperties(context, component, proxyProperties);

            for (String customizerType : customizers) {
                final ComponentProxyCustomizer customizer = resolveCustomizer(context, customizerType);

                // Set the camel context if the customizer implements
                // the CamelContextAware interface.
                ObjectHelper.trySetCamelContext(customizer, context);

                // Set input/output data shape if the customizer implements
                // Input/OutputDataShapeAware
                descriptor.getInputDataShape().ifPresent(ds -> trySetInputDataShape(customizer, ds));
                descriptor.getOutputDataShape().ifPresent(ds -> trySetOutputDataShape(customizer, ds));

                // Try to set properties to the component
                setProperties(context, customizer, proxyProperties);

                // Invoke the customizer
                customizer.customize(component, proxyProperties);
            }

            component.setCamelContext(context);
            component.setOptions(proxyProperties);

            // Remove component
            context.removeComponent(component.getComponentId());
            context.removeService(component);

            // Register component
            context.addService(component, true, true);
            context.addComponent(component.getComponentId(), component);

            if (route == null) {
                route = builder.from(componentId);
            } else {
                route = route.to(componentId);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return Optional.ofNullable(route);
    }

    // *************************
    // Helpers
    // *************************

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    private void setProperties(CamelContext context, Object target, Map<String, Object> properties) throws Exception {
        final Iterator<Map.Entry<String, Object>> iterator = properties.entrySet().iterator();

        while (iterator.hasNext()) {
            final Map.Entry<String, Object> entry = iterator.next();

            String key = entry.getKey();
            Object val = entry.getValue();

            if (val instanceof String) {
                val = context.resolvePropertyPlaceholders((String) val);
            }

            // Bind properties to the customizer
            if (IntrospectionSupport.setProperty(context, target, key, val)) {
                // Remove property if bound to the customizer.
                iterator.remove();
            }
        }
    }

    private ComponentProxyComponent resolveComponent(String componentId, String componentScheme, CamelContext context, Connector connector, ConnectorDescriptor descriptor) {
        ComponentProxyFactory factory = ComponentProxyComponent::new;

        if (descriptor.getConnectorFactory().isPresent()) {
            factory = resolveComponentProxyFactory(context, descriptor.getConnectorFactory()).orElse(factory);
        } else if (connector.getConnectorFactory().isPresent()) {
            factory = resolveComponentProxyFactory(context, connector.getConnectorFactory()).orElse(factory);
        }

        return factory.newInstance(componentId, componentScheme);
    }

    private Optional<ComponentProxyFactory> resolveComponentProxyFactory(CamelContext context, Optional<String> componentProxyFactory) {
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

    private ComponentProxyCustomizer resolveCustomizer(CamelContext context, String customizerType) {
        Class<ComponentProxyCustomizer> type = context.getClassResolver().resolveClass(customizerType, ComponentProxyCustomizer.class);
        if (type == null) {
            throw new IllegalArgumentException("Unable to resolve a ComponentProxyCustomizer of type: " + customizerType);
        }

        final ComponentProxyCustomizer customizer = context.getInjector().newInstance(type);
        if (customizer == null) {
            throw new IllegalArgumentException("Unable to instantiate a ComponentProxyCustomizer of type: " + customizerType);
        }

        return customizer;
    }
}
