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
        tree = convertPublicToInternalModel(tree);
        return unmarshal(tree);
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
