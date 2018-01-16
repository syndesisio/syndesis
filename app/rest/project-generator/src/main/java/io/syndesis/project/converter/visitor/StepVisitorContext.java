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
package io.syndesis.project.converter.visitor;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.syndesis.model.integration.Step;

@JsonDeserialize(builder = StepVisitorContext.Builder.class)
public class StepVisitorContext implements Iterator<StepVisitorContext> {

    private final GeneratorContext generatorContext;
    private final int index;
    private final Step step;
    private final List<? extends Step> remaining;

    public StepVisitorContext(GeneratorContext generatorContext, int index, Step step, List<? extends Step> remaining) {
        this.generatorContext = generatorContext;
        this.index = index;
        this.step = step;
        this.remaining = remaining == null ? Collections.emptyList() : Collections.unmodifiableList(remaining);
    }

    public GeneratorContext getGeneratorContext() {
        return generatorContext;
    }

    public int getIndex() {
        return index;
    }

    public Step getStep() {
        return step;
    }

    public List<? extends Step> getRemaining() {
        return remaining;
    }

    @Override
    public boolean hasNext() {
        return !getRemaining().isEmpty();
    }

    @Override
    public StepVisitorContext next() {
        final int index = getIndex();
        final List<? extends Step> remaining = getRemaining();
        final Step next = remaining.get(index);

        return StepVisitorContext.Builder.createFrom(this)
            .index(index + 1)
            .step(next)
            .remaining(remaining.subList(index + 1, remaining.size()))
            .build();
    }

    public static final class Builder {

        private GeneratorContext generatorContext;
        private int index;
        private Step step;
        private List<? extends Step> remaining;

        public Builder() {
        }

        public static Builder createFrom(StepVisitorContext c) {
            return new Builder().generatorContext(c.generatorContext).index(c.index).step(c.step).remaining(c.remaining);
        }

        public Builder generatorContext(GeneratorContext generatorContext) {
            this.generatorContext = generatorContext;
            return this;
        }

        public Builder index(int index) {
            this.index = index;
            return this;
        }

        public Builder step(Step step) {
            this.step = step;
            return this;
        }

        public Builder remaining(List<? extends Step> remaining) {
            this.remaining = remaining;
            return this;
        }

        public StepVisitorContext build() {
            return new StepVisitorContext(generatorContext, index, step, remaining);
        }
    }
}
