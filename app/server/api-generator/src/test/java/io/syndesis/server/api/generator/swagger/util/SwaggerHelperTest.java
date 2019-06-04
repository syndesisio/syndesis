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

import io.syndesis.common.model.Violation;
import io.syndesis.server.api.generator.APIValidationContext;
import io.syndesis.server.api.generator.swagger.AbstractSwaggerConnectorTest;
import io.syndesis.server.api.generator.swagger.SwaggerModelInfo;
import io.syndesis.server.jsondb.impl.JsonRecordSupport;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import static io.syndesis.server.api.generator.swagger.TestHelper.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

public class SwaggerHelperTest extends AbstractSwaggerConnectorTest {

    @Test
    public void convertingToJsonShouldNotLooseSecurityRequirements() throws JsonProcessingException, IOException {
        final String definition = "{\"swagger\":\"2.0\",\"paths\":{\"/api\":{\"get\":{\"security\":[{\"secured\":[\"scope\"]}]}}}}";
        final JsonNode node = SwaggerHelper.convertToJson(definition);
        assertThat(node.get("paths").get("/api").get("get").get("security"))
            .hasOnlyOneElementSatisfying(securityRequirement -> assertThat(securityRequirement.get("secured"))
                .hasOnlyOneElementSatisfying(scope -> assertThat(scope.asText()).isEqualTo("scope")));
    }

    @Test
    public void shouldNotReportIssuesWithSupportedVersions() {
        final SwaggerModelInfo validated = SwaggerHelper.parse(
            "{\"swagger\": \"2.0\", \"info\":{ \"title\": \"test\", \"version\": \"1\"}, \"paths\": { \"/api\": { \"get\": {\"responses\": { \"200\": { \"description\": \"OK\" }}}}}}",
            APIValidationContext.CONSUMED_API);

        final List<Violation> errors = validated.getErrors();
        assertThat(errors).isEmpty();
    }

    @Test
    public void shouldReportIssuesWithUnsupportedVersions() {
        final SwaggerModelInfo validated = SwaggerHelper.parse(
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
        assertThat(SwaggerHelper.sanitizeTags(Arrays.asList("tag", "wag ", " bag", ".]t%a$g#[/")))
            .containsExactly("tag", "wag", "bag");
    }

    @Test
    public void shouldSanitizeTags() {
        assertThat(SwaggerHelper.sanitizeTag("tag")).isEqualTo("tag");
        assertThat(SwaggerHelper.sanitizeTag(".]t%a$g#[/")).isEqualTo("tag");

        final char[] str = new char[1024];
        final String randomString = IntStream.range(0, str.length)
            .map(x -> (int) (Character.MAX_CODE_POINT * Math.random())).mapToObj(i -> new String(Character.toChars(i)))
            .collect(Collectors.joining(""));
        final String sanitized = SwaggerHelper.sanitizeTag(randomString);
        assertThatCode(() -> JsonRecordSupport.validateKey(sanitized)).doesNotThrowAnyException();
    }

    @Test
    public void testThatAllSwaggerFilesAreValid() throws IOException {
        final String[] specifications = {"/swagger/concur.swagger.json", "/swagger/petstore.swagger.json",
            "/swagger/todo.swagger.yaml"};

        for (final String specificationFile : specifications) {
            final String specification = resource(specificationFile);
            final SwaggerModelInfo info = SwaggerHelper.parse(specification, APIValidationContext.CONSUMED_API);

            assertThat(info.getErrors())
                .withFailMessage("Specification " + specificationFile + " has errors: " + info.getErrors()).isEmpty();
        }
    }

    @Test
    public void testThatInvalidFieldPetstoreSwaggerIsInvalid() throws IOException {
        final String specification = resource("/swagger/invalid/invalid-field.petstore.swagger.json");
        final SwaggerModelInfo info = SwaggerHelper.parse(specification, APIValidationContext.CONSUMED_API);

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
        final SwaggerModelInfo info = SwaggerHelper.parse(specification, APIValidationContext.CONSUMED_API);

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
        final SwaggerModelInfo info = SwaggerHelper.parse(specification, APIValidationContext.CONSUMED_API);

        assertThat(info.getErrors()).hasSize(1);
        assertThat(info.getWarnings()).isEmpty();
        assertThat(info.getErrors().get(0).message()).startsWith("instance failed to match exactly one schema");
        assertThat(info.getErrors().get(0).property()).contains("/paths/~1pet~1{petId}/post/parameters/2");
        assertThat(info.getErrors().get(0).error()).contains("validation");
    }

    @Test
    public void testThatWarningPetstoreSwaggerContainsWarnings() throws IOException {
        final String specification = resource("/swagger/invalid/warning-petstore.swagger.json");
        final SwaggerModelInfo info = SwaggerHelper.parse(specification, APIValidationContext.CONSUMED_API);

        assertThat(info.getErrors()).isEmpty();
        assertThat(info.getWarnings()).hasSize(2);
    }
}
