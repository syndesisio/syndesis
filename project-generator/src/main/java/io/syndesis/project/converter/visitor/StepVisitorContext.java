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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.syndesis.model.integration.Step;
import org.immutables.value.Value;

import java.util.Iterator;
import java.util.Queue;

@Value.Immutable
@JsonDeserialize(builder = StepVisitorContext.Builder.class)
public interface StepVisitorContext extends Iterator<StepVisitorContext> {

    int getIndex();

    Step getStep();

    Queue<Step> getRemaining();

    default boolean hasNext() {
        return !getRemaining().isEmpty();
    }

    default StepVisitorContext next() {
        Queue<Step> remaining = getRemaining();
        Step next = remaining.remove();

        return new StepVisitorContext.Builder()
            .index(getIndex()+1)
            .step(next)
            .remaining(remaining)
            .build();
    }

    class Builder extends ImmutableStepVisitorContext.Builder {
    }
}
