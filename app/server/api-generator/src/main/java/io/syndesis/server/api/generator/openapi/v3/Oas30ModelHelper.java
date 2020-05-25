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
import java.util.Objects;
import java.util.Optional;

import io.apicurio.datamodels.core.models.common.Server;
import io.apicurio.datamodels.core.models.common.ServerVariable;
import io.apicurio.datamodels.openapi.models.OasPathItem;
import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.v3.models.Oas30Document;
import io.apicurio.datamodels.openapi.v3.models.Oas30MediaType;
import io.apicurio.datamodels.openapi.v3.models.Oas30Operation;
import io.apicurio.datamodels.openapi.v3.models.Oas30Parameter;
import io.apicurio.datamodels.openapi.v3.models.Oas30RequestBody;
import io.apicurio.datamodels.openapi.v3.models.Oas30Response;
import io.apicurio.datamodels.openapi.v3.models.Oas30Schema;
import io.apicurio.datamodels.openapi.v3.models.Oas30SchemaDefinition;
import io.syndesis.server.api.generator.openapi.util.OasModelHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class Oas30ModelHelper {

    private static final Logger LOG = LoggerFactory.getLogger(Oas30ModelHelper.class);
    public static final String HTTP = "http";

    private Oas30ModelHelper() {
        // utility class
    }

    static Oas30SchemaDefinition dereference(final OasSchema model, final Oas30Document openApiDoc) {
        if (openApiDoc.components == null || openApiDoc.components.schemas == null) {
            return null;
        }

        String reference = OasModelHelper.getReferenceName(model.$ref);
        return openApiDoc.components.schemas.get(reference);
    }

    /**
     * Get schema from given parameter. This is either the direct schema defined on the parameter or the first
     * schema given in the list of media type mappings. According to the OpenAPI specification one of these two must
     * be set for a parameter object.
     * @param parameter the parameter maybe holding the schema.
     * @return the schema associated with the given parameter.
     */
    static Optional<Oas30Schema> getSchema(Oas30Parameter parameter) {
        if (parameter.schema != null) {
            return Optional.of((Oas30Schema) parameter.schema);
        }

        Map<String, Oas30MediaType> mediaTypes = parameter.content;
        if (mediaTypes == null) {
            return Optional.empty();
        }

        return mediaTypes.values()
                    .stream()
                    .map(media -> media.schema)
                    .filter(Objects::nonNull)
                    .findFirst();
    }

    /**
     * Get schema from given response. This is inspecting the given content media types mappings on the response and
     * returns the first finding where the mapping defines a schema.
     * @param response the response maybe holding a media type mapping with a schema.
     * @return the schema associated with the given response.
     */
    static Optional<Oas30Schema> getSchema(Oas30Response response) {
        return getSchema(response, null);
    }

    /**
     * Get schema from given response and media type. This is inspecting the given content media types mappings on the response and
     * preferably returns the schema associated with the given media type. If given media type is not found return first finding in
     * list of media types on the response that has a schema.
     * @param response the response maybe holding a media type mapping with a schema.
     * @param mediaType the media type to search for preferably when selecting the schema on the list of response media types.
     * @return the schema associated with the given response.
     */
    static Optional<Oas30Schema> getSchema(Oas30Response response, String mediaType) {
        return getMediaTypeWithSchema(response.content, mediaType)
            .map(m -> m.schema);
    }

    /**
     * Get media type from given request body that has a schema defined. This is inspecting the given content media types mappings on the request and
     * preferably returns the schema associated with the given media type name. If given media type is not found return first finding in
     * list of media types that has a schema.
     * @param requestBody the response maybe holding a media type mapping with a schema.
     * @param mediaType the media type to search for preferably when selecting the schema on the list of request media types.
     * @return the schema associated with the given request.
     */
    static Optional<Oas30MediaType> getMediaType(Oas30RequestBody requestBody, String mediaType) {
        return getMediaTypeWithSchema(requestBody.content, mediaType);
    }

    /**
     * Find media type associated with a schema. Search for media type with given name first. If no explicit media type
     * entry is found return first media type that has a schema defined.
     * @param content the list of media types.
     * @param mediaType preferred media type to search first.
     * @return media type with schema defined or null.
     */
    private static Optional<Oas30MediaType> getMediaTypeWithSchema(Map<String, Oas30MediaType> content, String mediaType) {
        if (content == null) {
            return Optional.empty();
        }

        if (mediaType != null && content.containsKey(mediaType)) {
            return Optional.of(content.get(mediaType));
        }

        return content.entrySet()
            .stream()
            .filter(entry -> !Oas30FormDataHelper.isFormDataMediaType(entry.getKey()))
            .filter(entry -> entry.getValue().schema != null)
            .findFirst()
            .map(Map.Entry::getValue);
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
        if (openApiDoc.servers == null || openApiDoc.servers.isEmpty()) {
            return "/";
        }

        return getBasePath(openApiDoc.servers.get(0));
    }

    /**
     * Determine base path from server specification. Reads URL from server definition and extracts base path from
     * that URL. The base path returned defaults to "/".
     * @param server the server holding the host URL.
     * @return base path of this API specification.
     */
    static String getBasePath(Server server) {
        String basePath = "/";

        String serverUrl = resolveUrl(server);
        if (serverUrl.startsWith(HTTP)) {
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

    /**
     * Determine server URL scheme. In case server URL is relative or no URL is set return "http" by default.
     * @param server the server holding the server URL.
     * @return server URL scheme or "http" as default
     */
    static String getScheme(Server server) {
        String serverUrl = resolveUrl(server);
        if (serverUrl.startsWith(HTTP)) {
            try {
                return new URL(serverUrl).getProtocol();
            } catch (MalformedURLException e) {
                LOG.warn(String.format("Unable to determine base path from server URL: %s", serverUrl));
            }
        }

        return HTTP;
    }

    /**
     * Determine host information from server specification. Reads URL from first available server definition and extracts host from
     * that URL. Defaults to null when no proper value is given.
     * @param openApiDoc the OpenAPI document.
     * @return server host of this API specification.
     */
    static String getHost(Oas30Document openApiDoc) {
        if (openApiDoc.servers == null || openApiDoc.servers.isEmpty()) {
            return null;
        }

        String serverUrl = resolveUrl(openApiDoc.servers.get(0));
        if (serverUrl.startsWith(HTTP)) {
            try {
                return new URL(serverUrl).getHost();
            } catch (MalformedURLException e) {
                LOG.warn(String.format("Unable to determine base path from server URL: %s", serverUrl));
            }
        }

        return null;
    }

    /**
     * Resolve given server url and replace variable placeholders if any with default variable values. Open API 3.x
     * supports variables with placeholders in form {variable_name} (e.g. "http://{hostname}:{port}/api/v1").
     * @param server the server holding a URL with maybe variable placeholders.
     * @return the server URL with all placeholders resolved or "/" by default.
     */
    private static String resolveUrl(Server server) {
        String url = Optional.ofNullable(server.url).orElse("/");
        if (server.variables != null) {
            for (Map.Entry<String, ServerVariable> variable: server.variables.entrySet()) {
                String defaultValue = Optional.ofNullable(variable.getValue().default_).orElse("");
                url = url.replaceAll(String.format("\\{%s\\}", variable.getKey()), defaultValue);
            }
        }

        return url;
    }
}
