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
import io.syndesis.common.model.extension.Extension;

import java.io.IOException;

/**
 * User-facing (public) extensions must follow a schema that is subject to backward-compatible changes only.
 * In order to allow the core API to evolve while supporting old extensions, the two schemas must be separated and
 * object should be converted.
 *
 * The public Extension model must respect the json schema contained in the extension-api module (syndesis/syndesis-extension-definition-schema.json).
 */
public interface ExtensionConverter {

    /**
     * Returns the current schema version used for the public model.
     */
    static String getCurrentSchemaVersion() {
        return "v1";
    }

    static ExtensionConverter getDefault() {
        return new DefaultExtensionConverter();
    }

    /**
     * Converts a public extension schema into the internal definition.
     */
    Extension toInternalExtension(JsonNode tree) throws IOException;

    /**
     * Converts a internal extension into the public format.
     */
    JsonNode toPublicExtension(Extension extension) throws IOException;

}
