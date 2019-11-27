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
package io.syndesis.server.api.generator.openapi.v3;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;

import io.apicurio.datamodels.openapi.models.OasPathItem;
import io.apicurio.datamodels.openapi.v3.models.Oas30Document;
import io.apicurio.datamodels.openapi.v3.models.Oas30Operation;
import io.syndesis.server.api.generator.openapi.util.OasModelHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class Oas30ModelHelper {

    private static final Logger LOG = LoggerFactory.getLogger(Oas30ModelHelper.class);

    private Oas30ModelHelper() {
        // utility class
    }

    /**
     * Delegates to common model helper with OpenAPI 3.x model type parameter.
     * @param pathItem holding the operations.
     * @return typed map of OpenAPI 3.x operations where the key is the Http method of the operation.
     */
    static Map<String, Oas30Operation> getOperationMap(OasPathItem pathItem) {
        return OasModelHelper.getOperationMap(pathItem, Oas30Operation.class);
    }

    /**
     * Determine base path from server specification. Reads URL from first available server definition and extracts base path from
     * that URL. The base path returned defaults to "/" when no other value is given.
     * @param openApiDoc the OpenAPI document.
     * @return base path of this API specification.
     */
    static String getBasePath(Oas30Document openApiDoc) {
        String basePath = "/";
        if (openApiDoc.servers == null || openApiDoc.servers.isEmpty()) {
            return basePath;
        }

        String serverUrl = Optional.ofNullable(openApiDoc.servers.get(0).url).orElse("/");
        if (serverUrl.startsWith("http")) {
            try {
                basePath = new URL(serverUrl).getPath();
            } catch (MalformedURLException e) {
                LOG.warn(String.format("Unable to determine base path from server URL: %s", serverUrl));
            }
        } else {
            basePath = serverUrl;
        }

        return basePath.length() > 0 ? basePath : "/";
    }
}
