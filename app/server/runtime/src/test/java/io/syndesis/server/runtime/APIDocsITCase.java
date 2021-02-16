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
package io.syndesis.server.runtime;

import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;

public class APIDocsITCase extends BaseITCase {

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static final Class<Map<String, Map<?, ?>>> TYPE = (Class) Map.class;

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("data")
    public void testOpenApiDocsIndex(final String path, final String text) {
        final ResponseEntity<String> response = restTemplate().getForEntity("/api/v1" + path + "index.html", String.class);
        assertThat(response.getStatusCode()).as("OpenAPI docs index.html response code").isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().length()).as("OpenAPI index.html length").isPositive();
        assertThat(response.getBody()).as("OpenAPI index.html example path").contains(text);
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("data")
    public void testOpenAPIDocsIndexWithToken(final String path, final String text) {
        final ResponseEntity<String> response = get("/api/v1" + path + "index.html", String.class);
        assertThat(response.getStatusCode()).as("OpenAPI docs index.html response code").isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().length()).as("OpenAPI index.html length").isPositive();
        assertThat(response.getBody()).as("OpenAPI index.html example path").contains(text);
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("data")
    public void testOpenAPIJson(final String path) {
        final ResponseEntity<JsonNode> response = restTemplate().getForEntity("/api/v1" + path + "openapi.json", JsonNode.class);
        assertThat(response.getStatusCode()).as("OpenAPI json response code").isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("paths").size()).as("OpenAPI json number of paths").isPositive();
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("data")
    public void testOpenAPIJsonWithToken(final String path) {
        final ResponseEntity<JsonNode> response = get("/api/v1" + path + "openapi.json", JsonNode.class);
        assertThat(response.getStatusCode()).as("OpenAPI json response code").isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("paths").size()).as("OpenAPI json number of paths").isPositive();
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("data")
    public void testOpenAPIYaml(final String path) {
        final ResponseEntity<Map<String, Map<?, ?>>> response = restTemplate().getForEntity("/api/v1" + path + "openapi.json", TYPE);
        assertThat(response.getStatusCode()).as("OpenAPI yaml response code").isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("paths").size()).as("OpenAPI json number of paths").isPositive();
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("data")
    public void testOpenAPIYamlWithToken(final String path) {
        final ResponseEntity<Map<String, Map<?, ?>>> response = get("/api/v1" + path + "openapi.yaml", TYPE);
        assertThat(response.getStatusCode()).as("OpenAPI yaml response code").isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("paths").size()).as("OpenAPI json number of paths").isPositive();
    }

    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {{"/internal/", "Syndesis Internal API"}, {"/", "Syndesis Supported API"}});
    }

}
