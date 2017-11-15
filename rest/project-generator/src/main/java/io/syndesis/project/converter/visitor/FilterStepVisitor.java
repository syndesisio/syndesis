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

package io.syndesis.project.converter.visitor;

import io.syndesis.integration.model.steps.Filter;
import io.syndesis.model.filter.FilterStep;
import io.syndesis.model.integration.Step;

import java.util.ArrayList;
import java.util.List;


public abstract class FilterStepVisitor implements StepVisitor {

    private final GeneratorContext generatorContext;

    FilterStepVisitor(GeneratorContext generatorContext) {
        this.generatorContext = generatorContext;
    }

    @Override
    public io.syndesis.integration.model.steps.Step visit(StepVisitorContext stepContext) {
        Step step = stepContext.getStep();
        if (step instanceof FilterStep && step.getStepKind().equals(getStepKind())) {
            Filter filter = createFilter((FilterStep) step);
            List<io.syndesis.integration.model.steps.Step> steps = new ArrayList<>();
            while (stepContext.hasNext()) {
                steps.add(visit(stepContext.next()));
            }
            filter.setSteps(steps);
            return filter;
        }

        StepVisitorFactory<?> factory = generatorContext.getVisitorFactoryRegistry().get(stepContext.getStep().getStepKind());
        StepVisitor visitor = factory.create(generatorContext);
        return visitor.visit(stepContext);
    }

    private Filter createFilter(FilterStep s) {
        Filter ret = new Filter();
        ret.setExpression(s.getFilterExpression());
        return ret;
    }

    protected abstract String getStepKind();
}
