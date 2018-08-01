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

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import io.syndesis.common.model.Violation;
import io.syndesis.common.model.integration.Integration;

import static org.assertj.core.api.Assertions.assertThat;

public class UpdaterITCase extends BaseITCase {

    @Test
    public void updaterShouldValidateAfterPatching() {
        dataManager.create(new Integration.Builder().name("Existing integration").build());

        final Integration integration = new Integration.Builder().name("New integration").build();

        final ResponseEntity<Integration> created = post("/api/v1/integrations", integration, Integration.class, tokenRule.validToken(),
            HttpStatus.OK);

        final String integrationId = created.getBody().getId().get();
        final ResponseEntity<List<Violation>> response = patch("/api/v1/integrations/" + integrationId,
            Collections.singletonMap("name", "Existing integration"), new ParameterizedTypeReference<List<Violation>>() {
                // type token pattern
            }, tokenRule.validToken(), HttpStatus.BAD_REQUEST);

        assertThat(response.getBody()).containsOnly(
            new Violation.Builder().error("NoDuplicateIntegration").property("name").message("Integration name 'Existing integration' is not unique").build());
    }

}
