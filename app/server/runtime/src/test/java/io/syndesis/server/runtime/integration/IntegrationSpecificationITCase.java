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
package io.syndesis.server.runtime.integration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import io.swagger.util.Json;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.util.KeyGenerator;
import io.syndesis.server.runtime.BaseITCase;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import static io.syndesis.common.util.Resources.getResourceAsText;
import static io.syndesis.server.runtime.integration.MultipartUtil.MULTIPART;
import static io.syndesis.server.runtime.integration.MultipartUtil.specification;

import static org.assertj.core.api.Assertions.assertThat;

public class IntegrationSpecificationITCase extends BaseITCase {
    @Test
    public void shouldServeOpenApiSpecificationInJsonFormat() throws IOException, JSONException {
        final MultiValueMap<Object, Object> data = specification("/io/syndesis/server/runtime/test-swagger.json");

        final ResponseEntity<Integration> integrationResponse = post("/api/v1/apis/generator", data, Integration.class, tokenRule.validToken(), HttpStatus.OK,
            MULTIPART);

        final Integration integration = integrationResponse.getBody();
        final String integrationId = KeyGenerator.createKey();
        dataManager.create(integration.builder()
            .id(integrationId)
            .build());

        final ResponseEntity<ByteArrayResource> specificationResponse = get("/api/v1/integrations/" + integrationId + "/specification",
            ByteArrayResource.class);

        assertThat(specificationResponse.getHeaders().getContentType()).isEqualTo(MediaType.valueOf("application/vnd.oai.openapi+json"));

        final String givenJson = getResourceAsText("io/syndesis/server/runtime/test-swagger.json");
        final String receivedJson = new String(specificationResponse.getBody().getByteArray(), StandardCharsets.UTF_8);

        final JSONCompareResult compareResult = JSONCompare.compareJSON(givenJson, receivedJson, JSONCompareMode.NON_EXTENSIBLE);
        assertThat(compareResult.getFieldMissing()).isEmpty();
        assertThat(compareResult.getFieldFailures()).isEmpty();
        assertThat(compareResult.getFieldUnexpected()).as("Backend is allowed to add missing operationId fields, found other differences").hasSize(3)
            .allSatisfy(failure -> "operationId".equals(failure.getActual()));
    }

    @Test
    public void shouldServeOpenApiSpecificationInYamlFormat() throws IOException, JSONException {
        final MultiValueMap<Object, Object> data = specification("/io/syndesis/server/runtime/test-swagger.yaml");

        final ResponseEntity<Integration> integrationResponse = post("/api/v1/apis/generator", data, Integration.class, tokenRule.validToken(), HttpStatus.OK,
            MULTIPART);

        final Integration integration = integrationResponse.getBody();
        final String integrationId = KeyGenerator.createKey();
        dataManager.create(integration.builder()
            .id(integrationId)
            .build());

        final ResponseEntity<ByteArrayResource> specificationResponse = get("/api/v1/integrations/" + integrationId + "/specification",
            ByteArrayResource.class);

        assertThat(specificationResponse.getHeaders().getContentType()).isEqualTo(MediaType.valueOf("application/vnd.oai.openapi"));
        final String givenYaml = getResourceAsText("io/syndesis/server/runtime/test-swagger.yaml");
        final String receivedJson = new String(specificationResponse.getBody().getByteArray(), StandardCharsets.UTF_8);

        final Object givenYamlObject = new Yaml(new SafeConstructor()).load(givenYaml);
        final String givenJson = Json.mapper().writer().writeValueAsString(givenYamlObject);

        final JSONCompareResult compareResult = JSONCompare.compareJSON(givenJson, receivedJson, JSONCompareMode.NON_EXTENSIBLE);
        assertThat(compareResult.getFieldMissing()).isEmpty();
        assertThat(compareResult.getFieldFailures()).isEmpty();
        assertThat(compareResult.getFieldUnexpected()).as("Backend is allowed to add missing operationId fields, found other differences").hasSize(5)
            .allSatisfy(failure -> "operationId".equals(failure.getActual()));
    }
}
