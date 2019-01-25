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

import io.syndesis.common.model.integration.Integration;
import io.syndesis.server.runtime.BaseITCase;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

import static io.syndesis.server.runtime.integration.MultipartUtil.MULTIPART;
import static io.syndesis.server.runtime.integration.MultipartUtil.specification;
import static io.syndesis.server.runtime.integration.SwaggerHickups.reparse;

import static org.assertj.core.api.Assertions.assertThat;

public class IntegrationSpecificationITCase extends BaseITCase {
    @Test
    public void shouldServeOpenApiSpecificationInJsonFormat() throws IOException, JSONException {
        final MultiValueMap<Object, Object> data = specification("/io/syndesis/server/runtime/test-swagger.json");

        final ResponseEntity<Integration> integrationResponse = post("/api/v1/apis/generator", data, Integration.class, tokenRule.validToken(), HttpStatus.OK,
            MULTIPART);

        final Integration integration = integrationResponse.getBody();
        dataManager.create(integration);

        final ResponseEntity<ByteArrayResource> specificationResponse = get("/api/v1/integrations/" + integration.getId().get() + "/specification",
            ByteArrayResource.class);

        assertThat(specificationResponse.getHeaders().getContentType()).isEqualTo(MediaType.valueOf("application/vnd.oai.openapi+json"));

        final String givenJson = reparse("io/syndesis/server/runtime/test-swagger.json");
        final String receivedJson = new String(specificationResponse.getBody().getByteArray(), StandardCharsets.UTF_8);
        JSONAssert.assertEquals(givenJson, receivedJson, JSONCompareMode.LENIENT);
    }

    @Test
    public void shouldServeOpenApiSpecificationInYamlFormat() throws IOException, JSONException {
        final MultiValueMap<Object, Object> data = specification("/io/syndesis/server/runtime/test-swagger.yaml");

        final ResponseEntity<Integration> integrationResponse = post("/api/v1/apis/generator", data, Integration.class, tokenRule.validToken(), HttpStatus.OK,
            MULTIPART);

        final Integration integration = integrationResponse.getBody();
        dataManager.create(integration);

        final ResponseEntity<ByteArrayResource> specificationResponse = get("/api/v1/integrations/" + integration.getId().get() + "/specification",
            ByteArrayResource.class);

        assertThat(specificationResponse.getHeaders().getContentType()).isEqualTo(MediaType.valueOf("application/vnd.oai.openapi"));
        final String givenJson = reparse("io/syndesis/server/runtime/test-swagger.yaml");
        final String receivedJson = new String(specificationResponse.getBody().getByteArray(), StandardCharsets.UTF_8);
        JSONAssert.assertEquals(givenJson, receivedJson, JSONCompareMode.LENIENT);
    }
}
