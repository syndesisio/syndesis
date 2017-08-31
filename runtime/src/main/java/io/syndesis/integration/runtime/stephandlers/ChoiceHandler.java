/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.integration.runtime.stephandlers;
import com.google.auto.service.AutoService;
import io.syndesis.integration.model.steps.Choice;
import io.syndesis.integration.model.steps.Filter;
import io.syndesis.integration.model.steps.Otherwise;
import io.syndesis.integration.model.steps.Step;
import io.syndesis.integration.runtime.StepHandler;
import io.syndesis.integration.runtime.SyndesisRouteBuilder;
import org.apache.camel.Predicate;
import org.apache.camel.model.ChoiceDefinition;
import org.apache.camel.model.ProcessorDefinition;

import java.util.List;

import static io.syndesis.integration.support.Lists.notNullList;

@AutoService(StepHandler.class)
public class ChoiceHandler implements StepHandler<Choice> {
  @Override
  public boolean canHandle(Step step) {
    return step.getClass().equals(Choice.class);
  }

  @Override
  public ProcessorDefinition handle(Choice step, ProcessorDefinition route, SyndesisRouteBuilder routeBuilder) {
    ChoiceDefinition choice = route.choice();
    List<Filter> filters = notNullList(step.getFilters());
    for (Filter filter : filters) {
      Predicate predicate = routeBuilder.getMandatorySimplePredicate(filter, filter.getExpression());
      ChoiceDefinition when = choice.when(predicate);
      route = routeBuilder.addSteps(when, filter.getSteps());
    }
    Otherwise otherwiseStep = step.getOtherwise();
    if (otherwiseStep != null) {
      List<Step> otherwiseSteps = notNullList(otherwiseStep.getSteps());
      if (!otherwiseSteps.isEmpty()) {
        ChoiceDefinition otherwise = choice.otherwise();
        route = routeBuilder.addSteps(otherwise, otherwiseSteps);
      }
    }

    return route;
  }
}
