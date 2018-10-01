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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.IntegrationDeploymentState;
import io.syndesis.server.runtime.BaseITCase;

import org.awaitility.Awaitility;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class IntegrationDeploymentITCase extends BaseITCase {

    public static final class IntegrationDeploymentResult {
        public List<IntegrationDeployment> items;

        public int totalCount;

    }

    @Before
    public void setupIntegration() {
        final Integration integration = new Integration.Builder().id("test-id").name("test").build();

        post("/api/v1/integrations", integration, Integration.class);
    }

    @Test
    public void shouldCreateInitialDeploymentAndFetchIt() {
        // create deployment 1
        final ResponseEntity<IntegrationDeployment> created = put("/api/v1/integrations/test-id/deployments", null,
            IntegrationDeployment.class, tokenRule.validToken(), HttpStatus.OK);

        final ResponseEntity<IntegrationDeployment> fetched = get("/api/v1/integrations/test-id/deployments/1",
            IntegrationDeployment.class);

        assertThat(created.getBody()).isNotNull().isEqualTo(fetched.getBody());
    }

    @Test
    public void shouldCreateSubsequentDeploymentsAndAvailThem() {
        final ResponseEntity<IntegrationDeployment> first = put("/api/v1/integrations/test-id/deployments", null,
            IntegrationDeployment.class, tokenRule.validToken(), HttpStatus.OK);

        final ResponseEntity<IntegrationDeployment> second = put("/api/v1/integrations/test-id/deployments", null,
            IntegrationDeployment.class, tokenRule.validToken(), HttpStatus.OK);

        final ResponseEntity<IntegrationDeploymentResult> fetched = get("/api/v1/integrations/test-id/deployments",
            IntegrationDeploymentResult.class);

        final IntegrationDeploymentResult deployments = fetched.getBody();
        assertThat(deployments.items).hasSize(2);
        assertThat(deployments.totalCount).isEqualTo(2);

        assertThat(deployments.items.get(0))
            .isEqualTo(first.getBody().withCurrentState(IntegrationDeploymentState.Unpublished)
                .withTargetState(IntegrationDeploymentState.Unpublished));
        assertThat(deployments.items.get(1))
            .isEqualTo(second.getBody().withCurrentState(IntegrationDeploymentState.Pending)
                .withTargetState(IntegrationDeploymentState.Published));
    }

    @Test
    public void shouldDirectlyManipulateDeploymentTargetState() {
        final ResponseEntity<IntegrationDeployment> version1 = put("/api/v1/integrations/test-id/deployments", null,
            IntegrationDeployment.class, tokenRule.validToken(), HttpStatus.OK);

        post("/api/v1/integrations/test-id/deployments/1/targetState",
            Collections.singletonMap("targetState", IntegrationDeploymentState.Unpublished), Void.class,
            tokenRule.validToken(), HttpStatus.NO_CONTENT);

        Awaitility.await().atMost(6, TimeUnit.SECONDS).untilAsserted(() -> {
            final ResponseEntity<IntegrationDeployment> fetched = get("/api/v1/integrations/test-id/deployments/1",
                IntegrationDeployment.class);

            assertThat(fetched.getBody()).isNotNull()
                .isEqualTo(version1.getBody().withCurrentState(IntegrationDeploymentState.Unpublished)
                    .withTargetState(IntegrationDeploymentState.Unpublished));
        });
    }
}
