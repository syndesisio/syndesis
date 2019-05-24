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
import java.util.Map;

import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.server.controller.integration.camelk.crd.Integration;
import io.syndesis.server.controller.integration.camelk.crd.IntegrationSpec;
import io.syndesis.server.controller.integration.camelk.crd.TraitSpec;
import io.syndesis.server.openshift.Exposure;

abstract class AbstractTraitCustomizer implements CamelKIntegrationCustomizer {
    @Override
    public Integration customize(IntegrationDeployment deployment, Integration integration, EnumSet<Exposure> exposure) {
        Map<String, TraitSpec> traits = computeTraits(integration, exposure);
        if (!traits.isEmpty()) {
            IntegrationSpec.Builder spec = new IntegrationSpec.Builder();
            if (integration.getSpec() != null) {
                spec = spec.from(integration.getSpec());
            }

            traits.forEach(spec::putTraits);

            integration.setSpec(spec.build());
        }

        return integration;
    }

    protected abstract Map<String, TraitSpec> computeTraits(Integration integration, EnumSet<Exposure> exposure);
}
