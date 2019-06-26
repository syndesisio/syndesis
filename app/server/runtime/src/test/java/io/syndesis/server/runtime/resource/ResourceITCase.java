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
package io.syndesis.server.runtime.resource;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import io.syndesis.common.model.Kind;
import io.syndesis.common.model.openapi.OpenApi;
import io.syndesis.server.runtime.BaseITCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourceITCase extends BaseITCase {

    private static final String ID = "openapi-1";

    @After
    public void removeTestDocument() {
        dataManager.delete(OpenApi.class, ID);
    }

    @Test
    public void shouldServeOpenApiResources() {
        final String url = "/api/v1/resources/" + Kind.OpenApi.modelName + "/" + ID;

        final ResponseEntity<String> resource = get(url, String.class, tokenRule.validToken(), HttpStatus.OK);

        assertThat(resource.getHeaders()).containsEntry("Content-Type", Collections.singletonList("application/vnd.oai.openapi+json"));
        assertThat(resource.getBody()).isEqualTo("specification");
    }

    @Before
    public void storeOpenApiDocument() {
        dataManager.store(new OpenApi.Builder()
            .id(ID)
            .document("specification".getBytes(StandardCharsets.UTF_8))
            .build(),
            OpenApi.class);
    }
}
