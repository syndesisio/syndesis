/*
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.integration.runtime.stephandlers;

import java.util.HashMap;
import java.util.Map;

import com.google.auto.service.AutoService;
import io.syndesis.integration.model.steps.Extension;
import io.syndesis.integration.model.steps.Step;
import io.syndesis.integration.runtime.StepHandler;
import io.syndesis.integration.runtime.SyndesisRouteBuilder;
import io.syndesis.integration.runtime.api.SyndesisStepExtension;
import io.syndesis.integration.support.Strings;
import org.apache.camel.CamelContext;
import org.apache.camel.TypeConverter;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.util.IntrospectionSupport;

@AutoService(StepHandler.class)
public class ExtensionHandler implements StepHandler<Extension> {
    @Override
    public boolean canHandle(Step step) {
        return step.getClass().equals(Extension.class);
    }

    @Override
    public ProcessorDefinition handle(Extension step, ProcessorDefinition route, SyndesisRouteBuilder routeBuilder) {
        final CamelContext context = routeBuilder.getContext();
        final TypeConverter converter = context.getTypeConverter();
        final String target = step.getName();

        if (!Strings.isEmpty(target)) {
            try {
                final Class<SyndesisStepExtension> clazz = context.getClassResolver().resolveMandatoryClass(target, SyndesisStepExtension.class);
                final SyndesisStepExtension stepExtension = context.getInjector().newInstance(clazz);
                final Map<String, Object> props = new HashMap<>(step.getProperties());

                try {
                    IntrospectionSupport.setProperties(context, converter, stepExtension, props);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }

                return stepExtension.configure(context, route, props);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }

        return route;
    }
}
