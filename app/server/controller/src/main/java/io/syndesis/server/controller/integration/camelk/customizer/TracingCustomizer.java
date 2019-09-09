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

import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.server.controller.integration.camelk.crd.ConfigurationSpec;
import io.syndesis.server.controller.integration.camelk.crd.Integration;
import io.syndesis.server.controller.integration.camelk.crd.IntegrationSpec;
import io.syndesis.server.openshift.Exposure;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

/**
 * Enables Jaeger tracing
 */
@Component
@ConditionalOnProperty(value = "features.jaeger-activity-tracing.enabled", havingValue = "true")
public class TracingCustomizer implements CamelKIntegrationCustomizer {

    @Override
    public Integration customize(IntegrationDeployment deployment, Integration integration, EnumSet<Exposure> exposures) {

        IntegrationSpec.Builder spec = new IntegrationSpec.Builder();
        if (integration.getSpec() != null) {
            spec = spec.from(integration.getSpec());
        }

        spec.addConfiguration(
            new ConfigurationSpec.Builder()
                .type("property")
                .value("customizer.tracing.serviceName=" + deployment.getIntegrationId().orElse("syndesis-unknown"))
                .build());

        spec.addConfiguration(
            new ConfigurationSpec.Builder()
                .type("env")
                .value("JAEGER_ENDPOINT=http://syndesis-jaeger-collector:14268/api/traces")
                .build());

        spec.addConfiguration(
            new ConfigurationSpec.Builder()
                .type("env")
                .value("JAEGER_TAGS=integration.version=" + deployment.getVersion())
                .build());

        spec.addConfiguration(
            new ConfigurationSpec.Builder()
                .type("env")
                .value("JAEGER_SAMPLER_TYPE=const")
                .build());

        spec.addConfiguration(
            new ConfigurationSpec.Builder()
                .type("env")
                .value("JAEGER_SAMPLER_PARAM=1")
                .build());

        integration.setSpec(spec.build());

        return integration;
    }

}
