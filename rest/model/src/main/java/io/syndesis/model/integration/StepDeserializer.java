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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.syndesis.model.filter.ExpressionFilterStep;
import io.syndesis.model.filter.RuleFilterStep;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class StepDeserializer extends JsonDeserializer<Step> {

    private static final String STEP_KIND = "stepKind";

    private static final Class<SimpleStep> DEFAULT_STEP_TYPE = SimpleStep.class;
    private static final Map<String, Class<? extends Step>> KIND_TO_STEP_MAPPING;

    static {
        final Map<String, Class<? extends Step>> mapping = new HashMap<>();
        mapping.put(ExpressionFilterStep.STEP_KIND, ExpressionFilterStep.class);
        mapping.put(RuleFilterStep.STEP_KIND, RuleFilterStep.class);

        KIND_TO_STEP_MAPPING = Collections.unmodifiableMap(mapping);
    }

    @Override
    public Step deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        ObjectNode node = jp.readValueAsTree();
        JsonNode stepKind = node.get(STEP_KIND);
        if (stepKind != null) {
            String value = stepKind.textValue();
            Class<? extends Step> resourceType = getTypeForName(value);
            if (resourceType == null) {
                throw ctxt.mappingException("No step type found for kind:" + value);
            }

            return jp.getCodec().treeToValue(node, resourceType);
        }
        return null;
    }

    private static Class<? extends Step> getTypeForName(String name) {
        Class<? extends Step> result = KIND_TO_STEP_MAPPING.get(name);
        if (result == null) {
            return DEFAULT_STEP_TYPE;
        }

        return result;
    }
}
