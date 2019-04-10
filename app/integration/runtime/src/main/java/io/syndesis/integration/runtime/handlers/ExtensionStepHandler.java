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

import io.syndesis.extension.api.Step;
import io.syndesis.integration.runtime.IntegrationRouteBuilder;
import io.syndesis.integration.runtime.IntegrationStepHandler;
import io.syndesis.integration.runtime.util.StringHelpers;
import io.syndesis.common.model.action.StepAction;
import io.syndesis.common.model.integration.StepKind;
import org.apache.camel.CamelContext;
import org.apache.camel.TypeConverter;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.util.IntrospectionSupport;
import org.apache.camel.util.ObjectHelper;

public class ExtensionStepHandler implements IntegrationStepHandler{
    @Override
    public boolean canHandle(io.syndesis.common.model.integration.Step step) {
        if (StepKind.extension != step.getStepKind()) {
            return false;
        }

        return step.getActionAs(StepAction.class).isPresent();
    }

    @SuppressWarnings("PMD")
    @Override
    public Optional<ProcessorDefinition<?>> handle(io.syndesis.common.model.integration.Step step, ProcessorDefinition<?> route, IntegrationRouteBuilder builder, String flowIndex, String stepIndex) {
        ObjectHelper.notNull(route, "route");

        // Model
        final StepAction action = step.getActionAs(StepAction.class).get();

        // Camel
        final Map<String, String> properties = step.getConfiguredProperties();
        final CamelContext context = builder.getContext();

        if (action.getDescriptor().getKind() == StepAction.Kind.ENDPOINT) {
            for (Map.Entry<String, String> entry: properties.entrySet()) {
                route.setHeader(entry.getKey(), builder.constant(entry.getValue()));
            }

            route = route.to(action.getDescriptor().getEntrypoint());
        } else if (action.getDescriptor().getKind() == StepAction.Kind.BEAN) {
            String method = null;
            String function = action.getDescriptor().getEntrypoint();
            String options = null;

            if (ObjectHelper.isEmpty(function)) {
                return Optional.empty();
            }

            int idx = function.indexOf("::");
            if (idx > 0 && !function.endsWith("::")) {
                method = function.substring(idx + 2);
                function = function.substring(0, idx);
            }

            if (ObjectHelper.isNotEmpty(properties)) {
                options = properties.entrySet().stream()
                    .filter(entry -> ObjectHelper.isNotEmpty(entry.getKey()))
                    .filter(entry -> ObjectHelper.isNotEmpty(entry.getValue()))
                    .map(entry -> "bean." + entry.getKey() + "=" + StringHelpers.sanitizeForURI(entry.getValue()))
                    .collect(Collectors.joining("&"));
            }

            String uri = "class:" + function;
            if (method != null) {
                uri += "?method=" + method;

                if (options != null){
                    uri += "&" + options;
                }
            } else if (options != null){
                uri += "?" + options;
            }

            route = route.to(uri);
        } else if (action.getDescriptor().getKind() == StepAction.Kind.STEP) {
            final String target = action.getDescriptor().getEntrypoint();
            final TypeConverter converter = context.getTypeConverter();

            if (!ObjectHelper.isEmpty(target)) {
                try {
                    final Class<Step> clazz = context.getClassResolver().resolveMandatoryClass(target, Step.class);
                    final Step stepExtension = context.getInjector().newInstance(clazz);
                    final Map<String, Object> props = new HashMap<>(properties);

                    try {
                        IntrospectionSupport.setProperties(context, converter, stepExtension, props);
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }

                    // Set the camel context if the step extension object implements
                    // CamelContextAware, this is a shortcut to retrieve it from
                    // the handler method.
                    ObjectHelper.trySetCamelContext(stepExtension, context);

                    @SuppressWarnings({"rawtypes", "unchecked"})
                    final Optional<ProcessorDefinition<?>> configured = (Optional) stepExtension.configure(context, route, props);

                    return configured;
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException(e);
                }
            }
        }

        return Optional.of(route);
    }
}
