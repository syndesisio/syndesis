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

import io.syndesis.model.filter.*;
import org.junit.Test;

import static org.junit.Assert.*;

public class RuleFilterStepVisitorTest {

    @Test
    public void createExpression() throws Exception {
        RuleFilterStep step = new RuleFilterStep.Builder()
            .id("1")
            .predicate(FilterPredicate.AND)
            .addRule(new FilterRule.Builder().path("person.name").op("==").value("Ioannis").build())
            .addRule(new FilterRule.Builder().path("person.favoriteDrinks").op("contains").value("Gin").build())
            .build();

        // Reading notes: Unit tests are like personal diaries. Feel honoured when you have the chance to be part of them ;-)
        assertEquals("${person.name} == 'Ioannis' && ${person.favoriteDrinks} contains 'Gin'", step.getFilterExpression());
    }

}
