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
import io.syndesis.model.filter.FilterPredicate;
import io.syndesis.model.filter.FilterStep;
import io.syndesis.model.filter.FilterType;
import org.junit.Test;

import static org.junit.Assert.*;

public class StepDeserializerTest {

    @Test
    public void shouldDeserializeFilterStep() throws Exception {
        FilterStep filterStep = (FilterStep) new ObjectMapper().registerModule(new Jdk8Module()).readValue(this.getClass().getResourceAsStream("/filterstep1.json"), Step.class);
        assertNotNull(filterStep);
        assertEquals("1", filterStep.getId().get());
        assertEquals(FilterType.RULE, filterStep.getType());
        assertEquals(FilterPredicate.AND, filterStep.getPredicate());
    }

}
