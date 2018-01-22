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
package io.syndesis.integration.runtime.stephandlers;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.auto.service.AutoService;
import io.syndesis.integration.model.steps.Function;
import io.syndesis.integration.model.steps.Step;
import io.syndesis.integration.runtime.StepHandler;
import io.syndesis.integration.runtime.SyndesisRouteBuilder;
import io.syndesis.integration.runtime.util.StringHelpers;
import org.apache.camel.CamelContext;
import org.apache.camel.NoTypeConversionAvailableException;
import org.apache.camel.TypeConverter;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.util.ObjectHelper;

@AutoService(StepHandler.class)
public class FunctionHandler implements StepHandler<Function> {
    @Override
    public boolean canHandle(Step step) {
        return step.getClass().equals(Function.class);
    }

    @Override
    public Optional<ProcessorDefinition> handle(Function step, ProcessorDefinition route, SyndesisRouteBuilder routeBuilder) {
        final CamelContext context = routeBuilder.getContext();
        final TypeConverter converter = context.getTypeConverter();

        String method = null;
        String function = step.getName();
        String options = null;

        if (ObjectHelper.isEmpty(function)) {
            return Optional.empty();
        }

        int idx = function.indexOf("::");
        if (idx > 0 && !function.endsWith("::")) {
            method = function.substring(idx + 2);
            function = function.substring(0, idx);
        }

        Map<String, Object> headers = step.getProperties();
        if (ObjectHelper.isNotEmpty(headers)) {
            options = headers.entrySet().stream()
                .filter(entry -> Objects.nonNull(entry.getValue()))
                .map(entry -> asBeanParameter(converter, entry))
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

        return Optional.of(route.to(uri));
    }

    private String asBeanParameter(TypeConverter converter, Map.Entry<String, Object> entry) {
        try {
            return "bean." + entry.getKey() + "=" + StringHelpers.sanitizeForURI(converter.mandatoryConvertTo(String.class, entry.getValue()));
        } catch (NoTypeConversionAvailableException e) {
            throw new IllegalStateException(e);
        }
    }
}
