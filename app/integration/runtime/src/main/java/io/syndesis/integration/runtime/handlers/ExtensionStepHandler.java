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
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.support.PropertyBindingSupport;
import org.apache.camel.util.ObjectHelper;
import io.syndesis.common.model.action.StepAction;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.extension.api.Step;
import io.syndesis.integration.runtime.IntegrationRouteBuilder;
import io.syndesis.integration.runtime.IntegrationStepHandler;
import io.syndesis.integration.runtime.util.StringHelpers;

public class ExtensionStepHandler implements IntegrationStepHandler{
    @Override
    public boolean canHandle(io.syndesis.common.model.integration.Step step) {
        if (StepKind.extension != step.getStepKind()) {
            return false;
        }

        return step.getActionAs(StepAction.class).isPresent();
    }

    @Override
    public Optional<ProcessorDefinition<?>> handle(final io.syndesis.common.model.integration.Step step, final ProcessorDefinition<?> route, final IntegrationRouteBuilder builder, final String flowIndex, final String stepIndex) {
        ObjectHelper.notNull(route, "route");

        // Model
        final StepAction action = step.getActionAs(StepAction.class).get();

        // Camel
        final Map<String, String> properties = step.getConfiguredProperties();
        final CamelContext context = builder.getContext();

        final ProcessorDefinition<?> definition;
        if (action.getDescriptor().getKind() == StepAction.Kind.ENDPOINT) {
            for (Map.Entry<String, String> entry: properties.entrySet()) {
                route.setHeader(entry.getKey(), builder.constant(entry.getValue()));
            }

            definition = route.to(action.getDescriptor().getEntrypoint());
        } else if (action.getDescriptor().getKind() == StepAction.Kind.BEAN) {
            String function = action.getDescriptor().getEntrypoint();
            if (ObjectHelper.isEmpty(function)) {
                return Optional.empty();
            }

            int idx = function.indexOf("::");
            String method = null;
            if (idx > 0 && !function.endsWith("::")) {
                method = function.substring(idx + 2);
                function = function.substring(0, idx);
            }

            String options = null;
            if (ObjectHelper.isNotEmpty(properties)) {
                options = properties.entrySet().stream()
                    .filter(entry -> ObjectHelper.isNotEmpty(entry.getKey()))
                    .filter(entry -> ObjectHelper.isNotEmpty(entry.getValue()))
                    .map(entry -> "bean." + entry.getKey() + "=" + StringHelpers.sanitizeForURI(entry.getValue()))
                    .collect(Collectors.joining("&"));
            }

            StringBuilder uri = new StringBuilder("class:").append(function);
            if (method != null) {
                uri.append("?method=").append(method);

                if (options != null){
                    uri.append('&').append(options);
                }
            } else if (options != null){
                uri.append('?').append(options);
            }

            definition = route.to(uri.toString());
        } else if (action.getDescriptor().getKind() == StepAction.Kind.STEP) {
            final String target = action.getDescriptor().getEntrypoint();

            if (!ObjectHelper.isEmpty(target)) {
                try {
                    final Class<Step> clazz = context.getClassResolver().resolveMandatoryClass(target, Step.class);
                    final Step stepExtension = context.getInjector().newInstance(clazz);
                    final Map<String, Object> props = new HashMap<>(properties);

                    try {
                        PropertyBindingSupport.bindProperties(context, stepExtension, props);
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }

                    // Set the camel context if the step extension object implements
                    // CamelContextAware, this is a shortcut to retrieve it from
                    // the handler method.
                    CamelContextAware.trySetCamelContext(stepExtension, context);

                    return stepExtension.configure(context, route, props);
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException(e);
                }
            }

            definition = route;
        } else {
            definition = route;
        }

        return Optional.of(definition);
    }
}
