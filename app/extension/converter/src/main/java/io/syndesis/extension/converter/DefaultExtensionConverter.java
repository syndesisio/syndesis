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
package io.syndesis.extension.converter;

import com.fasterxml.jackson.databind.JsonNode;
import io.syndesis.core.Json;
import io.syndesis.model.extension.Extension;

class DefaultExtensionConverter implements ExtensionConverter {

    private JsonNode convertInternalToPublicModel(JsonNode tree) {
        // Apply any custom conversion from the public model to the internal model here
        return tree;
    }

    private JsonNode convertPublicToInternalModel(JsonNode tree) {
        // Apply any custom conversion from the internal model to the public model here
        return tree;
    }

    @Override
    public Extension toInternalExtension(JsonNode tree) {
        JsonNode convertedTree = convertPublicToInternalModel(tree);
        return unmarshal(convertedTree);
    }

    @Override
    public JsonNode toPublicExtension(Extension extension) {
        JsonNode tree = marshal(extension);
        return convertInternalToPublicModel(tree);
    }

    private Extension unmarshal(JsonNode node) {
        return Json.mapper().convertValue(node, Extension.class);
    }

    private JsonNode marshal(Extension extension) {
        return Json.mapper().convertValue(extension, JsonNode.class);
    }

}
