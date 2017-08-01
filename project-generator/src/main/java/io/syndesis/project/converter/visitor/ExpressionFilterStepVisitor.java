package io.syndesis.project.converter.visitor;

import io.syndesis.model.filter.ExpressionFilterStep;

/**
 * @author roland
 * @since 01.08.17
 */
public class ExpressionFilterStepVisitor extends FilterStepVisitor {

    ExpressionFilterStepVisitor(GeneratorContext generatorContext) {
        super(generatorContext);
    }

    public static class Factory implements StepVisitorFactory<ExpressionFilterStepVisitor> {
        @Override
        public String getStepKind() {
            return ExpressionFilterStep.STEP_KIND;
        }

        @Override
        public ExpressionFilterStepVisitor create(GeneratorContext generatorContext) {
            return new ExpressionFilterStepVisitor(generatorContext);
        }
    }

    @Override
    protected String getStepKind() {
        return ExpressionFilterStep.STEP_KIND;
    }
}
