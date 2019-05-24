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

import com.google.common.collect.Iterables;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.server.controller.integration.camelk.TestResourceManager;
import io.syndesis.server.openshift.Exposure;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class OwnerCustomizerTest {
    @Test
    public void testOwnerCustomizerWithNon3ScaleExposure() {
        TestResourceManager manager = new TestResourceManager();
        Integration integration = manager.newIntegration();
        IntegrationDeployment deployment = new IntegrationDeployment.Builder()
            .userId("user")
            .id("idId")
            .spec(integration)
            .build();

        CamelKIntegrationCustomizer customizer = new OwnerCustomizer();

        io.syndesis.server.controller.integration.camelk.crd.Integration i = customizer.customize(
            deployment,
            new io.syndesis.server.controller.integration.camelk.crd.Integration(),
            EnumSet.of(Exposure.ROUTE, Exposure.SERVICE)
        );

        assertThat(i.getSpec().getTraits()).containsKey("owner");
        assertThat(i.getSpec().getTraits().get("owner").getConfiguration()).containsOnly(
            entry("target-labels", String.join(",", OwnerCustomizer.LABELS)),
            entry("target-annotations", String.join(",", OwnerCustomizer.ANNOTATIONS))
        );
    }

    @Test
    public void testOwnerCustomizerWith3ScaleExposure() {
        TestResourceManager manager = new TestResourceManager();
        Integration integration = manager.newIntegration();
        IntegrationDeployment deployment = new IntegrationDeployment.Builder()
            .userId("user")
            .id("idId")
            .spec(integration)
            .build();

        CamelKIntegrationCustomizer customizer = new OwnerCustomizer();

        io.syndesis.server.controller.integration.camelk.crd.Integration i = customizer.customize(
            deployment,
            new io.syndesis.server.controller.integration.camelk.crd.Integration(),
            EnumSet.of(Exposure._3SCALE)
        );

        assertThat(i.getSpec().getTraits()).containsKey("owner");
        assertThat(i.getSpec().getTraits().get("owner").getConfiguration()).containsOnly(
            entry("target-labels", String.join(",", Iterables.concat(OwnerCustomizer.LABELS, OwnerCustomizer.LABELS_3SCALE))),
            entry("target-annotations", String.join(",", Iterables.concat(OwnerCustomizer.ANNOTATIONS, OwnerCustomizer.ANNOTATIONS_3SCALE)))
        );
    }
}
