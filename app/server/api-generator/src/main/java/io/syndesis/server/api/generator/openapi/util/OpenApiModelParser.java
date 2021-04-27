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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

import com.fasterxml.jackson.databind.JsonNode;
import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.core.models.Document;
import io.apicurio.datamodels.openapi.models.OasDocument;
import io.syndesis.common.model.Violation;
import io.syndesis.common.util.IOStreams;
import io.syndesis.common.util.json.JsonUtils;
import io.syndesis.server.api.generator.APIValidationContext;
import io.syndesis.server.api.generator.openapi.OpenApiModelInfo;
import io.syndesis.server.api.generator.openapi.OpenApiVersion;
import io.syndesis.server.api.generator.openapi.v2.Oas20SchemaValidator;
import io.syndesis.server.api.generator.openapi.v2.Oas20ValidationRules;
import io.syndesis.server.api.generator.openapi.v3.Oas30SchemaValidator;
import io.syndesis.server.api.generator.openapi.v3.Oas30ValidationRules;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import static java.util.Optional.ofNullable;

public final class OpenApiModelParser {

    private static final Logger LOG = LoggerFactory.getLogger(OpenApiModelParser.class);

    private static final Yaml YAML_PARSER = new Yaml();

    private OpenApiModelParser() {
        // utility class
    }

    public static OpenApiModelInfo parse(final String specification, final APIValidationContext validationContext) {
        final OpenApiModelInfo.Builder resultBuilder = new OpenApiModelInfo.Builder();

        final String resolvedSpecification;
        try {
            resolvedSpecification = resolve(specification);
            resultBuilder.resolvedSpecification(resolvedSpecification);
        } catch (final Exception e) {
            LOG.debug("Unable to resolve OpenAPI document\n{}\n", specification, e);
            return resultBuilder
                .addError(new Violation.Builder().error("error").property("").message("Unable to resolve OpenAPI document from: "
                    + ofNullable(specification).map(s -> StringUtils.abbreviate(s, 100)).orElse("")).build())
                .build();
        }

        final JsonNode tree;
        try {
            tree = JsonUtils.reader().readTree(resolvedSpecification);
        } catch (final IOException e) {
            return new OpenApiModelInfo.Builder()
                .addError(new Violation.Builder()
                    .property("")
                    .error("unreadable-document")
                    .message("Unable to read OpenAPI document: " + e.getMessage())
                    .build())
                .build();
        }

        OpenApiVersion openApiVersion = getOpenApiVersion(tree);
        if (openApiVersion == null) {
            return new OpenApiModelInfo.Builder()
                .addError(new Violation.Builder()
                    .property("")
                    .error("unsupported-version")
                    .message(String.format("This document cannot be uploaded. " +
                        "Provide an OpenAPI document (supported versions are %s).", OpenApiVersion.getSupportedVersions()))
                    .build())
                .build();
        }

        final Document parsed = Library.readDocumentFromJSONString(resolvedSpecification);
        if (!(parsed instanceof OasDocument)) {
            LOG.debug("Unable to read OpenAPI document\n{}\n", specification);
            return resultBuilder
                .addError(new Violation.Builder()
                    .error("error")
                    .property("")
                    .message("Unable to read OpenAPI document from: '" + StringUtils.abbreviate(resolvedSpecification, 100)).build())
                .build();
        }

        resultBuilder.model((OasDocument) parsed);
        if (validationContext != APIValidationContext.NONE) {
            try {
                validateJsonSchema(convertToJson(resolvedSpecification), resultBuilder, openApiVersion, parsed.getClass());
            } catch (IOException e) {
                return resultBuilder
                    .addError(new Violation.Builder()
                        .error("error")
                        .property("")
                        .message("Unable to read OpenAPI document from: '" + StringUtils.abbreviate(resolvedSpecification, 100)).build())
                    .build();
            }
            return applyValidationRules(validationContext, resultBuilder.build(), openApiVersion);
        }
        return resultBuilder.build();
    }

    private static OpenApiModelInfo applyValidationRules(APIValidationContext validationContext, OpenApiModelInfo info,
                                                         OpenApiVersion openApiVersion) {
        switch (openApiVersion) {
            case V2:
                return Oas20ValidationRules.get(validationContext).apply(info);
            case V3:
                return Oas30ValidationRules.get(validationContext).apply(info);
            default:
                throw new IllegalStateException(String.format("Unable to apply validation rules for OpenAPI document type '%s'", info.getModel().getClass()));
        }
    }

    private static void validateJsonSchema(JsonNode jsonNode, OpenApiModelInfo.Builder resultBuilder,
                                           OpenApiVersion openApiVersion, Class<?> documentType) {
        switch (openApiVersion) {
            case V2:
                new Oas20SchemaValidator().validateJSonSchema(jsonNode, resultBuilder);
                break;
            case V3:
                new Oas30SchemaValidator().validateJSonSchema(jsonNode, resultBuilder);
                break;
            default:
                throw new IllegalStateException(String.format("Unable to determine proper schema validator for OpenAPI document type '%s'", documentType));
        }
    }

    private static OpenApiVersion getOpenApiVersion(JsonNode jsonNode) {
        final JsonNode v2Version = jsonNode.get("swagger");
        if (v2Version != null && !v2Version.isNull() && OpenApiVersion.V2.supports(v2Version.textValue())) {
            return OpenApiVersion.V2;
        }

        final JsonNode v3Version = jsonNode.get("openapi");
        if (v3Version != null && !v3Version.isNull() && OpenApiVersion.V3.supports(v3Version.textValue())) {
            return OpenApiVersion.V3;
        }

        return null;
    }

    public static boolean isJsonSpec(final String specification) {
        return specification.trim().startsWith("{");
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    private static String resolve(final String specification) throws Exception {
        final String specificationToUse;
        if (specification.toLowerCase(Locale.US).startsWith("http")) {
            specificationToUse = resolve(new URL(specification));
        } else {
            specificationToUse = specification;
        }

        final JsonNode node = convertToJson(specificationToUse);

        return JsonUtils.writer().writeValueAsString(node);
    }

    public static String resolve(URL url) {
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            int status = con.getResponseCode();
            if (status > 299) {
                throw new IllegalStateException(String.format("Failed to retrieve Open API specification from %s", url),
                    new IOException(IOStreams.readText(con.getErrorStream())));
            } else {
                return IOStreams.readText(con.getInputStream());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to retrieve Swagger Open API specification: " + url.toString(), e);
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }

    static JsonNode convertToJson(final String specification) throws IOException {
        final JsonNode specRoot;
        if (isJsonSpec(specification)) {
            specRoot = JsonUtils.reader().readTree(specification);
        } else {
            specRoot = JsonUtils.convertValue(YAML_PARSER.load(specification), JsonNode.class);
        }
        return specRoot;
    }
}
