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

package io.syndesis.model.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import org.junit.Test;

import io.syndesis.model.filter.ExpressionFilterStep;
import io.syndesis.model.filter.FilterPredicate;
import io.syndesis.model.filter.RuleFilterStep;

public class StepDeserializerTest {

    @Test
    public void shouldDeserializeRuleFilterStep() throws IOException {
        RuleFilterStep step = readTestFilter("/rule-filter-step.json", RuleFilterStep.class);
        assertEquals("1", step.getId().get());
        assertFalse(step.getConfiguredProperties().isEmpty());
        Map<String, String> props = step.getConfiguredProperties();
        assertEquals(FilterPredicate.AND, FilterPredicate.valueOf(props.get("predicate").toUpperCase(Locale.US)));
        assertNotNull(props.get("rules"));
        assertEquals("rule-filter", step.getStepKind());
        assertEquals("${body.text} == 'antman' && ${body.kind} =~ 'DC Comics'", step.getFilterExpression());
    }

    @Test
    public void shouldDeserializeExpressionFilterStep() throws IOException {
        ExpressionFilterStep step = readTestFilter("/filter-step.json", ExpressionFilterStep.class);
        ExpressionFilterStep exprFilterStep = step;
        String expr = "${body.text} contains '#RHSummit'";
        assertEquals("2", exprFilterStep.getId().get());
        assertEquals("filter", exprFilterStep.getStepKind());
        assertEquals(expr, exprFilterStep.getFilterExpression());
        assertEquals(1, exprFilterStep.getConfiguredProperties().size());
        assertEquals(expr, exprFilterStep.getConfiguredProperties().get("filter"));
    }

    private <T extends Step> T readTestFilter(String resource, Class<T> stepType) throws IOException {
        return new ObjectMapper().registerModule(new Jdk8Module())
                .readValue(this.getClass().getResourceAsStream(resource), stepType);
    }

}
