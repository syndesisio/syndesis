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
package io.syndesis.project.converter.visitor;

import io.syndesis.integration.model.steps.Split;
import io.syndesis.model.split.SplitStep;

import java.util.Collection;
import java.util.Collections;


public class SplitStepVisitor implements StepVisitor {

    protected String getStepKind() {
        return SplitStep.STEP_KIND;
    }

    @Override
    public Collection<io.syndesis.integration.model.steps.Step> visit(StepVisitorContext stepContext) {
        io.syndesis.model.integration.Step step = stepContext.getStep();
        if (step instanceof SplitStep && step.getStepKind().equals(getStepKind())) {
            Split split = new Split(((SplitStep) step).getExpression());

            return Collections.singletonList(split);
        }

        return Collections.emptyList();
    }

    public static class Factory implements StepVisitorFactory<SplitStepVisitor> {
        @Override
        public String getStepKind() {
            return SplitStep.STEP_KIND;
        }

        @Override
        public SplitStepVisitor create() {
            return new SplitStepVisitor();
        }
    }
}
