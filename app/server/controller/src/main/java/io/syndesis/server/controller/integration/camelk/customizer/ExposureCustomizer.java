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
import java.util.HashMap;

import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.server.controller.integration.camelk.crd.ConfigurationSpec;
import io.syndesis.server.controller.integration.camelk.crd.Integration;
import io.syndesis.server.controller.integration.camelk.crd.IntegrationSpec;
import io.syndesis.server.controller.integration.camelk.crd.TraitSpec;
import io.syndesis.server.openshift.Exposure;
import io.syndesis.server.openshift.OpenShiftService;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class ExposureCustomizer implements CamelKIntegrationCustomizer {
    @Override
    public Integration customize(IntegrationDeployment deployment, Integration integration, EnumSet<Exposure> exposure) {
        IntegrationSpec.Builder spec = new IntegrationSpec.Builder();
        if (integration.getSpec() != null) {
            spec = spec.from(integration.getSpec());
        }

        if (exposure.contains(Exposure.SERVICE)) {
            spec.putTraits(
                "service",
                new TraitSpec.Builder()
                    .putConfiguration("enabled", "true")
                    .putConfiguration("auto", "false")
                    .putConfiguration("port", Integer.toString(OpenShiftService.INTEGRATION_SERVICE_PORT))
                    .build()
            );

            spec.addConfiguration(
                new ConfigurationSpec.Builder()
                    .type("property")
                    .value("customizer.servlet.enabled=true")
                    .build()
            );
            spec.addConfiguration(
                new ConfigurationSpec.Builder()
                    .type("property")
                    .value("customizer.servlet.bindPort=" +OpenShiftService.INTEGRATION_SERVICE_PORT)
                    .build()
            );

            spec.addConfiguration("property", "camel.rest.contextPath=/");
            spec.addConfiguration("property", "camel.rest.component=servlet");
            spec.addConfiguration("property", "camel.rest.endpointProperty.headerFilterStrategy=syndesisHeaderStrategy");
        }

        if (exposure.contains(Exposure.ROUTE)) {
            spec.putTraits(
                "route",
                new TraitSpec.Builder()
                    .putConfiguration("enabled", "true")
                    .putConfiguration("tls-termination", "edge")
                    .build()
            );
        }

        if (exposure.contains(Exposure._3SCALE)) {
            if (integration.getMetadata().getLabels() == null) {
                integration.getMetadata().setLabels(new HashMap<>());
            }
            if (integration.getMetadata().getAnnotations() == null) {
                integration.getMetadata().setAnnotations(new HashMap<>());
            }

            integration.getMetadata().getLabels().put("discovery.3scale.net", "true");
            integration.getMetadata().getAnnotations().put("discovery.3scale.net/scheme", "http");
            integration.getMetadata().getAnnotations().put("discovery.3scale.net/port", "8080");
            integration.getMetadata().getAnnotations().put("discovery.3scale.net/description-path", "/openapi.json");
        }

        integration.setSpec(spec.build());

        return integration;
    }
}
