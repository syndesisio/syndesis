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

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import io.syndesis.common.model.Dependency;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.integration.api.IntegrationResourceManager;
import io.syndesis.integration.project.generator.mvn.MavenGav;
import io.syndesis.server.controller.integration.camelk.crd.Integration;
import io.syndesis.server.controller.integration.camelk.crd.IntegrationSpec;
import io.syndesis.server.endpoint.v1.VersionService;
import io.syndesis.server.openshift.Exposure;
import org.springframework.stereotype.Component;

/**
 * Configure dependencies
 */
@Component
public class DependenciesCustomizer implements CamelKIntegrationCustomizer {
    private final VersionService versionService;
    private final IntegrationResourceManager resourceManager;
    private final List<MavenGav> filteredOutDependencies = Collections.unmodifiableList(Arrays.asList(
        new MavenGav("org.apache.camel:camel-servlet-starter")));

    public DependenciesCustomizer(VersionService versionService, IntegrationResourceManager resourceManager) {
        this.versionService = versionService;
        this.resourceManager = resourceManager;
    }

    @Override
    public Integration customize(IntegrationDeployment deployment, Integration integration, EnumSet<Exposure> exposure) {
        IntegrationSpec.Builder spec = new IntegrationSpec.Builder();
        if (integration.getSpec() != null) {
            spec = spec.from(integration.getSpec());
        }

        spec.addDependencies("bom:io.syndesis.integration/integration-bom-camel-k/pom/" + versionService.getVersion());
        spec.addDependencies("mvn:io.syndesis.integration/integration-runtime-camelk");

        if (exposure.contains(Exposure.SERVICE)) {
            spec.addDependencies("mvn:org.apache.camel/camel-base64");
            spec.addDependencies("mvn:org.apache.camel.k/camel-k-runtime-servlet");
        }

        Set<MavenGav> filteredDependencies = getDependencies(deployment.getSpec()).stream()
            .filter(gav -> !filteredOutDependencies.stream()
                            .map(fgav->fgav.getArtifactId().equals(gav.getArtifactId())
                                    && fgav.getGroupId().equals(gav.getGroupId()) ).reduce(false, (b1,b2)->b1 || b2)
            ).collect(Collectors.toSet());

        for (MavenGav gav: filteredDependencies) {
            spec.addDependencies("mvn:" + gav.getGroupId() + "/" + gav.getArtifactId());
        }

        integration.setSpec(spec.build());

        return integration;
    }

    private Set<MavenGav> getDependencies(io.syndesis.common.model.integration.Integration integration) {
        return resourceManager.collectDependencies(integration).stream()
            .filter(Dependency::isMaven)
            .map(Dependency::getId)
            .map(MavenGav::new)
            .collect(Collectors.toCollection(TreeSet::new));
    }
}
