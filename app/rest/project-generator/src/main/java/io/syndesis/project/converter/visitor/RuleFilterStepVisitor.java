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

import io.syndesis.model.filter.RuleFilterStep;

/**
 * @author roland
 * @since 01.08.17
 */
public class RuleFilterStepVisitor extends FilterStepVisitor {
    public static class Factory implements StepVisitorFactory<RuleFilterStepVisitor> {
        @Override
        public String getStepKind() {
            return RuleFilterStep.STEP_KIND;
        }

        @Override
        public RuleFilterStepVisitor create() {
            return new RuleFilterStepVisitor();
        }
    }

    @Override
    protected String getStepKind() {
        return RuleFilterStep.STEP_KIND;
    }
}
