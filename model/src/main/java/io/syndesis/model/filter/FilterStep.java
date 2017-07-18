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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.syndesis.model.integration.Step;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonDeserialize(builder = FilterStep.Builder.class)
public interface FilterStep extends Step {

    /**
     *  Type of filter, which is either "RULE" or "TEXT"
     */
    FilterType getType();

    /**
     *  Predicate can be either "AND" (every) or "OR" (any)
     */
    FilterPredicate getPredicate();

    /**
     * List of rules to combine. null when type is "SIMPLE"
     * @return
     */
    List<FilterRule> getRules();

    /** Filter in the simple expression language. It is either the provided
     * freeform expression when type it "TEXT" or the calculated
     * filter text when type is "RULE" (but can be initially null)
     */
    String getSimple();


    class Builder extends ImmutableFilterStep.Builder {
    }

}
