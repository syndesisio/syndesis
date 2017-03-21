/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.ipaas.runtime;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.ipaas.model.integration.Integration;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

public class IntegrationsITCase extends BaseITCase {

    @Before
    public void databaseReset() {
        super.databaseReset();
    }

    @Test
    public void integrationsListWithoutToken() {
        ResponseEntity<JsonNode> response = restTemplate().getForEntity("/api/v1/integrations", JsonNode.class);
        assertThat(response.getStatusCode()).as("list status code").isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void integrationsListWithExpiredToken() {
        ResponseEntity<JsonNode> response = get("/api/v1/integrations", JsonNode.class, tokenRule.expiredToken(), null);
        assertThat(response.getStatusCode()).as("status code").isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void createAndGetIntegration() {

        // Verify that the integration does not exist.
        get("/api/v1/integrations/2001", Integration.class,
            tokenRule.validToken(), HttpStatus.NOT_FOUND);

        // Create the integration.
        Integration integration = new Integration.Builder().id("2001").name("test").build();
        post("/api/v1/integrations", integration, Integration.class);

        // Validate we can now fetch it.
        ResponseEntity<Integration> result = get("/api/v1/integrations/2001", Integration.class);
        assertThat(result.getBody().getName()).as("name").isEqualTo("test");

        // Create another integration.
        integration = new Integration.Builder().id("2002").name("test2").build();
        post("/api/v1/integrations", integration, Integration.class);

        // Check the we can list the integrations.
        ResponseEntity<IntegrationListResult> list = get("/api/v1/integrations", IntegrationListResult.class);

        assertThat(list.getBody().getTotalCount()).as("total count").isEqualTo(2);
        assertThat(list.getBody().getItems()).as("items").hasSize(2);

        // Lets delete it
        delete("/api/v1/integrations/2001");

        // We should not be able to fetch it again..
        get("/api/v1/integrations/2001", Integration.class,
            tokenRule.validToken(), HttpStatus.NOT_FOUND);


        // The list size should get smaller
        list = get("/api/v1/integrations", IntegrationListResult.class);
        assertThat(list.getBody().getTotalCount()).as("total count").isEqualTo(1);
        assertThat(list.getBody().getItems()).as("items").hasSize(1);

    }

    public static class IntegrationListResult {
        public int totalCount;
        public ArrayList<Integration> items;

        public int getTotalCount() {
            return totalCount;
        }

        public ArrayList<Integration> getItems() {
            return items;
        }
    };

}
