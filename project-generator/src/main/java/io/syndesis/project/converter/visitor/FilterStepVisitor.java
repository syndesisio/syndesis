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

import io.fabric8.funktion.model.StepKinds;
import io.fabric8.funktion.model.steps.Filter;
import io.syndesis.model.filter.FilterStep;
import io.syndesis.model.integration.Step;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class FilterStepVisitor implements StepVisitor {

    private static final String SPACE = " ";
    private final GeneratorContext generatorContext;

    public static class Factory implements StepVisitorFactory<FilterStepVisitor> {

        @Override
        public String getStepKind() {
            return StepKinds.FILTER;
        }

        @Override
        public FilterStepVisitor create(GeneratorContext generatorContext) {
            return new FilterStepVisitor(generatorContext);
        }
    }

    public FilterStepVisitor(GeneratorContext generatorContext) {
        this.generatorContext = generatorContext;
    }

    @Override
    public io.fabric8.funktion.model.steps.Step visit(StepVisitorContext stepContext) {
        Step s = stepContext.getStep();
        if (s instanceof FilterStep) {
            Filter filter = new Filter();
            FilterStep step = (FilterStep) s;

            switch (step.getType()) {
                case RULE:
                    filter.setExpression(createExpression(step));
                    break;
                case TEXT:
                    filter.setExpression(step.getSimple());
                    break;
                default:
                    //do nothing
                    break;
            }

            List<io.fabric8.funktion.model.steps.Step> steps = new ArrayList<>();
            while (stepContext.hasNext()) {
                steps.add(visit(stepContext.next()));
            }
            filter.setSteps(steps);
            return filter;
        } else {
            StepVisitorFactory factory = generatorContext.getVisitorFactoryRegistry().get(stepContext.getStep().getStepKind());
            StepVisitor visitor = factory.create(generatorContext);
            return visitor.visit(stepContext);
        }
    }


    protected static String createExpression(FilterStep step) {
        return step.getRules().stream().map(r ->
            r.getPath() + SPACE +
                r.getOp() + SPACE +
                r.getValue()).collect(Collectors.joining(SPACE + step.getPredicate().getOperator() + SPACE));
    }
}
