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
package io.fabric8.funktion.model;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 */
public class FunktionDeserializer extends JsonDeserializer {
    private Map<String, Class> kinds = new HashMap<>();

    public FunktionDeserializer() {
        kinds.put("function", InvokeFunction.class);
        kinds.put("endpoint", InvokeEndpoint.class);
    }

    @Override
    public FunktionAction deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        ObjectNode node = jp.readValueAsTree();
        JsonNode kind = node.get("kind");
        if (kind == null) {
            throw new JsonParseException(jp, "No `kind` property!");
        }

        Class kindClass = kinds.get(kind.asText());
        if (kindClass == null) {
            throw ctxt.mappingException("Unknown kind: " + kind);
        } else {
            return (FunktionAction) jp.getCodec().treeToValue(node, kindClass);
        }
    }
}
