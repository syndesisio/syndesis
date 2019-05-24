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
import java.util.Objects;

import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.server.controller.integration.camelk.TestResourceManager;
import io.syndesis.server.openshift.Exposure;
import io.syndesis.server.openshift.OpenShiftService;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class ExposureCustomizerTest {
    @Test
    public void testExposureCustomizerWithServiceExposure() {
        TestResourceManager manager = new TestResourceManager();
        Integration integration = manager.newIntegration();
        IntegrationDeployment deployment = new IntegrationDeployment.Builder()
            .userId("user")
            .id("idId")
            .spec(integration)
            .build();

        CamelKIntegrationCustomizer customizer = new ExposureCustomizer();

        io.syndesis.server.controller.integration.camelk.crd.Integration i = customizer.customize(
            deployment,
            new io.syndesis.server.controller.integration.camelk.crd.Integration(),
            EnumSet.of(Exposure.SERVICE)
        );

        assertThat(i.getMetadata().getLabels()).isNullOrEmpty();
        assertThat(i.getMetadata().getAnnotations()).isNullOrEmpty();
        assertThat(i.getSpec().getTraits()).doesNotContainKey("route");
        assertThat(i.getSpec().getTraits()).containsKey("service");
        assertThat(i.getSpec().getTraits().get("service").getConfiguration()).containsOnly(
            entry("enabled", "true"),
            entry("auto", "false"),
            entry("port", Integer.toString(OpenShiftService.INTEGRATION_SERVICE_PORT))
        );

        assertThat(i.getSpec().getConfiguration())
            .filteredOn("type", "property")
            .anyMatch(
            c -> Objects.equals(c.getValue(), "customizer.servlet.enabled=true")
        );
        assertThat(i.getSpec().getConfiguration())
            .filteredOn("type", "property")
            .anyMatch(
                c -> Objects.equals(c.getValue(), "customizer.servlet.bindPort=" +OpenShiftService.INTEGRATION_SERVICE_PORT)
            );
    }

    @Test
    public void testExposureCustomizerWithRouteExposure() {
        TestResourceManager manager = new TestResourceManager();
        Integration integration = manager.newIntegration();
        IntegrationDeployment deployment = new IntegrationDeployment.Builder()
            .userId("user")
            .id("idId")
            .spec(integration)
            .build();

        CamelKIntegrationCustomizer customizer = new ExposureCustomizer();

        io.syndesis.server.controller.integration.camelk.crd.Integration i = customizer.customize(
            deployment,
            new io.syndesis.server.controller.integration.camelk.crd.Integration(),
            EnumSet.of(Exposure.ROUTE)
        );

        assertThat(i.getMetadata().getLabels()).isNullOrEmpty();
        assertThat(i.getMetadata().getAnnotations()).isNullOrEmpty();
        assertThat(i.getSpec().getTraits()).doesNotContainKey("service");
        assertThat(i.getSpec().getTraits()).containsKey("route");
        assertThat(i.getSpec().getTraits().get("route").getConfiguration()).containsOnly(
            entry("enabled", "true"),
            entry("tls-termination", "edge")
        );
    }

    @Test
    public void testExposureCustomizerWith3ScaleExposure() {
        TestResourceManager manager = new TestResourceManager();
        Integration integration = manager.newIntegration();
        IntegrationDeployment deployment = new IntegrationDeployment.Builder()
            .userId("user")
            .id("idId")
            .spec(integration)
            .build();

        CamelKIntegrationCustomizer customizer = new ExposureCustomizer();

        io.syndesis.server.controller.integration.camelk.crd.Integration i = customizer.customize(
            deployment,
            new io.syndesis.server.controller.integration.camelk.crd.Integration(),
            EnumSet.of(Exposure._3SCALE)
        );

        assertThat(i.getMetadata().getLabels()).containsOnly(
            entry("discovery.3scale.net", "true")
        );
        assertThat(i.getMetadata().getAnnotations()).containsOnly(
            entry("discovery.3scale.net/scheme", "http"),
            entry("discovery.3scale.net/port", "8080"),
            entry("discovery.3scale.net/description-path", "/openapi.json")
        );
        assertThat(i.getSpec().getTraits()).doesNotContainKey("service");
        assertThat(i.getSpec().getTraits()).doesNotContainKey("route");
    }
}
