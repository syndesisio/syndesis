package io.syndesis.model.filter;

import io.syndesis.model.integration.Step;

/**
 * @author roland
 * @since 01.08.17
 */
public interface FilterStep extends Step {

    String getFilterExpression();
}
