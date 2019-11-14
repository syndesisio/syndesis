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
package io.syndesis.server.api.generator.swagger.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.openapi.v2.models.Oas20Document;
import io.apicurio.datamodels.openapi.v2.models.Oas20Operation;
import io.apicurio.datamodels.openapi.v2.models.Oas20SecurityScheme;
import io.syndesis.common.model.Violation;
import io.syndesis.common.util.json.JsonUtils;
import io.syndesis.server.api.generator.APIValidationContext;
import io.syndesis.server.api.generator.swagger.OpenApiModelInfo;
import io.syndesis.server.jsondb.impl.JsonRecordSupport;
import org.junit.Test;

import static io.syndesis.server.api.generator.swagger.TestHelper.resource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

public class Oas20ModelHelperTest {

    @Test
    public void convertingToJsonShouldNotLooseSecurityDefinitions() throws JsonProcessingException, IOException {
        final String definition = "{\"swagger\":\"2.0\",\"securityDefinitions\": {\n" +
            "        \"api-key-header\": {\n" +
            "            \"type\": \"apiKey\",\n" +
            "            \"name\": \"API-KEY\",\n" +
            "            \"in\": \"header\"\n" +
            "        },\n" +
            "        \"api-key-parameter\": {\n" +
            "            \"type\": \"apiKey\",\n" +
            "            \"name\": \"api_key\",\n" +
            "            \"in\": \"query\"\n" +
            "        }\n" +
            "    }}";

        final JsonNode node = Oas20ModelHelper.convertToJson(definition);

        final JsonNode securityDefinitions = node.get("securityDefinitions");

        assertThat(securityDefinitions.get("api-key-header")).isEqualTo(newNode()
            .put("type", "apiKey")
            .put("name", "API-KEY")
            .put("in", "header"));

        assertThat(securityDefinitions.get("api-key-parameter")).isEqualTo(newNode()
            .put("type", "apiKey")
            .put("name", "api_key")
            .put("in", "query"));
    }

    @Test
    public void convertingToJsonShouldNotLooseSecurityRequirements() throws JsonProcessingException, IOException {
        final String definition = "{\"swagger\":\"2.0\",\"paths\":{\"/api\":{\"get\":{\"security\":[{\"secured\":[\"scope\"]}]}}}}";
        final JsonNode node = Oas20ModelHelper.convertToJson(definition);
        assertThat(node.get("paths").get("/api").get("get").get("security"))
            .hasOnlyOneElementSatisfying(securityRequirement -> assertThat(securityRequirement.get("secured"))
                .hasOnlyOneElementSatisfying(scope -> assertThat(scope.asText()).isEqualTo("scope")));
    }

    @Test
    public void minimizingShouldNotLooseMultipleKeySecurityRequirements() throws JsonProcessingException, IOException {
        final String definition = "{\"swagger\":\"2.0\",\"paths\":{\"/api\":{\"get\":{\"security\":[{\"secured1\":[]},{\"secured2\":[]}]}}}}";

        final Oas20Document swagger = (Oas20Document) Library.readDocumentFromJSONString(definition);

        final String minimizedString = Oas20ModelHelper.minimalOpenApiUsedByComponent(swagger);

        final Oas20Document minimized = (Oas20Document) Library.readDocumentFromJSONString(minimizedString);

        final Oas20Operation getApi = (Oas20Operation) minimized.paths.getPathItem("/api").get;
        assertThat(getApi.security).hasSize(2);
        assertThat(getApi.security.get(0).getSecurityRequirementNames()).containsExactly("secured1");
        assertThat(getApi.security.get(0).getScopes("secured1")).isEmpty();
        assertThat(getApi.security.get(1).getSecurityRequirementNames()).containsExactly("secured2");
        assertThat(getApi.security.get(1).getScopes("secured2")).isEmpty();
    }

