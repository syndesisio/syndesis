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
package io.syndesis.server.controller.integration.online;

import java.util.Collections;
import java.util.List;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.integration.api.IntegrationProjectGenerator;
import io.syndesis.server.controller.integration.IntegrationPublishValidator;
import io.syndesis.server.controller.integration.online.customizer.DeploymentDataCustomizer;
import io.syndesis.server.dao.IntegrationDao;
import io.syndesis.server.dao.IntegrationDeploymentDao;
import io.syndesis.server.openshift.DeploymentData;
import io.syndesis.server.openshift.OpenShiftService;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class PublishHandlerTest {

    private final List<DeploymentDataCustomizer> customizers = Collections.emptyList();

    private final IntegrationDao integrationDao = mock(IntegrationDao.class);

    private final IntegrationDeploymentDao integrationDeploymentDao = mock(IntegrationDeploymentDao.class);

    private final OpenShiftService openShiftService = mock(OpenShiftService.class);

    private final IntegrationProjectGenerator projectGenerator = mock(IntegrationProjectGenerator.class);

    private final IntegrationPublishValidator validator = mock(IntegrationPublishValidator.class);

    @Test
    void shouldComputeRemovedEnvironmentVariables() {
        final PublishHandler handler = new PublishHandler(openShiftService, integrationDao, integrationDeploymentDao, projectGenerator, customizers, validator);

        final Integration integration = new Integration.Builder()
            .id("id")
            .putEnvironment("ENV1", "VALUE1")
            .putEnvironment("ENV4UPDATED", "VALUE4")
            .build();

        final IntegrationDeployment integrationDeployment = new IntegrationDeployment.Builder()
            .spec(integration)
            .userId("user")
            .build();

        final List<IntegrationDeployment> previousDeployments = Collections.singletonList(
            new IntegrationDeployment.Builder()
                .spec(new Integration.Builder()
                    .putEnvironment("ENV1", "VALUE1")
                    .putEnvironment("ENV2", "VALUE2")
                    .putEnvironment("ENV4", "VALUE4")
                    .build())
                .build());

        final DeploymentData deploymentData = handler.createDeploymentData(integration, integrationDeployment, previousDeployments);

        assertThat(deploymentData.getRemovedEnvironment()).containsOnly("ENV2", "ENV4");
        assertThat(deploymentData.getEnvironment()).extracting(EnvVar::getName).containsOnly("ENV1", "ENV4UPDATED");
    }
}
