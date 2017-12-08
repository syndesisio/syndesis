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

import com.google.auto.service.AutoService;
import io.syndesis.integration.model.steps.Split;
import io.syndesis.integration.model.steps.Step;
import io.syndesis.integration.runtime.StepHandler;
import io.syndesis.integration.runtime.SyndesisRouteBuilder;
import io.syndesis.integration.runtime.util.JsonSimpleHelpers;
import org.apache.camel.CamelContext;
import org.apache.camel.Expression;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.dataformat.JsonLibrary;

@AutoService(StepHandler.class)
public class SplitHandler implements StepHandler<Split> {
    @Override
    public boolean canHandle(Step step) {
        return step.getClass().equals(Split.class);
    }

    @Override
    public ProcessorDefinition handle(Split step, ProcessorDefinition route, SyndesisRouteBuilder routeBuilder) {
        CamelContext context = routeBuilder.getContext();
        Expression expression = JsonSimpleHelpers.getMandatoryExpression(context, step, step.getExpression());
        ProcessorDefinition split = route.split(expression).marshal().json(JsonLibrary.Jackson);
        return routeBuilder.addSteps(split, step.getSteps());
    }
}