    @Test
    public void minimizingShouldNotLooseSecurityDefinitions() throws JsonProcessingException, IOException {
        final String definition = "{\"swagger\":\"2.0\",\"securityDefinitions\": {\n" +
            "        \"api-key-header\": {\n" +
            "            \"type\": \"apiKey\",\n" +
            "            \"name\": \"API-KEY\",\n" +
            "            \"in\": \"header\"\n" +
            "        },\n" +
            "        \"api-key-parameter\": {\n" +
            "            \"type\": \"apiKey\",\n" +
            "            \"name\": \"api_key\",\n" +
            "            \"in\": \"query\"\n" +
            "        }\n" +
            "    }}";

        final Oas20Document swagger = (Oas20Document) Library.readDocumentFromJSONString(definition);

        final String minimizedString = Oas20ModelHelper.minimalOpenApiUsedByComponent(swagger);

        final Oas20Document minimized = (Oas20Document) Library.readDocumentFromJSONString(minimizedString);

        Oas20SecurityScheme apiKeyHeader = new Oas20SecurityScheme("api-key-header");
        apiKeyHeader.type = "apiKey";
        apiKeyHeader.in = "header";
        apiKeyHeader.name = "API-KEY";

        Oas20SecurityScheme apiKeyParameter = new Oas20SecurityScheme("api-key-parameter");
        apiKeyParameter.type = "apiKey";
        apiKeyParameter.in = "query";
        apiKeyParameter.name = "api_key";

        assertThat(minimized.securityDefinitions.getSecuritySchemes()).hasSize(2);
        assertThat(minimized.securityDefinitions.getSecurityScheme(apiKeyHeader._schemeName).type).isEqualTo(apiKeyHeader.type);
        assertThat(minimized.securityDefinitions.getSecurityScheme(apiKeyHeader._schemeName).in).isEqualTo(apiKeyHeader.in);
        assertThat(minimized.securityDefinitions.getSecurityScheme(apiKeyHeader._schemeName).name).isEqualTo(apiKeyHeader.name);
        assertThat(minimized.securityDefinitions.getSecurityScheme(apiKeyParameter._schemeName).type).isEqualTo(apiKeyParameter.type);
        assertThat(minimized.securityDefinitions.getSecurityScheme(apiKeyParameter._schemeName).in).isEqualTo(apiKeyParameter.in);
        assertThat(minimized.securityDefinitions.getSecurityScheme(apiKeyParameter._schemeName).name).isEqualTo(apiKeyParameter.name);
    }

    @Test
    public void shouldNotReportIssuesWithSupportedVersions() {
        final OpenApiModelInfo validated = Oas20ModelHelper.parse(
            "{\"swagger\": \"2.0\", \"info\":{ \"title\": \"test\", \"version\": \"1\"}, \"paths\": { \"/api\": { \"get\": {\"responses\": { \"200\": { \"description\": \"OK\" }}}}}}",
            APIValidationContext.CONSUMED_API);

        final List<Violation> errors = validated.getErrors();
        assertThat(errors).isEmpty();
    }

    @Test
    public void shouldReportIssuesWithUnsupportedVersions() {
        final OpenApiModelInfo validated = Oas20ModelHelper.parse(
            "{\"openapi\": \"3.0.0\", \"info\":{ \"title\": \"test\", \"version\": \"1\"}, \"paths\": { \"/api\": { \"get\": {\"responses\": { \"200\": { \"description\": \"OK\" }}}}}}",
            APIValidationContext.CONSUMED_API);

        final List<Violation> errors = validated.getErrors();
        assertThat(errors).containsOnly(new Violation.Builder()
            .property("")
            .error("unsupported-version")
            .message("This document cannot be uploaded. Provide an OpenAPI 2.0 document.")
            .build());
    }

    @Test
    public void shouldSanitizeListOfTags() {
        assertThat(Oas20ModelHelper.sanitizeTags(Arrays.asList("tag", "wag ", " bag", ".]t%a$g#[/")))
            .containsExactly("tag", "wag", "bag");
    }

