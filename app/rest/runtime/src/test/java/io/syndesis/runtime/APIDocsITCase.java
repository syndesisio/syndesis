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
package io.syndesis.runtime;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class APIDocsITCase extends BaseITCase {

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static final Class<Map<String, Map<?, ?>>> TYPE = (Class) Map.class;

    @Test
    public void testSwaggerJson() {
        ResponseEntity<JsonNode> response = restTemplate().getForEntity("/api/v1/swagger.json", JsonNode.class);
        assertThat(response.getStatusCode()).as("swagger json response code").isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("paths").size()).as("swagger json number of paths").isPositive();
    }

    @Test
    public void testSwaggerJsonWithToken() {
        ResponseEntity<JsonNode> response = get("/api/v1/swagger.json", JsonNode.class);
        assertThat(response.getStatusCode()).as("swagger json response code").isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("paths").size()).as("swagger json number of paths").isPositive();
    }

    @Test
    public void testSwaggerYaml() {
        ResponseEntity<Map<String, Map<?, ?>>> response = restTemplate().getForEntity("/api/v1/swagger.yaml", TYPE);
        assertThat(response.getStatusCode()).as("swagger yaml response code").isEqualTo(HttpStatus.OK);
        assertThat((response.getBody().get("paths")).size()).as("swagger json number of paths").isPositive();
    }

    @Test
    public void testSwaggerYamlWithToken() {
        ResponseEntity<Map<String, Map<?, ?>>> response = get("/api/v1/swagger.yaml", TYPE);
        assertThat(response.getStatusCode()).as("swagger yaml response code").isEqualTo(HttpStatus.OK);
        assertThat((response.getBody().get("paths")).size()).as("swagger json number of paths").isPositive();
    }

    @Test
    public void testSwaggerDocsIndex() {
        ResponseEntity<String> response = restTemplate().getForEntity("/api/v1/index.html", String.class);
        assertThat(response.getStatusCode()).as("swagger docs index.html response code").isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().length()).as("swagger index.html length").isPositive();
        assertThat(response.getBody()).as("swagger index.html example path").contains("/connectors/{id}");
    }

    @Test
    public void testSwaggerDocsIndexWithToken() {
        ResponseEntity<String> response = get("/api/v1/index.html", String.class);
        assertThat(response.getStatusCode()).as("swagger docs index.html response code").isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().length()).as("swagger index.html length").isPositive();
        assertThat(response.getBody()).as("swagger index.html example path").contains("/connectors/{id}");
    }

}
