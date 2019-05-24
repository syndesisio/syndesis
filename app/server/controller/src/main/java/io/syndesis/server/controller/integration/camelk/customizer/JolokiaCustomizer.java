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

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;

import io.syndesis.server.controller.integration.camelk.crd.Integration;
import io.syndesis.server.controller.integration.camelk.crd.TraitSpec;
import io.syndesis.server.openshift.Exposure;
import org.springframework.stereotype.Component;

/**
 * Configure jolokia trait
 */
@Component
public class JolokiaCustomizer extends AbstractTraitCustomizer {
    @Override
    protected Map<String, TraitSpec> computeTraits(Integration integration, EnumSet<Exposure> exposure) {
        return Collections.singletonMap(
            "jolokia",
            new TraitSpec.Builder()
                .putConfiguration("enabled", "true")
                .putConfiguration("port", "8778")
                .build()
        );
    }
}