    @Test
    public void shouldSanitizeTags() {
        assertThat(Oas20ModelHelper.sanitizeTag("tag")).isEqualTo("tag");
        assertThat(Oas20ModelHelper.sanitizeTag(".]t%a$g#[/")).isEqualTo("tag");

        final char[] str = new char[1024];
        final String randomString = IntStream.range(0, str.length)
            .map(x -> (int) (Character.MAX_CODE_POINT * Math.random())).mapToObj(i -> new String(Character.toChars(i)))
            .collect(Collectors.joining(""));
        final String sanitized = Oas20ModelHelper.sanitizeTag(randomString);
        assertThatCode(() -> JsonRecordSupport.validateKey(sanitized)).doesNotThrowAnyException();
    }

    @Test
    public void testThatAllSwaggerFilesAreValid() throws IOException {
        final String[] specifications = {"/swagger/concur.swagger.json", "/swagger/petstore.swagger.json",
            "/swagger/todo.swagger.yaml"};

        for (final String specificationFile : specifications) {
            final String specification = resource(specificationFile);
            final OpenApiModelInfo info = Oas20ModelHelper.parse(specification, APIValidationContext.CONSUMED_API);

            assertThat(info.getErrors())
                .withFailMessage("Specification " + specificationFile + " has errors: " + info.getErrors()).isEmpty();
        }
    }

    @Test
    public void testThatInvalidFieldPetstoreSwaggerIsInvalid() throws IOException {
        final String specification = resource("/swagger/invalid/invalid-field.petstore.swagger.json");
        final OpenApiModelInfo info = Oas20ModelHelper.parse(specification, APIValidationContext.CONSUMED_API);

        assertThat(info.getErrors()).hasSize(1);
        assertThat(info.getWarnings()).isEmpty();
        assertThat(info.getErrors().get(0).message())
            .startsWith("object instance has properties which are not allowed by the schema");
        assertThat(info.getErrors().get(0).property()).contains("/paths/~1pet/put");
        assertThat(info.getErrors().get(0).error()).contains("validation");
    }

    @Test
    public void testThatInvalidSchemePetstoreSwaggerIsInvalid() throws IOException {
        final String specification = resource("/swagger/invalid/invalid-scheme.petstore.swagger.json");
        final OpenApiModelInfo info = Oas20ModelHelper.parse(specification, APIValidationContext.CONSUMED_API);

        assertThat(info.getErrors()).hasSize(1);
        assertThat(info.getWarnings()).hasSize(1);
        assertThat(info.getErrors().get(0).message()).startsWith("instance value (\"httpz\") not found in enum");
        assertThat(info.getErrors().get(0).property()).contains("/schemes/0");
        assertThat(info.getErrors().get(0).error()).contains("validation");
        assertThat(info.getWarnings().get(0).message()).startsWith("Unable to determine the scheme");
        assertThat(info.getWarnings().get(0).property()).contains("/schemes");
        assertThat(info.getWarnings().get(0).error()).contains("missing-schemes");
    }

    @Test
    public void testThatInvalidTypePetstoreSwaggerIsInvalid() throws IOException {
        final String specification = resource("/swagger/invalid/invalid-type.petstore.swagger.json");
        final OpenApiModelInfo info = Oas20ModelHelper.parse(specification, APIValidationContext.CONSUMED_API);

        assertThat(info.getErrors()).hasSize(1);
        assertThat(info.getWarnings()).isEmpty();
        assertThat(info.getErrors().get(0).message()).startsWith("instance failed to match exactly one schema");
        assertThat(info.getErrors().get(0).property()).contains("/paths/~1pet~1{petId}/post/parameters/2");
        assertThat(info.getErrors().get(0).error()).contains("validation");
    }

    @Test
    public void testThatWarningPetstoreSwaggerContainsWarnings() throws IOException {
        final String specification = resource("/swagger/invalid/warning-petstore.swagger.json");
        final OpenApiModelInfo info = Oas20ModelHelper.parse(specification, APIValidationContext.CONSUMED_API);

        assertThat(info.getErrors()).isEmpty();
        assertThat(info.getWarnings()).hasSize(2);
    }

    private static ObjectNode newNode() {
        return JsonUtils.copyObjectMapperConfiguration().createObjectNode();
    }
}
