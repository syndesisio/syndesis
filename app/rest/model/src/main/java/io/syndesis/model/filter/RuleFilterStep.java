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
package io.syndesis.model.filter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.syndesis.core.Json;
import io.syndesis.model.Kind;
import org.immutables.value.Value;

/**
 * Specific rule filter step
 * @since 31.07.17
 */
@Value.Immutable
@JsonDeserialize(builder = RuleFilterStep.Builder.class)
@JsonIgnoreProperties("filterExpression")
@SuppressWarnings("immutables")
public interface RuleFilterStep extends FilterStep {

    String STEP_KIND = "rule-filter";

    /**
     * Filter in the simple expression language.
     */
    @Override
    default String getFilterExpression() {
        final Map<String, String> props = getConfiguredProperties();

        if (!props.isEmpty()) {
            FilterPredicate predicate = getPredicate(props.get("predicate"));
            List<FilterRule> rules = extractRules(props.get("rules"));
            if (rules != null && !rules.isEmpty()) {
                return rules
                    .stream()
                    .map(FilterRule::getFilterExpression)
                    .collect(Collectors.joining(predicate.getExpressionDelimiter()));
            }
            throw new IllegalStateException(String.format("No rules defined in step properties %s for rule filter step",props));
        }
        throw new IllegalStateException("No step properties defined for rule filter step");
    }

    default List<FilterRule> extractRules(String rulesString) {
        try {
            if (rulesString == null || rulesString.isEmpty()) {
                return null;
            }
            return Json.mapper().readValue(rulesString,new TypeReference<List<FilterRule>>(){});
        } catch (IOException e) {
            throw new IllegalStateException(String.format("Cannot deserialize %s: %s", rulesString, e.getMessage()),e);
        }
    }

    @SuppressWarnings("PMD.UseLocaleWithCaseConversions")
    default FilterPredicate getPredicate(String predicate) {
        if (predicate != null) {
            return FilterPredicate.valueOf(predicate.toUpperCase());
        }
        return FilterPredicate.OR;
    }

    class Builder extends ImmutableRuleFilterStep.Builder { }

    @Override
    @Value.Default default String getStepKind() {
        return STEP_KIND;
    }

    @Override
    @Value.Default default Kind getKind() {
        return Kind.Step;
    }

}
