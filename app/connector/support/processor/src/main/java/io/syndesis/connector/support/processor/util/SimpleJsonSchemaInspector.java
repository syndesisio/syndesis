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
package io.syndesis.connector.support.processor.util;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

/**
 * Utility class to inspect the content of JSON schema objects, useful when parsing the full schema is not a viable option.
 * This should not be affected by incompatibilities between draft-03 and draft-04.
 */
public final class SimpleJsonSchemaInspector {

    private SimpleJsonSchemaInspector() {
        // utility class
    }

    public static Optional<String> getId(JsonNode node) {
        return Optional.ofNullable(node)
            .flatMap(n -> Optional.ofNullable(n.get("id")))
            .map(JsonNode::asText);
    }

    public static Set<String> getProperties(JsonNode node, String... subPath) {
        if (node == null) {
            return Collections.emptySet();
        }

        JsonNode nodeProps = node.get("properties");
        if (nodeProps == null) {
            return Collections.emptySet();
        }

        if (subPath == null || subPath.length==0) {
            Set<String> props = new HashSet<>();
            Iterator<String> names = nodeProps.fieldNames();
            while (names.hasNext()) {
                props.add(names.next());
            }
            return props;
        } else {
            String[] subSubPath = new String[subPath.length-1];
            System.arraycopy(subPath, 1, subSubPath, 0, subSubPath.length);
            return getProperties(nodeProps.get(subPath[0]), subSubPath);
        }
    }

}
