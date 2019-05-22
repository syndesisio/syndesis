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
package io.syndesis.server.controller.integration.camelk.customizer;

import java.util.EnumSet;

import io.syndesis.common.model.Dependency;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.Step;
import io.syndesis.server.controller.integration.camelk.TestResourceManager;
import io.syndesis.server.endpoint.v1.VersionService;
import io.syndesis.server.openshift.Exposure;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DependenciesCustomizerTest {
    @Test
    public void testDependenciesCustomizer() {
        VersionService versionService = new VersionService();
        TestResourceManager manager = new TestResourceManager();
        Integration integration = manager.newIntegration(
            new Step.Builder()
                .addDependencies(Dependency.maven("io.syndesis.connector:syndesis-connector-api-provider"))
                .build()
        );

        IntegrationDeployment deployment = new IntegrationDeployment.Builder()
            .userId("user")
            .id("idId")
            .spec(integration)
            .build();

        CamelKIntegrationCustomizer customizer = new DependenciesCustomizer(versionService, manager);

        io.syndesis.server.controller.integration.camelk.crd.Integration i = customizer.customize(
            deployment,
            new io.syndesis.server.controller.integration.camelk.crd.Integration(),
            EnumSet.noneOf(Exposure.class)
        );

        assertThat(i.getSpec().getDependencies()).anyMatch(s -> s.startsWith("bom:io.syndesis.integration/integration-bom-camel-k/pom/"));
        assertThat(i.getSpec().getDependencies()).anyMatch(s -> s.startsWith("mvn:io.syndesis.integration/integration-runtime-camelk"));
        assertThat(i.getSpec().getDependencies()).anyMatch(s -> s.startsWith("mvn:io.syndesis.connector/syndesis-connector-api-provider"));
    }

    @Test
    public void testDependenciesCustomizerWithServiceExposure() {
        VersionService versionService = new VersionService();
        TestResourceManager manager = new TestResourceManager();
        Integration integration = manager.newIntegration(
            new Step.Builder()
                .addDependencies(Dependency.maven("io.syndesis.connector:syndesis-connector-api-provider"))
                .build()
        );

        IntegrationDeployment deployment = new IntegrationDeployment.Builder()
            .userId("user")
            .id("idId")
            .spec(integration)
            .build();

        CamelKIntegrationCustomizer customizer = new DependenciesCustomizer(versionService, manager);

        io.syndesis.server.controller.integration.camelk.crd.Integration i = customizer.customize(
            deployment,
            new io.syndesis.server.controller.integration.camelk.crd.Integration(),
            EnumSet.of(Exposure.SERVICE)
        );

        assertThat(i.getSpec().getDependencies()).anyMatch(s -> s.startsWith("bom:io.syndesis.integration/integration-bom-camel-k/pom/"));
        assertThat(i.getSpec().getDependencies()).anyMatch(s -> s.startsWith("mvn:io.syndesis.integration/integration-runtime-camelk"));
        assertThat(i.getSpec().getDependencies()).anyMatch(s -> s.startsWith("mvn:org.apache.camel.k/camel-k-runtime-servlet"));
        assertThat(i.getSpec().getDependencies()).anyMatch(s -> s.startsWith("mvn:io.syndesis.connector/syndesis-connector-api-provider"));
    }
}
