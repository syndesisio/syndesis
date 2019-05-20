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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.model.integration.step.template.TemplateStepLanguage;
import io.syndesis.server.controller.integration.camelk.crd.Integration;
import io.syndesis.server.controller.integration.camelk.crd.IntegrationSpec;
import io.syndesis.server.openshift.Exposure;
import org.springframework.stereotype.Component;

import static io.syndesis.common.util.Optionals.asStream;

/**
 * Adds libraries needed for templating steps
 */
@Component
public class TemplatingCustomizer implements CamelKIntegrationCustomizer {

    @Override
    public Integration customize(IntegrationDeployment deployment, Integration integration, EnumSet<Exposure> exposure) {
        Set<TemplateStepLanguage> languages = deployment.getSpec().getFlows().stream()
            .flatMap(f -> f.getSteps().stream())
            .filter(s -> s.getStepKind() == StepKind.template)
            .flatMap(s -> asStream(Optional.of(s.getConfiguredProperties().get("language"))))
            .map(TemplateStepLanguage::stepLanguage)
            .collect(Collectors.toSet());

        if (!languages.isEmpty()) {
            IntegrationSpec.Builder spec = new IntegrationSpec.Builder();
            if (integration.getSpec() != null) {
                spec = spec.from(integration.getSpec());
            }

            for (TemplateStepLanguage lan : languages) {
                spec = spec.addDependencies("mvn:" + lan.mavenDependency());
            }

            integration.setSpec(spec.build());
        }
        return integration;
    }

}
