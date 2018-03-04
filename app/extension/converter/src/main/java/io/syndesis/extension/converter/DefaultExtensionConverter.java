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
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.syndesis.common.util.Json;
import io.syndesis.common.model.extension.Extension;

import java.io.IOException;
import java.util.Optional;

class DefaultExtensionConverter implements ExtensionConverter {

    private static final String SCHEMA_VERSION_FIELD = "schemaVersion";

    /**
     * Apply any custom conversion from the public model to the internal model here.
     *
     * Version of the public model is assumed to be latest ({@link ExtensionConverter#getCurrentSchemaVersion()}).
     */
    private ObjectNode convertInternalToPublicModel(ObjectNode tree) {
        return tree;
    }

    /**
     * Apply any custom conversion from the internal model to the public model here.
     *
     * Version of the public model is assumed to be latest ({@link ExtensionConverter#getCurrentSchemaVersion()}).
     */
    private ObjectNode convertPublicToInternalModel(ObjectNode tree) {
        return tree;
    }

    /**
     * Apply any custom transformation to latest public schema versions.
     *
     * Normally public versions are backward compatible, so this method is likely to remain empty.
     */
    private ObjectNode convertToLatestSchemaVersion(ObjectNode tree, String sourceVersion) {
        if ("v0".equals(sourceVersion)) {
            // Custom transformations here
            return tree;
        }
        return tree;
    }

    @Override
    public Extension toInternalExtension(JsonNode tree) throws IOException {
        ObjectNode upgradedTree = updateToLatestVersion(toObjectNode(tree));
        ObjectNode convertedTree = convertPublicToInternalModel(upgradedTree);
        return unmarshal(convertedTree);
    }

    @Override
    public JsonNode toPublicExtension(Extension extension) throws IOException {
        ObjectNode internalTree = marshal(extension);
        ObjectNode publicTree = convertInternalToPublicModel(internalTree);
        return updateToLatestVersion(publicTree);
    }

    private ObjectNode updateToLatestVersion(ObjectNode tree) {
        Optional<String> treeVersion = getSchemaVersion(tree);
        if (treeVersion.isPresent() && !ExtensionConverter.getCurrentSchemaVersion().equals(treeVersion.get())) {
            ObjectNode converted = convertToLatestSchemaVersion(tree, treeVersion.get());
            converted.put(SCHEMA_VERSION_FIELD, ExtensionConverter.getCurrentSchemaVersion());
            return converted;
        } else if (!treeVersion.isPresent()) {
            tree.put(SCHEMA_VERSION_FIELD, ExtensionConverter.getCurrentSchemaVersion());
        }
        return tree;
    }

    private Optional<String> getSchemaVersion(ObjectNode tree) {
        return Optional.ofNullable(tree)
                .flatMap(t -> Optional.ofNullable(t.get(SCHEMA_VERSION_FIELD)))
                .flatMap(t -> Optional.ofNullable(t.textValue()));
    }

    private Extension unmarshal(JsonNode node) throws IOException {
        byte[] bytes = Json.writer().writeValueAsBytes(node);
        return Json.reader().forType(Extension.class).readValue(bytes);
    }

    private ObjectNode marshal(Extension extension) throws IOException {
        byte[] bytes = Json.writer().writeValueAsBytes(extension);
        return Json.reader().forType(ObjectNode.class).readValue(bytes);
    }

    private ObjectNode toObjectNode(JsonNode tree) {
        if (!(tree instanceof ObjectNode)) {
            throw new IllegalArgumentException("The JSON document is not an object: " + tree);
        }
        return (ObjectNode) tree;
    }

}
