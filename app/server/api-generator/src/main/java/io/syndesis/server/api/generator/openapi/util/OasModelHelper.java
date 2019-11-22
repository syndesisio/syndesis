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

package io.syndesis.server.api.generator.openapi.util;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.apicurio.datamodels.core.models.Extension;
import io.apicurio.datamodels.openapi.models.OasDocument;
import io.apicurio.datamodels.openapi.models.OasOperation;
import io.apicurio.datamodels.openapi.models.OasParameter;
import io.apicurio.datamodels.openapi.models.OasSchema;
import io.syndesis.server.api.generator.swagger.util.Oas20ModelHelper;

/**
 * @author Christoph Deppisch
 */
public final class OasModelHelper {

    private OasModelHelper() {
        // utility class
    }

    public static List<OasParameter> getParameters(final OasOperation operation) {
        return operation.getParameters() != null ? operation.getParameters() : Collections.emptyList();
    }

    public static boolean schemaIsNotSpecified(final OasSchema schema) {
        if (schema == null) {
            return true;
        }

        if (Oas20ModelHelper.isArrayType(schema)) {
            return schema.items == null;
        }

        final Map<String, OasSchema> properties = schema.properties;
        final boolean noProperties = properties == null || properties.isEmpty();
        final boolean noReference = schema.$ref == null;
        return noProperties && noReference;
    }

    public static URI specificationUriFrom(final OasDocument openApiDoc) {
        final Collection<Extension> vendorExtensions = openApiDoc.getExtensions();

        if (vendorExtensions == null) {
            return null;
        }

        return vendorExtensions.stream().filter(extension -> "x-syndesis-swagger-url".equals(extension.name))
            .map(extension -> (URI) extension.value)
            .findFirst()
            .orElse(null);
    }
}
