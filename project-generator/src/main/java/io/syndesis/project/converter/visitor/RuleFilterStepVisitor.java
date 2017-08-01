package io.syndesis.project.converter.visitor;

import io.syndesis.model.filter.RuleFilterStep;

/**
 * @author roland
 * @since 01.08.17
 */
public class RuleFilterStepVisitor extends FilterStepVisitor {


    RuleFilterStepVisitor(GeneratorContext generatorContext) {
        super(generatorContext);
    }

    public static class Factory implements StepVisitorFactory<RuleFilterStepVisitor> {
        @Override
        public String getStepKind() {
            return RuleFilterStep.STEP_KIND;
        }

        @Override
        public RuleFilterStepVisitor create(GeneratorContext generatorContext) {
            return new RuleFilterStepVisitor(generatorContext);
        }
    }

    @Override
    protected String getStepKind() {
        return RuleFilterStep.STEP_KIND;
    }
}
