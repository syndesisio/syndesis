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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import io.syndesis.model.filter.*;
import org.junit.Test;

import static org.junit.Assert.*;

public class StepDeserializerTest {

    @Test
    public void shouldDeserializeRuleFilterStep() throws Exception {
        RuleFilterStep step = readTestFilter("/rule-filter-step.json");
        RuleFilterStep ruleFilterStep = (RuleFilterStep) step;
        assertEquals("1", ruleFilterStep.getId().get());
        assertEquals(FilterPredicate.AND, ruleFilterStep.getPredicate());
        assertEquals("rule-filter", ruleFilterStep.getStepKind());
        assertEquals(2, ruleFilterStep.getRules().size());
        assertEquals("${body.text} == 'antman' && ${header.kind} =~ 'DC Comics'", ruleFilterStep.getFilterExpression());
    }

    @Test
    public void shouldDeserializeExpressionFilterStep() throws Exception {
        ExpressionFilterStep step = readTestFilter("/filter-step.json");
        ExpressionFilterStep exprFilterStep = (ExpressionFilterStep) step;
        String expr = "${in.body} contains '#RHSummit'";
        assertEquals("2", exprFilterStep.getId().get());
        assertEquals("filter", exprFilterStep.getStepKind());
        assertEquals(expr, exprFilterStep.getFilterExpression());
        assertEquals(1, exprFilterStep.getConfiguredProperties().get().size());
        assertEquals(expr, exprFilterStep.getConfiguredProperties().get().get("filter"));
    }

    private <T extends Step> T readTestFilter(String resource) throws java.io.IOException {
        return (T) new ObjectMapper().registerModule(new Jdk8Module()).readValue(this.getClass().getResourceAsStream(resource), Step.class);
    }

}
