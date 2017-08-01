package io.syndesis.model.filter;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.syndesis.model.integration.Step;
import org.immutables.value.Value;

/**
 * Specific rule filter step
 * @since 31.07.17
 */
@Value.Immutable
@JsonDeserialize(builder = RuleFilterStep.Builder.class)
@JsonIgnoreProperties({"stepKind", "kind", "configuredProperties"})
public interface RuleFilterStep extends FilterStep {

    String STEP_KIND = "rule-filter";

    /**
     *  Predicate can be either "AND" (every) or "OR" (any) and determines
     *  or rules are combined together
     */
    FilterPredicate getPredicate();

    /**
     * List of rules to combine.
     */
    List<FilterRule> getRules();

    /**
     * Filter in the simple expression language. This is calculated by evaluating
     * the rules and hence somewhat persisted redundantly. It's updated during storage
     */
    default String getFilterExpression() {
        return getRules()
            .stream()
            .map(FilterRule::getFilterExpression)
            .collect(Collectors.joining(getPredicate().getExpressionDelimiter()));
    }

    class Builder extends ImmutableRuleFilterStep.Builder { }

    @Override
    default String getStepKind() {
        return STEP_KIND;
    }
}
