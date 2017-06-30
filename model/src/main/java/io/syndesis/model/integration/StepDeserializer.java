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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.syndesis.model.filter.FilterStep;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StepDeserializer extends JsonDeserializer<Step> {

    private static final String KIND = "stepKind";

    private static final Class<SimpleStep> DEFAULT_STEP_TYPE = SimpleStep.class;
    private static final Map<String, Class<? extends Step>> EXCEPTIONS = new HashMap<>();

    static {
        EXCEPTIONS.put("filter", FilterStep.class);
    }

    @Override
    public Step deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        ObjectNode node = jp.readValueAsTree();
        JsonNode kind = node.get(KIND);
        if (kind != null) {
            String value = kind.textValue();
            Class<? extends Step> resourceType = getTypeForName(value);
            if (resourceType == null) {
                throw ctxt.mappingException("No step type found for kind:" + value);
            } else {
                return jp.getCodec().treeToValue(node, resourceType);
            }
        }
        return null;
    }

    private static Class getTypeForName(String name) {
        Class result = EXCEPTIONS.get(name);
        if (result == null) {
            return DEFAULT_STEP_TYPE;
        }

        return result;
    }
}
