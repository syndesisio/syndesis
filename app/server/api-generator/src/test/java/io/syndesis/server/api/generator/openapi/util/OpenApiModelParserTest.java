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
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.syndesis.common.model.Violation;
import io.syndesis.common.util.json.JsonUtils;
import io.syndesis.server.api.generator.APIValidationContext;
import io.syndesis.server.api.generator.openapi.OpenApiModelInfo;
import org.junit.jupiter.api.Test;

import static io.syndesis.server.api.generator.openapi.TestHelper.resource;
import static org.assertj.core.api.Assertions.assertThat;

public class OpenApiModelParserTest {

    @Test
    public void convertingToJsonShouldNotLooseSecurityDefinitions() throws IOException {
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

        final JsonNode node = OpenApiModelParser.convertToJson(definition);

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
    public void convertingToJsonShouldNotLooseSecurityRequirements() throws IOException {
        final String definition = "{\"swagger\":\"2.0\",\"paths\":{\"/api\":{\"get\":{\"security\":[{\"secured\":[\"scope\"]}]}}}}";
        final JsonNode node = OpenApiModelParser.convertToJson(definition);
        assertThat(node.get("paths").get("/api").get("get").get("security"))
            .hasOnlyOneElementSatisfying(securityRequirement -> assertThat(securityRequirement.get("secured"))
                .hasOnlyOneElementSatisfying(scope -> assertThat(scope.asText()).isEqualTo("scope")));
    }

    @Test
    public void shouldNotReportIssuesWithSupportedV2Versions() {
        final OpenApiModelInfo validated = OpenApiModelParser.parse(
            "{\"swagger\": \"2.0\", \"info\":{ \"title\": \"test\", \"version\": \"1\"}, \"paths\": { \"/api\": { \"get\": {\"responses\": { \"200\": { \"description\": \"OK\" }}}}}}",
            APIValidationContext.CONSUMED_API);

        final List<Violation> errors = validated.getErrors();
        assertThat(errors).isEmpty();
    }

    @Test
    public void shouldReportIssuesWithUnsupportedV2Versions() {
        final OpenApiModelInfo validated = OpenApiModelParser.parse(
            "{\"swagger\": \"1.0\", \"info\":{ \"title\": \"test\", \"version\": \"1\"}, \"paths\": { \"/api\": { \"get\": {\"responses\": { \"200\": { \"description\": \"OK\" }}}}}}",
            APIValidationContext.CONSUMED_API);

        final List<Violation> errors = validated.getErrors();
        assertThat(errors).containsOnly(new Violation.Builder()
            .property("")
            .error("unsupported-version")
            .message("This document cannot be uploaded. Provide an OpenAPI document (supported versions are 2.0, 3.0).")
            .build());
    }

    @Test
    public void shouldNotReportIssuesWithSupportedV3Versions() {
        final OpenApiModelInfo validated = OpenApiModelParser.parse(
            "{\"openapi\": \"3.0.2\", \"info\": { \"title\": \"test\", \"description\": \"\", \"version\": \"0.0.1\" }, \"paths\": { \"/api\": { \"get\": {\"responses\": { \"200\": { \"description\": \"OK\" }}}}}}",
            APIValidationContext.CONSUMED_API);

        final List<Violation> errors = validated.getErrors();
        assertThat(errors).isEmpty();
    }

    @Test
    public void shouldReportIssuesWithUnsupportedV3Versions() {
        final OpenApiModelInfo validated = OpenApiModelParser.parse(
            "{\"openapi\": \"4.0\", \"info\": { \"title\": \"test\", \"description\": \"\", \"version\": \"0.0.1\" }, \"paths\": { \"/api\": { \"get\": {\"responses\": { \"200\": { \"description\": \"OK\" }}}}}}",
            APIValidationContext.CONSUMED_API);

        final List<Violation> errors = validated.getErrors();
        assertThat(errors).containsOnly(new Violation.Builder()
            .property("")
            .error("unsupported-version")
            .message("This document cannot be uploaded. Provide an OpenAPI document (supported versions are 2.0, 3.0).")
            .build());
    }

    @Test
    public void testThatAllSwaggerFilesAreValid() throws IOException {
        final String[] specifications = {"/openapi/v2/concur.json", "/openapi/v2/petstore.json",
            "/openapi/v2/todo.yaml"};

        for (final String specificationFile : specifications) {
            final String specification = resource(specificationFile);
            final OpenApiModelInfo info = OpenApiModelParser.parse(specification, APIValidationContext.CONSUMED_API);

            assertThat(info.getErrors())
                .withFailMessage("Specification " + specificationFile + " has errors: " + info.getErrors()).isEmpty();
        }
    }

    @Test
    public void testThatInvalidFieldPetstoreSwaggerIsInvalid() throws IOException {
        final String specification = resource("/openapi/v2/invalid/invalid-field.petstore.json");
        final OpenApiModelInfo info = OpenApiModelParser.parse(specification, APIValidationContext.CONSUMED_API);

        assertThat(info.getErrors()).hasSize(1);
        assertThat(info.getWarnings()).isEmpty();
        assertThat(info.getErrors().get(0).message())
            .startsWith("object instance has properties which are not allowed by the schema");
        assertThat(info.getErrors().get(0).property()).contains("/paths/~1pet/put");
        assertThat(info.getErrors().get(0).error()).contains("validation");
    }

    @Test
    public void testThatInvalidSchemePetstoreSwaggerIsInvalid() throws IOException {
        final String specification = resource("/openapi/v2/invalid/invalid-scheme.petstore.json");
        final OpenApiModelInfo info = OpenApiModelParser.parse(specification, APIValidationContext.CONSUMED_API);

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
        final String specification = resource("/openapi/v2/invalid/invalid-type.petstore.json");
        final OpenApiModelInfo info = OpenApiModelParser.parse(specification, APIValidationContext.CONSUMED_API);

        assertThat(info.getErrors()).hasSize(1);
        assertThat(info.getWarnings()).isEmpty();
        assertThat(info.getErrors().get(0).message()).startsWith("instance failed to match exactly one schema");
        assertThat(info.getErrors().get(0).property()).contains("/paths/~1pet~1{petId}/post/parameters/2");
        assertThat(info.getErrors().get(0).error()).contains("validation");
    }

    @Test
    public void testThatWarningPetstoreSwaggerContainsWarnings() throws IOException {
        final String specification = resource("/openapi/v2/invalid/warning-petstore.json");
        final OpenApiModelInfo info = OpenApiModelParser.parse(specification, APIValidationContext.CONSUMED_API);

        assertThat(info.getErrors()).isEmpty();
        assertThat(info.getWarnings()).hasSize(2);
    }

    private static ObjectNode newNode() {
        return JsonUtils.copyObjectMapperConfiguration().createObjectNode();
    }
}
