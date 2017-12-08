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
import io.syndesis.integration.model.steps.Filter;
import io.syndesis.integration.model.steps.Step;
import io.syndesis.integration.runtime.StepHandler;
import io.syndesis.integration.runtime.SyndesisRouteBuilder;
import io.syndesis.integration.runtime.util.JsonSimpleHelpers;
import org.apache.camel.CamelContext;
import org.apache.camel.Predicate;
import org.apache.camel.model.FilterDefinition;
import org.apache.camel.model.ProcessorDefinition;

@AutoService(StepHandler.class)
public class FilterHandler implements StepHandler<Filter> {
    @Override
    public boolean canHandle(Step step) {
        return step.getClass().equals(Filter.class);
    }

  @Override
  public ProcessorDefinition handle(Filter step, ProcessorDefinition route, SyndesisRouteBuilder routeBuilder) {
    CamelContext context = routeBuilder.getContext();
    Predicate predicate = JsonSimpleHelpers.getMandatorySimplePredicate(context, step, step.getExpression());
    FilterDefinition filter = route.filter(predicate);
    return routeBuilder.addSteps(filter, step.getSteps());
  }
}

