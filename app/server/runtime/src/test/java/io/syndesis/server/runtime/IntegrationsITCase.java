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
import java.util.Map;
import java.util.Optional;

import io.syndesis.common.model.Violation;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.IntegrationOverview;
import io.syndesis.server.endpoint.v1.handler.exception.RestError;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;

public class IntegrationsITCase extends BaseITCase {

    @SuppressWarnings({"unchecked", "rawtypes"})
    private final static Class<List<Violation>> RESPONSE_TYPE = (Class) List.class;

    @Override
    @Before
    public void clearDB() {
        super.clearDB();
    }

    @Test
    public void integrationsListForbidden() {
        ResponseEntity<JsonNode> response = restTemplate().getForEntity("/api/v1/integrations", JsonNode.class);
        assertThat(response.getStatusCode()).as("integrations list status code").isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void invalidSortField() {
        ResponseEntity<RestError> response = get("/api/v1/integrations?sort=invalid_field", RestError.class, HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getErrorCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().getUserMsg()).isEqualTo("Please check your sorting arguments");
        assertThat(response.getBody().getDeveloperMsg()).startsWith("Illegal Argument on Call");
    }

    @Test
    public void createAndGetIntegration() {

        // Verify that the integration does not exist.
        get("/api/v1/integrations/2001", RestError.class,
            tokenRule.validToken(), HttpStatus.NOT_FOUND);

        // Create the integration.
        Integration integration = new Integration.Builder()
            .id("2001")
            .name("test")
            .build();
        post("/api/v1/integrations", integration, Integration.class);

        // Validate we can now fetch it.
        ResponseEntity<IntegrationOverview> result = get("/api/v1/integrations/2001", IntegrationOverview.class);
        assertThat(result.getBody().getName()).as("name").isEqualTo("test");

        // Create another integration.
        integration = new Integration.Builder()
            .id("2002")
            .name("test2")
            .build();
        post("/api/v1/integrations", integration, Integration.class);

        // Check the we can list the integrations.
        ResponseEntity<IntegrationListResult> list = get("/api/v1/integrations", IntegrationListResult.class);

        assertThat(list.getBody().getTotalCount()).as("total count").isEqualTo(2);
        assertThat(list.getBody().getItems()).as("items").hasSize(2);

        // We should be able to export the integration too.
        ResponseEntity<byte[]> exportData = get("/api/v1/integration-support/export.zip?id=2001", byte[].class);
        assertThat(exportData.getBody()).isNotNull();

        // Lets delete it
        delete("/api/v1/integrations/2001");

        // We should not be able to fetch it again..
        get("/api/v1/integrations/2001", RestError.class,
            tokenRule.validToken(), HttpStatus.NOT_FOUND);

        // The list size should get smaller
        list = get("/api/v1/integrations", IntegrationListResult.class);
        assertThat(list.getBody().getTotalCount()).as("total count").isEqualTo(1);
        assertThat(list.getBody().getItems()).as("items").hasSize(1);

        // Lets now re-import the integration:
        post("/api/v1/integration-support/import", exportData.getBody(), byte[].class);
    }

    @Test
    public void shouldDetermineValidityForInvalidIntegrations() {
        dataManager.create(new Integration.Builder().name("Existing integration").build());

        final Integration integration = new Integration.Builder().name("Existing integration").build();

        final ResponseEntity<List<Violation>> got = post("/api/v1/integrations/validation", integration, RESPONSE_TYPE,
            tokenRule.validToken(), HttpStatus.BAD_REQUEST);

        assertThat(got.getBody()).hasSize(1);
    }

    @Test
    public void shouldDetermineValidityForValidIntegrations() {
        final Integration integration = new Integration.Builder().name("Test integration").build();

        final ResponseEntity<List<Violation>> got = post("/api/v1/integrations/validation", integration, RESPONSE_TYPE,
            tokenRule.validToken(), HttpStatus.NO_CONTENT);

        assertThat(got.getBody()).isNull();
    }

    @Test
    public void patchIntegrationDescription() {
        Integration integration = new Integration.Builder()
            .id("3001")
            .name("test")
            .description("My first description")
            .build();

        post("/api/v1/integrations", integration, Integration.class);
        ResponseEntity<IntegrationOverview> result = get("/api/v1/integrations/3001", IntegrationOverview.class);
        assertThat(result.getBody().getDescription())
            .as("description")
            .isEqualTo(Optional.of("My first description"));

        // Do the PATCH API call:
        Map<String, Object> patchDoc = Collections.singletonMap("description", "The second description");
        patch("/api/v1/integrations/3001", patchDoc);

        result = get("/api/v1/integrations/3001", IntegrationOverview.class);
        assertThat(result.getBody().getDescription())
            .as("description")
            .isEqualTo(Optional.of("The second description"));
    }

    public static class IntegrationListResult {
        public int totalCount;
        public List<IntegrationOverview> items;

        public int getTotalCount() {
            return totalCount;
        }

        public List<IntegrationOverview> getItems() {
            return items;
        }
    }

}
