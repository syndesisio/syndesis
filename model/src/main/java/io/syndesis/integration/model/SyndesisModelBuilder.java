/*
 * Copyright 2016 Red Hat, Inc.
 * <p>
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */
package io.syndesis.integration.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import io.syndesis.integration.model.steps.Step;

public class SyndesisModelBuilder {
    private final Map<String, Step> steps;

    public SyndesisModelBuilder() {
        this.steps = new HashMap<>();

        for (Step step : SyndesisModelHelpers.loadDefaultSteps()) {
            steps.put(step.getKind(), step);
        }
    }

    public SyndesisModelBuilder addStep(Step step) {
        steps.put(step.getKind(), step);

        return this;
    }

    public SyndesisModelBuilder addSteps(Collection<Step> steps) {
        steps.forEach(this::addStep);

        return this;
    }

    public SyndesisModel build(InputStream source) throws IOException {
        final ObjectMapper mapper = SyndesisModelHelpers.createObjectMapper();

        for (Step step : steps.values()) {
            mapper.registerSubtypes(new NamedType(step.getClass(), step.getKind()));
        }

        return mapper.readValue(source, SyndesisModel.class);
    }
}
