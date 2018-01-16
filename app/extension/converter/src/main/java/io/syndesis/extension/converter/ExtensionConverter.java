package io.syndesis.extension.converter;

import com.fasterxml.jackson.databind.JsonNode;
import io.syndesis.model.extension.Extension;

/**
 * User-facing (public) extensions must follow a schema that is subject to backward-compatible changes only.
 * In order to allow the core API to evolve while supporting old extensions, the two schemas must be separated and
 * object should be converted.
 *
 * The public Extension model must respect the json schema contained in the extension-api module (syndesis/syndesis-extension-definition-schema.json).
 */
public interface ExtensionConverter {

    static ExtensionConverter getDefault() {
        return new DefaultExtensionConverter();
    }

    /**
     * Converts a public extension schema into the internal definition.
     */
    Extension toInternalExtension(JsonNode tree);

    /**
     * Converts a internal extension into the public format.
     */
    JsonNode toPublicExtension(Extension extension);

}
