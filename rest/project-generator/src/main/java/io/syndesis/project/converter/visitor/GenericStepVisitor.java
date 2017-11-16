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

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.syndesis.core.SyndesisServerException;
import io.syndesis.integration.model.YamlHelpers;
import io.syndesis.model.integration.Step;

public class GenericStepVisitor implements StepVisitor {
    private static final ObjectMapper OBJECT_MAPPER = YamlHelpers.createObjectMapper();
    public static final String GENERIC = "generic";

    public static class Factory implements StepVisitorFactory<GenericStepVisitor> {
        @Override
        public String getStepKind() {
            return GENERIC;
        }

        @Override
        public GenericStepVisitor create() {
            return new GenericStepVisitor();
        }
    }

    @Override
    public Collection<io.syndesis.integration.model.steps.Step> visit(StepVisitorContext stepContext) {
        try {
            Step step = stepContext.getStep();
            Map<String, Object> stepMap = new HashMap<>(step.getConfiguredProperties());
            stepMap.put("kind", step.getStepKind());
            String json = OBJECT_MAPPER.writeValueAsString(stepMap);

            return Collections.singletonList(
                OBJECT_MAPPER.readValue(json, io.syndesis.integration.model.steps.Step.class)
            );
        } catch (IOException e) {
            throw SyndesisServerException.launderThrowable(e);
        }
    }
}
