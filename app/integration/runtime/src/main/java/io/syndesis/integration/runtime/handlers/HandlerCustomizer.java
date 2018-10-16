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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.util.CollectionsUtils;
import io.syndesis.integration.component.proxy.ComponentCustomizer;

import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.util.IntrospectionSupport;
import org.apache.camel.util.ObjectHelper;

import static io.syndesis.common.model.InputDataShapeAware.trySetInputDataShape;
import static io.syndesis.common.model.OutputDataShapeAware.trySetOutputDataShape;

final class HandlerCustomizer {

    private HandlerCustomizer() {
        // utility class
    }

    public static void customizeComponent(final CamelContext context, final Connector connector, final ConnectorDescriptor descriptor, final Component component,
        final Map<String, Object> properties) {
        final List<String> customizers = CollectionsUtils.aggregate(ArrayList::new, connector.getConnectorCustomizers(), descriptor.getConnectorCustomizers());

        // Set input/output data shape if the component proxy implements
        // Input/OutputDataShapeAware
        descriptor.getInputDataShape().ifPresent(ds -> trySetInputDataShape(component, ds));
        descriptor.getOutputDataShape().ifPresent(ds -> trySetOutputDataShape(component, ds));

        for (final String customizerType : customizers) {
            final ComponentCustomizer<Component> customizer = resolveCustomizer(context, customizerType);

            // Set the camel context if the customizer implements
            // the CamelContextAware interface.
            ObjectHelper.trySetCamelContext(customizer, context);

            // Set input/output data shape if the customizer implements
            // Input/OutputDataShapeAware
            descriptor.getInputDataShape().ifPresent(ds -> trySetInputDataShape(customizer, ds));
            descriptor.getOutputDataShape().ifPresent(ds -> trySetOutputDataShape(customizer, ds));

            // Try to set properties to the component
            setProperties(context, customizer, properties);

            // Invoke the customizer
            customizer.customize(component, properties);
        }
    }

    public static void setProperties(final CamelContext context, final Object target, final Map<String, Object> properties) {
        final Iterator<Map.Entry<String, Object>> iterator = properties.entrySet().iterator();

        while (iterator.hasNext()) {
            final Map.Entry<String, Object> entry = iterator.next();

            final String key = entry.getKey();
            Object val = entry.getValue();

            try {
                if (val instanceof String) {
                    val = context.resolvePropertyPlaceholders((String) val);
                }

                // Bind properties to the customizer
                if (IntrospectionSupport.setProperty(context, target, key, val)) {
                    // Remove property if bound to the customizer.
                    iterator.remove();
                }
            } catch (final Exception e) {
                throw new IllegalStateException("Unable to set property `" + key + "` = `" + val + "`", e);
            }
        }
    }

    private static ComponentCustomizer<Component> resolveCustomizer(final CamelContext context, final String customizerType) {
        @SuppressWarnings("rawtypes")
        final Class<ComponentCustomizer> type = context.getClassResolver().resolveClass(customizerType, ComponentCustomizer.class);
        if (type == null) {
            throw new IllegalArgumentException("Unable to resolve a ComponentProxyCustomizer of type: " + customizerType);
        }

        @SuppressWarnings("unchecked")
        final ComponentCustomizer<Component> customizer = context.getInjector().newInstance(type);
        if (customizer == null) {
            throw new IllegalArgumentException("Unable to instantiate a ComponentProxyCustomizer of type: " + customizerType);
        }

        return customizer;
    }
}
