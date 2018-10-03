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
package io.syndesis.server.controller.integration.online.customizer;

import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.Step;
import io.syndesis.server.controller.ControllersConfigurationProperties;
import io.syndesis.server.openshift.Exposure;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ExposureDeploymentDataCustomizerTest {

    final IntegrationDeployment simpleIntegration = new IntegrationDeployment.Builder()
        .spec(new Integration.Builder().build()).build();

    final IntegrationDeployment exposedIntegration = new IntegrationDeployment.Builder().spec(new Integration.Builder()
        .addFlow(new Flow.Builder()
            .addStep(new Step.Builder().action(new ConnectorAction.Builder().addTag("expose").build()).build()).build())
        .build()).build();

    @Test
    public void shouldDetermineExposureNeed() {
        assertThat(ExposureDeploymentDataCustomizer.needsExposure(simpleIntegration)).isFalse();

        assertThat(ExposureDeploymentDataCustomizer.needsExposure(exposedIntegration)).isTrue();
    }

    @Test
    public void shouldDetermineWhenExposureIsNotNeeded() {
        final ExposureDeploymentDataCustomizer customizer = new ExposureDeploymentDataCustomizer(
            new ControllersConfigurationProperties());

        assertThat(customizer.determineExposure(simpleIntegration)).isEmpty();
    }

    @Test
    public void shouldDetermineExposureWhen3scaleIsNotEnabled() {
        final ExposureDeploymentDataCustomizer customizer = new ExposureDeploymentDataCustomizer(
            new ControllersConfigurationProperties());

        assertThat(customizer.determineExposure(exposedIntegration)).containsOnly(Exposure.SERVICE, Exposure.ROUTE);
    }

    @Test
    public void shouldDetermineExposureWhen3scaleIsEnabled() {
        final ControllersConfigurationProperties properties = new ControllersConfigurationProperties();
        properties.setExposeVia3scale(true);

        final ExposureDeploymentDataCustomizer customizer = new ExposureDeploymentDataCustomizer(properties);

        assertThat(customizer.determineExposure(exposedIntegration)).containsOnly(Exposure.SERVICE, Exposure._3SCALE);
    }

    @Test
    public void shouldNotExposeWebHooksVia3scaleWhen3scaleIsEnabled() {
        final ControllersConfigurationProperties properties = new ControllersConfigurationProperties();
        properties.setExposeVia3scale(true);

        final ExposureDeploymentDataCustomizer customizer = new ExposureDeploymentDataCustomizer(properties);

        final IntegrationDeployment webHookIntegration = new IntegrationDeployment.Builder()
            .spec(new Integration.Builder()
                .addFlow(new Flow.Builder().addStep(new Step.Builder().action(new ConnectorAction.Builder()
                    .descriptor(new ConnectorDescriptor.Builder().connectorId("webhook").build()).addTag("expose")
                    .build()).build()).build())
                .build())
            .build();

        assertThat(customizer.determineExposure(webHookIntegration)).containsOnly(Exposure.SERVICE, Exposure.ROUTE);
    }
}
