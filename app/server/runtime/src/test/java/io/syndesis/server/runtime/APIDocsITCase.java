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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class APIDocsITCase extends BaseITCase {

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static final Class<Map<String, Map<?, ?>>> TYPE = (Class) Map.class;

    @Parameter(0)
    public String path;

    @Parameter(1)
    public String text;

    @Test
    public void testSwaggerDocsIndex() {
        final ResponseEntity<String> response = restTemplate().getForEntity("/api/v1" + path + "index.html", String.class);
        assertThat(response.getStatusCode()).as("swagger docs index.html response code").isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().length()).as("swagger index.html length").isPositive();
        assertThat(response.getBody()).as("swagger index.html example path").contains(text);
    }

    @Test
    public void testSwaggerDocsIndexWithToken() {
        final ResponseEntity<String> response = get("/api/v1" + path + "index.html", String.class);
        assertThat(response.getStatusCode()).as("swagger docs index.html response code").isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().length()).as("swagger index.html length").isPositive();
        assertThat(response.getBody()).as("swagger index.html example path").contains(text);
    }

    @Test
    public void testSwaggerJson() {
        final ResponseEntity<JsonNode> response = restTemplate().getForEntity("/api/v1" + path + "swagger.json", JsonNode.class);
        assertThat(response.getStatusCode()).as("swagger json response code").isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("paths").size()).as("swagger json number of paths").isPositive();
    }

    @Test
    public void testSwaggerJsonWithToken() {
        final ResponseEntity<JsonNode> response = get("/api/v1" + path + "swagger.json", JsonNode.class);
        assertThat(response.getStatusCode()).as("swagger json response code").isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("paths").size()).as("swagger json number of paths").isPositive();
    }

    @Test
    public void testSwaggerYaml() {
        final ResponseEntity<Map<String, Map<?, ?>>> response = restTemplate().getForEntity("/api/v1" + path + "swagger.json", TYPE);
        assertThat(response.getStatusCode()).as("swagger yaml response code").isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("paths").size()).as("swagger json number of paths").isPositive();
    }

    @Test
    public void testSwaggerYamlWithToken() {
        final ResponseEntity<Map<String, Map<?, ?>>> response = get("/api/v1" + path + "swagger.yaml", TYPE);
        assertThat(response.getStatusCode()).as("swagger yaml response code").isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("paths").size()).as("swagger json number of paths").isPositive();
    }

    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {{"/internal/", "Syndesis API"}, {"/", "Syndesis Supported API"}});
    }

}
