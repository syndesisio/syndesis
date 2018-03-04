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
package io.syndesis.integration.runtime.handlers;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import io.syndesis.common.util.Json;
import io.syndesis.common.model.filter.FilterPredicate;
import io.syndesis.common.model.filter.FilterRule;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;

public class RuleFilterStepHandler extends AbstractFilterStepHandler {
    @Override
    public boolean canHandle(Step step) {
        return StepKind.ruleFilter == step.getStepKind();
    }

    @Override
    protected String getFilterExpression(Step step) {
        final Map<String, String> props = step.getConfiguredProperties();

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

    // *******************************
    // Helpers
    // *******************************

    private List<FilterRule> extractRules(String rulesString) {
        try {
            if (rulesString == null || rulesString.isEmpty()) {
                return null;
            }
            return Json.reader().forType(new TypeReference<List<FilterRule>>(){}).readValue(rulesString);
        } catch (IOException e) {
            throw new IllegalStateException(String.format("Cannot deserialize %s: %s", rulesString, e.getMessage()),e);
        }
    }

    @SuppressWarnings("PMD.UseLocaleWithCaseConversions")
    private FilterPredicate getPredicate(String predicate) {
        if (predicate != null) {
            return FilterPredicate.valueOf(predicate.toUpperCase());
        }
        return FilterPredicate.OR;
    }
}
