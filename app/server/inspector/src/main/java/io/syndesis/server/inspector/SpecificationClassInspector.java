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
package io.syndesis.server.inspector;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Optional;

import io.syndesis.common.util.Json;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

@Component
public final class SpecificationClassInspector extends DataMapperBaseInspector<JsonNode> {

    @Autowired
    public SpecificationClassInspector() {
        super(false);
    }

    @Override
    protected Context<JsonNode> createContext(final String kind, final String type, final String specification,
        final Optional<byte[]> exemplar) {
        try {
            return new Context<>(Json.reader().readTree(specification));
        } catch (final IOException e) {
            throw new IllegalArgumentException("Unable to parse specification", e);
        }
    }

    @Override
    protected String fetchJsonFor(final String fullyQualifiedName, final Context<JsonNode> context) throws IOException {
        final JsonNode classNode = findClassNode(fullyQualifiedName, context.getState());

        final JsonNode javaClass = JsonNodeFactory.instance.objectNode().set("JavaClass", classNode);

        return Json.writer().writeValueAsString(javaClass);
    }

    @Override
    protected boolean internalSupports(final String kind, final String type, final String specification, final Optional<byte[]> exemplar) {
        return !StringUtils.isEmpty(specification);
    }

    private static JsonNode findClassNode(final String fullyQualifiedName, final JsonNode root) {
        for (final Entry<String, JsonNode> pair : (Iterable<Entry<String, JsonNode>>) root::fields) {
            final String fieldName = pair.getKey();
            final JsonNode value = pair.getValue();

            if ("className".equals(fieldName) && fullyQualifiedName.equals(value.asText())) {
                return root;
            }

            final JsonNode found = findClassNode(fullyQualifiedName, value);
            if (found != null) {
                return found;
            }
        }

        if (root.isArray()) {
            for (final JsonNode node : (Iterable<JsonNode>) root::elements) {
                final JsonNode found = findClassNode(fullyQualifiedName, node);
                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }

}
