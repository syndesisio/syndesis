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

package io.syndesis.server.api.generator.openapi;

import java.util.Arrays;

import io.apicurio.datamodels.openapi.models.OasDocument;
import io.apicurio.datamodels.openapi.v2.models.Oas20Document;
import io.apicurio.datamodels.openapi.v3.models.Oas30Document;

/**
 * List of supported OpenAPI specification versions and their corresponding model document types.
 */
public enum OpenApiVersion {
    V2(Oas20Document.class),
    V3(Oas30Document.class);

    private final Class<? extends OasDocument> documentType;

    OpenApiVersion(Class<? extends OasDocument> documentType) {
        this.documentType = documentType;
    }

    static OpenApiVersion fromDocumentType(OasDocument model) {
        return Arrays.stream(values())
            .filter(version -> version.documentType.isInstance(model))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unable get OpenAPI version from given document type"));
    }
}
