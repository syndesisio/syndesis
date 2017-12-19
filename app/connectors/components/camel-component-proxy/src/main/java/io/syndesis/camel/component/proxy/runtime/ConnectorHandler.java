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
package io.syndesis.camel.component.proxy.runtime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.syndesis.camel.component.proxy.ComponentProxyComponent;
import io.syndesis.camel.component.proxy.ComponentProxyCustomizer;
import io.syndesis.camel.component.proxy.ComponentProxyFactory;
import io.syndesis.integration.model.steps.Step;
import io.syndesis.integration.runtime.StepHandler;
import io.syndesis.integration.runtime.SyndesisRouteBuilder;
import org.apache.camel.CamelContext;
import org.apache.camel.TypeConverter;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.spi.ClassResolver;
import org.apache.camel.spi.Injector;
import org.apache.camel.util.IntrospectionSupport;
import org.apache.camel.util.ObjectHelper;

public class ConnectorHandler implements StepHandler<Connector> {
    @Override
    public boolean canHandle(Step step) {
        return step.getClass().equals(Connector.class);
    }

    @Override
    public Optional<ProcessorDefinition> handle(Connector step, ProcessorDefinition definition, SyndesisRouteBuilder builder) {
        try {
            final CamelContext context = builder.getContext();
            final TypeConverter converter = context.getTypeConverter();
            final Map<String, Object> options = new HashMap<>(step.getProperties());
            final ComponentProxyComponent component = resolveComponent(context, step);
            final List<String> customizers = step.getCustomizers();

            if (ObjectHelper.isNotEmpty(customizers)) {
                for (Map.Entry<String, Object> entry : options.entrySet()) {
                    Object val = entry.getValue();

                    if (val instanceof String) {
                        // Resolve placeholder
                        val = context.resolvePropertyPlaceholders((String) val);

                        // Update entry with resolved value
                        entry.setValue(val);
                    }
                }

                for (String customizerType : customizers) {
                    ComponentProxyCustomizer customizer = resolveCustomizer(context, customizerType);

                    // Set the camel context if the customizer implements
                    // the CamelContextAware interface.
                    ObjectHelper.trySetCamelContext(customizer, context);

                    // Bind properties to the customizer
                    IntrospectionSupport.setProperties(converter, customizer, options);

                    customizer.customize(component, options);
                }
            }

            // Set connector options with remaining properties and original values
            // to keep placeholders and delegate resolution to the final endpoint.
            component.setOptions(
                step.getProperties().entrySet().stream()
                    .filter(e -> options.containsKey(e.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
            );

            // Remove component
            context.removeComponent(component.getComponentId());
            context.removeService(component);

            // Register component
            context.addService(component, true, true);
            context.addComponent(component.getComponentId(), component);

            if (definition == null) {
                definition = builder.from(step.getComponentId());
            } else {
                definition = definition.to(step.getComponentId());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return Optional.of(definition);
    }

    // *************************
    // Helpers
    // *************************

    private ComponentProxyComponent resolveComponent(CamelContext context, Connector step) {
        ComponentProxyFactory factory = ComponentProxyComponent::new;

        if (step.getFactory() != null ) {
            final ClassResolver resolver = context.getClassResolver();
            final Injector injector = context.getInjector();

            Class<? extends ComponentProxyFactory> type = resolver.resolveClass(step.getFactory(), ComponentProxyFactory.class);
            if (type == null) {
                throw new IllegalArgumentException("Unable to resolve a ComponentProxyFactory of type: " + step.getFactory());
            }

            factory = injector.newInstance(type);
            if (factory == null) {
                throw new IllegalArgumentException("Unable to instantiate a ComponentProxyFactory of type: " + step.getFactory());
            }
        }

        return factory.newInstance(step.getComponentId(), step.getComponentScheme());
    }


    private ComponentProxyCustomizer resolveCustomizer(CamelContext context, String customizerType) {
        final ComponentProxyCustomizer customizer;

        Class<ComponentProxyCustomizer> type = context.getClassResolver().resolveClass(customizerType, ComponentProxyCustomizer.class);
        if (type == null) {
            throw new IllegalArgumentException("Unable to resolve a ComponentProxyCustomizer of type: " + customizerType);
        }

        customizer = context.getInjector().newInstance(type);
        if (customizer == null) {
            throw new IllegalArgumentException("Unable to instantiate a ComponentProxyCustomizer of type: " + customizerType);
        }

        return customizer;
    }
}
